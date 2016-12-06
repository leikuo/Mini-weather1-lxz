package com.example.administrator.miniweather1;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

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
import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;

import pku.ss.lei.bean.TodayWeather;

/**
 * Created by Administrator on 2016/11/29.
 */
public class MyService extends Service {
    int counter = 0;
    static final int UPDATE_INTERVAL = 1000*60*60;
    private Timer timer = new Timer();

    @Nullable
    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        doSomethingRepeatedly();
        Log.d("myService", "onStartCommand");
        return START_STICKY;
    }

    private void doSomethingRepeatedly(){
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.d("MyService", String.valueOf(++counter));

                //向MainActivity广播
                Intent intent = new Intent();
                intent.setAction("UPDATE_TODAY_WEATHER");
                sendBroadcast(intent);
            }
        },0,UPDATE_INTERVAL);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

