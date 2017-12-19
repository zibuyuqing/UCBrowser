package com.zibuyuqing.ucbrowser.widget.favorite;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * Created by Xijun.Wang on 2017/12/19.
 */

public class CellLayout extends ViewGroup{
    private int mCellWidth;
    private int mCellHeight;
    private int mCountX;
    private int mCountY;
    private final int[] mDragCell = new int[2];
    private boolean mDragging = false;
    public CellLayout(Context context) {
        this(context,null);
    }

    public CellLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CellLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public void setGridSize(int x, int y) {
        mCountX = x;
        mCountY = y;
        requestLayout();
    }
    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {

    }
}
