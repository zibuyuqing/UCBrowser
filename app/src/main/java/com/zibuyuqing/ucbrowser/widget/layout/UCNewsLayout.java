package com.zibuyuqing.ucbrowser.widget.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.zibuyuqing.common.utils.ViewUtil;
import com.zibuyuqing.ucbrowser.R;
import com.zibuyuqing.ucbrowser.base.BaseLayout;
import com.zibuyuqing.ucbrowser.widget.root.UCRootView;

/**
 * Created by Xijun.Wang on 2017/11/29.
 */

public class UCNewsLayout extends BaseLayout {
    private int mNewsTabBarHeight,mLayoutHeight,mLayoutWidth,
            mScreenWidth,mScreenHeight,mTopSearchBarHeight,mNewsContainHeight;
    private View mNewsFlag,mNewsTabBar,mNewsContain;
    public UCNewsLayout(Context context) {
        this(context,null);
    }

    public UCNewsLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public UCNewsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mNewsFlag = findViewById(R.id.flUCNewsFlag);
        mNewsTabBar = findViewById(R.id.llUCNewsTabBar);
        mNewsContain = findViewById(R.id.vpUCNewsPager);
        mNewsFlag.setTranslationY(0);
        mNewsTabBar.setTranslationY(mNewsTabBarHeight);
    }

    @Override
    protected void init() {
        super.init();
        mNewsTabBarHeight = mRes.getDimensionPixelSize(R.dimen.dimen_36dp);
        mTopSearchBarHeight = mRes.getDimensionPixelSize(R.dimen.dimen_48dp);
        mScreenWidth = ViewUtil.getScreenSize(mContext).x;
        mScreenHeight = ViewUtil.getScreenSize(mContext).y;
        mLayoutWidth = mScreenWidth;
        mLayoutHeight = mScreenHeight - 2 * mTopSearchBarHeight;
        mNewsContainHeight = mLayoutHeight - mNewsTabBarHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int newsBarWidthSpec = MeasureSpec.makeMeasureSpec(mScreenWidth , MeasureSpec.EXACTLY);
        int newsContainHeightSpec = MeasureSpec.makeMeasureSpec(mNewsContainHeight,MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        mNewsContain.measure(newsBarWidthSpec,newsContainHeightSpec);
        setMeasuredDimension(mLayoutWidth,mLayoutHeight);
    }

    @Override
    public void onScroll(float rate) {
        super.onScroll(rate);
        if(mDirection == UCRootView.SCROLL_VERTICALLY) {
            if (rate <= 0) {
                mNewsFlag.setTranslationY(mNewsTabBarHeight * (Math.abs(rate)));
                mNewsTabBar.setTranslationY(mNewsTabBarHeight * (1.0f + rate));
            }
        }
    }

    @Override
    public void onEndScroll() {
        super.onEndScroll();
    }
}
