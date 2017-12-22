package com.zibuyuqing.ucbrowser.widget.favorite;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

/**
 * Created by xijun.wang on 2017/12/22.
 */

public class DragLayer extends FrameLayout {
    private static final String TAG = "DragLayer";
    private final int[] mTmpXY = new int[2];
    private DragController mDragController;
    private int mXDown, mYDown;
    private Context mContext;
    private DragView mDropView;
    private View mAnchorView;
    private int mAnchorViewInitialScrollX = 0;
    private ValueAnimator mDropAnim = null;
    public void setup(DragController dragController) {
        mDragController = dragController;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean handled = mDragController.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
        return handled;
    }

    public DragLayer(@NonNull Context context) {
        super(context);
    }

    public DragLayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DragLayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.e(TAG, "onInterceptTouchEvent: action = " + ev.getAction()
                + ", x = " + ev.getX() + ", y = " + ev.getY());
        int event = ev.getAction() & MotionEvent.ACTION_MASK;
        return mDragController.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean handled = false;
        int action = ev.getAction();
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        return mDragController.onTouchEvent(ev);
    }

    @Override
    public boolean dispatchUnhandledMove(View focused, int direction) {
        Log.e(TAG, "dispatchUnhandledMove: focused = " + focused
                + ", direction = " + direction);
        // return mDragController.dispatchUnhandledMove(focused, direction);
        return false;
    }

    /**
     * Determine the rect of the descendant in this DragLayer's coordinates
     *
     * @param descendant The descendant whose coordinates we want to find.
     * @param r          The rect into which to place the results.
     * @return The factor by which this descendant is scaled relative to this DragLayer.
     */
    public float getDescendantRectRelativeToSelf(View descendant, Rect r) {
        mTmpXY[0] = 0;
        mTmpXY[1] = 0;
        float scale = getDescendantCoordRelativeToSelf(descendant, mTmpXY);

        r.set(mTmpXY[0], mTmpXY[1],
                (int) (mTmpXY[0] + scale * descendant.getMeasuredWidth()),
                (int) (mTmpXY[1] + scale * descendant.getMeasuredHeight()));
        return scale;
    }

    public float getLocationInDragLayer(View child, int[] loc) {
        loc[0] = 0;
        loc[1] = 0;
        return getDescendantCoordRelativeToSelf(child, loc);
    }

    public float getLocationInDragLayer(View child, int[] loc, boolean isIncludeScroll) {
        loc[0] = 0;
        loc[1] = 0;
        return getDescendantCoordRelativeToSelf(child, loc, isIncludeScroll);
    }

    public float getDescendantCoordRelativeToSelf(View descendant, int[] coord) {
        return getDescendantCoordRelativeToSelf(descendant, coord, false);
    }

    /**
     * Given a coordinate relative to the descendant, find the coordinate in this DragLayer's
     * coordinates.
     *
     * @param descendant        The descendant to which the passed coordinate is relative.
     * @param coord             The coordinate that we want mapped.
     * @param includeRootScroll Whether or not to account for the scroll of the root descendant:
     *                          sometimes this is relevant as in a child's coordinates within the root descendant.
     * @return The factor by which this descendant is scaled relative to this DragLayer. Caution
     * this scale factor is assumed to be equal in X and Y, and so if at any point this
     * assumption fails, we will need to return a pair of scale factors.
     */
    public float getDescendantCoordRelativeToSelf(View descendant, int[] coord,
                                                  boolean includeRootScroll) {
        return ViewUtil.getDescendantCoordRelativeToParent(descendant, this,
                coord, includeRootScroll);
    }

    public void getViewRectRelativeToSelf(View v, Rect r) {
        int[] loc = new int[2];
        getLocationInWindow(loc);
        int x = loc[0];
        int y = loc[1];

        v.getLocationInWindow(loc);
        int vX = loc[0];
        int vY = loc[1];

        int left = vX - x;
        int top = vY - y;
        r.set(left, top, left + v.getMeasuredWidth(), top + v.getMeasuredHeight());
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    // Override to allow type-checking of LayoutParams.
    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    public static class LayoutParams extends FrameLayout.LayoutParams {
        public int x, y;
        public boolean customPosition = false;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams lp) {
            super(lp);
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getWidth() {
            return width;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getHeight() {
            return height;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getX() {
            return x;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getY() {
            return y;
        }

    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            final FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) child.getLayoutParams();
            if (flp instanceof LayoutParams) {
                final LayoutParams lp = (LayoutParams) flp;
                if (lp.customPosition) {
                    child.layout(lp.x, lp.y, lp.x + lp.width, lp.y + lp.height);
                }
            }
        }
    }

    public void animateViewIntoPosition(DragView dragView, final int[] pos, float scaleX, float scaleY,
                                        int animationEndStyle, Runnable onFinishRunnable, int duration) {
        Rect r = new Rect();
        getViewRectRelativeToSelf(dragView, r);
        final int fromX = r.left;
        final int fromY = r.top;

        animateViewIntoPosition(dragView, fromX, fromY, pos[0], pos[1], 1, 1, scaleX, scaleY,
                onFinishRunnable, duration, null);
    }

    public void animateViewIntoPosition(DragView dragView, final View child,
                                        final Runnable onFinishAnimationRunnable, View anchorView) {
        animateViewIntoPosition(dragView, child, -1, onFinishAnimationRunnable, anchorView);
    }


    public void animateViewIntoPosition(DragView dragView, final View child, int duration,
                                        final Runnable onFinishAnimationRunnable, View anchorView) {
        Runnable onCompleteRunnable = new Runnable() {
            public void run() {
                child.setVisibility(VISIBLE);
                if (onFinishAnimationRunnable != null) {
                    onFinishAnimationRunnable.run();
                }
            }
        };
        CellLayout parentChildren = (CellLayout) child.getParent();
        CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
        parentChildren.measureChild(child);

        Rect r = new Rect();
        getViewRectRelativeToSelf(dragView, r);
        Log.e(TAG, "animateViewIntoPosition: dragView = " + dragView + ", r = " + r
                + ", lp.x = " + lp.x + ", lp.y = " + lp.y);
        int coord[] = new int[2];
        float childScale = child.getScaleX();
        coord[0] = lp.x + (int) (child.getMeasuredWidth() * (1 - childScale) / 2);
        coord[1] = lp.y + (int) (child.getMeasuredHeight() * (1 - childScale) / 2);

        // Since the child hasn't necessarily been laid out, we force the lp to be updated with
        // the correct coordinates (above) and use these to determine the final location
        float scale = getDescendantCoordRelativeToSelf((View) child.getParent(), coord);
        // We need to account for the scale of the child itself, as the above only accounts for
        // for the scale in parents.
        scale *= childScale;
        int toX = coord[0];
        int toY = coord[1];
        float toScale = scale;
        if (child instanceof FavoriteItemView) {
            // Account for the source scale of the icon (ie. from AllApps to Workspace, in which
            // the workspace may have smaller icon bounds).
            toScale = scale / dragView.getIntrinsicIconScaleFactor();

            // The child may be scaled (always about the center of the view) so to account for it,
            // we have to offset the position by the scaled size.  Once we do that, we can center
            // the drag view about the scaled child view.
            toY += Math.round(toScale * child.getPaddingTop());
            toY -= dragView.getMeasuredHeight() * (1 - toScale) / 2;
            if (dragView.getDragVisualizeOffset() != null) {
                toY -= Math.round(toScale * dragView.getDragVisualizeOffset().y);
            }
            toX -= (dragView.getMeasuredWidth() - Math.round(scale * child.getMeasuredWidth())) / 2;
        }

        final int fromX = r.left;
        final int fromY = r.top;
        child.setVisibility(INVISIBLE);
        animateViewIntoPosition(dragView, fromX, fromY, toX, toY,
                1, 1, toScale, toScale, onCompleteRunnable, duration, anchorView);
    }
    public void animateViewIntoPosition(final DragView view, final int fromX, final int fromY,
                                        final int toX, final int toY, float initScaleX, float initScaleY,
                                        float finalScaleX, float finalScaleY, Runnable onCompleteRunnable, int duration, View anchorView) {
        Rect from = new Rect(fromX, fromY, fromX +
                view.getMeasuredWidth(), fromY + view.getMeasuredHeight());
        Rect to = new Rect(toX, toY, toX + view.getMeasuredWidth(), toY + view.getMeasuredHeight());
        animateView(view, from, to, initScaleX, initScaleY, finalScaleX, finalScaleY, duration, onCompleteRunnable, anchorView);
    }
    /**
     * This method animates a view at the end of a drag and drop animation.
     *
     * @param view The view to be animated. This view is drawn directly into DragLayer, and so
     *        doesn't need to be a child of DragLayer.
     * @param from The initial location of the view. Only the left and top parameters are used.
     * @param to The final location of the view. Only the left and top parameters are used. This
     *        location doesn't account for scaling, and so should be centered about the desired
     *        final location (including scaling).
     * @param duration The duration of the animation.
     * @param onCompleteRunnable Optional runnable to run on animation completion.

     * @param anchorView If not null, this represents the view which the animated view stays
     *        anchored to in case scrolling is currently taking place. Note: currently this is
     *        only used for the X dimension for the case of the workspace.
     */
    public void animateView(final DragView view, final Rect from, final Rect to,
                            final float initScaleX, final float initScaleY,
                            final float finalScaleX, final float finalScaleY, int duration,
                            final Runnable onCompleteRunnable, View anchorView) {

        if (duration < 0) {
            duration = 500;
        }

        // Animate the view
        final float dropViewScale = view.getScaleX();
        ValueAnimator.AnimatorUpdateListener updateCb = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float percent = (Float) animation.getAnimatedValue();
                final int width = view.getMeasuredWidth();
                final int height = view.getMeasuredHeight();

                float initialScaleX = initScaleX * dropViewScale;
                float initialScaleY = initScaleY * dropViewScale;
                float scaleX = finalScaleX * percent + initialScaleX * (1 - percent);
                float scaleY = finalScaleY * percent + initialScaleY * (1 - percent);

                float fromLeft = from.left + (initialScaleX - 1f) * width / 2;
                float fromTop = from.top + (initialScaleY - 1f) * height / 2;

                int x = (int) (fromLeft + Math.round(((to.left - fromLeft) * percent)));
                int y = (int) (fromTop + Math.round(((to.top - fromTop) * percent)));

                int anchorAdjust = mAnchorView == null ? 0 : (int) (mAnchorView.getScaleX() *
                        (mAnchorViewInitialScrollX - mAnchorView.getScrollX()));

                int xPos = x - mDropView.getScrollX() + anchorAdjust;
                int yPos = y - mDropView.getScrollY();

                mDropView.setTranslationX(xPos);
                mDropView.setTranslationY(yPos);
                mDropView.setScaleX(scaleX);
                mDropView.setScaleY(scaleY);
            }
        };
        animateView(view, updateCb, duration, onCompleteRunnable,anchorView);
    }
    public void animateView(final DragView view,
                            ValueAnimator.AnimatorUpdateListener updateCb,
                            int duration, final Runnable onCompleteRunnable, View anchorView) {
        if (mDropAnim != null) mDropAnim.cancel();
        mDropView = view;
        mDropView.cancelAnimation();
        mDropView.resetLayoutParams();

        // Set the anchor view if the page is scrolling
        if (anchorView != null) {
            mAnchorViewInitialScrollX = anchorView.getScrollX();
        }
        mAnchorView = anchorView;

        // Create and start the animation
        mDropAnim = new ValueAnimator();
        mDropAnim.setDuration(duration);
        mDropAnim.setFloatValues(0f, 1f);
        mDropAnim.addUpdateListener(updateCb);
        mDropAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                if (onCompleteRunnable != null) {
                    onCompleteRunnable.run();
                }
            }
        });
        mDropAnim.start();
    }
    public void clearAnimatedView() {
        if (mDropAnim != null) {
            mDropAnim.cancel();
        }
        if (mDropView != null) {
            mDragController.onEndDrag(mDropView);
        }
        mDropView = null;
        invalidate();
    }
}
