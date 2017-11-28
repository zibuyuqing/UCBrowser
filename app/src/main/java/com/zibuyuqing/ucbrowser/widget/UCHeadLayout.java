package com.zibuyuqing.ucbrowser.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zibuyuqing.ucbrowser.R;

/**
 * Created by Xijun.Wang on 2017/11/28.
 */

public class UCHeadLayout extends BaseLayout{
    private static final int ALPHA_255 = 255;
    private Drawable mForeground;
    private TextView mCoverTip;
    private RelativeLayout mUCCoverLayout;
    public UCHeadLayout(Context context) {
        this(context,null);
    }

    public UCHeadLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public UCHeadLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mUCCoverLayout = (RelativeLayout)findViewById(R.id.rlUCCoverLayout);
        mCoverTip = (TextView)mUCCoverLayout.findViewById(R.id.ucCoverTip);
    }


    private void init() {
        mForeground = getForeground();
        mForeground.setAlpha(0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onScroll(float rate) {
        if(rate > 0) {
            mCoverTip.setTranslationY(100 * Math.abs(rate));
            mStop = true;
        } else {
            mForeground.setAlpha((int) (ALPHA_255 * Math.abs(rate)));
        }
        super.onScroll(rate);
    }

}
