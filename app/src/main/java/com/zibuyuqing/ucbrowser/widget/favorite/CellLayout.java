package com.zibuyuqing.ucbrowser.widget.favorite;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.zibuyuqing.ucbrowser.R;
import com.zibuyuqing.ucbrowser.model.bean.favorite.FavoriteShortcutInfo;
import com.zibuyuqing.ucbrowser.model.bean.favorite.ItemInfo;

import java.util.ArrayList;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Xijun.Wang on 2017/12/19.
 */

public class CellLayout extends ViewGroup {
    private static final String TAG ="CellLayout";
    private static final int START_VIEW_REORDER_DELAY = 30;
    private static final int REORDER_ANIMATION_DURATION = 230;
    private static final float VIEW_REORDER_DELAY_FACTOR = 0.9f;
    private int mCellWidth;

    public int getCellWidth() {
        return mCellWidth;
    }

    public int getCellHeight() {
        return mCellHeight;
    }

    private int mCellHeight;
    private int mGridCountX;
    private int mGridCountY;
    private final int[] mDragCell = new int[2];
    private boolean mDragging = false;
    private Context mContext;
    private final int[] mTmpPoint = new int[2];
    private boolean[][] mOccupied;
    public CellLayout(Context context) {
        this(context, null);
    }

    public CellLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CellLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mContext = getContext();
    }
    public void resetContentDimensions(int count) {
        boolean done = false;
        while (!done) {
            int oldCountX = mGridCountX;
            int oldCountY = mGridCountY;
            if (mGridCountX * mGridCountY < count) {
                mGridCountY++;
            } else if ((mGridCountY - 1) * mGridCountX >= count && mGridCountY >= mGridCountX) {
                mGridCountY = Math.max(0, mGridCountY - 1);
            }
            done = mGridCountX == oldCountX && mGridCountY == oldCountY;
        }
        resizeOccupied();
        Log.e(TAG,"resetContentDimensions :: mGridCountY =:" + mGridCountY +",mGridCountX =:" + mGridCountX);
        requestLayout();
    }
    private void resizeOccupied(){
        mOccupied = new boolean[mGridCountX][mGridCountY];
    }
    public void setGridSize(int x, int y) {
        mGridCountX = x;
        mGridCountY = y;
        resizeOccupied();
        requestLayout();
    }
    public void setCellDimensions(int width,int height){
        mCellWidth = width;
        mCellHeight = height;
        requestLayout();
    }
    void onDropChild(View child) {
        if (child != null) {
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            child.requestLayout();
            // #ifdef LAVA_EDIT
            // wangxijun. 2017/6/2, Descrition
            markCellsAsOccupiedForView(child);
            lp.isLockedToGrid = true;
            // #endif

        }
    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        for(int i = 0; i < count;i++){
            View child = getChildAt(i);
            if(child.getVisibility() != GONE){
                CellLayout.LayoutParams lp = (LayoutParams) child.getLayoutParams();
                int childLeft = lp.x;
                int childTop = lp.y;
                child.layout(childLeft,childTop,childLeft + lp.width,childTop + lp.height);
            }
        }
    }
    public void clearOccupiedCells() {
        for (int x = 0; x < mGridCountX; x++) {
            for (int y = 0; y < mGridCountY; y++) {
                mOccupied[x][y] = false;
            }
        }
    }
    public void markCellsAsOccupiedForView(View view){
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        updateCellsOccupiedState(lp.cellX, lp.cellY, true);
    }

    public void markCellsAsUnoccupiedForView(View view) {
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        updateCellsOccupiedState(lp.cellX, lp.cellY, false);
    }
    public void updateCellsOccupiedState(int cellX,int cellY ,boolean occupied){
        mOccupied[cellX][cellY] = occupied;

    }
    public View getChildAt(int x, int y) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
            if ((lp.cellX == x)  && (lp.cellY == y)) {
                return child;
            }
        }
        return null;
    }
    /**
     * Adds the {@param view} to the layout based on {@param rank} and updated the position
     * related attributes. It assumes that {@param item} is already attached to the view.
     */
    public void addViewForRank(View view, ItemInfo item, int rank) {
        item.rank = rank;
        item.cellX = rank % mGridCountX;
        item.cellY = rank / mGridCountX;

        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        lp.cellX = item.cellX;
        lp.cellY = item.cellY;
        addViewToCellLayout(view, -1, lp, true);
    }
    public boolean addViewToCellLayout(View child, int index, LayoutParams params,
                                       boolean markCells) {
        final LayoutParams lp = params;

        Log.e(TAG,"addViewToCellLayout ::.addView lp.cellX =:" + lp.cellX +",lp.cellY =:" + lp.cellY);
        if (lp.cellX >= 0 && lp.cellX <= mGridCountX - 1 && lp.cellY >= 0 && lp.cellY <= mGridCountY - 1) {
            try {
                addView(child, index, lp);
                if(markCells){
                    markCellsAsOccupiedForView(child);
                }
            } catch (IllegalStateException e) {}

            return true;
        }
        return false;
    }
    /**
     * Reorders the items such that the {@param empty} spot moves to {@param target}
     */
    public void realTimeReorder(int empty, int target) {
        int delay = 0;
        float delayAmount = START_VIEW_REORDER_DELAY;
        int startPos, endPos;
        int moveStart, moveEnd;
        int direction = 0;
        if(empty == -1){
            return;
        }
        if (target == empty) {
            // No animation
            return;
        } else if (target > empty) {
            direction = 1;
        } else {
            // The items will move forward.
            direction = -1;
        }
        moveStart = -1;
        moveEnd = -1;
        startPos = empty;
        endPos = target;
        // Instant moving views.
        while (moveStart != moveEnd) {
            int rankToMove = moveStart + direction;
            int pagePos = rankToMove;
            int x = pagePos % mGridCountX;
            int y = pagePos / mGridCountX;

            final View v = getChildAt(x, y);
            if (v != null) {
                // Do a fake animation before removing it.
                final int newRank = moveStart;
                final float oldTranslateX = v.getTranslationX();

                Runnable endAction = new Runnable() {

                    @Override
                    public void run() {
                        v.setTranslationX(oldTranslateX);
                        ((CellLayout) v.getParent()).removeView(v);
                        addViewForRank(v, (FavoriteShortcutInfo) v.getTag(), newRank);
                    }
                };
                v.animate()
                        .translationXBy((direction > 0) ? -v.getWidth() : v.getWidth())
                        .setDuration(REORDER_ANIMATION_DURATION)
                        .setStartDelay(0)
                        .withEndAction(endAction);
            }
            moveStart = rankToMove;
        }

        if ((endPos - startPos) * direction <= 0) {
            // No animation
            return;
        }

        Log.e(TAG,"realTimeReorder :: startPos :: " + startPos +",endPos =:" + endPos);
        for (int i = startPos; i != endPos; i += direction) {
            int nextPos = i + direction;
            View v = getChildAt(nextPos % mGridCountX, nextPos / mGridCountX);
            if (v != null) {
                ((ItemInfo) v.getTag()).rank -= direction;
            }
            if (animateChildToPosition(v, i % mGridCountX, i / mGridCountX,
                    REORDER_ANIMATION_DURATION, delay, true, true)) {
                delay += delayAmount;
                delayAmount *= VIEW_REORDER_DELAY_FACTOR;
            }
        }
    }
    private final Stack<Rect> mTempRectStack = new Stack<Rect>();
    private void lazyInitTempRectStack() {
        if (mTempRectStack.isEmpty()) {
            for (int i = 0; i < mGridCountX * mGridCountY; i++) {
                mTempRectStack.push(new Rect());
            }
        }
    }

    public int getCountX() {
        return mGridCountX;
    }

    public int getCountY() {
        return mGridCountY;
    }

    private void recycleTempRects(Stack<Rect> used) {
        while (!used.isEmpty()) {
            mTempRectStack.push(used.pop());
        }
    }
    public int[] findNearestArea(int pixelX, int pixelY, int[] result) {
        lazyInitTempRectStack();

        // Keep track of best-scoring drop area
        final int[] bestXY = result != null ? result : new int[2];
        double bestDistance = Double.MAX_VALUE;
        final Rect bestRect = new Rect(-1, -1, -1, -1);
        final Stack<Rect> validRegions = new Stack<Rect>();

        final int countX = mGridCountX;
        final int countY = mGridCountY;

        for (int y = 0; y < countY; y++) {
            for (int x = 0; x < countX; x++) {
                final int[] cellXY = mTmpPoint;
                cellToCenterPoint(x, y, cellXY);
                Rect currentRect = mTempRectStack.pop();
                currentRect.set(x, y, x - 1, y - 1);
                boolean contained = false;
                for (Rect r : validRegions) {
                    if (r.contains(currentRect)) {
                        contained = true;
                        break;
                    }
                }
                validRegions.push(currentRect);
                double distance = Math.hypot(cellXY[0] - pixelX,  cellXY[1] - pixelY);

                if ((distance <= bestDistance && !contained) ||
                        currentRect.contains(bestRect)) {
                    bestDistance = distance;
                    bestXY[0] = x;
                    bestXY[1] = y;
                    bestRect.set(currentRect);
                }
            }
        }

        // Return -1, -1 if no suitable location found
        if (bestDistance == Double.MAX_VALUE) {
            bestXY[0] = -1;
            bestXY[1] = -1;
        }
        recycleTempRects(validRegions);
        return bestXY;
    }

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        markCellsAsUnoccupiedForView(child);
    }

    private void cellToCenterPoint(int cellX, int cellY, int[] result) {
        result[0] = cellX * mCellWidth;
        result[1] = cellY * mCellHeight;
    }

    public boolean animateChildToPosition(final View child, int cellX, int cellY, int duration,
                                          int delay, boolean permanent, boolean adjustOccupied) {
        if(indexOfChild(child) != -1) {
            boolean[][] occupied = mOccupied;
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            final ItemInfo info = (ItemInfo) child.getTag();
            final int oldX = lp.x;
            final int oldY = lp.y;
            if (adjustOccupied) {
                occupied[lp.cellX][lp.cellY] = false;
                occupied[cellX][cellY] = true;
            }
            lp.isLockedToGrid = true;
            lp.cellX = info.cellX = cellX;
            lp.cellY = info.cellY = cellY;

            setupLp(lp);
            lp.isLockedToGrid = false;
            final int newX = lp.x;
            final int newY = lp.y;
            lp.x = oldX;
            lp.y = oldY;
            // Exit early if we're not actually moving the view
            if (oldX == newX && oldY == newY) {
                lp.isLockedToGrid = true;
                return true;
            }
            Log.e(TAG, "animateChildToPosition cellX =:" + cellX + ",cellY= :" + cellY + ",oldX =:" + oldX +",newX =:" + newX);

            ValueAnimator va = AnimUtil.ofFloat(child, 0f, 1f);
            va.setDuration(duration);

            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float r = ((Float) animation.getAnimatedValue()).floatValue();
                    lp.x = (int) ((1 - r) * oldX + r * newX);
                    lp.y = (int) ((1 - r) * oldY + r * newY);
                    child.requestLayout();
                }
            });
            va.addListener(new AnimatorListenerAdapter() {
                boolean cancelled = false;

                public void onAnimationEnd(Animator animation) {
                    // If the animation was cancelled, it means that another animation
                    // has interrupted this one, and we don't want to lock the item into
                    // place just yet.
                    if (!cancelled) {
                        lp.isLockedToGrid = true;
                        child.requestLayout();
                    }
                    Log.e(TAG, "animateChildToPosition onAnimationEnd:");
                }

                public void onAnimationCancel(Animator animation) {
                    cancelled = true;
                }
            });

            va.setStartDelay(delay);
            va.start();
            return true;
        }
        return false;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize =  MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(widthSpecSize, heightSpecSize);
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChild(child);
            }
        }
    }

    public void measureChild(View child) {
        CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
        setupLp(lp);
        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);
        child.measure(childWidthMeasureSpec,childHeightMeasureSpec);
    }

    public void setupLp(LayoutParams lp) {
        lp.setup(mCellWidth,mCellHeight);
    }

    public void removeAndUnMakerView(View child) {
        markCellsAsUnoccupiedForView(child);
        removeView(child);
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        public int cellX;
        public int cellY;
        public int x;
        public int y;
        public boolean isLockedToGrid = true;
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int cellX, int cellY) {
            super(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            this.cellX = cellX;
            this.cellY = cellY;
        }

        public LayoutParams(LayoutParams source) {
            super(source);
            this.cellX = source.cellX;
            this.cellY = source.cellY;
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public void setup(int cellWidth, int cellHeight) {
            if (isLockedToGrid) {
                width = cellWidth;
                height = cellHeight;
                x = cellX * cellWidth + leftMargin;
                y = cellY * cellHeight + topMargin;
            }
        }

        @Override
        public String toString() {
            return "CellLayout LayoutParams (" +
                    " x = :" + x + ",y = :" + y +",cellX = :" + cellX + ",cellY =:" + cellY +
                    ",width =:" + width +",height =:" + height + ",this =:" + this.hashCode() +")";
        }
    }
}
