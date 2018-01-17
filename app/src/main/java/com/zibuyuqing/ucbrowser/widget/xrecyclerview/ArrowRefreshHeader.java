package com.zibuyuqing.ucbrowser.widget.xrecyclerview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zibuyuqing.ucbrowser.R;

import java.util.Date;

/**
 * Created by Xijun.Wang on 2017/8/1.
 */

public class ArrowRefreshHeader extends LinearLayout {
    private final static int STATE_NORMAL = 0;
    private final static int STATE_RELEASE_TO_REFRESH = 1;
    public final static int STATE_REFRESHING = 2;
    private final static int STATE_DONE = 3;
    private static final long ROTATE_ANIM_DURATION = 300L;
    private LinearLayout mContains;
    private ImageView mArrowView;
    private ProgressBar mProgressBar;
    private TextView mStatusTextView;
    private TextView mLastRefreshTimeView;
    private Animation mRotateUpAnim ,mRotateDownAnim;
    private Context mContext;
    private int mMeasuredHeight;
    private int mState;
    public ArrowRefreshHeader(Context context) {
        this(context,null);
    }

    public ArrowRefreshHeader(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ArrowRefreshHeader(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        intiViews();
    }

    private void intiViews() {
        mContains = (LinearLayout) LayoutInflater.from(mContext).
                inflate(R.layout.layout_arrow_refresh_header,null);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.setMargins(0,0,0,0);
        setLayoutParams(lp);
        setPadding(0,0,0,0);
        addView(mContains, new LayoutParams(LayoutParams.MATCH_PARENT, 0));
        setGravity(Gravity.BOTTOM);
        mArrowView = (ImageView) mContains.findViewById(R.id.header_arrow);
        mProgressBar = (ProgressBar) mContains.findViewById(R.id.header_progressbar);
        mStatusTextView = (TextView) mContains.findViewById(R.id.refresh_status_textview);
        mLastRefreshTimeView = (TextView) mContains.findViewById(R.id.last_refresh_time);
        mRotateUpAnim = new RotateAnimation(0.0f, -180.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateUpAnim.setFillAfter(true);
        mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateDownAnim.setFillAfter(true);
        measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mMeasuredHeight = getMeasuredHeight();
    }
    public void setState(int state){
        if(mState == state){
            return;
        }
        switch (state){
            case STATE_DONE:
                mStatusTextView.setText(R.string.refresh_done);
                mArrowView.setVisibility(INVISIBLE);
                mProgressBar.setVisibility(INVISIBLE);
                break;
            case STATE_NORMAL:
                mArrowView.setVisibility(VISIBLE);
                mProgressBar.setVisibility(INVISIBLE);
                if(mState == STATE_RELEASE_TO_REFRESH){
                    mArrowView.startAnimation(mRotateDownAnim);
                }
                if(mState == STATE_REFRESHING){
                    mArrowView.clearAnimation();
                }
                mStatusTextView.setText(R.string.listview_header_hint_normal);
                break;
            case STATE_REFRESHING:
                mArrowView.clearAnimation();
                mArrowView.setVisibility(INVISIBLE);
                mProgressBar.setVisibility(VISIBLE);
                mStatusTextView.setText(R.string.refreshing);
                break;
            case STATE_RELEASE_TO_REFRESH:
                mArrowView.setVisibility(VISIBLE);
                mProgressBar.setVisibility(INVISIBLE);
                if(mState != STATE_RELEASE_TO_REFRESH) {
                    mArrowView.clearAnimation();
                    mArrowView.setAnimation(mRotateUpAnim);
                    mStatusTextView.setText(R.string.listview_header_hint_release);
                }
                break;
        }
        mState = state;
    }
    public int getState(){
        return mState;
    }
    public void setVisibleHeight(int height){
        LayoutParams lp = (LayoutParams) mContains.getLayoutParams();
        lp.height = height < 0 ? 0 : height;
        mContains.setLayoutParams(lp);
    }
    public int getVisibleHeight(){
        return mContains.getLayoutParams() == null ?
                0 : mContains.getLayoutParams().height;
    }
    public void onDrag(float delta){
        if(getVisibleHeight() > 0 || delta > 0) {
            setVisibleHeight((int) delta + getVisibleHeight());
            if (mState <= STATE_RELEASE_TO_REFRESH) {
                if (getVisibleHeight() > mMeasuredHeight) {
                    setState(STATE_RELEASE_TO_REFRESH);
                }else {
                    setState(STATE_NORMAL);
                }
            }
        }
    }
    public boolean onRelease(){
        boolean isOnRefresh = false;
        int height = getVisibleHeight();
        if (height == 0) // not visible.
            isOnRefresh = false;

        if(getVisibleHeight() > mMeasuredHeight &&  mState < STATE_REFRESHING){
            setState(STATE_REFRESHING);
            isOnRefresh = true;
        }
        // refreshing and header isn't shown fully. do nothing.
        if (mState == STATE_REFRESHING && height <=  mMeasuredHeight) {
            //return;
        }
        int destHeight = 0; // default: scroll back to dismiss header.
        // is refreshing, just scroll back to show all the header.
        if (mState == STATE_REFRESHING) {
            destHeight = mMeasuredHeight;
        }
        smoothScrollTo(destHeight);

        return isOnRefresh;
    }
    public void onComplete(){
        mLastRefreshTimeView.setText(friendlyTime(new Date()));
        setState(STATE_DONE);
        new Handler().postDelayed(new Runnable(){
            public void run() {
                reset();
            }
        }, 200);
    }
    public void reset() {
        smoothScrollTo(0);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                setState(STATE_NORMAL);
            }
        }, 500);
    }
    private void smoothScrollTo(int destHeight) {
        ValueAnimator animator = ValueAnimator.ofInt(getVisibleHeight(), destHeight);
        animator.setDuration(300).start();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                setVisibleHeight((int) animation.getAnimatedValue());
            }
        });
        animator.start();
    }
    public static String friendlyTime(Date time) {
        //获取time距离当前的秒数
        int ct = (int)((System.currentTimeMillis() - time.getTime())/1000);

        if(ct == 0) {
            return "刚刚";
        }

        if(ct > 0 && ct < 60) {
            return ct + "秒前";
        }

        if(ct >= 60 && ct < 3600) {
            return Math.max(ct / 60,1) + "分钟前";
        }
        if(ct >= 3600 && ct < 86400)
            return ct / 3600 + "小时前";
        if(ct >= 86400 && ct < 2592000){ //86400 * 30
            int day = ct / 86400 ;
            return day + "天前";
        }
        if(ct >= 2592000 && ct < 31104000) { //86400 * 30
            return ct / 2592000 + "月前";
        }
        return ct / 31104000 + "年前";
    }
}
