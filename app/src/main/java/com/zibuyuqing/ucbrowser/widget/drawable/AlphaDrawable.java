package com.zibuyuqing.ucbrowser.widget.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Xijun.Wang on 2017/12/11.
 */

public class AlphaDrawable extends Drawable{
    private static final String TAG ="AlphaDrawable";
    private Drawable mChild;
    public AlphaDrawable(Drawable child){
        this.mChild = child;
    }
    @Override
    public void draw(@NonNull Canvas canvas) {
        mChild.draw(canvas);
    }
    @Override
    public void setAlpha(int i) {
        Log.e(TAG,"i =:" + i);
        mChild.setAlpha(i);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        mChild.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return mChild.getOpacity();
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        mChild.setBounds(bounds);
    }

    @Override
    public int getIntrinsicHeight() {
        return mChild.getIntrinsicHeight();
    }

    @Override
    public int getIntrinsicWidth() {
        return mChild.getIntrinsicWidth();
    }
}
