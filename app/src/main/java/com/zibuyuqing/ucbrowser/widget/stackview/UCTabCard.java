package com.zibuyuqing.ucbrowser.widget.stackview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by Xijun.Wang on 2017/12/4.
 */

public class UCTabCard extends FrameLayout  {
    private final static String TAG = UCTabCard.class.getSimpleName();
    public UCTabCard(@NonNull Context context) {
        this(context, null);
    }

    public UCTabCard(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UCTabCard(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
