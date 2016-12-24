package com.example.administrator.miniweather1;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import pku.ss.lei.bean.TodayWeather;
import pku.ss.lei.util.NetUtil;


public class MainActivity extends Activity implements View.OnClickListener ,ViewPager.OnPageChangeListener{
    //private static final int UPDATE_TODAY_WEATHER = 1;

    private ImageView mUpdateBtn;//刷新按钮
    private ImageView mCitySelect;//选择城市按钮

    private ProgressBar mUpdateProgressBar;  //进度条

    private String cityCode = "101010100";
    //定义相关的控件对象
    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv,
            temperatureTv, climateTv, windTv, city_name_TV, wenduTv;
    private TextView cur_temperatureTv;
    private ImageView weatherImg, pmImg;


    private IntentFilter intentFilter;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    List<View> views;

    private LayoutInflater inflater;

    //PagerView导航小圆点
    private ImageView[] dots;
    //声明一个int型数组，用于存放两个小圆点的id
    private int[] ids = {R.id.iv01, R.id.iv02};


    BReceiver mReeiver;

    private void initViewPager(){   //用于初始化ViewPager
        inflater = LayoutInflater.from(this);
        views = new ArrayList<View>();

        views.add(inflater.inflate(R.layout.first_3day,null));
        views.add(inflater.inflate(R.layout.second_3day,null));

        viewPagerAdapter = new ViewPagerAdapter(views, this);
        viewPagerAdapter.notifyDataSetChanged();
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(viewPagerAdapter);

        //为ViewPager控件设置页面变化的监听事件
        viewPager.addOnPageChangeListener(this);
    }


    void initDots(){    //用于将两个小圆点控件对象存入数组中
        dots = new ImageView[views.size()];
        for(int i = 0; i < views.size(); i++){
            dots[i] = (ImageView) findViewById(ids[i]);
        }

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        for(int i = 0; i < ids.length; i++){
            if(i == position){
                dots[i].setImageResource(R.drawable.page_indicator_focused);
            }else {
                dots[i].setImageResource(R.drawable.page_indicator_unfocused);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    class BReceiver extends BroadcastReceiver {//广播接收器 内部类
        @Override
        public void onReceive(Context context, Intent intent){
            Log.d("MyService", "BroadcastReceiver...");
            queryWeatherCode(cityCode);
        }
    }
    private static final int UPDATE_TODAY_WEATHER = 1;
    //private TodayWeather serviceweather = null;
    //UI线程通过Handle接收子线程通过Message传送过来的更新数据
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);

        mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);         //为更新按钮ImageView增加单击事件

        mUpdateProgressBar = (ProgressBar) findViewById(R.id.title_update_progress);

        if (NetUtil.getNetworkState(this) != NetUtil.NETWORK_NONE) {
            Log.d("myWeather", "网络OK!");
            Toast.makeText(MainActivity.this, "网络OK!", Toast.LENGTH_LONG).show();
        } else {
            Log.d("myWeather", "网络挂了!");
            Toast.makeText(MainActivity.this, "网络挂了!", Toast.LENGTH_LONG).show();
        }

        //为选择城市ImageView添加OnClick单击事件
        mCitySelect = (ImageView) findViewById(R.id.title_city_manager);
        mCitySelect.setOnClickListener(this);

        //在onCreate方法中调用initView函数，初始化控件内容
        initView();//初始化控件内容

        startService();

        //initViewPager();    //用于初始化ViewPager

        initDots();
    }

    @Override
    protected void onStart(){
        super.onStart();
        mReeiver = new BReceiver();
        intentFilter = new IntentFilter();  //创建广播过滤器
        intentFilter.addAction("UPDATE_TODAY_WEATHER");
        registerReceiver(mReeiver, intentFilter);    //注册
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    protected void onPause(){
        super.onPause();
    }

    protected void onDestroy(){
        super.onDestroy();
        stopService(new Intent(this, MyService.class));
        unregisterReceiver(mReeiver);
    }

    private void startService(){
        Intent mIntent = new Intent(this,MyService.class);
        startService(mIntent);
    }

    //初始化控件内容
    void initView() {
        //初始化ViewPager
        initViewPager();

        city_name_TV = (TextView) findViewById(R.id.title_city_name);
        cityTv = (TextView) findViewById(R.id.city);
        timeTv = (TextView) findViewById(R.id.time);
        humidityTv = (TextView) findViewById(R.id.humidity);
        weekTv = (TextView) findViewById(R.id.week_today);
        pmDataTv = (TextView) findViewById(R.id.pm_data);
        pmQualityTv = (TextView) findViewById(R.id.pm2_5_quality);
        temperatureTv = (TextView) findViewById(R.id.temperature);
        climateTv = (TextView) findViewById(R.id.climate);
        windTv = (TextView) findViewById(R.id.wind);
        wenduTv = (TextView) findViewById(R.id.wendu);
        cur_temperatureTv = (TextView) findViewById(R.id.cur_temperature);
        pmImg = (ImageView) findViewById(R.id.pm2_5_img);
        weatherImg = (ImageView) findViewById(R.id.weather_img);



        city_name_TV.setText("N/A");
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");
        cur_temperatureTv.setText("N/A");

    }



    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.title_city_manager) {
            Intent i = new Intent(this, SelectCity.class);
            //startActivity(i);
            startActivityForResult(i, 1);
        }

