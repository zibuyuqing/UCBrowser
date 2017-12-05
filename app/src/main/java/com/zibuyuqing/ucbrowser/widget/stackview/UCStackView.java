package com.zibuyuqing.ucbrowser.widget.stackview;

import android.content.Context;
import android.content.res.Resources;
import android.database.Observable;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.OverScroller;

import com.zibuyuqing.ucbrowser.R;
import com.zibuyuqing.ucbrowser.utils.ViewUtil;

import java.security.acl.LastOwnerException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Xijun.Wang on 2017/12/4.
 */

public class UCStackView extends FrameLayout implements SwipeHelper.Callback {
    private static final String TAG = "UCStackView";
    private static final int INVALID_POINTER = -1;
    public static final int INVALID_TYPE = -1;
    public static final int INVALID_POSITION = -1;
    public static final float PROGRESS_STEP = 0.2f;
    public static final float PROGRESS_START = 0.61f;
    public static final float DEFAULT_VIEW_MAX_SCALE = 0.95f;
    public static final float DEFAULT_VIEW_MIN_SCALE = 0.8f;
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
    private int mTopMenuBarHeight;
    private float mViewMinTop;
    private float mViewMaxTop;
    private float mViewMinScale;
    private float mViewMaxScale;
    private float mViewMinAlpha;
    private float mViewMaxAlpha;
    private final ViewDataObserver mObserver = new ViewDataObserver();
    private boolean mInterceptedBySwipeHelper;
    private SwipeHelper mSwipeHelper;
    private float mLastMotionY;
    private float mTotalMotionY;
    private float mInitialMotionX;
    private float mLastMotionX;
    private float mOffsetProgress;
    private int mDirection;
    private float mInitialMotionY;
    private boolean mIsScrolling;
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
        mTopMenuBarHeight = resources.getDimensionPixelSize(R.dimen.dimen_48dp);
        mScreenWidth = ViewUtil.getScreenSize(mContext).x;
        mSreenHeight = ViewUtil.getScreenSize(mContext).y;
        mViewMinTop = 0;
        mViewMaxTop = mSreenHeight;
        mViewMinScale = DEFAULT_VIEW_MIN_SCALE;
        mViewMaxScale = DEFAULT_VIEW_MAX_SCALE;
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
        layoutChildren();
    }

    private void layoutChildren() {
        int childCount = getChildCount();
        float progress;
        float transY;
        View child;
        for (int i = 0; i < childCount; i++) {
            child = getChildAt(i);
            progress = calculateViewProgress(i);
            transY = calculateProgress2TransY(progress);
            Log.e(TAG, "layoutChildren :: progress =:" + progress + ",transY =:" + transY);
            translateViewY(transY, child);
            scaleView(calculateProgress2Scale(progress), child);
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
    int calculateProgress2TransY(float progress) {
        if (progress < 0) return (int) mViewMinTop;
        if (progress > 1) return (int) mViewMaxTop;
        return (int) (mViewMinTop + Math.pow(progress, 3) * (mViewMaxTop - mViewMinTop));
    }

    /**
     * calculate from the progress along the curve to a scale.
     */
    float calculateProgress2Scale(float progress) {
        if (progress < 0) return mViewMinScale;
        if (progress > 1) return mViewMaxScale;
        float scaleRange = (mViewMaxScale - mViewMinScale);
        float scale = mViewMinScale + (progress * scaleRange);
        return scale;
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
    private float getScrollRate(){
        float topSpace = mViewMaxTop - mViewMinTop;
        return mTotalMotionY / topSpace;
    }
    private float calculateViewProgress(int index){
        return (PROGRESS_START + (index - mSelectPosition) * PROGRESS_STEP) - getScrollRate();
    }


    private boolean attachToFinalY(){
        View child;
        switch (mDirection){
            case 1:
                child = getChildAt(getChildCount() -1);
                Log.e(TAG,"attachToFinalY :: 1 child.getTranslationY() =:" +child.getTranslationY());
                return child.getTranslationY() < 100;
            case -1:
                child = getChildAt(0);
                Log.e(TAG,"attachToFinalY :: -1 child.getTranslationY() =:" +child.getTranslationY());
                return child.getTranslationY() > mSreenHeight / 2;
        }
        return true;
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
        if (getChildCount() <= 0) {
            return false;
        }
        int action = ev.getAction();
        // Pass through to swipe helper if we are swiping
        mInterceptedBySwipeHelper = mSwipeHelper.onInterceptTouchEvent(ev);
        if (mInterceptedBySwipeHelper) {
            return true;
        }
        boolean wasScrolling = !mScroller.isFinished();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                // Save the touch down info
                mInitialMotionX = mLastMotionX = (int) ev.getX();
                mInitialMotionY = mLastMotionY = (int) ev.getY();
                mActivePointerId = ev.getPointerId(0);
                // Stop the current scroll if it is still flinging
                mScroller.forceFinished(true);
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
                // Animate the scroll back if we've cancelled
                // mScroller.animateBoundScroll();
                // Reset the drag state and the velocity tracker
                resetTouchState();
                break;
            }
        }
        return wasScrolling || mIsScrolling;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (getChildCount() <= 0) {
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
                // Stop the current scroll if it is still flinging
                mScroller.forceFinished(true);
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
                    scroll();
                    /*
                    float curStackScroll = mScroller.getStackScroll();
                    float overScrollAmount = mScroller.getScrollAmountOutOfBounds(curStackScroll + deltaP);
                    if (Float.compare(overScrollAmount, 0f) != 0) {
                        // Bound the overscroll to a fixed amount, and inversely scale the y-movement
                        // relative to how close we are to the max overscroll
                        float maxOverScroll = mConfig.taskStackOverscrollPct;
                        deltaP *= (1f - (Math.min(maxOverScroll, overScrollAmount)
                                / maxOverScroll));
                    }
                     mScroller.setStackScroll(curStackScroll + deltaP);
                    */
                    mTotalMotionY += deltaP;
                    if(deltaP > 0){
                        mDirection = 1;
                    }else if(deltaP < 0){
                        mDirection = -1;
                    }
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
                }
                resetTouchState();
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
                /*
                if (mScroller.isScrollOutOfBounds()) {
                    // Animate the scroll back into bounds
                    mScroller.animateBoundScroll();
                }
                */
                resetTouchState();
                break;
            }
        }
        return true;
    }


    private void scroll() {
        layoutChildren();
    }
    private void resetTouchState(){
        mActivePointerId = INVALID_POINTER;
        mIsScrolling = false;
        // mTotalMotionY = 0;
        recycleVelocityTracker();
    }
    private void scrollToPositivePosition(){

    }
    @Override
    public void computeScroll() {
        Log.e(TAG,"computeScroll :: attachToFinalY :" + attachToFinalY());
        if(mScroller.computeScrollOffset()) {
            if (attachToFinalY()) {
                int currentVelocity = (int) (mDirection * mScroller.getCurrVelocity() / 5);
                mScroller.forceFinished(true);
                Log.e(TAG, "computeScroll :: currentVelocity =:" + - currentVelocity + ",mTotalMotionY =:" + mTotalMotionY);
                mScroller.fling(0, (int) mTotalMotionY, 0, - currentVelocity, 0,
                        0, Integer.MIN_VALUE, Integer.MAX_VALUE);
                mDirection *= -1;
                invalidate();
            } else {
                mTotalMotionY = mScroller.getCurrY();
                Log.e(TAG, "computeScroll :: getScrollY() =:" + mScroller.getCurrY() + ",mTotalMotionY =:" + mTotalMotionY);
                scroll();
            }
        }
        super.computeScroll();
    }

    public void fling(int velocity) {
        mScroller.fling(0, (int) mTotalMotionY, 0, - velocity, 0,
                0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        Log.e(TAG, "fling :: getScrollY() =:" + getScrollY()+"mViewMaxTop + mTotalMotionY ==:" +(mViewMaxTop - mTotalMotionY));
        invalidate();
    }

    @Override
    public View getChildAtPosition(MotionEvent ev) {
        return null;
    }

    @Override
    public boolean canChildBeDismissed(View v) {
        return false;
    }

    @Override
    public void onBeginDrag(View v) {

    }

    @Override
    public void onSwipeChanged(View v, float delta) {

    }

    @Override
    public void onChildDismissed(View v) {

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
