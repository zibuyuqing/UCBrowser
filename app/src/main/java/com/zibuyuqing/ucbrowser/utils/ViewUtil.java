package com.zibuyuqing.ucbrowser.utils;

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

    /**
     * Scales a rect about its centroid
     */
    public static void scaleRectAboutCenter(Rect r, float scale) {
        if (scale != 1.0f) {
            int cx = r.centerX();
            int cy = r.centerY();
            r.offset(-cx, -cy);
            r.left = (int) (r.left * scale + 0.5f);
            r.top = (int) (r.top * scale + 0.5f);
            r.right = (int) (r.right * scale + 0.5f);
            r.bottom = (int) (r.bottom * scale + 0.5f);
            r.offset(cx, cy);
        }
    }

    /**
     * Cancels an animation ensuring that if it has listeners, onCancel and onEnd
     * are not called.
     */
    public static void cancelAnimationWithoutCallbacks(Animator animator) {
        if (animator != null) {
            animator.removeAllListeners();
            animator.cancel();
        }
    }

    public static void getAvailableStackBounds(Context context, Rect taskStackBounds) {

        taskStackBounds.set(0, 0, getScreenSize(context).x, getScreenSize(context).y);
    }
}
