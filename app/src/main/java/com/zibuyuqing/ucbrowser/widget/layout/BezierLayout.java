package com.zibuyuqing.ucbrowser.widget.layout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.zibuyuqing.ucbrowser.R;
import com.zibuyuqing.ucbrowser.base.BaseLayout;
import com.zibuyuqing.ucbrowser.utils.ViewUtil;
import com.zibuyuqing.ucbrowser.widget.drawable.AlphaDrawable;
import com.zibuyuqing.ucbrowser.widget.root.UCRootView;

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
    private int mHeight,mWeatherLayoutHeight, mCategoryLayoutHeight,mSearchboxMarginLeft;
    private int mEdgeHeight;
    private int mMinHeight;
    private ViewGroup.LayoutParams mLayoutParams;
    private View mContain;
    private View mWeather,mSearchbox,mCategory;
    private AlphaDrawable mSearchboxBg;
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
        mWeather = findViewById(R.id.rlUCWeather);
        mSearchbox = findViewById(R.id.rlUCSearchbox);
        mSearchboxBg = new AlphaDrawable(
                mRes.getDrawable(R.drawable.search_box_bg,null));
        mSearchbox.setBackground(mSearchboxBg);
        mCategory = findViewById(R.id.llUCCategory);
    }

    @Override
    protected void init() {
        super.init();
        mScreenWidth = ViewUtil.getScreenSize(mContext).x;
        mHeight = mRes.getDimensionPixelSize(R.dimen.bezier_layout_height);
        mWeatherLayoutHeight = mRes.getDimensionPixelSize(R.dimen.dimen_64dp);
        mCategoryLayoutHeight = mRes.getDimensionPixelSize(R.dimen.dimen_72dp);
        mSearchboxMarginLeft = mRes.getDimensionPixelSize(R.dimen.dimen_16dp);
        mEdgeHeight = mHeight;
        mThemeColor = mRes.getColor(R.color.themeBlue,null);
        mControlPoint = new Point(0,mHeight);
        mPaint = new Paint();
        mPaint.setColor(mThemeColor);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
    }


    @Override
    public void move(float x, float y) {
        super.move(x, y);
        mControlPoint.set((int) x, mControlPoint.y);
        invalidate();
    }

    @Override
    public void onScroll(float rate) {
        // 获取 LayoutParams 根据滑动状态动态更新视图大小
        if(rate < -1){
            return;
        }
        if(mLayoutParams == null){
            mLayoutParams = getLayoutParams();
        }
        if(mDirection == UCRootView.SCROLL_HORIZONTALLY){
            mContain.setTranslationY(mWeatherLayoutHeight * rate);
            mSearchboxBg.setAlpha((int) (255 * (1 + rate)));
            int dis = (int) ((mCategoryLayoutHeight + mWeatherLayoutHeight )* rate);
            mEdgeHeight = mHeight + dis;
            mControlPoint.set(mControlPoint.x, mHeight + dis);
            LinearLayout.LayoutParams layoutParams =
                    (LinearLayout.LayoutParams) mSearchbox.getLayoutParams();
            if(layoutParams != null){
                layoutParams.rightMargin = layoutParams.leftMargin =
                        (int) (mSearchboxMarginLeft * (1 + rate));
                mSearchbox.setLayoutParams(layoutParams);
            }
            mCategory.setAlpha(1 + 1.25f * rate);
        } else if(mDirection == UCRootView.SCROLL_VERTICALLY) {

            if (rate >= 0 ) {

                // 下拉

                // FINAL_DISTANCE 为最大能滑动的距离
                float adjustRate = rate * 0.6f;
                int dis = (int) (FINAL_DISTANCE * adjustRate);

                // 左右边界更新速度是控制点的0.5倍

                mEdgeHeight = (int) (mHeight + dis * 0.5f);

                //控制点更新
                mControlPoint.set(mControlPoint.x, mHeight + dis);

                // 视图内容改变大小，位置和透明度
                mContain.setScaleX(1.0f - rate * 0.2f);
                mContain.setScaleY(1.0f - rate * 0.2f);
                mContain.setTranslationY(dis * 0.5f);
                mContain.setAlpha(1.0f - rate * 1.5f);
            } else {
                // 上滑
                mContain.setTranslationY(0);
                mControlPoint.set(0, mHeight);
            }
            mLayoutParams.height = mControlPoint.y;
        }
        // 改变视图大小
        setLayoutParams(mLayoutParams);
        requestLayout();
    }

    @Override
    public void onStartScroll(int direction) {
        mStartScroll = true;
        super.onStartScroll(direction);
    }

    @Override
    public void onEndScroll() {
        mStartScroll = false;
        super.onEndScroll();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        drawBg(canvas);
        super.dispatchDraw(canvas);
    }

    private void drawBg(Canvas canvas) {
        mPath.reset();
        // 顶部开始
        mPath.moveTo(0,0);
        mPath.lineTo(0,mEdgeHeight);
        // 贝塞尔曲线
        mPath.quadTo(mControlPoint.x,mControlPoint.y,mScreenWidth,mEdgeHeight);
        mPath.lineTo(mScreenWidth,0);
        // 闭合
        mPath.lineTo(0,0);
        canvas.drawPath(mPath,mPaint);
        Paint paint = new Paint(Color.RED);
        paint.setStyle(Paint.Style.FILL);
    }
}
