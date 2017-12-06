package com.zibuyuqing.ucbrowser.widget.layout;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.zibuyuqing.ucbrowser.R;
import com.zibuyuqing.ucbrowser.base.BaseLayout;
import com.zibuyuqing.ucbrowser.utils.ViewUtil;

/**
 * Created by Xijun.Wang on 2017/11/28.
 */

public class BezierLayout extends BaseLayout {
    public static final int FINAL_DISTANCE = 300;
    private Paint mPaint;
    private int mThemeColor;
    private Point mControlPoint;
    private int mScreenWidth;
    private Path mPath = new Path();
    private int mHeight;
    private int mEdgeHeight;
    private boolean mStartScroll = false;
    private ViewGroup.LayoutParams mLayoutParams;
    private View mContain;
    public BezierLayout(Context context) {
        this(context,null);
    }

    public BezierLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BezierLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContain = findViewById(R.id.llBezierContain);
    }

    @Override
    protected void init() {
        super.init();
        mScreenWidth = ViewUtil.getScreenSize(mContext).x;
        mHeight = mRes.getDimensionPixelSize(R.dimen.bezier_layout_height);
        mEdgeHeight = mHeight;
        mThemeColor = mRes.getColor(R.color.themeBlue,null);
        mControlPoint = new Point(0,mHeight);
        mPaint = new Paint();
        mPaint.setColor(mThemeColor);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
    }

    private void drawBg(Canvas canvas) {
        mPath.reset();
        mPath.moveTo(0,0);
        mPath.lineTo(0,mEdgeHeight);
        mPath.quadTo(mControlPoint.x,mControlPoint.y,mScreenWidth,mEdgeHeight);
        mPath.lineTo(mScreenWidth,0);
        mPath.lineTo(0,0);
        canvas.drawPath(mPath,mPaint);
    }
    @Override
    public void onTouch(float x, float y) {
        super.onTouch(x, y);
        touch(x,y);
    }

    @Override
    public void onScroll(float rate) {
        if(!mStartScroll){
            return;
        }
        if(mLayoutParams == null){
            mLayoutParams = getLayoutParams();
        }
        if(rate >= 0) {
            int dis = (int) (FINAL_DISTANCE * rate);
            mEdgeHeight = (int) (mHeight + dis * 0.5f);
            mControlPoint.set(mControlPoint.x, mHeight + dis);
            mContain.setScaleX(1.0f - rate * 0.2f);
            mContain.setScaleY(1.0f - rate * 0.2f);
            mContain.setTranslationY(dis * 0.5f);
            mContain.setAlpha(1.0f - rate * 1.5f);
        } else {
            mControlPoint.set(0,mHeight);
        }
        mLayoutParams.height = mControlPoint.y;
        setLayoutParams(mLayoutParams);
        requestLayout();
        super.onScroll(rate);
    }

    @Override
    public void onStartScroll() {
        mStartScroll = true;
        super.onStartScroll();
    }

    @Override
    public void onEndScroll() {
        mStartScroll = false;
        super.onEndScroll();
    }

    public void touch(float x, float y) {
        mControlPoint.set((int) x, mControlPoint.y);
        invalidate();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        drawBg(canvas);
        super.dispatchDraw(canvas);
    }

}
