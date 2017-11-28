package com.zibuyuqing.ucbrowser.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;

import com.zibuyuqing.ucbrowser.R;
import com.zibuyuqing.ucbrowser.utils.ViewUtil;

/**
 * Created by Xijun.Wang on 2017/11/28.
 */

public class BezierLayout extends BaseLayout {
    private Paint mPaint;
    private int mThemeColor;
    private Context mContext;
    private Point mControlPoint;
    private int mScreenWidth;
    private Path mPath = new Path();
    private int mHeight;
    private int mEdgeHeight;
    public BezierLayout(Context context) {
        this(context,null);
    }

    public BezierLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BezierLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        Resources res = mContext.getResources();
        mScreenWidth = ViewUtil.getScreenSize(mContext).x;
        mHeight = res.getDimensionPixelSize(R.dimen.bezier_layout_height);
        mEdgeHeight = mHeight;
        mThemeColor = res.getColor(R.color.themeBlue,null);
        Log.e("-----","---------------");
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
        if(rate >= 0) {
            int finalDis = 100;
            ViewGroup.LayoutParams lp = getLayoutParams();
            int dis = (int) (finalDis * rate);
            mEdgeHeight = (int) (mHeight + dis / 2);
            mControlPoint.set(mControlPoint.x, mHeight + dis);
            if (lp != null) {
                lp.height = mControlPoint.y;
                setLayoutParams(lp);
            }
            requestLayout();
        } else {
            mControlPoint.set(0,mHeight);
        }
        super.onScroll(rate);
    }

    public void touch(float x, float y) {
        mControlPoint.set((int) x, mControlPoint.y);
        Log.e("-----","mControlPoint :" + mControlPoint.x + ",y = " + y);
        invalidate();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        drawBg(canvas);
        super.dispatchDraw(canvas);
    }

}
