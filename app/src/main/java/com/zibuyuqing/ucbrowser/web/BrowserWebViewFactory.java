package com.zibuyuqing.ucbrowser.web;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.zibuyuqing.ucbrowser.utils.Constants;

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
        WebSettings webSettings = w.getSettings();
        //设置支持缩放
        webSettings.setBuiltInZoomControls(true);
        //开启 database storage API 功能
        webSettings.setDatabaseEnabled(true);
        // 开启 DOM storage API 功能
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptEnabled(true);
        //设置渲染的优先级
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);

        String cacheDirPath = mContext.getFilesDir().getAbsolutePath() + Constants.APP_CACHE_DIRNAME;
        //设置  Application Caches 缓存目录
        webSettings.setAppCachePath(cacheDirPath);
        //开启 Application Caches 功能
        webSettings.setAppCacheEnabled(true);

        //设置可以访问文件
        webSettings.setAllowFileAccess(true);
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
