package com.zibuyuqing.ucbrowser.widget.favorite;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Trace;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Xijun.Wang on 2017/12/20.
 */

public class DragView extends View{
    private static final String TAG ="DragView";
    private int mRegistrationX;
    private int mRegistrationY;
    private float mOffsetX;
    private float mOffsetY;
    private Rect mDragRegion = null;
    public Bitmap mBitmap;
    ValueAnimator mAnim;
    private Point mDragVisualizeOffset = null;
    private boolean mHasDrawn = false;
    private float mInitialScale = 1f;
    private float mIntrinsicIconScale = 1f;
    private Paint mPaint;
    private DragLayer mParent;
    public DragView(Context context) {
        this(context,null);
    }

    public DragView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public DragView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DragView(
            Context context,
            Bitmap b,
            int registrationX,
            int registrationY,
            int left,
            int top,
            int width,
            int height,
            final float initialScale) {

        super(context);
        mInitialScale = initialScale;
        final float scale = 1.15f;
        setScaleX(initialScale);
        setScaleY(initialScale);
        mAnim = AnimUtil.ofFloat(this,0f,1f);
        mAnim.setDuration(150);
        mAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                final float value = (Float) valueAnimator.getAnimatedValue();
                final int deltaX = (int) (-mOffsetX);
                final int deltaY = (int) (-mOffsetY);
                mOffsetX += deltaX;
                mOffsetY += deltaY;
                setScaleX(initialScale + (value * (scale - initialScale)));
                setScaleY(initialScale + (value * (scale - initialScale)));
                if (getParent() == null) {
                    valueAnimator.cancel();
                } else {
                    setTranslationX(getTranslationX() + deltaX);
                    setTranslationY(getTranslationY() + deltaY);
                }
            }
        });
        mBitmap = Bitmap.createBitmap(b, left, top, width, height);
        setDragRegion(new Rect(0, 0, width, height));

        // The point in our scaled bitmap that the touch events are located
        mRegistrationX = registrationX;
        mRegistrationY = registrationY;
        // Force a measure, because Workspace uses getMeasuredHeight() before the layout pass
        int ms = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        measure(ms, ms);
        mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mBitmap.getWidth(), mBitmap.getHeight());
    }
    @Override
    protected void onDraw(Canvas canvas) {
        @SuppressWarnings("all") // suppress dead code warning
        final boolean debug = false;
        if (debug) {
            Paint p = new Paint();
            p.setStyle(Paint.Style.FILL);
            p.setColor(0x66ffffff);
            canvas.drawRect(0, 0, getWidth(), getHeight(), p);
        }
        mHasDrawn = true;
        canvas.drawBitmap(mBitmap, 0.0f, 0.0f, mPaint);
    }
    public boolean hasDrawn() {
        return mHasDrawn;
    }
    /**
     * Move the window containing this view.
     *
     * @param touchX the x coordinate the user touched in DragLayer coordinates
     * @param touchY the y coordinate the user touched in DragLayer coordinates
     */
    void move(int touchX, int touchY) {
        setTranslationX(touchX - mRegistrationX + (int) mOffsetX);
        setTranslationY(touchY - mRegistrationY + (int) mOffsetY);
    }

    public void showAt(DragLayer parent,int touchX,int touchY){
        mParent = parent;
        show(touchX,touchY);
    }
    /**
     * Create a window containing this view and show it.
     *
     * @param windowToken obtained from v.getWindowToken() from one of your views
     * @param touchX the x coordinate the user touched in DragLayer coordinates
     * @param touchY the y coordinate the user touched in DragLayer coordinates
     */
    private void show(int touchX, int touchY) {
        mParent.addView(this);
        // Start the pick-up animation
        DragLayer.LayoutParams lp = new DragLayer.LayoutParams(0, 0);
        lp.width = mBitmap.getWidth();
        lp.height = mBitmap.getHeight();
        lp.customPosition = true;
        setLayoutParams(lp);
        setTranslationX(touchX - mRegistrationX);
        setTranslationY(touchY - mRegistrationY);

        // Post the animation to skip other expensive work happening on the first frame
        post(new Runnable() {
            public void run() {
                mAnim.start();
            }
        });
    }
    public void remove(){
        if(mParent != null){
            mParent.removeView(this);
        }
    }
    public void setDragRegion(Rect r) {
        mDragRegion = r;
    }
    public Rect getDragRegion() {
        return mDragRegion;
    }
    public void setDragVisualizeOffset(Point p) {
        mDragVisualizeOffset = p;
    }

    public Point getDragVisualizeOffset() {
        return mDragVisualizeOffset;
    }

    public float getIntrinsicIconScaleFactor() {
        return mIntrinsicIconScale;
    }
    public void cancelAnimation() {
        if (mAnim != null && mAnim.isRunning()) {
            mAnim.cancel();
        }
    }
    public void resetLayoutParams() {
        mOffsetX = mOffsetY = 0;
        requestLayout();
    }
}
