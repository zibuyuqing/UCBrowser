package com.zibuyuqing.ucbrowser.web;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * Created by Xijun.Wang on 2018/1/25.
 */

public class BrowserWebViewFactory implements WebViewFactory {
    private final Context mContext;
    public BrowserWebViewFactory(Context context) {
        mContext = context;
    }
    private WebView instantiateWebView(AttributeSet attrs, int defStyle) {
        return new WebView(mContext, attrs, defStyle);
    }
    @Override
    public WebView createWebView() {
        WebView w = instantiateWebView(null, android.R.attr.webViewStyle);
        initWebViewSettings(w);
        return w;
    }

    protected void initWebViewSettings(WebView w) {
        w.setScrollbarFadingEnabled(true);
        w.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        // Enable the built-in zoom
        WebSettings settings = w.getSettings();
        settings.setBuiltInZoomControls(true);
        settings.setDomStorageEnabled(true);
        settings.setJavaScriptEnabled(true);
        /// M: Add to disable overscroll mode
        w.setOverScrollMode(View.OVER_SCROLL_NEVER);
        final PackageManager pm = mContext.getPackageManager();
        boolean supportsMultiTouch =
                pm.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH)
                        || pm.hasSystemFeature(PackageManager.FEATURE_FAKETOUCH_MULTITOUCH_DISTINCT);
        w.getSettings().setDisplayZoomControls(!supportsMultiTouch);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptThirdPartyCookies(w, cookieManager.acceptCookie());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            // Remote Web Debugging is always enabled, where available.
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }
}
