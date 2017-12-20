package com.zibuyuqing.ucbrowser.widget.favorite;

/**
 * Created by Xijun.Wang on 2017/12/20.
 */

public interface DragListener {
    void onDragStart();
    void onDragEnd();
    void move(int x,int y);
}
