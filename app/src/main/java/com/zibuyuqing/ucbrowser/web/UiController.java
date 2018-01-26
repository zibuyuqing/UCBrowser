package com.zibuyuqing.ucbrowser.web;

/**
 * Created by Xijun.Wang on 2018/1/26.
 */

public interface UiController extends WebViewController{
    void selectTab(Tab tab);
    void closeTab(Tab tab);
    void onTabCountChanged();
    void onTabDataChanged(Tab tab);
}
