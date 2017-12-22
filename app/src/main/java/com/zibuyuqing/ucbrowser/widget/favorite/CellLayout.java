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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.zibuyuqing.ucbrowser.R;
import com.zibuyuqing.ucbrowser.model.bean.favorite.FavoriteShortcutInfo;
import com.zibuyuqing.ucbrowser.model.bean.favorite.ItemInfo;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Xijun.Wang on 2017/12/19.
 */

public class CellLayout extends ViewGroup implements DragSource {
    private static final String TAG ="CellLayout";
    private static final int START_VIEW_REORDER_DELAY = 30;
    private static final int REORDER_ANIMATION_DURATION = 230;
    private static final float VIEW_REORDER_DELAY_FACTOR = 0.9f;
    public static final int DRAG_BITMAP_PADDING = 2;
    private int mCellWidth;
    private int mCellHeight;
    private int mCountX;
    private int mCountY;
    private int mGridCountX;
    private int mGridCountY;
    private final int[] mDragCell = new int[2];
    private boolean mDragging = false;
    private final Alarm mReorderAlarm = new Alarm();
    private int mTargetRank, mPrevTargetRank, mEmptyCellRank;
    private Canvas mCanvas = new Canvas();
    private Resources mResources;
    private Context mContext;
    private static final Rect sTempRect = new Rect();
    private final int[] mTempXY = new int[2];
    private DragController mDragController;
    private DragLayer mDragLayer;
    private ItemInfo mCurrentDragInfo;
    private View mCurrentDragView;
    private ArrayList<ItemInfo> mInfos = new ArrayList<>();
    private OnAlarmListener mReorderAlarmListener = new OnAlarmListener() {
        public void onAlarm(Alarm alarm) {
            realTimeReorder(mEmptyCellRank, mTargetRank);
            mEmptyCellRank = mTargetRank;
        }
    };
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
    public void setup(DragLayer dragLayer ,DragController dragController){
        mDragLayer = dragLayer;
        mDragController = dragController;
    }
    private void init() {
        mContext = getContext();
        mResources = mContext.getResources();
    }

    public void setGridSize(int x, int y) {
        mCountX = x;
        mCountY = y;
        requestLayout();
    }
    public Bitmap createDragOutline(int width,int height){
        final Bitmap b = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        mCanvas.setBitmap(b);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(mResources.getColor(R.color.windowBg,null));
        mCanvas.drawRoundRect(new RectF(0,0,width,height),8,8,paint);
        return b;
    }
    public boolean onLongClick(View v){
        return beginDragShared(v);
    }
    private boolean beginDragShared(View v){
        Object tag = v.getTag();
        if(tag instanceof ItemInfo) {
            if(!v.isInTouchMode()){
                return false;
            }
            ItemInfo item = (ItemInfo) tag;
            beginDragShared(v, new Point(), this);
            mCurrentDragInfo = item;
            mEmptyCellRank = item.rank;
            mCurrentDragView = v;
            removeView(mCurrentDragView);
            mInfos.remove(item);
        }
        return true;
    }
    private Bitmap createDragBitmap(View child, AtomicInteger aPadding) {
        int padding = aPadding.get();
        Bitmap b = Bitmap.createBitmap(
                child.getWidth() + padding,
                child.getHeight() + padding,
                Bitmap.Config.ARGB_8888);
        mCanvas.setBitmap(b);
        drawDragView(child,mCanvas,padding);
        mCanvas.setBitmap(null);
        return b;
    }

    private void drawDragView(View child, Canvas canvas, int padding) {
        final Rect clipRect = sTempRect;
        child.getDrawingRect(clipRect);
        canvas.translate(-child.getScrollX() + padding / 2, -child.getScrollY() + padding / 2);
        canvas.clipRect(clipRect, Region.Op.REPLACE);
        child.draw(canvas);
    }

