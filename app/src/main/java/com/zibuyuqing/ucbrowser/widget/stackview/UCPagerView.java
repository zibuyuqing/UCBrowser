package com.zibuyuqing.ucbrowser.widget.stackview;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zibuyuqing.ucbrowser.R;

/**
 * Created by Xijun.Wang on 2017/12/4.
 */

public class UCPagerView extends FrameLayout  {
    private final static String TAG = UCPagerView.class.getSimpleName();
    public UCPagerView(@NonNull Context context) {
        this(context, null);
    }

    public UCPagerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UCPagerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public interface CallBack{
        void onSelect(int key);
        void onClose(int key);
    }
}
