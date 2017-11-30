package com.zibuyuqing.ucbrowser.widget.layout;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.zibuyuqing.ucbrowser.R;
import com.zibuyuqing.ucbrowser.base.BaseLayout;

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
        mWebsiteContain = findViewById(R.id.llUCHeadWebsiteContain);
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
    public void onStartScroll() {
        mUCCoverLayout.setVisibility(VISIBLE);
        mUCCoverLayout.setAlpha(0.f);
        super.onStartScroll();
    }

    @Override
    public void onEndScroll() {
        super.onEndScroll();
    }

    @Override
    public void onScroll(float rate) {
        if(rate > 0) {
            mCoverTip.setTranslationY(100 * Math.abs(rate));
            mUCCoverLayout.setAlpha(rate * 1.5f);
        } else {
            mUCCoverLayout.setVisibility(GONE);
            mForeground.setAlpha((int) (ALPHA_255 * Math.abs(rate)));
            float adjustRate = 1.0f + rate * 0.05f;
            mCategoryContain.setScaleX(adjustRate);
            mCategoryContain.setScaleY(adjustRate);
            mWebsiteContain.setScaleX(adjustRate);
            mWebsiteContain.setScaleY(adjustRate);
        }
        super.onScroll(rate);
    }

}
