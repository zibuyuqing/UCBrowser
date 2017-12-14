package com.zibuyuqing.ucbrowser.widget.root;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.OverScroller;
import android.widget.RelativeLayout;

import com.zibuyuqing.ucbrowser.R;

import java.security.acl.LastOwnerException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xijun.Wang on 2017/11/27.
 */

public class UCRootView extends RelativeLayout {
    private static final String TAG = "UCRootView";
    public static final int INVALID_POINTER = -1;
    private static final int FLING_SPEED = 50;
    private static final int MSG_FLING = 2222;
    public final static int NEWS_MODE = 3;
    public final static int NORMAL_MODE = 4;
    public final static int WEBSITE_MODE = 5;
    public final static int SCROLL_HORIZONTALLY = 5;
    public final static int SCROLL_VERTICALLY = 6;
    public final static int SCROLL_NONE = 0;
    private int mTouchSlop;
    private float mPagingTouchSlop;
    private float mLastMotionY;
    private float mLastMotionX;
    private float mTotalMotionX;
    protected float mTotalMotionY;
    private Context mContext;
    private List<ScrollStateListener> mListeners = new ArrayList<>();
    private boolean mIsScrolling;
    private VelocityTracker mVelocityTracker;
    private int mFinalDistanceY;
    private int mFinalDistanceX;
    private int mCurrentMode = NORMAL_MODE;
    private int mNextMode = NORMAL_MODE;
    private int mDirection;
    private boolean mScrollHEnable = true;
    private boolean mScrollVEnable = true;

    private int mActivePointerId;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private OverScroller mScroller;
    private boolean mIsOverScroll;
    private float mCurrentVelocity;
    private int mDuration;
    ObjectAnimator mScrollAnimator;
    private Interpolator mLinearOutSlowInInterpolator;
    private float mRate;
    private boolean mStartedScroll = false;
    public UCRootView(@NonNull Context context) {
        this(context, null);
    }

