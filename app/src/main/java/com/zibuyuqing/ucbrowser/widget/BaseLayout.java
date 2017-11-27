package com.zibuyuqing.ucbrowser.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

/**
 * Created by Xijun.Wang on 2017/11/27.
 */

public class BaseLayout extends LinearLayout implements UCRootView.ScrollStateListener {
    private static final String TAG = "BaseLayout";
    public static final int UP_WHEN_SCROLL_UP = 0;
    public static final int DOWN_WHEN_SCROLL_UP = 1;
    public static final int GROWUP_WHEN_SCROLL_UP = 2;
    public static final int SHRINK_WHEN_SCROLL_UP = 3;
    private int mStartY;
    private int mEndY;
    private float mStartScale;
    private float mEndScale;
    private int mDirection;
    private int mDistance;
    private float mScale;
    public BaseLayout(Context context) {
        super(context);
    }

    public BaseLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public void initTranslationY(int startY,int endY){
        mStartY = startY;
        mEndY = endY;
        mDistance = Math.abs(endY - startY);
        setTranslationY(startY);
    }
    public void initScale(float startScale,float endScale){
        mStartScale = startScale;
        mEndScale = endScale;
        setScaleX(startScale);
        setScaleY(startScale);
        mScale = Math.abs(endScale - startScale);
    }
    private void setScaleXY(float rate){
        setScaleX(calculateScale(rate));
        setScaleY(calculateScale(rate));
    }
    private float calculateScale(float rate){
        return mStartScale + mScale * rate;
    }
    private float calculateTransY(float rate){
        return mStartY + mDistance * rate;
    }

    public void setDirection(int direction){
        mDirection = direction;
    }
    @Override
    public void onStartScroll() {
        Log.e(TAG,"onStartScroll");
        setVisibility(VISIBLE);
    }

    @Override
    public void onScroll(float rate) {
        Log.e(TAG,"onScroll rate =:" + rate);
        switch (mDirection){
            case UP_WHEN_SCROLL_UP:
                setTranslationY(calculateTransY(-rate));
                break;
            case DOWN_WHEN_SCROLL_UP:
                setTranslationY(calculateTransY(rate));
                break;
            case GROWUP_WHEN_SCROLL_UP:
                setScaleXY(rate);
                break;
            case SHRINK_WHEN_SCROLL_UP:
                setScaleXY(-rate);
                break;
        }

    }

    @Override
    public void onEndScroll() {
        Log.e(TAG,"onEndScroll");
    }
}