        if (view.getId() == R.id.title_update_btn) {
            /**
             * 通过SharedPreferences 读取城市id，如果没有定义则缺省为北京市ID:101010100
             */

//            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
//            String code = sharedPreferences.getString("main_city_code", cityCode);
            Log.d("myWeather", cityCode);

            if (NetUtil.getNetworkState(this) != NetUtil.NETWORK_NONE) {
                Log.d("myWeather", "网络OK");
                queryWeatherCode(cityCode);
            } else {
                Log.d("myWeather", "网络挂了!");
                Toast.makeText(MainActivity.this, "网络挂了!", Toast.LENGTH_LONG).show();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            cityCode = data.getStringExtra("cityCode");
            Log.d("myWeather", "选择的城市代码为" + cityCode);

            if (NetUtil.getNetworkState(this) != NetUtil.NETWORK_NONE) {
                Log.d("myWeather", "网络OK!");
                queryWeatherCode(cityCode);
            } else {
                Log.d("myWeather", "网络挂了");
                Toast.makeText(MainActivity.this, "网络挂了!", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * @param cityCode 使用cityCode获取网络数据
     */

    private void queryWeatherCode(String cityCode) {
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("myWeather", address);
        new Thread(new Runnable() {  //使用匿名内部类创建一个线程
            @Override
            public void run() {
                HttpURLConnection con = null;
                //调用parseXML， 并返回TodayWeather对象
                TodayWeather todayWeather = null;
                try {
                    URL url = new URL(address);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream in = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while ((str = reader.readLine()) != null) {
                        response.append(str);
                        Log.d("myWeather", str);
                    }
                    String responseStr = response.toString();
                    Log.d("myWeather", responseStr);

                    //在获取网络数据后，调用解析函数
                    todayWeather = parseXML(responseStr);
                    if (todayWeather != null) {
                        Log.d("myWeather", todayWeather.toString());

                        Message msg = new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj = todayWeather;
                        mHandler.sendMessage(msg);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                }

            }
        }).start();
    }

    /**
     * 编写解析函数，解析出城市名称已经更新时间信息
     */
    private TodayWeather parseXML(String xmldata) {
        TodayWeather todayWeather = null;

        int fengxiangCount = 0;
        int fengliCount = 0;
        //int dateCount = 0;

        int yesterdayFX = 0;
        int yesterdayFL = 0;
        int yesterdayType = 0;

        int i = 0;   //五天天气索引
        boolean isFirstFengli = true;
        boolean isFirstFengxiang = true;

        //int highCount = 0;
        //int lowCount = 0;
        int typeCount = 0;

        try {
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType = xmlPullParser.getEventType();
            Log.d("myWeather", "parseXML");
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    //判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    //判断当前事件是否为标签元素开始事件
                    case XmlPullParser.START_TAG:
                        if (xmlPullParser.getName().equals("resp")) {
                            todayWeather = new TodayWeather();
                        }
                        if (todayWeather != null) {
                            boolean isDay = true;
                            if (xmlPullParser.getName().equals("city")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("updatetime")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                                String[] time = xmlPullParser.getText().split(":");
                                isDay = isDayFun(time[0]);
                            } else if (xmlPullParser.getName().equals("shidu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("wendu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("pm25")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("quality")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengxiang")) {
                                eventType = xmlPullParser.next();
                                //todayWeather.setFengxiang(xmlPullParser.getText());
                                //fengxiangCount++;
                                if (!isFirstFengxiang){
                                    if (isDay){ //如果是白天，直接记下遇到的第一个风向数据
                                        if (fengxiangCount == 0){
                                            todayWeather.getWeatherDetails(i).setFengxiang(xmlPullParser.getText());
                                            Log.d("five", i + ":" + xmlPullParser.getText());
                                            fengxiangCount++;
                                        }else {
                                            fengxiangCount = 0;
                                        }
                                    }
                                    else if (fengxiangCount == 0){//是夜晚的话，就跳过前一个白天的风向数据
                                        fengxiangCount++;
                                    }else {
                                        todayWeather.getWeatherDetails(i).setFengxiang(xmlPullParser.getText());
                                        Log.d("five", i + ":" + xmlPullParser.getText());
                                        fengxiangCount = 0;
                                    }
                                }
                                isFirstFengxiang = false;
                            } else if (xmlPullParser.getName().equals("fengli") ) {
                                eventType = xmlPullParser.next();
                                //todayWeather.setFengli(xmlPullParser.getText());
                                //fengliCount++;
                                if (!isFirstFengli){
                                    if (isDay){
                                        if (fengliCount == 0){
                                            todayWeather.getWeatherDetails(i).setFengli(xmlPullParser.getText());
                                            Log.d("five", i + ":" + xmlPullParser.getText());
                                            i++;
                                            fengliCount++;
                                        }
                                        else {
                                            fengliCount = 0;
                                        }
                                    }
                                    else if (fengliCount == 0){     //是夜晚的话，就跳过前一个白天的风向数据
                                        fengliCount++;
                                    }else {
                                        todayWeather.getWeatherDetails(i).setFengli(xmlPullParser.getText());
                                        fengliCount = 0;
                                        Log.d("five", i + ":" +xmlPullParser.getText());
                                        i++;
                                    }
                                }
                                isFirstFengli = false;
                            } else if (xmlPullParser.getName().equals("date") ) {
                                eventType = xmlPullParser.next();
                                //todayWeather.setDate(xmlPullParser.getText());
                                //dateCount++;
                                todayWeather.getWeatherDetails(i).setDate(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("high") ) {
                                eventType = xmlPullParser.next();
                                //todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                                //highCount++;
                                todayWeather.getWeatherDetails(i).setHigh(xmlPullParser.getText().substring(2).trim());
                            } else if (xmlPullParser.getName().equals("low") ) {
                                eventType = xmlPullParser.next();
                                //todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                //lowCount++;
                                todayWeather.getWeatherDetails(i).setLow(xmlPullParser.getText().substring(2).trim());
                            } else if (xmlPullParser.getName().equals("type") ) {
                                eventType = xmlPullParser.next();
                                //todayWeather.setType(xmlPullParser.getText());
                                //typeCount++;
                                if(isDay){
                                    if (typeCount == 0){
                                        todayWeather.getWeatherDetails(i).setType(xmlPullParser.getText());
                                        Log.d("five", i + ":" + xmlPullParser.getText());
                                        typeCount++;
                                    }else {
                                        typeCount = 0;
                                    }
                                }else if (typeCount == 0){
                                    typeCount++;
                                }else {
                                    todayWeather.getWeatherDetails(i).setType(xmlPullParser.getText());
                                    Log.d("five", i + ":" + xmlPullParser.getText());
                                    typeCount = 0;
                                }
                            }else if (xmlPullParser.getName().equals("date_1")){
                                eventType = xmlPullParser.next();
                                String date1 = xmlPullParser.getText();
                                todayWeather.setYesterdayDate(date1.substring(date1.length() - 3, date1.length()).trim());

                            }else if (xmlPullParser.getName().equals("high_1")){
                                eventType = xmlPullParser.next();
                                todayWeather.setYesterdayHigh(xmlPullParser.getText().substring(2).trim());
                            }else if (xmlPullParser.getName().equals("low_1")){
                                eventType = xmlPullParser.next();
                                todayWeather.setYesterdayLow(xmlPullParser.getText().substring(2).trim());
                            }else if (xmlPullParser.getName().equals("type_1")){
                                eventType = xmlPullParser.next();
                                if (isDay){
                                    todayWeather.setYesterdayType(xmlPullParser.getText());
                                    yesterdayType++;
                                }else if (yesterdayType == 0){
                                    yesterdayType++;
                                }else {
                                    todayWeather.setYesterdayType(xmlPullParser.getText());
                                }
                            }else if (xmlPullParser.getName().equals("f1_1")){
                                eventType = xmlPullParser.next();
                                if (isDay){
                                    todayWeather.setYesterdayFengli(xmlPullParser.getText());
                                    yesterdayFL++;
                                }else if (yesterdayFL == 0){
                                    yesterdayFL++;
                                }else {
                                    todayWeather.setYesterdayFengli(xmlPullParser.getText());
                                }
                            }else if (xmlPullParser.getName().equals("fx_1")){
                                eventType= xmlPullParser.next();
                                if (isDay){
                                    todayWeather.setYesterdayFengxiang(xmlPullParser.getText());
                                    yesterdayFX++;
                                }else if (yesterdayFX == 0){
                                    yesterdayFX++;
                                }else {
                                    todayWeather.setYesterdayFengxiang(xmlPullParser.getText());
                                }
                            }


                        }
                        break;
                    //判断当前事件是否为标签元素结束事件
                    case XmlPullParser.END_TAG:
                        break;
                }
                //进入下一个元素并触发相应事件
                eventType = xmlPullParser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return todayWeather;
    }

    private void update5dayWeather(TodayWeather todayWeather){
        viewPager.removeAllViews();
        views.clear();
        View page_one = inflater.inflate(R.layout.first_3day,null);
        View page_two = inflater.inflate(R.layout.second_3day,null);

        //昨日天气
        ((TextView)page_one.findViewById(R.id.yesterday_weatherType)).setText(todayWeather.getYesterdayType());
        ((TextView)page_one.findViewById(R.id.yesterday_date)).setText(todayWeather.getYesterdayDate());
        ((TextView)page_one.findViewById(R.id.yesterday_temperature_high_low)).setText(
                todayWeather.getYesterdayHigh() + "~" + todayWeather.getYesterdayLow());
        ((TextView)page_one.findViewById(R.id.yesterday_fengli)).setText(todayWeather.getYesterdayFengli());
        setWeatherTypeImage(todayWeather.getYesterdayType(),
                ((ImageView)page_one.findViewById(R.id.yesterday_weatherImg)));

        //五日天气1
        String day1_Date = todayWeather.getWeatherDetails(0).getDate();
        ((TextView)page_one.findViewById(R.id.day1_week)).setText(day1_Date.substring(day1_Date.length()-3,day1_Date.length()));
        ((TextView)page_one.findViewById(R.id.day1_weatherType)).setText(todayWeather.getWeatherDetails(0).getType());
        ((TextView)page_one.findViewById(R.id.day1_temperature)).setText(
                todayWeather.getWeatherDetails(0).getHigh() + "~" + todayWeather.getWeatherDetails(0).getLow());
        ((TextView)page_one.findViewById(R.id.day1_fengli)).setText(todayWeather.getWeatherDetails(0).getFengli());
        setWeatherTypeImage(todayWeather.getWeatherDetails(0).getType(),
                ((ImageView)page_one.findViewById(R.id.day1_weather)));

        //——五日天气——2
        String day2_Date = todayWeather.getWeatherDetails(1).getDate();
        ((TextView)page_one.findViewById(R.id.day2_week)).setText(day2_Date.substring(day2_Date.length()-3,day2_Date.length()));
        ((TextView)page_one.findViewById(R.id.day2_weatherType)).setText(todayWeather.getWeatherDetails(1).getType());
        ((TextView)page_one.findViewById(R.id.day2_temperature)).setText(
                todayWeather.getWeatherDetails(1).getHigh() + "~" + todayWeather.getWeatherDetails(1).getLow());
        ((TextView)page_one.findViewById(R.id.day2_fengli)).setText(todayWeather.getWeatherDetails(1).getFengli());
        if(null == todayWeather.getWeatherDetails(1).getType()) {
            Log.d("null","weather 1 is null" );
        }
        setWeatherTypeImage(todayWeather.getWeatherDetails(1).getType(),
                ((ImageView)page_one.findViewById(R.id.day2_weather)));

        //——五日天气——3
        String day3_Date = todayWeather.getWeatherDetails(2).getDate();
        ((TextView)page_two.findViewById(R.id.day3_week)).setText(day3_Date.substring(day3_Date.length()-3,day3_Date.length()));
        ((TextView)page_two.findViewById(R.id.day3_weatherType)).setText(todayWeather.getWeatherDetails(2).getType());
        ((TextView)page_two.findViewById(R.id.day3_temperature)).setText(
                todayWeather.getWeatherDetails(2).getHigh() + "~" + todayWeather.getWeatherDetails(2).getLow());
        ((TextView)page_two.findViewById(R.id.day3_fengli)).setText(todayWeather.getWeatherDetails(2).getFengli());
        setWeatherTypeImage(todayWeather.getWeatherDetails(2).getType(),
                ((ImageView)page_two.findViewById(R.id.day3_weather)));

        //——五日天气——4
        String day4_Date = todayWeather.getWeatherDetails(3).getDate();
        ((TextView)page_two.findViewById(R.id.day4_week)).setText(day4_Date.substring(day4_Date.length()-3,day4_Date.length()));
        ((TextView)page_two.findViewById(R.id.day4_weatherType)).setText(todayWeather.getWeatherDetails(3).getType());
        ((TextView)page_two.findViewById(R.id.day4_temperature)).setText(
                todayWeather.getWeatherDetails(3).getHigh() + "~" + todayWeather.getWeatherDetails(3).getLow());
        ((TextView)page_two.findViewById(R.id.day4_fengli)).setText(todayWeather.getWeatherDetails(3).getFengli());
        setWeatherTypeImage(todayWeather.getWeatherDetails(3).getType(),
                ((ImageView)page_two.findViewById(R.id.day4_weather)));

        //——五日天气——5
        String day5_Date = todayWeather.getWeatherDetails(4).getDate();
        ((TextView)page_two.findViewById(R.id.day5_week)).setText(day5_Date.substring(day5_Date.length()-3,day5_Date.length()));
        ((TextView)page_two.findViewById(R.id.day5_weatherType)).setText(todayWeather.getWeatherDetails(4).getType());
        ((TextView)page_two.findViewById(R.id.day5_temperature)).setText(
                todayWeather.getWeatherDetails(4).getHigh() + "~" + todayWeather.getWeatherDetails(4).getLow());
        ((TextView)page_two.findViewById(R.id.day5_fengli)).setText(todayWeather.getWeatherDetails(4).getFengli());
        setWeatherTypeImage(todayWeather.getWeatherDetails(4).getType(),
                ((ImageView)page_two.findViewById(R.id.day5_weather)));

        views.add(page_one);
        views.add(page_two);
        viewPagerAdapter.notifyDataSetChanged();

    }


    //利用TodayWeather对象更新UI中的控件
    private void updateTodayWeather(TodayWeather todayWeather) {

        //更新五天天气
        update5dayWeather(todayWeather);

        city_name_TV.setText(todayWeather.getCity() + "天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime() + "发布");
        humidityTv.setText("湿度：" + todayWeather.getShidu());
        weekTv.setText(todayWeather.getWeatherDetails(0).getDate());
        //temperatureTv.setText(todayWeather.getHigh() + "~" + todayWeather.getLow());
        //climateTv.setText(todayWeather.getType());
        //windTv.setText("风力:" + todayWeather.getFengli());

        if(todayWeather.getPm25() != null){
            pmDataTv.setText(todayWeather.getPm25());
        }

        if(todayWeather.getQuality()!=null){
            pmQualityTv.setText(todayWeather.getQuality());
        }

        cur_temperatureTv.setText("温度：" + todayWeather.getWendu() + "℃");
        temperatureTv.setText(todayWeather.getWeatherDetails(0).getHigh() + "~" + todayWeather.getWeatherDetails(0).getLow());
        climateTv.setText(todayWeather.getWeatherDetails(0).getType());
        windTv.setText("风力：" + todayWeather.getWeatherDetails(0).getFengli());

        //设置PM2.5图片
        setPM25(todayWeather.getPm25());

        //设置天气图片
        setWeatherTypeImage(todayWeather.getWeatherDetails(0).getType(), weatherImg);

       /* if(todayWeather.getQuality() != null){
            if (pmQualityTv.getText() == "优")
                pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
            else if (todayWeather.getQuality().equals("良"))
                pmImg.setImageResource(R.drawable.biz_plugin_weather_51_100);
            else if (pmQualityTv.getText() == "轻度污染")
                pmImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_101_150));
            else if (pmQualityTv.getText() == "中度污染")
                pmImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_151_200));
            else if (pmQualityTv.getText() == "重度污染")
                pmImg.setImageResource(R.drawable.biz_plugin_weather_201_300);
            else
                pmImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_greater_300));
        }
        if (climateTv.getText() == "暴雪")
            weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_baoxue));
        else if (climateTv.getText().toString() == "暴雨")
            weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_baoyu));
        else if (climateTv.getText() == "大暴雨")
            weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_dabaoyu));
        else if (climateTv.getText() == "大雪")
            weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_daxue));
        else if (climateTv.getText() == "大雨")
            weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_dayu));
        else if (climateTv.getText() == "多云")
            weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_duoyun));
        else if (climateTv.getText() == "雷阵雨")
            weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_leizhenyu));
        else if (climateTv.getText() == "雷阵雨冰雹")
            weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_leizhenyubingbao));
        else if (climateTv.getText() == "特大暴雨")
            weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_tedabaoyu));
        else if (climateTv.getText() == "雾")
            weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_wu));
        else if (climateTv.getText().equals("小雪"))
            weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_xiaoxue));
        else if (climateTv.getText().equals("阴"))
            weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_yin));
        else if (climateTv.getText().equals("雨夹雪"))
            weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_yujiaxue));
        else if (climateTv.getText() == "阵雪")
            weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_zhenxue));
        else if (climateTv.getText() == "阵雨")
            weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_zhenyu));
        else if (climateTv.getText() == "中雪")
            weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_zhongxue));
        else if (climateTv.getText().equals("中雨"))
            weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_zhongyu));
        else if (climateTv.getText() == "晴")
            weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_qing));
            */
        //pmImg.setImageResource(R.drawable.biz_plugin_weather_51_100);
        //weatherImg.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
        Toast.makeText(MainActivity.this, "更新成功!", Toast.LENGTH_LONG).show();

        //恢复更新按钮
        mUpdateBtn.setVisibility(View.VISIBLE);
        mUpdateProgressBar.setVisibility(View.GONE);

    }

    private void setPM25(String pm25Value){
        if (pm25Value != null){

            int pm25 = Integer.parseInt(pm25Value);
            if (pm25 >= 0 && pm25 <=50){
                pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
            }else if (pm25 >= 51 && pm25 <= 100){
                pmImg.setImageResource(R.drawable.biz_plugin_weather_51_100);
            }else if (pm25 >= 101 && pm25 <= 150){
                pmImg.setImageResource(R.drawable.biz_plugin_weather_101_150);
            }else if (pm25 >= 151 && pm25 <= 200){
                pmImg.setImageResource(R.drawable.biz_plugin_weather_151_200);
            }else if (pm25 >= 201 && pm25 <= 300){
                pmImg.setImageResource(R.drawable.biz_plugin_weather_201_300);
            }else {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_greater_300);
            }
        }
    }

    //设置天气图片
    private void setWeatherTypeImage(String weatherType, ImageView imageView){
        switch (weatherType){
            case "暴雪":
                imageView.setImageResource(R.drawable.biz_plugin_weather_baoxue);
                break;
            case "暴雨":
                imageView.setImageResource(R.drawable.biz_plugin_weather_baoyu);
                break;
            case "大暴雨":
                imageView.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
                break;
            case "大雪":
                imageView.setImageResource(R.drawable.biz_plugin_weather_daxue);
                break;
            case "大雨":
                imageView.setImageResource(R.drawable.biz_plugin_weather_dayu);
                break;
            case "多云":
                imageView.setImageResource(R.drawable.biz_plugin_weather_duoyun);
                break;
            case "雷阵雨":
                imageView.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
                break;
            case "雷阵雨冰雹":
                imageView.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
                break;
            case "晴":
                imageView.setImageResource(R.drawable.biz_plugin_weather_qing);
                break;
            case "沙尘暴":
                imageView.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
                break;
            case "特大暴雨":
                imageView.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
                break;
            case "雾":
                imageView.setImageResource(R.drawable.biz_plugin_weather_wu);
                break;
            case "小雪":
                imageView.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
                break;
            case "小雨":
                imageView.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
                break;
            case "阴":
                imageView.setImageResource(R.drawable.biz_plugin_weather_yin);
                break;
            case "雨夹雪":
                imageView.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
                break;
            case "阵雪":
                imageView.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
                break;
            case "阵雨":
                imageView.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
                break;
            case "中雪":
                imageView.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
                break;
            case "中雨":
                imageView.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
                break;

        }
    }


    //判断是白天还是晚上
    public boolean isDayFun(String updateTime){
        int time = Integer.valueOf(updateTime);
        if((time>=0 && time<6) || (time >= 18 && time<24)){
            return false;
        }else {
            return true;
        }
    }

}