    public UCRootView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
        init();
    }
    private void init() {
        final ViewConfiguration configuration = ViewConfiguration.get(mContext);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mScroller = new OverScroller(mContext);
        mDuration = 400;
        mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(mContext, R.anim.linear_out_show_in);
    }
    public void attachScrollStateListener(ScrollStateListener listener) {
        mListeners.add(listener);
    }

    public void removeScrollStateListener(ScrollStateListener listener) {
        mListeners.remove(listener);
    }

    public void setFinalDistance(int xDis, int yDis) {
        if (xDis <= 0) {
            mScrollHEnable = false;
        }
        if (yDis <= 0) {
            mScrollVEnable = false;
        }
        mFinalDistanceX = xDis;
        mFinalDistanceY = yDis;
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }
    private void resetTouchState() {
        mIsScrolling = false;
        mDirection = SCROLL_NONE;
        mCurrentVelocity = 0;
        mStartedScroll = false;
        recycleVelocityTracker();
    }

    private void onStartScroll() {
        mStartedScroll = true;
        for (ScrollStateListener listener : mListeners) {
            listener.onStartScroll(mDirection);
        }
    }

    private void doScroll() {
        onScroll(mRate);
        invalidate();
    }

    private void onScroll(float rate) {
        for (ScrollStateListener listener : mListeners) {
            listener.onScroll(rate);
        }
    }

    private void move(float x, float y) {
        for (ScrollStateListener listener : mListeners) {
            listener.move(x, y);
        }
    }

    private void onEndScroll() {
        mStartedScroll = false;
        for (ScrollStateListener listener : mListeners) {
            listener.onEndScroll();
        }
    }

    public UCRootView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private boolean determineScrollingStart(MotionEvent ev, float touchSlopScale) {
        if (mCurrentMode == NEWS_MODE || mDirection != SCROLL_NONE) {
            return false;
        }
        final float y = ev.getY();
        final float x = ev.getX();
        float deltaY = y - mLastMotionY;
        float deltaX = x - mLastMotionX;
        final int yDiff = (int) Math.abs(deltaY);
        final int xDiff = (int) Math.abs(deltaX);
        final int touchSlop = Math.round(touchSlopScale * mTouchSlop);
        Log.e(TAG, "determineScrollingStart ::onStartScroll  SCROLL_HORIZONTALLY =:" + mDirection+",yDiff =:"+yDiff);
        boolean moved = yDiff > touchSlop;
        if(moved){
            if(mCurrentMode == NORMAL_MODE) {
                mDirection = SCROLL_VERTICALLY;
                mIsScrolling = true;
                onStartScroll();
                verifyMode(false);
                mLastMotionX = x;
                mLastMotionY = y;
                return true;
            }
        }
        moved = xDiff > touchSlop;
        Log.e(TAG, "determineScrollingStart :: SCROLL_HORIZONTALLY =:" + mDirection+",xDiff =:"+xDiff);
        if(moved){
            if(mCurrentMode == NORMAL_MODE){
                if(deltaX > 0){
                    return false;
                }
            }
            mDirection = SCROLL_HORIZONTALLY;
            mIsScrolling = true;
            onStartScroll();
            verifyMode(false);
            mLastMotionX = x;
            mLastMotionY = y;
            return true;
        }
        return false;
    }

    private boolean determineScrollingStart(MotionEvent ev) {
        return determineScrollingStart(ev, 1.0f);
    }
    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }
    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (getChildCount() < 0) {
            Log.e(TAG, "There are no children to scroll");
            return super.onInterceptTouchEvent(ev);
        }
        final int action = ev.getAction();
        boolean wasScrolling = mIsScrolling ||
                (mScrollAnimator != null && mScrollAnimator.isRunning());
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                stopScroller();
                mLastMotionY = ev.getY();
                mLastMotionX = ev.getX();
                mActivePointerId = ev.getPointerId(0);
                initOrResetVelocityTracker();
                mVelocityTracker.addMovement(ev);
                Log.e(TAG, "onInterceptTouchEvent :: ACTION_DOWN mDirection = :" + mDirection);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                initVelocityTrackerIfNotExists();
                mVelocityTracker.addMovement(ev);
                int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                if (activePointerIndex < 0) {
                    Log.d(TAG, "findPointerIndex failed");
                    mActivePointerId = INVALID_POINTER;
                    break;
                }
                determineScrollingStart(ev);
                Log.e(TAG, "onInterceptTouchEvent :: ACTION_MOVE mDirection = :" + mDirection);
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                // Animate the doScroll back if we've cancelled
                if (wasScrolling) {
                    scrollToPositivePosition();
                }
                Log.e(TAG, "onInterceptTouchEvent :: ACTION_UP mDirection = :" + mDirection);
                // Reset the drag state and the velocity tracker
                break;
            }
        }
        Log.e(TAG, "onInterceptTouchEvent :: ACTION_UP wasScrolling = :" + wasScrolling);
        return wasScrolling;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (getChildCount() <= 0) return super.onTouchEvent(ev);
        initVelocityTrackerIfNotExists();
        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                stopScroller();
                Log.e(TAG, "onTouchEvent :: ACTION_DOWN");
                mLastMotionY = ev.getY();
                mLastMotionX = ev.getX();
                mActivePointerId = ev.getPointerId(0);
                initOrResetVelocityTracker();
                mVelocityTracker.addMovement(ev);
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                final int index = ev.getActionIndex();
                mActivePointerId = ev.getPointerId(index);
                mLastMotionX = (int) ev.getX(index);
                mLastMotionY = (int) ev.getY(index);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (mActivePointerId == INVALID_POINTER) break;
                Log.e(TAG, "onTouchEvent :: ACTION_MOVE mIsScrolling =:" +
                        mIsScrolling + ",attachToFinal =:" + attachToFinal() +",mDirection =:" + mDirection);
                mVelocityTracker.addMovement(ev);
                int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                int x = (int) ev.getX(activePointerIndex);
                int y = (int) ev.getY(activePointerIndex);
                move(x, y);
                if (mIsScrolling) {
                    float delta = 0f;
                    if (!attachToFinal()) {
                        switch (mDirection) {
                            case SCROLL_HORIZONTALLY:
                                delta = x - mLastMotionX;
                                mTotalMotionX += delta;
                                mRate = mTotalMotionX / mFinalDistanceX;
                                doScroll();
                                break;
                            case SCROLL_VERTICALLY:
                                delta = y - mLastMotionY;
                                mTotalMotionY += delta;
                                mRate = mTotalMotionY / mFinalDistanceY;
                                doScroll();
                                break;
                        }
                    } else {
                        // TODO: 2017/12/12
                    }
                    mLastMotionY = y;
                    mLastMotionX = x;
                } else {
                    determineScrollingStart(ev);
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_UP:{
                int pointerIndex = ev.getActionIndex();
                int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    // Select a new active pointer id and reset the motion state
                    final int newPointerIndex = (pointerIndex == 0) ? 1 : 0;
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                    mLastMotionX = (int) ev.getX(newPointerIndex);
                    mLastMotionY = (int) ev.getY(newPointerIndex);
                    mVelocityTracker.clear();
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                switch (mDirection) {
                    case SCROLL_HORIZONTALLY:
                        mCurrentVelocity = (int) mVelocityTracker.getXVelocity(mActivePointerId);
                        break;
                    case SCROLL_VERTICALLY:
                        mCurrentVelocity = (int) mVelocityTracker.getYVelocity(mActivePointerId);
                        break;
                }
                verifyMode(true);
                checkPoint();
                Log.e(TAG, "onTouchEvent :: ACTION_UP");
                break;
            }
        }
        return true;
    }
    private boolean isAnimating() {
        return mScrollAnimator != null && mScrollAnimator.isRunning();
    }
    private boolean attachToFinal() {
        if(mDirection == SCROLL_VERTICALLY) {
            if(mNextMode == NEWS_MODE){
                return mRate <= -1.0f;
            }
        } else if(mDirection == SCROLL_HORIZONTALLY){
            if(mNextMode == WEBSITE_MODE){
                return mRate <= -1.0f;
            }
        } else {
            return true;
        }
        return mRate >= 0.f;
    }

    void stopScroller() {
        if (!mScroller.isFinished()) {
            mScroller.forceFinished(true);
            mScroller.abortAnimation();
        }
    }

    void animateScroll(float curScroll, float newScroll, final Runnable postRunnable) {
        // Finish any current scrolling animations
        stopScroller();
        if(mScrollAnimator != null){
            if(mScrollAnimator.isRunning()){
                mScrollAnimator.cancel();
            }
        }
        mScrollAnimator = ObjectAnimator.ofFloat(this, "rate", curScroll, newScroll);
        mScrollAnimator.setDuration(mDuration);
        mScrollAnimator.setInterpolator(mLinearOutSlowInInterpolator);
        mScrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                setRate((Float) valueAnimator.getAnimatedValue());
            }
        });
        mScrollAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (postRunnable != null) {
                    postRunnable.run();
                }
                mScrollAnimator.removeAllListeners();
            }
        });
        mScrollAnimator.start();
    }
    private void scrollToPositivePosition() {
        float curRate = getRate();
        float positiveRate = getPositiveRate();
        Log.e(TAG, "scrollToPositivePosition curRate =:" + curRate +",positiveRate =:" + positiveRate);
        if (Float.compare(curRate, positiveRate) != 0) {
            animateScroll(curRate, positiveRate, new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "checkPoint scrollToPositivePosition curRate finish" +",mDirection =:" + mDirection);
                    checkPoint();
                    // endScroll();
                }
            });
            invalidate();
        }
    }

    @Override
    public void computeScroll() {
        Log.e(TAG, "computeScroll :: mIsOverScroll :" + attachToFinal());
        if(attachToFinal()){
            endScroll();
        }
        if (mScroller.computeScrollOffset()) {
            Log.e(TAG, "computeScroll :111: mIsOverScroll :" + attachToFinal());
            if(!attachToFinal()){
                if(mScroller.isFinished()){
                    scrollToPositivePosition();
                } else {
                    Log.e(TAG, "computeScroll :222 : mIsOverScroll :" + attachToFinal() +",mDirection =:" + mDirection);
                    switch (mDirection) {
                        case SCROLL_HORIZONTALLY:
                            mTotalMotionX =  mScroller.getCurrX();
                            Log.e(TAG, "computeScroll :333 : mTotalMotionX :" + mTotalMotionX +",mDirection =:SCROLL_HORIZONTALLY" +",mScroller.getCurrX() =:" + mScroller.getCurrX());
                            mRate = mTotalMotionX / mFinalDistanceX;
                            doScroll();
                            break;
                        case SCROLL_VERTICALLY:
                            mTotalMotionY = mScroller.getCurrY();
                            mRate = mTotalMotionY / mFinalDistanceY;
                            doScroll();
                            break;
                    }
                }
            }
        }
        super.computeScroll();
    }
    private void verifyMode(boolean fling){
        switch (mDirection){
            case SCROLL_VERTICALLY :
                if(Math.abs(mCurrentVelocity) > mMinimumVelocity && getRate() <= 0){
                    if(mCurrentVelocity < 0){
                        mNextMode = NEWS_MODE;
                        Log.e(TAG,"verifyMode :: 111");
                    } else {
                        mNextMode = NORMAL_MODE;
                        Log.e(TAG,"verifyMode :: 112");
                    }
                } else {
                    if(mCurrentMode == NORMAL_MODE){
                        mNextMode = NEWS_MODE;
                        Log.e(TAG,"verifyMode :: 113");
                    } else {
                        mNextMode = NORMAL_MODE;
                        Log.e(TAG,"verifyMode :: 114");
                    }
                }
                break;
            case SCROLL_HORIZONTALLY:
                if(Math.abs(mCurrentVelocity) > mMinimumVelocity){
                    if(mCurrentVelocity < 0){
                        mNextMode = WEBSITE_MODE;
                    } else {
                        mNextMode = NORMAL_MODE;
                    }
                } else {
                    if(mCurrentMode == NORMAL_MODE){
                        if(fling) {
                            if (Math.abs(mTotalMotionX) > mFinalDistanceX * 0.4f) {
                                mNextMode = WEBSITE_MODE;
                            } else {
                                mNextMode = NORMAL_MODE;
                            }
                        } else {
                            mNextMode = WEBSITE_MODE;
                        }
                    } else {
                        if(fling) {
                            if (Math.abs(mTotalMotionX) > mFinalDistanceX * 0.8f) {
                                mNextMode = WEBSITE_MODE;
                            } else {
                                mNextMode = NORMAL_MODE;
                            }
                        } else {
                            Log.e(TAG,"verifyMode :: mRate =:" + mRate);
                            mNextMode = NORMAL_MODE;
                        }
                    }
                }
                break;
            case SCROLL_NONE:
                break;
        }
        Log.e(TAG,"verifyMode :: mCurrentMode =:" + mCurrentMode +
                ",mNextMode =:" + mNextMode +",mCurrentVelocity =:" + mCurrentVelocity +
                ",mDirection =:" + mDirection +",fling =:" + fling);
    }
    private void endScroll(){
        Log.e(TAG,"endScroll attachToFinal() =:" + attachToFinal()+",mNextMode =:" + mNextMode+",mRate =:" + mRate);
        if(mNextMode != NORMAL_MODE){
            setRate(-1.0f);
        } else {
            setRate(0.0f);
        }
        mCurrentMode = mNextMode;
        onEndScroll();
        resetTouchState();
        stopScroller();
    }
    private void checkPoint() {
        Log.e(TAG,"checkPoint :: attachToFinal :: =:" + attachToFinal() +"." +
                "mode =" + mCurrentMode+",nextMode =:" + mNextMode +",rate =:" + getRate() +",mIsScrolling =:" + mIsScrolling);
        if (!mIsScrolling) {
            return;
        }
        if (!attachToFinal()) {
            onStartScroll();
            scrollToPositivePosition();
        } else {
          endScroll();
        }
    }

    public void back2Normal() {
        Log.e(TAG,"back2Normal :: mCurrentMode =:" + mCurrentMode+",mIsScrolling + ;" + mIsScrolling);
        if(mIsScrolling){
            return;
        }
        mIsScrolling = true;
        mDirection = SCROLL_VERTICALLY;

        if(mCurrentMode == NORMAL_MODE){
            mNextMode = NEWS_MODE;
        } else {
            mNextMode = NORMAL_MODE;
        }
        checkPoint();
    }
    public void back2Home() {
        if(mIsScrolling){
            return;
        }
        mIsScrolling = true;
        mDirection = SCROLL_HORIZONTALLY;
        Log.e(TAG,"back2Home :: mCurrentMode =:" + mCurrentMode);
        if(mCurrentMode == NORMAL_MODE){
            mNextMode = WEBSITE_MODE;
        } else {
            mNextMode = NORMAL_MODE;
        }
        checkPoint();
    }

    public int getMode() {
        return mCurrentMode;
    }

    public void setRate(float rate) {
        if(!mStartedScroll){
            return;
        }
        switch (mDirection) {
            case SCROLL_HORIZONTALLY:
                mTotalMotionX = mFinalDistanceX * rate;
                break;
            case SCROLL_VERTICALLY:
                mTotalMotionY = mFinalDistanceY * rate;
                break;
        }
        Log.e(TAG,"setRate :: mRate =:" + mRate +",mTotalMotionX =:"
                + mTotalMotionX +",mTotalMotionY =:" + mTotalMotionY +",mDirection =:" + mDirection);
        mRate = rate;
        doScroll();
    }
    public float getRate(){
        return mRate;
    }

    public float getPositiveRate() {
        float positiveRate = 0.0f;
        switch (mNextMode){
            case NEWS_MODE :
                positiveRate = -1.0f;
                break;
            case WEBSITE_MODE:
                positiveRate = -1.0f;
                break;
            case NORMAL_MODE:
                positiveRate = 0.0f;
                break;
            default:break;
        }
        return positiveRate;
    }

    public interface ScrollStateListener {
        void onStartScroll(int direction);

        void onScroll(float rate);

        void onEndScroll();

        void move(float x, float y);//手指位置
    }
}
