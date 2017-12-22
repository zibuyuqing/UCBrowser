package com.zibuyuqing.ucbrowser.widget.favorite;

import android.view.View;

/**
 * Created by xijun.wang on 2017/12/22.
 */

public interface DragSource {
    void onDropCompleted(View target, DragObject d, boolean isFlingToDelete, boolean success);
}
