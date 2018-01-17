package com.zibuyuqing.ucbrowser;

import android.app.Application;

/**
 * Created by Xijun.Wang on 2018/1/16.
 */

public class MyApp extends Application{
    private static MyApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
    public static MyApp getInstance(){
        return instance;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
