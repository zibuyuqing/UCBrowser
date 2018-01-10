package com.zibuyuqing.stackview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
/**
 * Created by Xijun.Wang on 2017/12/4.
 */

public class SwipeHelper {
    static final String TAG = "SwipeHelper";
    private static final boolean SLOW_ANIMATIONS = false; // DEBUG;
    private static final boolean CONSTRAIN_SWIPE = true;
    private static final boolean FADE_OUT_DURING_SWIPE = true;
    private static final boolean DISMISS_IF_SWIPED_FAR_ENOUGH = true;

    public static final int X = 0;
    public static final int Y = 1;
    private static LinearInterpolator sLinearInterpolator = new LinearInterpolator();
    private float SWIPE_ESCAPE_VELOCITY = 100f; // dp/sec
    private int DEFAULT_ESCAPE_ANIMATION_DURATION = 75; // ms
    private int MAX_ESCAPE_ANIMATION_DURATION = 150; // ms
    private static final int SNAP_ANIM_LEN = SLOW_ANIMATIONS ? 1000 : 250; // ms
    public static float ALPHA_FADE_START = 0.15f; // fraction of thumbnail width
    // where fade starts
    static final float ALPHA_FADE_END = 0.65f; // fraction of thumbnail width
    // beyond which alpha->0
    private float mMinAlpha = 0f;

    private float mPagingTouchSlop;
    Callback mCallback;
    private int mSwipeDirection;
    private VelocityTracker mVelocityTracker;

    private float mInitialTouchPos;
    private boolean mDragging;

    private View mCurrView;
    private boolean mCanCurrViewBeDimissed;
    private float mDensityScale;

    public boolean mAllowSwipeTowardsStart = true;
    public boolean mAllowSwipeTowardsEnd = true;
    private TimeInterpolator linearOutSlowInInterpolator;
    public SwipeHelper(Context context,int swipeDirection, Callback callback, float densityScale,
                       float pagingTouchSlop) {
        mCallback = callback;
        mSwipeDirection = swipeDirection;
        mVelocityTracker = VelocityTracker.obtain();
        mDensityScale = densityScale;
        mPagingTouchSlop = pagingTouchSlop;
        linearOutSlowInInterpolator = AnimationUtils.loadInterpolator(
                context, R.anim.linear_out_show_in);
    }
    public void setDensityScale(float densityScale) {
        mDensityScale = densityScale;
    }

    public void setPagingTouchSlop(float pagingTouchSlop) {
        mPagingTouchSlop = pagingTouchSlop;
    }

    public void cancelOngoingDrag() {
        if (mDragging) {
            if (mCurrView != null) {
                mCallback.onDragCancelled(mCurrView);
                setTranslation(mCurrView, 0);
                mCallback.onSnapBackCompleted(mCurrView);
                mCurrView = null;
            }
            mDragging = false;
        }
    }
    public void resetTranslation(View v) {
        setTranslation(v, 0);
    }
    // mSwipeDirection 配置的拖动方向
    private float getPos(MotionEvent ev) {
        return mSwipeDirection == X ? ev.getX() : ev.getY();
    }

    private float getTranslation(View v) {
        return mSwipeDirection == X ? v.getTranslationX() : v.getTranslationY();
    }

    private float getVelocity(VelocityTracker vt) {
        return mSwipeDirection == X ? vt.getXVelocity() :
                vt.getYVelocity();
    }

