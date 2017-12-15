package com.zibuyuqing.ucbrowser.widget.stackview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.database.Observable;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.OverScroller;
import android.widget.Toast;

import com.zibuyuqing.ucbrowser.R;
import com.zibuyuqing.ucbrowser.utils.ViewUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Xijun.Wang on 2017/12/4.
 */

public class UCStackView extends FrameLayout implements SwipeHelper.Callback {
    private static final String TAG = "UCStackView";
    private static final int INVALID_POINTER = -1;
    public static final int INVALID_POSITION = -1;
    private static final int LAYOUT_ALL = 0;
    private static final int LAYOUT_PRE_ACTIVE = -1;
    public static final int LAYOUT_AFTER_ACTIVE = 1;
    public static final float PROGRESS_STEP = 0.2f;
    public static final float BASE_MAX_SCROLL_P = 0.72f;
    public static final float BASE_MIN_SCROLL_P = 0.34f;
    public static final float PROGRESS_START = 0.8f;
    public static final float DEFAULT_VIEW_MAX_SCALE = 0.9f;
    public static final float DEFAULT_VIEW_MIN_SCALE = 0.7f;
    private StackAdapter mStackAdapter;
    private int mSelectPager = 0;
    private List<ViewHolder> mViewHolders;
    private int mDuration;
    private OverScroller mScroller;
    private VelocityTracker mVelocityTracker;
    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private int mActivePointerId;
    private boolean mScrollEnable;
    private Context mContext;
    private Rect mChildTouchRect[];
    private int mScreenWidth;
    private int mScreenHeight;
    private float mViewMinTop;
    private float mViewMaxTop;
    private float mViewMinScale;
    private float mViewMaxScale;
    private float mViewMinAlpha;
    private float mViewMaxAlpha;
    private float mMinScrollP;
    private float mMaxScrollP;
    private float mMinPositiveScrollP;
    private float mMaxPositiveScrollP;
    private float mScrollProgress;
    private final ViewDataObserver mObserver = new ViewDataObserver();
    private boolean mInterceptedBySwipeHelper;
    private SwipeHelper mSwipeHelper;
    private float mLastMotionY;
    private float mTotalMotionY;
    private float mInitialMotionX;
    private float mLastMotionX;
    private float mInitialMotionY;
    private boolean mIsScrolling;
    private boolean mIsOverScroll = false;
    ObjectAnimator mScrollAnimator;
    private int mActivePager;
    boolean mIsFirstLayout = true;
    private int mLayoutState = LAYOUT_ALL;
    private Interpolator mLinearOutSlowInInterpolator;
    private View mPreviousView;
    private View mTargetView;
    public UCStackView(@NonNull Context context) {
        this(context, null);
    }

    public UCStackView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UCStackView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        mViewHolders = new ArrayList<ViewHolder>();
        mScroller = new OverScroller(mContext);
        mScroller.setFriction(0.02f);
        ViewConfiguration configuration = ViewConfiguration.get(mContext);
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mTouchSlop = configuration.getScaledTouchSlop();
        Resources resources = mContext.getResources();
        mScreenWidth = ViewUtil.getScreenSize(mContext).x;
        mScreenHeight = ViewUtil.getScreenSize(mContext).y;
        mViewMinTop = 0;
        mViewMaxTop = mScreenHeight;
        mViewMinScale = DEFAULT_VIEW_MIN_SCALE;
        mViewMaxScale = DEFAULT_VIEW_MAX_SCALE;
        mDuration = 500;
        float densityScale = resources.getDisplayMetrics().density;
        mSwipeHelper = new SwipeHelper(mContext, SwipeHelper.X, this, densityScale, mTouchSlop);
        mSwipeHelper.setMinAlpha(1f);
        mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(mContext, R.anim.linear_out_show_in);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void measureChildren(int widthMeasureSpec, int heightMeasureSpec) {

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(mIsFirstLayout) {
            calculateInitialScrollP();
            mIsFirstLayout = false;
        }
        layoutChildren();
    }

