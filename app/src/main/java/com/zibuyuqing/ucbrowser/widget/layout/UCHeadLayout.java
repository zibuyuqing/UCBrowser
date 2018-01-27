package com.zibuyuqing.ucbrowser.widget.layout;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.zibuyuqing.ucbrowser.R;
import com.zibuyuqing.ucbrowser.base.BaseLayout;
import com.zibuyuqing.ucbrowser.widget.root.UCRootView;

/**
 * Created by Xijun.Wang on 2017/11/28.
 */

public class UCHeadLayout extends BaseLayout {
    private static final int ALPHA_255 = 255;
    private Drawable mForeground;
    private View mCoverTip;
    private View mUCCoverLayout;
    private View mCategoryContain;
    private View mWebsiteContain;
    private View mWebsiteLayout;
    public UCHeadLayout(Context context) {
        this(context,null);
    }

    public UCHeadLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public UCHeadLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mUCCoverLayout = findViewById(R.id.rlUCCoverLayout);
        mCoverTip = mUCCoverLayout.findViewById(R.id.ucCoverTip);
        mCategoryContain = findViewById(R.id.llBezierContain);
        mWebsiteContain = findViewById(R.id.rvWebsiteList);
        mWebsiteLayout = findViewById(R.id.llUCWebsiteLayout);
    }

    @Override
    protected void init() {
        super.init();
        mForeground = getForeground();
        mForeground.setAlpha(0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onStartScroll(int direction) {
        super.onStartScroll(direction);
        // mUCCoverLayout 为下拉时所看到的提示“上滑进入UC头条”的布局
        mUCCoverLayout.setVisibility(VISIBLE);
        mUCCoverLayout.setAlpha(0.f);

    }

    @Override
    public void onEndScroll() {
        super.onEndScroll();
    }

    @Override
    public void onScroll(float rate) {
        Log.e("wanngx","onScroll ::" + mStartScroll);
        if(mDirection == UCRootView.SCROLL_HORIZONTALLY){
            if(rate > 0){
                return;
            }
            mWebsiteLayout.setTranslationX(calculateTransX(rate));

        } else if(mDirection == UCRootView.SCROLL_VERTICALLY) {
            if (rate > 0) {
                // 下拉
                mUCCoverLayout.setVisibility(VISIBLE);
                // 显示提示语并下移
                mCoverTip.setTranslationY(100 * Math.abs(rate));
                // 提示布局逐渐显现
                mUCCoverLayout.setAlpha(rate * 1.5f);
            } else {
                // 上滑

                // 隐藏提示布局
                mUCCoverLayout.setVisibility(GONE);
                // foreground 逐渐显现，布局变黑
                mForeground.setAlpha((int) (ALPHA_255 * Math.abs(rate)));
                float adjustRate = 1.0f + rate * 0.05f;

                // 布局内容逐渐变小
                mCategoryContain.setScaleX(adjustRate);
                mCategoryContain.setScaleY(adjustRate);
                mWebsiteContain.setScaleX(adjustRate);
                mWebsiteContain.setScaleY(adjustRate);
            }
        }
    }

}
