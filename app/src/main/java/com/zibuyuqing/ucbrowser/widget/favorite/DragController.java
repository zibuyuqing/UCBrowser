package com.zibuyuqing.ucbrowser.widget.favorite;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by xijun.wang on 2017/12/21.
 */

public class DragController {
    private static final String TAG = "DragController";
    public static int DRAG_ACTION_MOVE = 0;
    private boolean mDragging;
    private int mMotionDownX;
    private int mMotionDownY;
    private DragObject mDragObject;
    private ArrayList<DragListener> mListeners = new ArrayList<DragListener>();
    private Context mContext;

    private int mLastTouch[] = new int[2];
    private int mTmpPoint[] = new int[2];
    private DragLayer mDragLayer;
    // temporaries to avoid gc thrash
    private Rect mRectTemp = new Rect();
    private final int[] mCoordinatesTemp = new int[2];
    private Rect mDragLayerRect = new Rect();

    public DragController(Context context){
        mContext = context;
    }
    public DragView startDrag(
            DragLayer parent,
            Bitmap b,
            int dragLayerX,
            int dragLayerY,
            DragSource source,
            Object dragInfo,
            int dragAction,
            Point dragOffset,
            Rect dragRegion,
            float initialDragViewScale) {
        mDragLayer = parent;
        for (DragListener listener : mListeners) {
            listener.onDragStart(source, dragInfo, dragAction);
        }

        final int registrationX = mMotionDownX - dragLayerX;
        final int registrationY = mMotionDownY - dragLayerY;

        Log.e(TAG, "startDrag: dragLayerX = " + dragLayerX + ", dragLayerY = "
                + dragLayerY + ", dragInfo = " + dragInfo + ", registrationX = "
                + registrationX + ", registrationY = " + registrationY + ", dragRegion = "
                + dragRegion);

        final int dragRegionLeft = dragRegion == null ? 0 : dragRegion.left;
        final int dragRegionTop = dragRegion == null ? 0 : dragRegion.top;

        mDragging = true;
        mDragObject = new DragObject();

        mDragObject.dragComplete = false;
        mDragObject.xOffset = mMotionDownX - (dragLayerX + dragRegionLeft);
        mDragObject.yOffset = mMotionDownY - (dragLayerY + dragRegionTop);
        mDragObject.dragSource = source;
        mDragObject.dragInfo = dragInfo;

        final DragView dragView = mDragObject.dragView = new DragView(mContext, b, registrationX,
                registrationY, 0, 0, b.getWidth(), b.getHeight(), initialDragViewScale);

        if (dragOffset != null) {
            dragView.setDragVisualizeOffset(new Point(dragOffset));
        }
        if (dragRegion != null) {
            dragView.setDragRegion(new Rect(dragRegion));
        }

        dragView.showAt(parent,mMotionDownX, mMotionDownY);
        handleMoveEvent(mMotionDownX, mMotionDownY);
        return dragView;
    }
    public boolean onInterceptTouchEvent(MotionEvent ev){
        final int action = ev.getAction();
        final int[] dragLayerPos = getClampedDragLayerPos(ev.getX(), ev.getY());
        final int dragLayerX = dragLayerPos[0];
        final int dragLayerY = dragLayerPos[1];
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_DOWN:
                // Remember location of down touch
                mMotionDownX = dragLayerX;
                mMotionDownY = dragLayerY;
                break;
            case MotionEvent.ACTION_UP:
                if (mDragging) {
                    drop(dragLayerX, dragLayerY);
                }
                endDrag();
                break;
            case MotionEvent.ACTION_CANCEL:
                cancelDrag();
                break;
        }

        return mDragging;
    }
    /**
     * Call this from a drag source view.
     */
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mDragging) {
            return false;
        }
        final int action = ev.getAction();
        final int[] dragLayerPos = getClampedDragLayerPos(ev.getX(), ev.getY());
        final int dragLayerX = dragLayerPos[0];
        final int dragLayerY = dragLayerPos[1];
        Log.e(TAG, "onTouchEvent: action = " + action + ", dragLayerX = " + dragLayerX
                + ", dragLayerY = " + dragLayerY + ", mMotionDownX = " + mMotionDownX
                + ", mMotionDownY = " + mMotionDownY);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // Remember where the motion event started
                mMotionDownX = dragLayerX;
                mMotionDownY = dragLayerY;
                handleMoveEvent(dragLayerX, dragLayerY);
                break;
            case MotionEvent.ACTION_MOVE:
                handleMoveEvent(dragLayerX, dragLayerY);
                break;
            case MotionEvent.ACTION_UP:
                // Ensure that we've processed a move event at the current pointer location.
                handleMoveEvent(dragLayerX, dragLayerY);
                if (mDragging) {
                    drop(dragLayerX, dragLayerY);
                }
                endDrag();
                break;
            case MotionEvent.ACTION_CANCEL:
                cancelDrag();
                break;
        }

        return true;
    }

    private void handleMoveEvent(int x, int y) {
        mDragObject.dragView.move(x, y);

        // Drop on someone?
        final int[] coordinates = mCoordinatesTemp;
        mDragObject.x = coordinates[0];
        mDragObject.y = coordinates[1];

        Log.e(TAG, "handleMoveEvent: x = " + x + ", y = " + y
                + ", dragView = " + mDragObject.dragView + ", dragX = "
                + mDragObject.x + ", dragY = " + mDragObject.y);

        // Check if we are hovering over the scroll areas
        mLastTouch[0] = x;
        mLastTouch[1] = y;
    }
    public boolean dispatchKeyEvent(KeyEvent keyEvent){
        return mDragging;
    }
    public boolean isDragging(){
        return mDragging;
    }
    /**
     * This only gets called as a result of drag view cleanup being deferred in endDrag();
     */
    void onEndDrag(DragView dragView) {
        dragView.remove();
        // If we skipped calling onDragEnd() before, do it now
        for (DragListener listener : new ArrayList<>(mListeners)) {
            listener.onDragEnd();
        }
    }

    public void cancelDrag(){
        Log.e(TAG,"cancelDrag mDragging = :" +mDragging);

        if (mDragging) {
            mDragObject.cancelled = true;
            mDragObject.dragComplete = true;
            mDragObject.dragSource.onDropCompleted(null, mDragObject, false, false);
        }
        endDrag();
    }
    public void drop(float x,float y){
        final int[] coordinates = mCoordinatesTemp;
        mDragObject.x = coordinates[0];
        mDragObject.y = coordinates[1];
        mDragObject.dragComplete = true;
        // mDragObject.dragSource.onDropCompleted((View) dropTarget, mDragObject, false, accepted);
    }
    private void endDrag(){
        Log.e(TAG,"cancelDrag endDrag = :" +mDragging);
        if(mDragging){
            mDragging = false;
            if(mDragObject.dragView != null){
                mDragObject.dragView.remove();
            }
            mDragObject.dragView = null;
        }
    }
    /**
     * Clamps the position to the drag layer bounds.
     */
    private int[] getClampedDragLayerPos(float x, float y) {
        mDragLayer.getLocalVisibleRect(mDragLayerRect);
        mTmpPoint[0] = (int) Math.max(mDragLayerRect.left, Math.min(x, mDragLayerRect.right - 1));
        mTmpPoint[1] = (int) Math.max(mDragLayerRect.top, Math.min(y, mDragLayerRect.bottom - 1));
        return mTmpPoint;
    }
    public DragView getDragView(){
        return mDragObject.dragView;
    }
}
