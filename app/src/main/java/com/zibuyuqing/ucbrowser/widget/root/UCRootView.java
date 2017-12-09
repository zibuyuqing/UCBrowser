package com.zibuyuqing.ucbrowser.widget.root;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xijun.Wang on 2017/11/27.
 */

public class UCRootView extends RelativeLayout {
    private static final String TAG = "UCRootView";
    private static final int FLING_SPEED = 50;
    private static final int MSG_FLING = 2222;
    private final static int TOUCH_STATE_REST = 0;
    private final static int TOUCH_STATE_SCROLLING = 1;
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
    private int mTouchState = TOUCH_STATE_REST;
    private VelocityTracker mVelocityTracker;
    private int mFinalDistanceY;
    private int mFinalDistanceX;
    private Handler mHandler;
    private int mMode = NORMAL_MODE;
    private int mDirection;
    private boolean mScrollHEnable = true;
    private boolean mScrollVEnable = true;

    public void attachScrollStateListener(ScrollStateListener listener) {
        mListeners.add(listener);
    }

    public void removeScrollStateListener(ScrollStateListener listener) {
        mListeners.remove(listener);
    }

    public UCRootView(@NonNull Context context) {
        this(context, null);
    }

    public UCRootView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
        init();
    }

    public void setFinalDistance(int xDis, int yDis) {
        Log.e(TAG, "setFinalDistance :: xDis =:" + xDis + ",yDis =:" + yDis);
        if (xDis <= 0) {
            mScrollHEnable = false;
        }
        if (yDis <= 0) {
            mScrollVEnable = false;
        }
        mFinalDistanceX = xDis;
        mFinalDistanceY = yDis;
    }

    private void acquireVelocityTrackerAndAddMovement(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
    }

    private void resetTouchState() {
        mTouchState = TOUCH_STATE_REST;
        mDirection = SCROLL_NONE;
    }

    private void init() {
        final ViewConfiguration configuration = ViewConfiguration.get(mContext);
        mTouchSlop = configuration.getScaledTouchSlop();
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_FLING) {
                    int speed = mMode == NORMAL_MODE ? -FLING_SPEED : FLING_SPEED;
                    doScroll(speed);
                    checkPoint();
                }
                super.handleMessage(msg);
            }
        };
    }

    private void onStartScroll() {
        for (ScrollStateListener listener : mListeners) {
            listener.onStartScroll(mDirection);
        }
    }

    private void doScroll(float delta) {
        Log.e(TAG,"doScroll :: mDirection =:" + mDirection +",delta =:" + delta);
        float rate = 0.0f;
        switch (mDirection) {
            case SCROLL_HORIZONTALLY:
                if(mScrollHEnable){
                    mTotalMotionX += delta;
                    rate = mTotalMotionX / mFinalDistanceX;
                }
                break;
            case SCROLL_VERTICALLY:
                if(mScrollVEnable){
                    mTotalMotionY += delta;
                    rate = mTotalMotionY / mFinalDistanceY;
                }
                break;
        }
        onScroll(rate);
    }

    private void onScroll(float rate) {
        for (ScrollStateListener listener : mListeners) {
            listener.onScroll(rate);
        }
    }

    private void onTouch(float x, float y) {
        for (ScrollStateListener listener : mListeners) {
            listener.onTouch(x, y);
        }
    }

    private void onEndScroll() {
        for (ScrollStateListener listener : mListeners) {
            listener.onEndScroll();
        }
    }

    public UCRootView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private boolean determineScrollingStart(MotionEvent ev, float touchSlopScale) {
        if (mMode == NEWS_MODE || mDirection != SCROLL_NONE) {
            return false;
        }
        final float y = ev.getY();
        final float x = ev.getX();
        float deltaY = y - mLastMotionY;
        float deltaX = x - mLastMotionX;
        final int yDiff = (int) Math.abs(deltaY);
        final int xDiff = (int) Math.abs(deltaX);
        final int touchSlop = Math.round(touchSlopScale * mTouchSlop);
        Log.i(TAG, "determineScrollingStart ::onStartScroll  SCROLL_HORIZONTALLY =:" + mDirection+",yDiff =:"+yDiff);
        boolean moved = yDiff > touchSlop;
        if(moved){
            if(mMode != WEBSITE_MODE) {
                mDirection = SCROLL_VERTICALLY;
                mTouchState = TOUCH_STATE_SCROLLING;
                Log.i(TAG, "determineScrollingStart :: SCROLL_VERTICALLY =:" + mDirection);
                onStartScroll();
                return true;
            }
        }
        moved = xDiff > touchSlop;
        Log.i(TAG, "determineScrollingStart :: SCROLL_HORIZONTALLY =:" + mDirection+",xDiff =:"+xDiff);
        if(moved){
            if(mMode == NORMAL_MODE){
                if(deltaX > 0){
                    return false;
                }
            }
            mDirection = SCROLL_HORIZONTALLY;
            mTouchState = TOUCH_STATE_SCROLLING;
            Log.i(TAG, "determineScrollingStart ::onStartScroll  SCROLL_HORIZONTALLY =:" + mDirection+",xDiff =:"+xDiff);
            onStartScroll();
            return true;
        }
        return false;
    }

    private boolean determineScrollingStart(MotionEvent ev) {
        return determineScrollingStart(ev, 1.0f);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (getChildCount() < 0) {
            Log.e(TAG, "There are no children to scroll");
            return super.onInterceptTouchEvent(ev);
        }
        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionY = ev.getY();
                mLastMotionX = ev.getX();
            case MotionEvent.ACTION_MOVE: {
                determineScrollingStart(ev);
                Log.i(TAG, "onInterceptTouchEvent :: ACTION_MOVE mDirection = :" + mDirection);
                break;
            }
        }
        return mTouchState != TOUCH_STATE_REST;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (getChildCount() <= 0) return super.onTouchEvent(ev);
        acquireVelocityTrackerAndAddMovement(ev);
        boolean attachToFinal = attachToFinal();
        final int action = ev.getAction();
        float y = ev.getY();
        float x = ev.getX();
        onTouch(x, y);
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                Log.e(TAG, "onTouchEvent :: ACTION_DOWN");
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                Log.e(TAG, "onTouchEvent :: ACTION_MOVE mDirection =:" + mDirection);
                if (mTouchState == TOUCH_STATE_SCROLLING) {
                    float delta = 0f;
                    switch (mDirection){
                        case SCROLL_HORIZONTALLY :
                            delta = x - mLastMotionX;
                            break;
                        case SCROLL_VERTICALLY:
                            delta = y - mLastMotionY;
                            break;
                    }
                    if (Math.abs(delta) >= 1.0f) {
                        if (!attachToFinal) {
                            doScroll(delta);
                        } else {
                            doScroll(0);
                        }
                    }
                } else {
                    determineScrollingStart(ev);
                }
                Log.e(TAG, "onTouchEvent :: ACTION_MOVE mTouchState =:" + mTouchState + "attachToFinal =:" + attachToFinal());
                mLastMotionY = y;
                mLastMotionX = x;
                //
                return attachToFinal;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                checkPoint();
                Log.i(TAG, "onTouchEvent :: ACTION_UP");
                break;
        }
        return true;
    }

    private boolean attachToFinal() {
        if(mDirection == SCROLL_VERTICALLY) {
            if (mMode == NEWS_MODE) {
                return mTotalMotionY >= 0;
            }
            return - mTotalMotionY >= mFinalDistanceY;
        } else if(mDirection == SCROLL_HORIZONTALLY){
            if(mMode == WEBSITE_MODE){
                return mTotalMotionX >= 0;
            }
            return - mTotalMotionX >= mFinalDistanceX;
        }
        return false;
    }

    private void checkPoint() {
        if (mTouchState == TOUCH_STATE_REST) {
            return;
        }
        Log.e(TAG,"checkPoint :: attachToFinal :: =:" + attachToFinal());
        if (!attachToFinal()) {
            mHandler.sendEmptyMessage(MSG_FLING);
            onStartScroll();
        } else {
            mHandler.removeMessages(MSG_FLING);
            if(mDirection == SCROLL_VERTICALLY) {
                if (mMode == NORMAL_MODE) {
                    mTotalMotionY = -mFinalDistanceY;
                    onScroll(-1.0f);
                    mMode = NEWS_MODE;
                } else {
                    mTotalMotionY = 0;
                    onScroll(0.0f);
                    mMode = NORMAL_MODE;
                }
            } else if(mDirection == SCROLL_HORIZONTALLY){
                if(mMode == NORMAL_MODE){
                    mTotalMotionX = -mFinalDistanceX;
                    onScroll(-1.0f);
                    mMode = WEBSITE_MODE;
                } else {
                    mTotalMotionX = 0;
                    onScroll(0.0f);
                    mMode = NORMAL_MODE;
                }
            }
            onEndScroll();
            resetTouchState();
        }
    }

    public void back2Normal() {
        mTouchState = TOUCH_STATE_SCROLLING;
        mDirection = SCROLL_VERTICALLY;
        checkPoint();
    }
    public void back2Home() {
        mTouchState = TOUCH_STATE_SCROLLING;
        mDirection = SCROLL_HORIZONTALLY;
        checkPoint();
    }

    public int getMode() {
        return mMode;
    }

    public interface ScrollStateListener {
        void onStartScroll(int direction);

        void onScroll(float rate);

        void onEndScroll();

        void onTouch(float x, float y);//手指位置
    }
}
