package com.example.administrator.miniweather1;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import app.MyApplication;
import pku.ss.lei.bean.City;

/**
 * Created by Administrator on 2016/11/20.
 */
public class SelectCity extends Activity implements View.OnClickListener{

    private ImageView mBackBtn;
    private ArrayList<String> dataCity;
    private ArrayList<String> dataCode;
    private ListView mListView;
    private String selectCode;
    private TextView selectCT_tv;   //当选中某一城市时，将标题改为你所选择的城市
    List<City> cityList;

    private MyApplication mApplication;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);
        selectCT_tv = (TextView) findViewById(R.id.title_name1);
        mBackBtn = (ImageView) findViewById(R.id.title_back1);
        mBackBtn.setOnClickListener(this);

        dataCity = new ArrayList<String>();  //用于存放城市数据
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
    }

    @Override
    public void onClick(View v ){
        switch (v.getId()){
            case R.id.title_back1:
                Intent i = new Intent();
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
