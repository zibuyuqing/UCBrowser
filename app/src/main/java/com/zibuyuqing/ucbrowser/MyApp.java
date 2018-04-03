package com.zibuyuqing.ucbrowser;

import android.app.Application;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.LogStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

/**
 * Created by Xijun.Wang on 2018/1/16.
 */

public class MyApp extends Application{
    private static MyApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initLogger();
    }
    private void initLogger(){
        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(false)
                .methodCount(3)
                .methodOffset(7)
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));
    }
    public static MyApp getInstance(){
        return instance;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
