package com.zibuyuqing.common.utils;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.Display;
import android.view.WindowManager;


/**
 * Created by Xijun.Wang on 2017/11/28.
 */

public class ViewUtil {
    public static Point getScreenSize(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point realSize = new Point();
        display.getRealSize(realSize);
        return realSize;
    }
}
