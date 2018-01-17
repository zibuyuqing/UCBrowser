package com.zibuyuqing.ucbrowser.base;

/**
 * Created by Xijun.Wang on 2018/1/16.
 */

public interface BasePresenter <T extends BaseView>{
    void attachView(T view);
    void detachView();
}