    private void layoutChildren() {
        int childCount = getChildCount();
        float progress;
        float transY;
        float transZ;
        View child;
        mChildTouchRect = new Rect[childCount];
        for (int i = 0; i < childCount; i++) {

            child = getChildAt(i);
            Rect rect = new Rect();
            child.getHitRect(rect);
            mChildTouchRect[i] = rect;
            switch (mLayoutState){
                case LAYOUT_PRE_ACTIVE:
                    if(i > mActivePager){
                        continue;
                    }
                    break;
                case LAYOUT_AFTER_ACTIVE:
                    if(i < mActivePager){
                        continue;
                    }
            }
            progress = getScrollP();
            transY = calculateProgress2TransY(i,progress);
            transZ = calculateProgress2TransZ(progress);
            Log.e(TAG, "layoutChildren :: progress =:" + progress + ",transY =:" + transY);
            translateViewY(transY, child);
            //translateViewZ(transZ, child);
            scaleView(calculateProgress2Scale(i,progress), child);
        }
        invalidate();
    }
    public void setAdapter(StackAdapter adapter) {
        mStackAdapter = adapter;
        mStackAdapter.registerObserver(mObserver);
        refreshViews();
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

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void refreshViews() {
        removeAllViews();
        mViewHolders.clear();
        int count = mStackAdapter.getItemCount();
        for (int i = 0; i < count; i++) {
            ViewHolder holder = getViewHolder(i);
            holder.position = i;
            addView(holder.itemView);
            mStackAdapter.bindViewHolder(holder, i);
        }
        requestLayout();
    }

    private void translateViewX(float transX, View view) {
        view.setTranslationX(transX);
    }

    private void translateViewY(float transY, View view) {
        view.setTranslationY(transY);
        if(view == getChildAt(mSelectPager - 1)){
            Log.e(TAG,"translateViewY =:" + view.getTranslationY());
        }
    }

    private void translateViewZ(float transZ, View view) {
        view.setTranslationZ(transZ);
    }

    public void selectPager(int key){
        mSelectPager = key;
        animateShow(mSelectPager, mPreviousView, mTargetView, false, new Runnable() {
            @Override
            public void run() {
                mTargetView.setVisibility(GONE);
            }
        });
        Toast.makeText(mContext, "点击第" + mSelectPager +" 项", Toast.LENGTH_SHORT).show();
    }
    public void closePager(int key){
        Toast.makeText(mContext, "关闭了第" + key +" 项", Toast.LENGTH_SHORT).show();
        mSwipeHelper.dismissChildByClick(getChildAt(key - 1));
    }
    private void scaleView(float scale, View view) {
        view.setScaleX(scale);
        view.setScaleY(scale);
    }

    private void alphaView(float alpha, View view) {
        view.setAlpha(alpha);
    }

    /**
     * calculate from the progress along the curve to a screen coordinate.
     */
    int calculateProgress2TransY(int i,float progress) {
        return (int) (mViewMinTop +
                Math.pow(calculateViewProgress(i,progress),4) * (mViewMaxTop - mViewMinTop));
    }

    int calculateProgress2TransZ(float progress) {
        return (int) (mViewMinTop + Math.pow(progress, 3) * (200));
    }

    /**
     * calculate from the progress along the curve to a scale.
     */
    float calculateProgress2Scale(int i,float progress) {
        float scaleRange = (mViewMaxScale - mViewMinScale);
        return mViewMinScale + (calculateViewProgress(i,progress) * scaleRange);
    }

    /**
     * calculate from the progress along the curve to a scale.
     */
    float calculateProgress2Alpha(float progress) {
        if (progress < 0) return mViewMinScale;
        if (progress > 1) return mViewMaxScale;
        float scaleRange = (mViewMaxScale - mViewMinScale);
        float scale = mViewMinScale + (progress * scaleRange);
        return scale;
    }

    /**
     * 计算阻尼
     * @return
     */
    private float calculateDamping(){
        float damping = (1.0f - Math.abs(mScrollProgress - getPositiveScrollP()) * 5);
        Log.e(TAG,"calculateDamping :: damping = :" + damping);
        return damping;
    }
    private float getScrollRate() {
        float topSpace = mViewMaxTop;
        return mTotalMotionY / topSpace;
    }
    public void animateShow(
            int selectPager,
            final View from,
            final View to,
            final boolean show,
            final Runnable onCompletedRunnable)
    {
        int duration = 400;
        int startDelay = 200;
        mSelectPager = selectPager;
        Log.e(TAG,"animateShow :: selectPager =:" + selectPager);
        mPreviousView = from;
        mTargetView = to;
        if(show) {
            calculateInitialScrollP();
            layoutChildren();
        }
        final View selectChild = getChildAt(selectPager - 1);

        float nextChildEndTransY = 0;
        float nextChildStartTransY = 0;
        int endRange = show ? Math.min(selectPager + 1,getChildCount()) : getChildCount();
        for(int i= selectPager ;i < endRange; i++){
            View nextChild = getChildAt(i);
            nextChildStartTransY = show ? ViewUtil.getScreenSize(getContext()).y : nextChild.getTranslationY();
            nextChildEndTransY = show ? nextChild.getTranslationY() : ViewUtil.getScreenSize(getContext()).y;
            ObjectAnimator nextChildAnimator = ObjectAnimator.ofFloat(
                    nextChild,"translationY",nextChildStartTransY,nextChildEndTransY);
            nextChildAnimator.setDuration(duration);
            if(show) {
                nextChildAnimator.setStartDelay(startDelay);
            }
            nextChildAnimator.start();
            Log.e(TAG,"animateShow :: transY = :" + nextChildStartTransY + " ,nextChildEndTransY =:" + nextChildEndTransY);
        }

        float transY = selectChild.getTranslationY();
        float scaleX = selectChild.getScaleX();
        float scaleY = selectChild.getScaleY();
        float startScaleX = show ? 1.0f : scaleX;
        float startScaleY = show ? 1.0f : scaleY;
        float startTransY = show ? 0 : transY;
        float endScaleX = show ? scaleX : 1.0f;
        float endScaleY = show ? scaleY : 1.0f;
        float endTransY = show ? transY : 0;
        /*
        final float dis = Math.abs(endScaleX - startScaleX);
        from.setScaleX(startScaleX);
        from.setScaleY(startScaleY);
        from.setTranslationY(startTransY);
        */
        selectChild.setScaleX(startScaleX);
        selectChild.setScaleY(startScaleY);
        selectChild.setTranslationY(startTransY);
        if(show) {
           to.setAlpha(0);
        }
        PropertyValuesHolder scaleXHolder = PropertyValuesHolder.ofFloat("scaleX",startScaleX,endScaleX);
        PropertyValuesHolder scaleYHolder = PropertyValuesHolder.ofFloat("scaleY",startScaleY,endScaleY);
        PropertyValuesHolder transYHolder = PropertyValuesHolder.ofFloat("translationY",startTransY,endTransY);
        ObjectAnimator showAnimator = ObjectAnimator.ofPropertyValuesHolder(selectChild,scaleXHolder,scaleYHolder,transYHolder);
        showAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                /*
                float value = ((Float) valueAnimator.getAnimatedValue()).floatValue();

                float fromViewAlpha = (1.0f - (1.f - value) / dis * 1.2f);
                Log.e(TAG,"value =:" + fromViewAlpha);
                float toViewAlpha = 1.0f - fromViewAlpha;
                from.setAlpha(fromViewAlpha);
                to.setAlpha(toViewAlpha > 1.0f ? 1.0f : toViewAlpha);
                */
            }
        });
        showAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if(onCompletedRunnable != null){
                    onCompletedRunnable.run();
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                mPreviousView.setVisibility(VISIBLE);
                mTargetView.setVisibility(VISIBLE);
                selectChild.setVisibility(VISIBLE);
                to.setAlpha(1.0f);
            }
        });
        if(show) {
            showAnimator.setStartDelay(startDelay);
        }
        showAnimator.setDuration(duration);
        showAnimator.start();
    }
    private float calculateViewProgress(int index,float progress) {
        return PROGRESS_STEP * index + progress;
    }
    private void updateScrollProgressRange(){
        mMinScrollP = BASE_MIN_SCROLL_P - (getChildCount() - 2) * PROGRESS_STEP;
        mMaxScrollP = BASE_MAX_SCROLL_P;
        mMinPositiveScrollP = mMinScrollP + PROGRESS_STEP * 0.25f;
        mMaxPositiveScrollP = mMaxScrollP - PROGRESS_STEP * 0.75f;
        Log.e(TAG,"updateScrollProgressRange ::mMinScrollP =:" + mMinScrollP +",mMaxScrollP =:" + mMaxScrollP);
    }
    private void calculateInitialScrollP(){
        Log.e(TAG,"calculateInitialScrollP:: ");
        updateScrollProgressRange();
        mScrollProgress = PROGRESS_START - mSelectPager * PROGRESS_STEP;
        mTotalMotionY = mScrollProgress * mViewMaxTop;
    }
    private float calculateProgress2Y(float progress) {
        return progress * mViewMaxTop;
    }


    private ViewHolder getViewHolder(int position) {
        if (position == INVALID_POSITION) {
            return null;
        }
        ViewHolder viewHolder;
        if (mViewHolders.size() <= position ||
                mViewHolders.get(position).itemViewType != mStackAdapter.getItemViewType(position)) {
            viewHolder = mStackAdapter.createView(this, mStackAdapter.getItemViewType(position));
            mViewHolders.add(viewHolder);
        } else {
            viewHolder = mViewHolders.get(position);
        }
        return viewHolder;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (getChildCount() <= 0 || isAnimating()) {
            return false;
        }
        int action = ev.getAction();
        // Pass through to swipe helper if we are swiping
        mInterceptedBySwipeHelper = mSwipeHelper.onInterceptTouchEvent(ev);
        if (mInterceptedBySwipeHelper) {
            return true;
        }
        boolean wasScrolling = mIsScrolling ||
                (mScrollAnimator != null && mScrollAnimator.isRunning());
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                // Save the move down info
                mInitialMotionX = mLastMotionX = (int) ev.getX();
                mInitialMotionY = mLastMotionY = (int) ev.getY();
                mActivePointerId = ev.getPointerId(0);
                // Stop the current doScroll if it is still flinging
                stopScroller();
                // Initialize the velocity tracker
                initOrResetVelocityTracker();
                mVelocityTracker.addMovement(ev);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (mActivePointerId == INVALID_POINTER) break;
                Log.e(TAG, "onInterceptTouchEvent :: ACTION_MOVE = ");
                // Initialize the velocity tracker if necessary
                initVelocityTrackerIfNotExists();
                mVelocityTracker.addMovement(ev);

                int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                if (activePointerIndex < 0) {
                    Log.d(TAG, "findPointerIndex failed");
                    mActivePointerId = INVALID_POINTER;
                    break;
                }
                int y = (int) ev.getY(activePointerIndex);
                int x = (int) ev.getX(activePointerIndex);
                if (Math.abs(y - mInitialMotionY) > mTouchSlop) {
                    // Save the move move info
                    mIsScrolling = true;
                }

                mLastMotionX = x;
                mLastMotionY = y;
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                int pointerIndex = ev.getActionIndex();
                int pointerId = ev.getPointerId(pointerIndex);
                Log.d(TAG, "Ignore multi-move "
                        + pointerIndex + "(" + pointerId + ")");
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                // Animate the doScroll back if we've cancelled
                if (wasScrolling) {
                    scrollToPositivePosition();
                }
                // Reset the drag state and the velocity tracker
                break;
            }
        }
        return wasScrolling || mIsScrolling;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (getChildCount() <= 0 || isAnimating()) {
            return false;
        }
        int action = ev.getAction();
        // Pass through to swipe helper if we are swiping
        if (mInterceptedBySwipeHelper && mSwipeHelper.onTouchEvent(ev)) {
            return true;
        }
        // Update the velocity tracker
        initVelocityTrackerIfNotExists();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                // Save the move down info
                mInitialMotionX = mLastMotionX = (int) ev.getX();
                mInitialMotionY = mLastMotionY = (int) ev.getY();
                mActivePointerId = ev.getPointerId(0);
                // Stop the current doScroll if it is still flinging
                stopScroller();
                // Initialize the velocity tracker
                initOrResetVelocityTracker();
                mVelocityTracker.addMovement(ev);
                // Disallow parents from intercepting move events
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
                Log.e(TAG, "onTouchEvent :: ACTION_MOVE = ");
                mVelocityTracker.addMovement(ev);

                int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                int x = (int) ev.getX(activePointerIndex);
                int y = (int) ev.getY(activePointerIndex);
                int yTotal = Math.abs(y - (int) mInitialMotionY);
                float deltaP = mLastMotionY - y;
                if (!mIsScrolling) {
                    if (yTotal > mTouchSlop) {
                        mIsScrolling = true;
                    }
                }
                if (mIsScrolling) {
                    if (isOverPositiveScrollP()) {
                        mTotalMotionY -= deltaP *(calculateDamping());
                    } else {
                        mTotalMotionY -= deltaP;
                    }
                    doScroll();
                }

                mLastMotionX = x;
                mLastMotionY = y;
                break;
            }
            case MotionEvent.ACTION_UP: {
                mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int velocity = (int) mVelocityTracker.getYVelocity(mActivePointerId);
                if (mIsScrolling && (Math.abs(velocity) > mMinimumVelocity)) {
                    fling(velocity);
                } else {
                    scrollToPositivePosition();
                }
                resetTouchState();
                Log.e(TAG, "onTouchEvent :: mIsOverScroll =:" + mIsOverScroll);
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
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
            case MotionEvent.ACTION_CANCEL: {
                scrollToPositivePosition();
                resetTouchState();
                break;
            }
        }
        return true;
    }

    private boolean computeScrollProgress() {
        if (getChildCount() <= 0) {
            return false;
        }
        mIsOverScroll = false;
        mScrollProgress = getScrollRate();
        mIsOverScroll = (mScrollProgress > mMaxScrollP || mScrollProgress < mMinScrollP);
        return mIsOverScroll;
    }

    public void setScrollP(float progress) {
        Log.e(TAG, "rate =:" + progress);
        mTotalMotionY = calculateProgress2Y(progress);
        mScrollProgress = progress;
        layoutChildren();
    }

    public float getScrollP() {
        return mScrollProgress;
    }

    /**
     * Returns the stack scroll progress
     */
    float getPositiveScrollP() {
        if (mScrollProgress < mMinPositiveScrollP) {
            return mMinPositiveScrollP;
        } else if(mScrollProgress > mMaxPositiveScrollP){
            return mMaxPositiveScrollP;
        }
        return mScrollProgress;
    }
    boolean isOverPositiveScrollP(){
        return (mScrollProgress > mMaxPositiveScrollP || mScrollProgress < mMinPositiveScrollP);
    }
    void stopScroller() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        mIsScrolling = false;
    }

    private boolean isAnimating() {
        return mScrollAnimator != null && mScrollAnimator.isRunning();
    }

    /**
     * Animates the stack scroll
     */
    void animateScroll(float curScroll, float newScroll, final Runnable postRunnable) {
        // Finish any current scrolling animations
        stopScroller();
        mScrollAnimator = ObjectAnimator.ofFloat(this, "scrollP", curScroll, newScroll);
        mScrollAnimator.setDuration(mDuration);
        mScrollAnimator.setInterpolator(mLinearOutSlowInInterpolator);
        mScrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                setScrollP((Float) valueAnimator.getAnimatedValue());
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
        Log.e(TAG, "scrollToPositivePosition mScrollProgress =:" + mScrollProgress);
        float curScroll = getScrollP();
        float positiveScrollP = getPositiveScrollP();
        if(Float.compare(curScroll,positiveScrollP) != 0) {
            animateScroll(curScroll, getPositiveScrollP(), new Runnable() {
                @Override
                public void run() {
                    resetTouchState();
                }
            });
            invalidate();
        }
    }

    private void doScroll() {
        computeScrollProgress();
        layoutChildren();
    }

    private void resetTouchState() {
        mActivePointerId = INVALID_POINTER;
        mIsScrolling = false;
        recycleVelocityTracker();
    }

    @Override
    public void computeScroll() {
        Log.e(TAG, "computeScroll :: mIsOverScroll :" + mIsOverScroll);
        if (mScroller.computeScrollOffset()) {
            Log.e(TAG, "computeScroll :111: mIsOverScroll :" + mIsOverScroll);
            if(mIsOverScroll){
                scrollToPositivePosition();
            } else {
                if(mScroller.isFinished()){
                    scrollToPositivePosition();
                }
                mTotalMotionY = mScroller.getCurrY();
                doScroll();
            }
        }
        super.computeScroll();
    }

    public void fling(int velocity) {
        mScroller.fling(
                0,
                (int) mTotalMotionY,
                0,
                 velocity,
                0,
                0,
                Integer.MIN_VALUE,
                Integer.MAX_VALUE);
        invalidate();
    }

    private View findChildAtPosition(MotionEvent event) {
        int activePointId = event.getPointerId(0);
        if(activePointId == INVALID_POINTER){
            return null;
        }
        int x = (int) event.getX(activePointId);
        int y = (int) event.getY(activePointId);
        int count = getChildCount();
        for (int i = count - 1; i >= 0; i --) {
            if (mChildTouchRect[i].contains(x, y)) {
                mActivePager = i;
                return getChildAt(i);
            }
        }
        return null;
    }

    @Override
    public View getChildAtPosition(MotionEvent ev) {

        return findChildAtPosition(ev);
    }

    @Override
    public boolean canChildBeDismissed(View v) {
        return true;
    }

    @Override
    public void onBeginDrag(View v) {
        Log.e(TAG, "onBeginDrag :: v =:" + v);
    }

    @Override
    public void onSwipeChanged(View v, float delta) {
        float alpha = 1.f - Math.abs(delta) / mScreenWidth * 0.5f;
        alphaView(alpha, v);
        Log.e(TAG, "onSwipeChanged :: delta =:" + delta);
    }

    @Override
    public void onChildDismissed(final View v) {
        final float curProgress = getScrollP();
        float newProgress = curProgress  + PROGRESS_STEP;
        mLayoutState = LAYOUT_PRE_ACTIVE;
        if(newProgress > mMaxPositiveScrollP){
            newProgress = curProgress  - PROGRESS_STEP;
            mLayoutState = LAYOUT_AFTER_ACTIVE;
        } else if(newProgress < mMinPositiveScrollP){
            newProgress = curProgress  + PROGRESS_STEP;
            mLayoutState = LAYOUT_PRE_ACTIVE;
        }

        animateScroll(curProgress, newProgress, new Runnable() {
            @Override
            public void run() {
                removeView(v);
                if(mLayoutState == LAYOUT_AFTER_ACTIVE) {
                    setScrollP(curProgress);
                }
                mLayoutState = LAYOUT_ALL;
                updateScrollProgressRange();
                mActivePager = INVALID_POSITION;
                mStackAdapter.get
            }
        });
    }

    @Override
    public void onSnapBackCompleted(View v) {

    }

    @Override
    public void onDragCancelled(View v) {

    }

    public static abstract class Adapter<VH extends ViewHolder> {
        private final AdapterDataObservable observable = new AdapterDataObservable();

        VH createView(ViewGroup parent, int viewType) {
            VH holder = onCreateView(parent, viewType);
            holder.itemViewType = viewType;
            return holder;
        }

        protected abstract VH onCreateView(ViewGroup parent, int viewType);

        public void bindViewHolder(VH holder, int position) {
            onBindViewHolder(holder, position);
        }

        protected abstract void onBindViewHolder(VH holder, int position);

        public abstract int getItemCount();

        public final void notifyDataSetChanged() {
            observable.notifyDataChanged();
        }

        public int getItemViewType(int position) {
            return 0;
        }

        public void registerObserver(AdapterDataObserver observer) {
            observable.registerObserver(observer);
        }

    }

    public static class AdapterDataObservable extends Observable<AdapterDataObserver> {
        public boolean hasObservers() {
            return !mObservers.isEmpty();
        }

        public void notifyDataChanged() {
            for (AdapterDataObserver observer : mObservers) {
                observer.onChanged();
            }
        }
    }

    public static abstract class AdapterDataObserver {
        public void onChanged() {
        }
    }

    public static abstract class ViewHolder {
        public View itemView;
        public int itemViewType;
        int position;

        public ViewHolder(View view) {
            itemView = view;
        }

        public Context getContext() {
            return itemView.getContext();
        }
    }

    private class ViewDataObserver extends AdapterDataObserver {
        @Override
        public void onChanged() {
            refreshViews();
        }
    }


}
