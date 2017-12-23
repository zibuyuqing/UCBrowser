package com.zibuyuqing.ucbrowser.widget.favorite;

import android.graphics.Rect;

/**
 * Created by xijun.wang on 2017/12/23.
 */

public interface DropTarget {
    class DragObject {
        public int x = -1;
        public int y = -1;

        /** X offset from the upper-left corner of the cell to where we touched.  */
        public int xOffset = -1;

        /** Y offset from the upper-left corner of the cell to where we touched.  */
        public int yOffset = -1;

        /** This indicates whether a drag is in final stages, either drop or cancel. It
         * differentiates onDragExit, since this is called when the drag is ending, above
         * the current drag target, or when the drag moves off the current drag object.
         */
        public boolean dragComplete = false;

        /** The view that moves around while you drag.  */
        public DragView dragView = null;

        /** The data associated with the object being dragged */
        public Object dragInfo = null;

        /** Where the drag originated */
        public DragSource dragSource = null;

        /** Post drag animation runnable */
        public Runnable postAnimationRunnable = null;

        /** Indicates that the drag operation was cancelled */
        public boolean cancelled = false;
        public DragObject() {
        }
        public String toString() {
            return "DragObject{x = " + x + ",y = " + y + ",xOffset = " + xOffset + ",yOffset = "
                    + yOffset + ",dragComplete = " + dragComplete + ",dragInfo = " + dragInfo
                    + ",dragSource = " + dragSource + "}";
        }
        public final float[] getVisualCenter(float[] recycle) {
            final float res[] = (recycle == null) ? new float[2] : recycle;

            // These represent the visual top and left of drag view if a dragRect was provided.
            // If a dragRect was not provided, then they correspond to the actual view left and
            // top, as the dragRect is in that case taken to be the entire dragView.
            // R.dimen.dragViewOffsetY.
            int left = x - xOffset;
            int top = y - yOffset;

            // In order to find the visual center, we shift by half the dragRect
            res[0] = left + dragView.getDragRegion().width() / 2;
            res[1] = top + dragView.getDragRegion().height() / 2;

            return res;
        }
    }
    boolean isDropEnabled();
    void onDrop(DragObject dragObject);
    void onDragEnter(DragObject dragObject);
    void onDragOver(DragObject dragObject);
    void onDragExit(DragObject dragObject);
    void getHitRectRelativeToDragLayer(Rect outRect);
    void onDragStart(DragSource source, Object info, int dragAction);
    void onDragEnd();
}
