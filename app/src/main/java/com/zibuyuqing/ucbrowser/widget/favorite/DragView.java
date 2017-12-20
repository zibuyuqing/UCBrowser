package com.zibuyuqing.ucbrowser.widget.favorite;

import android.content.Context;
import android.os.Trace;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Xijun.Wang on 2017/12/20.
 */

public class DragView extends View{
    private int mRegistrationX;
    private int mRegistrationY;
    private float mOffsetX;
    private float mOffsetY;
    public DragView(Context context) {
        this(context,null);
    }

    public DragView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public DragView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Move the window containing this view.
     *
     * @param touchX the x coordinate the user touched in DragLayer coordinates
     * @param touchY the y coordinate the user touched in DragLayer coordinates
     */
    void move(int touchX, int touchY) {
        setTranslationX(touchX - mRegistrationX + (int) mOffsetX);
        setTranslationY(touchY - mRegistrationY + (int) mOffsetY);
    }
}
