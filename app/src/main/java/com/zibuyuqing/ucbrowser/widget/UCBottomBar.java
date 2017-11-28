package com.zibuyuqing.ucbrowser.widget;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.zibuyuqing.ucbrowser.R;
import com.zibuyuqing.ucbrowser.utils.ViewUtil;

/**
 * Created by Xijun.Wang on 2017/11/28.
 */
public class UCBottomBar extends BaseLayout {
    private View ivForward,ivBack,ivMenu, flWindowNum,tvHeadline,tvVideo,tvSubscribe;
    private int mScreenWidth;
    private int mHalfHeight;
    private String TAG ="UCBottomBar";
    public UCBottomBar(Context context) {
        this(context,null);
    }

    public UCBottomBar(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public UCBottomBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mScreenWidth = ViewUtil.getScreenSize(getContext()).x;
        mHalfHeight = getResources().getDimensionPixelSize(R.dimen.dimen_48dp);
        setTranslationY(mHalfHeight);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ivForward = findViewById(R.id.ivForward);
        ivBack = findViewById(R.id.ivBack);
        ivMenu = findViewById(R.id.ivMenu);
        flWindowNum = findViewById(R.id.flWindowsNum);
        tvHeadline = findViewById(R.id.tvHeadline);
        tvVideo = findViewById(R.id.tvVideo);
        tvSubscribe = findViewById(R.id.tvSubscribe);
    }

    @Override
    public void onStartScroll() {

    }

    @Override
    public void onEndScroll() {

    }

    @Override
    public void onScroll(float rate) {
        Log.e(TAG,"onScroll :: rate =:" + rate);
        if(rate > 0){
            return;
        }
        ivForward.setAlpha(calculateBtnAlpha(rate));
        ivBack.setAlpha(calculateBtnAlpha(rate));
        flWindowNum.setAlpha(calculateBtnAlpha(rate));
        ivMenu.setTranslationX(calculateMenuBtnTransX(rate));
        tvSubscribe.setTranslationY(calculateNewsBtnTranY(mHalfHeight,rate,1.0f));
        tvVideo.setTranslationY(calculateNewsBtnTranY(mHalfHeight,rate,1.5f));
        tvHeadline.setTranslationY(calculateNewsBtnTranY(mHalfHeight ,rate,2.0f));
    }
    private float calculateMenuBtnTransX(float rate){
        float dis = mScreenWidth / 5;
        return - dis * rate;
    }
    private float calculateNewsBtnTranY(int finalY,float rate,float velocity){
        float adjustRate = rate * velocity;
        rate = adjustRate < -1.0f ? -1.0f : adjustRate;
        return 0 + finalY * rate;
    }
    private float calculateBtnAlpha(float rate){
        return 1.0f + rate;
    }
}
