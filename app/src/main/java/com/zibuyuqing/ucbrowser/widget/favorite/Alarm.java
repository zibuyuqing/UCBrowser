package com.zibuyuqing.ucbrowser.widget.favorite;

import android.os.Handler;
import android.util.Log;

/**
 * Created by Xijun.Wang on 2017/12/20.
 */

public class Alarm implements Runnable {
    private static final String TAG = "Alarm";
    // if we reach this time and the alarm hasn't been cancelled, call the listener
    private long mAlarmTriggerTime;

    // if we've scheduled a call to run() (ie called mHandler.postDelayed), this variable is true.
    // We use this to avoid having multiple pending callbacks
    private boolean mWaitingForCallback;

    private Handler mHandler;
    private OnAlarmListener mAlarmListener;
    private boolean mAlarmPending = false;
    public Alarm() {
        mHandler = new Handler();
    }
    public void setOnAlarmListener(OnAlarmListener alarmListener) {
        mAlarmListener = alarmListener;
    }
    // Sets the alarm to go off in a certain number of milliseconds. If the alarm is already set,
    // it's overwritten and only the new alarm setting is used
    public void setAlarm(long millisecondsInFuture) {
        long currentTime = System.currentTimeMillis();
        mAlarmPending = true;
        mAlarmTriggerTime = currentTime + millisecondsInFuture;
            Log.i(TAG, "setAlarm: currentTime = " + currentTime
                    + ", millisecondsInFuture = " + millisecondsInFuture
                    + ", mAlarmListener = " + mAlarmListener);
        if (!mWaitingForCallback) {
            mHandler.postDelayed(this, mAlarmTriggerTime - currentTime);
            mWaitingForCallback = true;
        }
    }
    public void cancelAlarm() {
        Log.i(TAG, "Cancel alarm here: mAlarmListener = " + mAlarmListener);
        mAlarmTriggerTime = 0;
        mAlarmPending = false;
    }

    @Override
    public void run() {
        mWaitingForCallback = false;
        if (mAlarmTriggerTime != 0) {
            long currentTime = System.currentTimeMillis();

            Log.i(TAG, "run: mAlarmTriggerTime = " + mAlarmTriggerTime
                    + ", currentTime = " + currentTime + ", mAlarmListener = "
                    + mAlarmListener);

            if (mAlarmTriggerTime > currentTime) {
                // We still need to wait some time to trigger spring loaded mode--
                // post a new callback
                mHandler.postDelayed(this, Math.max(0, mAlarmTriggerTime - currentTime));
                mWaitingForCallback = true;
            } else {
                mAlarmPending = false;
                if (mAlarmListener != null) {
                    mAlarmListener.onAlarm(this);
                }
            }
        }
    }
    public boolean alarmPending() {
        return mAlarmPending;
    }
}
