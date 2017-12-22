package com.zibuyuqing.ucbrowser.widget.favorite;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by xijun.wang on 2017/12/22.
 */

public abstract class FavoriteItemView extends View {

    public FavoriteItemView(Context context) {
        super(context);
    }

    public FavoriteItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FavoriteItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
