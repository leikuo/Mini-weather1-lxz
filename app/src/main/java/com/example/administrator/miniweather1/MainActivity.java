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
import android.util.Log;
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

import pku.ss.lei.bean.TodayWeather;
import pku.ss.lei.util.NetUtil;


public class MainActivity extends Activity implements View.OnClickListener {
    //private static final int UPDATE_TODAY_WEATHER = 1;

    private ImageView mUpdateBtn;//刷新按钮
    private ImageView mCitySelect;//选择城市按钮

    private ProgressBar mUpdateProgressBar;  //进度条

    private String cityCode = "101010100";
    //定义相关的控件对象
    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv,
            temperatureTv, climateTv, windTv, city_name_TV, wenduTv;
    private ImageView weatherImg, pmImg;


    private IntentFilter intentFilter;
    BReceiver mReeiver;


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
        initView();

        startService();
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
        int dateCount = 0;
        int highCount = 0;
        int lowCount = 0;
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
                            if (xmlPullParser.getName().equals("city")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("updatetime")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
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
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                typeCount++;
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

    //利用TodayWeather对象更新UI中的控件
    void updateTodayWeather(TodayWeather todayWeather) {
        city_name_TV.setText(todayWeather.getCity() + "天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime() + "发布");
        humidityTv.setText("湿度：" + todayWeather.getShidu());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getHigh() + "~" + todayWeather.getLow());
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力:" + todayWeather.getFengli());

        if(todayWeather.getPm25() != null){
            pmDataTv.setText(todayWeather.getPm25());
        }

        if(todayWeather.getQuality()!=null){
            pmQualityTv.setText(todayWeather.getQuality());
        }

        if(todayWeather.getQuality() != null){
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

        //pmImg.setImageResource(R.drawable.biz_plugin_weather_51_100);
        //weatherImg.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
        Toast.makeText(MainActivity.this, "更新成功!", Toast.LENGTH_LONG).show();

        //恢复更新按钮
        mUpdateBtn.setVisibility(View.VISIBLE);
        mUpdateProgressBar.setVisibility(View.GONE);

    }

}
