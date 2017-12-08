package com.zibuyuqing.ucbrowser.base;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

import com.zibuyuqing.ucbrowser.widget.root.UCRootView;

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
    protected Context mContext;
    protected Resources mRes;
    public BaseLayout(Context context) {
        this(context,null);
    }
    public BaseLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BaseLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }
    protected void init(){
        mRes = mContext.getResources();
    }
    public void setTranslateEnable(boolean translateEnable) {
        mTranslateEnable = translateEnable;
    }

    public void setScaleEnable(boolean scaleEnable) {
        mScaleEnable = scaleEnable;
    }

    /**
     *
     * @param from 起始位置
     * @param to 最终位置
     */
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

    /**
     *
     * @param rate 滑动的相对比率
     * @return
     */
    private float calculateTransY(float rate){
        Log.e(TAG,"rate :: =;" + rate);
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