    public void beginDragShared(View child, Point relativeTouchPos,DragSource source){
        child.clearFocus();
        child.setPressed(false);
        AtomicInteger padding = new AtomicInteger(DRAG_BITMAP_PADDING);
        final Bitmap b = createDragBitmap(child, padding);
        final int bmpWidth = b.getWidth();
        final int bmpHeight = b.getHeight();
        float scale = mDragLayer.getLocationInDragLayer(child, mTempXY);
        int dragLayerX = Math.round(mTempXY[0] -
                (bmpWidth - scale * child.getWidth()) / 2);
        int dragLayerY = Math.round(mTempXY[1] -
                (bmpHeight - scale * bmpHeight) / 2 - padding.get() / 2);
        Point dragVisualizeOffset = null;
        Rect dragRect = null;
        if(child instanceof FavoriteItemView){
            dragVisualizeOffset = new Point(-padding.get() / 2,
                    padding.get() / 2 - child.getPaddingTop());
            dragRect = new Rect(0, child.getPaddingTop(), child.getWidth(), child.getHeight());
        }
        ItemInfo info = (ItemInfo) child.getTag();
        mDragController.startDrag(mDragLayer,b, dragLayerX, dragLayerY, source, info,
                DragController.DRAG_ACTION_MOVE, dragVisualizeOffset, dragRect, scale);
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

    public View getChildAt(int x, int y) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();

            if ((lp.cellX <= x) && (x < lp.cellX) &&
                    (lp.cellY <= y) && (y < lp.cellY)) {
                return child;
            }
        }
        return null;
    }
    /**
     * Adds the {@param view} to the layout based on {@param rank} and updated the position
     * related attributes. It assumes that {@param item} is already attached to the view.
     */
    public void addViewForRank(View view, FavoriteShortcutInfo item, int rank) {
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

        Log.i(TAG,"addViewToCellLayout :: mShortcutsAndWidgets.addView lp.cellX =:" + lp.cellX +",lp.cellY =:" + lp.cellY);
        if (lp.cellX >= 0 && lp.cellX <= mCountX - 1 && lp.cellY >= 0 && lp.cellY <= mCountY - 1) {
            try {
                Log.i(TAG,"addViewToCellLayout :: mShortcutsAndWidgets.addView lp.x =:" + lp.x+",lp.cellX =:" + lp.cellX);
                addView(child, index, lp);
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
        int direction;

        if (target == empty) {
            // No animation
            return;
        } else if (target > empty) {
            direction = 1;
        } else {
            // The items will move forward.
            direction = -1;
        }
        moveStart = moveEnd = -1;
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
                        ((CellLayout) v.getParent().getParent()).removeView(v);
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
    public boolean animateChildToPosition(final View child, int cellX, int cellY, int duration,
                                          int delay, boolean permanent, boolean adjustOccupied) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        final ItemInfo info = (ItemInfo) child.getTag();

        final int oldX = lp.x;
        final int oldY = lp.y;
        lp.cellX = info.cellX = cellX;
        lp.cellY = info.cellY = cellY;
        final int newX = lp.x;
        final int newY = lp.y;

        lp.x = oldX;
        lp.y = oldY;

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
                    child.requestLayout();
                }
            }

            public void onAnimationCancel(Animator animation) {
                cancelled = true;
            }
        });
        va.setStartDelay(delay);
        va.start();
        return true;
    }

    public void measureChild(View child) {
        final int cellWidth = mCellWidth;
        final int cellHeight = mCellHeight;
        CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
        lp.setup(cellWidth, cellHeight);
        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);
        child.measure(childWidthMeasureSpec,childHeightMeasureSpec);
    }

    @Override
    public void onDropCompleted(View target, DragObject d, boolean isFlingToDelete, boolean success) {

    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        public int cellX;
        public int cellY;
        public int x;
        public int y;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
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
            width = cellWidth;
            height = cellHeight;
            x = cellX * cellWidth + leftMargin;
            y = cellY * cellHeight + topMargin;
        }
    }
}
