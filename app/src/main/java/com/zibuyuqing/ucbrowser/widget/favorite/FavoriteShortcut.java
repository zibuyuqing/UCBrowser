package com.zibuyuqing.ucbrowser.widget.favorite;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
/**
 * Created by xijun.wang on 2017/12/22.
 */

public class FavoriteShortcut extends FavoriteItemView {

    public FavoriteShortcut(Context context) {
        this(context,null);
    }

    public FavoriteShortcut(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public FavoriteShortcut(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

}
