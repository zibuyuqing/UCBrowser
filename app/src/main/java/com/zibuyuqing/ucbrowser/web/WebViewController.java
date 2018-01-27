package com.zibuyuqing.ucbrowser.web;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.webkit.WebView;


/**
 * Created by Xijun.Wang on 2018/1/25.
 */

public interface WebViewController {
    Context getContext();
    Activity getActivity();
    TabController getTabController();
    WebViewFactory getWebViewFactory();
    void onSetWebView(Tab tab, WebView view);
    void onPageStarted(Tab tab, WebView webView, Bitmap favicon);
    void onPageFinished(Tab tab);
    void onProgressChanged(Tab tab);
    void onReceivedTitle(Tab tab,final String title);
    void onFavicon(Tab tab,WebView view,Bitmap icon);
}
