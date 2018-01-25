package com.zibuyuqing.ucbrowser.web;

import android.content.Context;
import android.graphics.Bitmap;
import android.webkit.WebView;


/**
 * Created by Xijun.Wang on 2018/1/25.
 */

public interface WebViewController {
    Context getContext();
    TabController getTabController();
    WebViewFactory getWebViewFactory();
    void onSetWebView(Tab tab, WebView view);
    void onPageStarted(Tab tab, WebView webView, Bitmap favicon);
    void onPageFinished(Tab tab);
    void onProgressChanged(Tab tab);
    void onReceivedTitle(Tab tab,final String title);
    void onFavicon(Tab tab,WebView view,Bitmap icon);
    Tab openTab(String url, boolean setActive,boolean useCurrent);
    Tab openTab(String url, Tab parent, boolean setActive,
                boolean useCurrent);
    boolean switchToTab(Tab tab);
    void closeTab(Tab tab);
    boolean shouldCaptureThumbnails();
    void setActiveTab(Tab tab);
}
