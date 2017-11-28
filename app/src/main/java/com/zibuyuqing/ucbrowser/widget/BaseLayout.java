package com.zibuyuqing.ucbrowser.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

/**
 * Created by Xijun.Wang on 2017/11/27.
 */

public class BaseLayout extends LinearLayout implements UCRootView.ScrollStateListener {
    private static final String TAG = "BaseLayout";
    private int mFromPosition;
    private int mToPosition;
    private float mStartScale;
    private float mEndScale;
    private int mDistance;
    private float mScale;
    private boolean mTranslateEnable = true;
    private boolean mScaleEnable = false;
    protected boolean mStop = false;
    public BaseLayout(Context context) {
        super(context);
    }

    public void setTranslateEnable(boolean translateEnable) {
        mTranslateEnable = translateEnable;
    }

    public void setScaleEnable(boolean scaleEnable) {
        mScaleEnable = scaleEnable;
    }

    public BaseLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public void initTranslationY(int from, int to){
        mFromPosition = from;
        mToPosition = to;
        setTranslationY(from);
        mDistance = from - to;
    }
    public void initScale(float startScale,float endScale){
        mStartScale = startScale;
        mEndScale = endScale;
        setScaleX(startScale);
        setScaleY(startScale);
        mScale = endScale - startScale;
    }
    private void setScaleXY(float rate){
        setScaleX(calculateScale(rate));
        setScaleY(calculateScale(rate));
    }
    private float calculateScale(float rate){
        return mStartScale + mScale * rate;
    }
    private float calculateTransY(float rate){
        return mFromPosition + mDistance * rate;
    }

    @Override
    public void onStartScroll() {
        Log.e(TAG,"onStartScroll");
        setVisibility(VISIBLE);
    }

    @Override
    public void onScroll(float rate) {
        Log.e(TAG,"onScroll rate =:" + rate);
        if(rate > 0){
            return;
        }
        if(mTranslateEnable){
            setTranslationY(calculateTransY(rate));
        }
        if(mScaleEnable){
            setScaleXY(-rate);
        }
    }

    @Override
    public void onEndScroll() {
        Log.e(TAG,"onEndScroll");
    }


    @Override
    public void onTouch(float x, float y) {

    }
}
