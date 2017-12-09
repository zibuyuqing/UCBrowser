package com.zibuyuqing.ucbrowser.base;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

import com.zibuyuqing.ucbrowser.widget.root.UCRootView;

import static com.zibuyuqing.ucbrowser.widget.root.UCRootView.SCROLL_HORIZONTALLY;
import static com.zibuyuqing.ucbrowser.widget.root.UCRootView.SCROLL_VERTICALLY;

/**
 * Created by Xijun.Wang on 2017/11/27.
 */

public class BaseLayout extends LinearLayout implements UCRootView.ScrollStateListener {
    private static final String TAG = "BaseLayout";
    private int mStartX;
    private int mEndX;
    private int mStartY;
    private int mEndY;
    private float mStartScale;
    private float mEndScale;
    private int mDistanceX;
    private int mDistanceY;
    private float mScale;
    protected boolean mTransYEnable = false;
    protected boolean mTransXEnable = false;
    protected boolean mScaleEnable = false;
    protected Context mContext;
    protected Resources mRes;
    protected boolean mStartScroll = false;
    protected int mDirection = UCRootView.SCROLL_NONE;

    public BaseLayout(Context context) {
        this(context, null);
    }

    public BaseLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    protected void init() {
        mRes = mContext.getResources();
    }

    public void setTransYEnable(boolean transYEnable) {
        mTransYEnable = transYEnable;
    }

    public void setTransXEnable(boolean transYEnable) {
        mTransXEnable = transYEnable;
    }

    public void setScaleEnable(boolean scaleEnable) {
        mScaleEnable = scaleEnable;
    }

    /**
     * @param from 起始位置
     * @param to   最终位置
     */
    public void initTranslationY(int from, int to) {
        mStartY = from;
        mEndY = to;
        setTranslationY(from);
        mDistanceY = from - to;
    }
    public void initTranslationX(int from, int to) {
        mStartX = from;
        mEndX = to;
        setTranslationX(from);
        mDistanceX = from - to;
    }
    public void initScale(float startScale, float endScale) {
        mStartScale = startScale;
        mEndScale = endScale;
        setScaleX(startScale);
        setScaleY(startScale);
        mScale = endScale - startScale;
    }

    private void setScaleXY(float rate) {
        setScaleX(calculateScale(rate));
        setScaleY(calculateScale(rate));
    }

    public float calculateScale(float rate) {
        return mStartScale + mScale * rate;
    }

    /**
     * @param rate 滑动的相对比率
     * @return
     */
    public float calculateTransY(float rate) {
        Log.e(TAG, "rate :: =;" + rate);
        if(Math.abs(rate) < 1) {
            return mStartY + mDistanceY * rate;
        } else {
            return mEndY;
        }
    }

    public float calculateTransX(float rate) {
        Log.e(TAG, "rate :: =;" + rate);
        if(Math.abs(rate) < 1) {
            return mStartX + mDistanceX * rate;
        } else {
            return mEndX;
        }
    }

    @Override
    public void onStartScroll(int direction) {
        Log.e(TAG, "onStartScroll");
        mDirection = direction;
        mStartScroll = true;
        setVisibility(VISIBLE);
    }

    @Override
    public void onScroll(float rate) {
        if (!mStartScroll || rate > 0) {
            return;
        }
        Log.e(TAG, "onScroll rate =:" + rate);

        switch (mDirection) {
            case SCROLL_HORIZONTALLY:
                if (mTransXEnable) setTranslationX(calculateTransX(rate));
                break;
            case SCROLL_VERTICALLY:
                if (mTransYEnable) setTranslationY(calculateTransY(rate));
                break;
        }
        if (mScaleEnable) {
            setScaleXY(-rate);
        }
    }

    @Override
    public void onEndScroll() {
        Log.e(TAG, "onEndScroll");
        mStartScroll = false;
    }


    @Override
    public void onTouch(float x, float y) {

    }
}
