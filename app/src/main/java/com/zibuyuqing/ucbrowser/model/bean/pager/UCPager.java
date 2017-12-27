package com.zibuyuqing.ucbrowser.model.bean.pager;

import android.graphics.Bitmap;

/**
 * Created by Xijun.Wang on 2017/12/14.
 */

public class UCPager {
    private String title;
    private int websiteIcon;//网站图标更合理的是在云端下载，为了方便，我先使用本地的
    private Bitmap pagerPreview;
    private int key;
    public UCPager(String title, int websiteIcon, Bitmap pagerPreview,int key) {
        this.title = title;
        this.websiteIcon = websiteIcon;
        this.pagerPreview = pagerPreview;
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getWebsiteIcon() {
        return websiteIcon;
    }

    public void setWebsiteIcon(int websiteIcon) {
        this.websiteIcon = websiteIcon;
    }

    public Bitmap getPagerPreview() {
        return pagerPreview;
    }

    public void setPagerPreview(Bitmap pagerPreview) {
        this.pagerPreview = pagerPreview;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }
}
