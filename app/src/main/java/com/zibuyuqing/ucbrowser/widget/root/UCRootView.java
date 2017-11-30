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
import android.widget.FrameLayout;
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
    private int mTouchSlop;
    private float mLastMotionY;
    private float mLastMotionX;
    protected float mTotalMotionY;
    private Context mContext;
    private List<ScrollStateListener> mListeners = new ArrayList<>();
    private int mTouchState = TOUCH_STATE_REST;
    private VelocityTracker mVelocityTracker;
    private int mFinalDistance;
    private Handler mHandler;
    private int mMode = NORMAL_MODE;
    public void attachScrollStateListener(ScrollStateListener listener){
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

    private void resetTouchState(){
        mTouchState = TOUCH_STATE_REST;
    }
    private void init(){
        final ViewConfiguration configuration = ViewConfiguration.get(mContext);
        mTouchSlop = configuration.getScaledTouchSlop();
        mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == MSG_FLING){
                    int speed = mMode == NORMAL_MODE ? -FLING_SPEED : FLING_SPEED;
                    mTotalMotionY += speed;
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
    public void onTouch(float x,float y){
        for(ScrollStateListener listener : mListeners){
            listener.onTouch(x,y);
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
    private boolean determineScrollingStart(MotionEvent ev, float touchSlopScale) {

        // Disallow scrolling if we started the gesture from outside the viewport
        boolean scroll = false;
        final float y = ev.getY();
        final float x = ev.getX();
        float deltaY = y - mLastMotionY;

        final int yDiff = (int) Math.abs(deltaY);

        final int touchSlop = Math.round(touchSlopScale * mTouchSlop);
        boolean yMoved = yDiff > touchSlop;
        Log.e(TAG,"determineScrollingStart :: touchSlop =:" + touchSlop+",xDiff =:" + yDiff);
        if (yMoved) {
            // Scroll if the user moved far enough along the X axis
            if(mMode == NEWS_MODE){
                return false;
            }
            mTouchState = TOUCH_STATE_SCROLLING;
            onStartScroll();
            scroll = true;
        }
        return scroll;
    }
    private boolean determineScrollingStart(MotionEvent ev) {
        return determineScrollingStart(ev, 1.0f);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(getChildCount() < 0){
            Log.e(TAG,"There are no children to scroll");
            return super.onInterceptTouchEvent(ev);
        }
        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:
                mLastMotionY = ev.getY();
                mLastMotionX = ev.getX();
            case MotionEvent.ACTION_MOVE: {
                determineScrollingStart(ev);
                Log.e(TAG,"onInterceptTouchEvent :: ACTION_MOVE");
                break;
            }
        }
        return mTouchState != TOUCH_STATE_REST;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (getChildCount() <= 0) return super.onTouchEvent(ev);
        acquireVelocityTrackerAndAddMovement(ev);
        final int action = ev.getAction();
        float y = ev.getY();
        float x = ev.getX();
        onTouch(x,y);
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:{
                Log.e(TAG,"onTouchEvent :: ACTION_DOWN");
                break;
            }
            case MotionEvent.ACTION_MOVE:{
                if (mTouchState == TOUCH_STATE_SCROLLING) {
                    float deltaY = y - mLastMotionY;
                    float deltaX = x - mLastMotionX;
                    mTotalMotionY += deltaY;
                    if(Math.abs(deltaY) >= 1.0f) {
                        float rate = mTotalMotionY / mFinalDistance;
                        onScroll(rate);
                    }
                } else {
                    determineScrollingStart(ev);
                }
                Log.e(TAG,"onTouchEvent :: ACTION_MOVE mTouchState =:" +mTouchState);
                mLastMotionY = y;
                mLastMotionX = x;
                return attachToFinal();
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                checkPoint();
                Log.e(TAG,"onTouchEvent :: ACTION_UP");
                break;
        }
        return true;
    }
    private boolean attachToFinal(){
        if(mMode == NEWS_MODE){
            return mTotalMotionY >= 0;
        }
        return -mTotalMotionY >= mFinalDistance;
    }
    private void checkPoint() {
        if(mTouchState == TOUCH_STATE_REST){
            return;
        }
        if(!attachToFinal()){
            mHandler.sendEmptyMessage(MSG_FLING);
        } else {
            mHandler.removeMessages(MSG_FLING);
            if(mMode == NORMAL_MODE) {
                mTotalMotionY = -mFinalDistance;
                onScroll(-1.0f);
                mMode = NEWS_MODE;
            } else {
                mTotalMotionY = 0;
                onScroll(0.0f);
                mMode = NORMAL_MODE;
            }
            onEndScroll();
            resetTouchState();
        }
    }
    public void back2Normal(){
        mTouchState = TOUCH_STATE_SCROLLING;
        checkPoint();
    }
    public int getMode(){
        return mMode;
    }
    public interface ScrollStateListener{
        void onStartScroll();
        void onScroll(float rate);
        void onEndScroll();
        void onTouch(float x,float y);
    }
}
