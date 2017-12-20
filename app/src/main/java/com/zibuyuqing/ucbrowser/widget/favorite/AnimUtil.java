package com.zibuyuqing.ucbrowser.widget.favorite;

import android.animation.ValueAnimator;
import android.view.View;

/**
 * Created by Xijun.Wang on 2017/12/20.
 */

public class AnimUtil {
    public static ValueAnimator ofFloat(View target, float... values) {
        ValueAnimator anim = new ValueAnimator();
        anim.setFloatValues(values);
        return anim;
    }
}
