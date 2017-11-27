package com.zibuyuqing.ucbrowser.widget;

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
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;

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
    private float mInitialY;
    private int mTouchSlop;
    private float mLastMotionY;
    protected float mTotalMotionY;
    private Context mContext;
    private List<ScrollStateListener> mListeners = new ArrayList<>();
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private int mTouchState = TOUCH_STATE_REST;
    private VelocityTracker mVelocityTracker;
    private int mFinalDistance;
    private Handler mHandler;
    public void addScrollStateListener(ScrollStateListener listener){
        mListeners.add(listener);
    }
    public void removeScrollStateListener(ScrollStateListener listener){
        mListeners.remove(listener);
    }
    public UCRootView(@NonNull Context context) {
        this(context,null);
    }

    public UCRootView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
        mContext = context;
        init();
    }
    public void setFinalDistance(int distance){
        Log.e(TAG,"distance =:" + distance);
        mFinalDistance = distance;
    }
    private void acquireVelocityTrackerAndAddMovement(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
    }

    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }
    private void resetTouchState(){
        releaseVelocityTracker();
        mTouchState = TOUCH_STATE_REST;
    }
    private void init(){
        final ViewConfiguration configuration = ViewConfiguration.get(mContext);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == MSG_FLING){
                    mTotalMotionY += FLING_SPEED;
                    onScroll(mTotalMotionY / mFinalDistance);
                    checkPoint();
                }
                super.handleMessage(msg);
            }
        };
    }
    public void onStartScroll(){
        for(ScrollStateListener listener : mListeners){
            listener.onStartScroll();
        }
    }
    public void onScroll(float rate){
        for(ScrollStateListener listener : mListeners){
            listener.onScroll(rate);
        }
    }

    public void onEndScroll(){
        for(ScrollStateListener listener : mListeners){
            listener.onEndScroll();
        }
    }
    public UCRootView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    private void determineScrollingStart(MotionEvent ev, float touchSlopScale) {

        // Disallow scrolling if we started the gesture from outside the viewport
        final float y = ev.getY();
        final int xDiff = (int) Math.abs(y - mLastMotionY);

        final int touchSlop = Math.round(touchSlopScale * mTouchSlop);
        boolean xMoved = xDiff > touchSlop;
        Log.e(TAG,"determineScrollingStart :: touchSlop =:" + touchSlop+",xDiff =:" + xDiff);
        if (xMoved) {
            // Scroll if the user moved far enough along the X axis
            mTouchState = TOUCH_STATE_SCROLLING;
            mLastMotionY = y;
            onStartScroll();
        }
    }
    private void determineScrollingStart(MotionEvent ev) {
        determineScrollingStart(ev, 1.0f);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(getChildCount() < 0){
            Log.e(TAG,"There are no children to scroll");
            return super.onInterceptTouchEvent(ev);
        }
        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_MOVE: {
                determineScrollingStart(ev);
                Log.e(TAG,"onInterceptTouchEvent :: ACTION_MOVE");
                break;
            }
        }
        return mTouchState != TOUCH_STATE_REST;
    }
    private void flingUp(){

    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (getChildCount() <= 0) return super.onTouchEvent(ev);
        acquireVelocityTrackerAndAddMovement(ev);
        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:{
                mInitialY = ev.getY();
                Log.e(TAG,"onTouchEvent :: ACTION_DOWN");
                break;
            }
            case MotionEvent.ACTION_MOVE:{
                if (mTouchState == TOUCH_STATE_SCROLLING) {
                    float y = ev.getY();
                    float deltaY = mLastMotionY - y;
                    mTotalMotionY += deltaY;
                    onScroll(mTotalMotionY / mFinalDistance);
                    mLastMotionY = y;
                } else {
                    determineScrollingStart(ev);
                }
                Log.e(TAG,"onTouchEvent :: ACTION_MOVE mTouchState =:" +mTouchState);
                return mTotalMotionY <= mFinalDistance;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                checkPoint();
                Log.e(TAG,"onTouchEvent :: ACTION_UP");
                break;
        }
        return true;
    }

    private void checkPoint() {
        if(mTotalMotionY < mFinalDistance){
            mHandler.sendEmptyMessage(MSG_FLING);
        }else {
            mHandler.removeMessages(MSG_FLING);
            mTotalMotionY = mFinalDistance;
            onScroll(1.0f);
            resetTouchState();
        }
    }

    public interface ScrollStateListener{
        void onStartScroll();
        void onScroll(float rate);
        void onEndScroll();
    }
}