    private ObjectAnimator createTranslationAnimation(View v, float newPos) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(v,
                mSwipeDirection == X ? View.TRANSLATION_X : View.TRANSLATION_Y, newPos);
        return anim;
    }

    private float getPerpendicularVelocity(VelocityTracker vt) {
        return mSwipeDirection == X ? vt.getYVelocity() :
                vt.getXVelocity();
    }

    private void setTranslation(View v, float translate) {
        if (mSwipeDirection == X) {
            v.setTranslationX(translate);
        } else {
            v.setTranslationY(translate);
        }
    }

    private float getSize(View v) {
        final DisplayMetrics dm = v.getContext().getResources().getDisplayMetrics();
        return mSwipeDirection == X ? dm.widthPixels : dm.heightPixels;
    }

    public void setMinAlpha(float minAlpha) {
        mMinAlpha = minAlpha;
    }

    float getAlphaForOffset(View view) {
        float viewSize = getSize(view);
        final float fadeSize = ALPHA_FADE_END * viewSize;
        float result = 1.0f;
        float pos = getTranslation(view);
        if (pos >= viewSize * ALPHA_FADE_START) {
            result = 1.0f - (pos - viewSize * ALPHA_FADE_START) / fadeSize;
        } else if (pos < viewSize * (1.0f - ALPHA_FADE_START)) {
            result = 1.0f + (viewSize * ALPHA_FADE_START + pos) / fadeSize;
        }
        result = Math.min(result, 1.0f);
        result = Math.max(result, 0f);
        return Math.max(mMinAlpha, result);
    }


    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        Log.e(TAG,"onInterceptTouchEvent ::action =:" + action);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.e(TAG,"onInterceptTouchEvent ;; ev.getPointerCount() =:" + ev.getPointerCount());
                if(ev.getPointerCount() < 1){
                    return false;
                }
                mDragging = false;
                mCurrView = mCallback.getChildAtPosition(ev);// 获取拖动的view
                mVelocityTracker.clear();
                if (mCurrView != null) {
                    // 标记view是否可以消逝
                    mCanCurrViewBeDimissed = mCallback.canChildBeDismissed(mCurrView);
                    mVelocityTracker.addMovement(ev);
                    mInitialTouchPos = getPos(ev); // 根据配置获取位置
                } else {
                    mCanCurrViewBeDimissed = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mCurrView != null) {
                    mVelocityTracker.addMovement(ev);
                    float pos = getPos(ev);
                    float delta = pos - mInitialTouchPos;
                    if (Math.abs(delta) > mPagingTouchSlop) {
                        mCallback.onBeginDrag(mCurrView);
                        mDragging = true;
                        mInitialTouchPos = pos - getTranslation(mCurrView);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mDragging = false;
                mCurrView = null;
                break;
        }
        return mDragging;
    }

    /**
     * @param view The view to be dismissed
     * @param velocity The desired pixels/second speed at which the view should move
     */
    private void dismissChild(final View view, float velocity) {
        final boolean canAnimViewBeDismissed = mCallback.canChildBeDismissed(view);
        float newPos;
        if (velocity < 0
                || (velocity == 0 && getTranslation(view) < 0)
                // if we use the Menu to dismiss an item in landscape, animate up
                || (velocity == 0 && getTranslation(view) == 0 && mSwipeDirection == Y)) {
            newPos = -getSize(view);
        } else {
            newPos = getSize(view);
        }
        int duration = MAX_ESCAPE_ANIMATION_DURATION;
        if (velocity != 0) {
            duration = Math.min(duration,
                    (int) (Math.abs(newPos - getTranslation(view)) *
                            1000f / Math.abs(velocity)));
        } else {
            duration = DEFAULT_ESCAPE_ANIMATION_DURATION;
        }

        ValueAnimator anim = createTranslationAnimation(view, newPos);
        anim.setInterpolator(sLinearInterpolator);
        anim.setDuration(duration);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCallback.onChildDismissed(view);
                if (FADE_OUT_DURING_SWIPE && canAnimViewBeDismissed) {
                    view.setAlpha(1.f);
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                mCallback.onChildFling(view);
            }
        });
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (FADE_OUT_DURING_SWIPE && canAnimViewBeDismissed) {
                    view.setAlpha(getAlphaForOffset(view));
                }
            }
        });
        anim.start();
    }

    private void snapChild(final View view, float velocity) {
        final boolean canAnimViewBeDismissed = mCallback.canChildBeDismissed(view);
        ValueAnimator anim = createTranslationAnimation(view, 0);
        int duration = SNAP_ANIM_LEN;
        anim.setDuration(duration);
        anim.setInterpolator(linearOutSlowInInterpolator);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (FADE_OUT_DURING_SWIPE && canAnimViewBeDismissed) {
                    view.setAlpha(getAlphaForOffset(view));
                }
                mCallback.onSwipeChanged(mCurrView, view.getTranslationX());
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (FADE_OUT_DURING_SWIPE && canAnimViewBeDismissed) {
                    view.setAlpha(1.0f);
                }
                mCallback.onSnapBackCompleted(view);
            }
        });
        anim.start();
    }

    public boolean onTouchEvent(MotionEvent ev) {
        Log.e(TAG,"onTouchEvent =:" + mDragging);
        if (!mDragging) {
            if (!onInterceptTouchEvent(ev)) {
                return mCanCurrViewBeDimissed;
            }
        }

        mVelocityTracker.addMovement(ev);
        final int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_MOVE:
                if (mCurrView != null) {
                    float delta = getPos(ev) - mInitialTouchPos;
                    setSwipeAmount(delta);
                    mCallback.onSwipeChanged(mCurrView, delta);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mCurrView != null) {
                    endSwipe(mVelocityTracker);
                }
                break;
        }
        return true;
    }

    private void setSwipeAmount(float amount) {
        // don't let items that can't be dismissed be dragged more than
        // maxScrollDistance
        if (CONSTRAIN_SWIPE
                && (!isValidSwipeDirection(amount) || !mCallback.canChildBeDismissed(mCurrView))) {
            float size = getSize(mCurrView);
            float maxScrollDistance = 0.15f * size;
            if (Math.abs(amount) >= size) {
                amount = amount > 0 ? maxScrollDistance : -maxScrollDistance;
            } else {
                amount = maxScrollDistance * (float) Math.sin((amount/size)*(Math.PI/2));
            }
        }
        setTranslation(mCurrView, amount);
        if (FADE_OUT_DURING_SWIPE && mCanCurrViewBeDimissed) {
            float alpha = getAlphaForOffset(mCurrView);
            mCurrView.setAlpha(alpha);
        }
    }

    private boolean isValidSwipeDirection(float amount) {
        if (mSwipeDirection == X) {
            return (amount <= 0) ? mAllowSwipeTowardsStart : mAllowSwipeTowardsEnd;
        }

        // Vertical swipes are always valid.
        return true;
    }
    public void dismissChildByClick(View view){
        Log.e(TAG,"dismissChildByClick :: view");
        dismissChild(view,500);
    }
    private void endSwipe(VelocityTracker velocityTracker) {
        velocityTracker.computeCurrentVelocity(1000 /* px/sec */);
        float velocity = getVelocity(velocityTracker);
        float perpendicularVelocity = getPerpendicularVelocity(velocityTracker);
        float escapeVelocity = SWIPE_ESCAPE_VELOCITY * mDensityScale;
        float translation = getTranslation(mCurrView);
        // Decide whether to dismiss the current view
        boolean childSwipedFarEnough = DISMISS_IF_SWIPED_FAR_ENOUGH &&
                Math.abs(translation) > 0.6 * getSize(mCurrView);
        boolean childSwipedFastEnough = (Math.abs(velocity) > escapeVelocity) &&
                (Math.abs(velocity) > Math.abs(perpendicularVelocity)) &&
                (velocity > 0) == (translation > 0);

        boolean dismissChild = mCallback.canChildBeDismissed(mCurrView)
                && isValidSwipeDirection(translation)
                && (childSwipedFastEnough || childSwipedFarEnough);

        if (dismissChild) {
            // flingadingy
            dismissChild(mCurrView, childSwipedFastEnough ? velocity : 0f);
        } else {
            // snappity
            mCallback.onDragCancelled(mCurrView);
            snapChild(mCurrView, velocity);
        }
    }
    public interface Callback {
        View getChildAtPosition(MotionEvent ev);// 获取拖动的view

        boolean canChildBeDismissed(View v); // view是否可以通过滑动消逝

        void onBeginDrag(View v);// 开始拖动

        void onSwipeChanged(View v, float delta);// 拖动过程中通知监听者进度

        void onChildDismissed(View v); // view 消逝后调用

        void onSnapBackCompleted(View v); // 如果view没有消逝而是回到初始位置，调用这个

        void onDragCancelled(View v); // 拖动取消

        void onChildFling(View v); // view 在自己滚动时调用
    }
}
