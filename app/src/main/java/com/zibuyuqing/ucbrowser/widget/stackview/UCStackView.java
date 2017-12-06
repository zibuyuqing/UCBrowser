package com.zibuyuqing.ucbrowser.widget.stackview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.database.Observable;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.OverScroller;

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
    public static final float PROGRESS_STEP = 0.2f;
    public static final float BASE_MAX_SCROLL_P = 0.72f;
    public static final float BASE_MIN_SCROLL_P = 0.34f;
    public static final float PROGRESS_START = 0.6f;
    public static final float DEFAULT_VIEW_MAX_SCALE = 0.9f;
    public static final float DEFAULT_VIEW_MIN_SCALE = 0.7f;
    private StackAdapter mStackAdapter;
    private int mSelectPosition = 4;
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
    private int mSreenHeight;
    private float mViewMinTop;
    private float mViewMaxTop;
    private float mViewMinScale;
    private float mViewMaxScale;
    private float mViewMinAlpha;
    private float mViewMaxAlpha;
    private float mMinScrollP;
    private float mMaxScrollP;
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
    boolean mRequireLayoutAllChildren = true;

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
        ViewConfiguration configuration = ViewConfiguration.get(mContext);
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mTouchSlop = configuration.getScaledTouchSlop();
        Resources resources = mContext.getResources();
        mScreenWidth = ViewUtil.getScreenSize(mContext).x;
        mSreenHeight = ViewUtil.getScreenSize(mContext).y;
        mViewMinTop = 0;
        mViewMaxTop = mSreenHeight;
        mViewMinScale = DEFAULT_VIEW_MIN_SCALE;
        mViewMaxScale = DEFAULT_VIEW_MAX_SCALE;
        mDuration = 200;
        float densityScale = resources.getDisplayMetrics().density;
        mSwipeHelper = new SwipeHelper(mContext, SwipeHelper.X, this, densityScale, mTouchSlop);
        mSwipeHelper.setMinAlpha(1f);
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

            if(!mRequireLayoutAllChildren){
                if(i >= mActivePager){
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
    }

    private void translateViewZ(float transZ, View view) {
        view.setTranslationZ(transZ);
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

    private float getScrollRate() {
        float topSpace = mViewMaxTop;
        return mTotalMotionY / topSpace;
    }

    private float calculateViewProgress(int index,float progress) {
        return PROGRESS_STEP * index + progress;
    }
    private void updateScrollProgressRange(){
        mMinScrollP = BASE_MIN_SCROLL_P - (getChildCount() - 2) * PROGRESS_STEP;
        mMaxScrollP = BASE_MAX_SCROLL_P;
    }
    private void calculateInitialScrollP(){
        updateScrollProgressRange();
        mScrollProgress = PROGRESS_START - mSelectPosition * PROGRESS_STEP;
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
                // Save the touch down info
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
                /// M: [ALPS01903572] handle multi-touch exception @{
                if (activePointerIndex < 0) {
                    Log.d(TAG, "findPointerIndex failed");
                    mActivePointerId = INVALID_POINTER;
                    break;
                }
                /// M: [ALPS01903572] handle multi-touch exception @}
                int y = (int) ev.getY(activePointerIndex);
                int x = (int) ev.getX(activePointerIndex);
                if (Math.abs(y - mInitialMotionY) > mTouchSlop) {
                    // Save the touch move info
                    mIsScrolling = true;
                }

                mLastMotionX = x;
                mLastMotionY = y;
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                int pointerIndex = ev.getActionIndex();
                int pointerId = ev.getPointerId(pointerIndex);
                Log.d(TAG, "Ignore multi-touch "
                        + pointerIndex + "(" + pointerId + ")");
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                // Animate the doScroll back if we've cancelled
                if (wasScrolling && mIsOverScroll) {
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
                // Save the touch down info
                mInitialMotionX = mLastMotionX = (int) ev.getX();
                mInitialMotionY = mLastMotionY = (int) ev.getY();
                mActivePointerId = ev.getPointerId(0);
                // Stop the current doScroll if it is still flinging
                stopScroller();
                // Initialize the velocity tracker
                initOrResetVelocityTracker();
                mVelocityTracker.addMovement(ev);
                // Disallow parents from intercepting touch events
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
                    if (mIsOverScroll) {
                        mTotalMotionY -= deltaP * 0.3;
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
                    if (mIsOverScroll) {
                        scrollToPositivePosition();
                    }
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
                if (computeScrollProgress()) {
                    scrollToPositivePosition();
                }
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
     * Returns the bounded stack scroll
     */
    float getPositiveScrollP() {
        if (mScrollProgress < mMinScrollP) {
            return mMinScrollP;
        } else if(mScrollProgress > mMaxScrollP){
            return mMaxScrollP;
        }
        return mScrollProgress;
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
        mScrollAnimator.setInterpolator(new LinearOutSlowInInterpolator());
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
        animateScroll(curScroll, getPositiveScrollP(), new Runnable() {
            @Override
            public void run() {
                resetTouchState();
            }
        });
        invalidate();
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
            if (mIsOverScroll) {
                scrollToPositivePosition();
            } else {
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
        float newProgress = mActivePager == 0 ? curProgress  - PROGRESS_STEP:curProgress  + PROGRESS_STEP;
        mRequireLayoutAllChildren = mActivePager == 0 ? true : false;
        Log.e(TAG,"(mActivePager - getChildCount() + 2) * PROGRESS_STEP =:" +(mActivePager - getChildCount() + 2) * PROGRESS_STEP);
        animateScroll(curProgress, newProgress, new Runnable() {
            @Override
            public void run() {
                removeView(v);
                if(mRequireLayoutAllChildren) {
                    setScrollP(curProgress);
                }
                mRequireLayoutAllChildren = true;
                updateScrollProgressRange();
                mSelectPosition --;
                mActivePager = INVALID_POSITION;
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
