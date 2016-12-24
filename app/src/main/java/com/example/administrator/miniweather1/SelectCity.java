package com.example.administrator.miniweather1;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import app.MyApplication;
import pku.ss.lei.bean.City;

/**
 * Created by Administrator on 2016/11/20.
 */
public class SelectCity extends Activity implements View.OnClickListener{

    private ImageView mBackBtn;
    private ListView mListView;
    private TextView selectCT_tv;   //当选中某一城市时，将标题改为你所选择的城市

    private String selectCode;
    private MyApplication mApplication;
    private ArrayList<String> cityName = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private String[] data;
    private String[] backeupData;

    private ArrayList<String> dataCity;
    private ArrayList<String> dataCode;

    private List<City> list = new ArrayList();
    private HashMap<String,String> cityMap = new HashMap<>();   //city为键
    private HashMap<String,String> cityMap2 = new HashMap<>();  //number为键
    private EditText m_EditText;

    List<City> cityList;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);
        selectCT_tv = (TextView) findViewById(R.id.title_name1);
        mBackBtn = (ImageView) findViewById(R.id.title_back1);
        mBackBtn.setOnClickListener(this);

        selectCode = this.getIntent().getStringExtra("cityCode");
        Log.d("myWeather", "原cityCode:  " + selectCode);
        mApplication = (MyApplication) getApplication();
        list = mApplication.getCityList();

        for(int i = 0; i < list.size(); i++){
            cityName.add(list.get(i).getCity());
            cityMap.put(list.get(i).getCity(), list.get(i).getNumber());
            cityMap2.put(list.get(i).getNumber(), list.get(i).getCity());
        }
        selectCT_tv.setText("当前城市：" + cityMap2.get(selectCode));

        data = new String[list.size()];
        backeupData = new String[list.size()];
        data = cityName.toArray(data);
        System.arraycopy(data, 0, backeupData, 0, data.length);
        mListView = (ListView) findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(SelectCity.this, android.R.layout.simple_list_item_1,data);

        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s = (String) parent.getItemAtPosition(position);
                Log.d("ListView", s);
                selectCode = cityMap.get(s);
                selectCT_tv.setText("当前城市：" + cityMap2.get(selectCode));
                Toast.makeText(SelectCity.this, "您选择了：" + cityMap2.get(selectCode),Toast.LENGTH_SHORT).show();

            }
        });

        m_EditText =(EditText) findViewById(R.id.search_edit);
        m_EditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d("EditText", "beforeTextChanged");
            }


            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchContent = s.toString();
                ArrayList<String> newDataList = new ArrayList<String>();
                for (int i = 0; i < backeupData.length; i++){
                    Log.d("EditText", backeupData[i].substring(0, searchContent.length()));
                    if (backeupData[i].substring(0, searchContent.length()).equals(searchContent)){
                        newDataList.add(backeupData[i]);
                    }
                }
                int j = 0;
                for(; j < newDataList.size(); j++){
                    data[j] = newDataList.get(j);
                }

                for (; j < data.length; j++){
                    data[j] = "";
                }

                adapter.notifyDataSetChanged();
                Log.d("EditText", "onTextChanged" + s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

        /*dataCity = new ArrayList<String>();  //用于存放城市数据
        dataCode = new ArrayList<String>();  //用于存放对应的城市代码数据
        mApplication = (MyApplication) getApplication(); //用Application来进行数据传递
        cityList = mApplication.getCityList();//用cityList来存放MyApplication传递的城市数据库数据

        for (int i = 0; i < cityList.size(); i++){
            dataCity.add(cityList.get(i).getCity().toString());
            dataCode.add(cityList.get(i).getNumber().toString());
        }

        mListView = (ListView)findViewById(R.id.list_view);
        //ArrayAdapter用于将数据映射到ListView的中介
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(SelectCity.this,
                android.R.layout.simple_list_item_1,dataCity);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {
                Toast.makeText(SelectCity.this, "你单击了：" + dataCode.get(i),Toast.LENGTH_SHORT).show();
                selectCode = dataCode.get(i);
                selectCT_tv.setText("你选择了：" + dataCity.get(i));
            }
        });
        Log.d("info", cityList.get(0).getNumber());
    }*/


    @Override
    public void onClick(View v ){
        switch (v.getId()){
            case R.id.title_back1:
                Intent i = new Intent();
                Log.d("myapp", selectCode);
                i.putExtra("cityCode", selectCode);
                setResult(RESULT_OK, i);

//                SharedPreferences mSharedPreferences = getSharedPreferences("config", Activity.MODE_PRIVATE);
//                SharedPreferences.Editor editor = mSharedPreferences.edit();
//                editor.putString("main_city_code", selectCode);
//                editor.commit();

                finish();
                break;
            default:
                break;
        }
    }



}
