package com.zibuyuqing.ucbrowser.widget.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.zibuyuqing.ucbrowser.R;
import com.zibuyuqing.ucbrowser.base.BaseLayout;
import com.zibuyuqing.ucbrowser.utils.ViewUtil;
import com.zibuyuqing.ucbrowser.widget.root.UCRootView;

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
    }
    @Override
    protected void init() {
        super.init();
        mScreenWidth = ViewUtil.getScreenSize(mContext).x;
        mHalfHeight = mRes.getDimensionPixelSize(R.dimen.dimen_48dp);
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
    public void onStartScroll(int direction) {
        super.onStartScroll(direction);
    }

    @Override
    public void onEndScroll() {

    }

    @Override
    public void onScroll(float rate) {
        Log.i(TAG,"onScroll :: rate =:" + rate);
        super.onScroll(rate);
        if(mDirection == UCRootView.SCROLL_HORIZONTALLY){
            return;
        }
        //第1,2,4个按钮渐隐
        ivForward.setAlpha(calculateBtnAlpha(rate));
        ivBack.setAlpha(calculateBtnAlpha(rate));
        flWindowNum.setAlpha(calculateBtnAlpha(rate));
        // 第三个按钮移动到第四个位置
        ivMenu.setTranslationX(calculateMenuBtnTransX(rate));

        // 新闻按钮依次上升出现
        tvSubscribe.setTranslationY(calculateNewsBtnTransY(mHalfHeight,rate,1.0f));
        tvVideo.setTranslationY(calculateNewsBtnTransY(mHalfHeight,rate,1.5f));
        tvHeadline.setTranslationY(calculateNewsBtnTransY(mHalfHeight ,rate,2.0f));
    }
    private float calculateMenuBtnTransX(float rate){
        float dis = mScreenWidth / 5;
        return - dis * rate;
    }
    private float calculateNewsBtnTransY(int finalY, float rate, float velocity){

        // velocity 是调整速率
        float adjustRate = rate * velocity;
        rate = adjustRate < -1.0f ? -1.0f : adjustRate;
        return 0 + finalY * rate;
    }
    private float calculateBtnAlpha(float rate){
        return 1.0f + rate;
    }
}
