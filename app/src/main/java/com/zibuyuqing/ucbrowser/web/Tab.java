package com.zibuyuqing.ucbrowser.web;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.zibuyuqing.ucbrowser.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Xijun.Wang on 2018/1/25.
 */

public class Tab{
    private final static String TAG = "TAB";
    private static final int INITIAL_PROGRESS = 5;
    private static final int MSG_CAPTURE = 42;
    private boolean mWillBeClosed = false;
    private static Bitmap sDefaultFavicon; //默认网站图标

    private long mId = -1;

    // WebView controller
    WebViewController mWebViewController;

    //
    Context mContext;

    // Main WebView wrapper
    private View mContainer;
    // Main WebView
    private WebView mMainView;
    // Subwindow container
    private View mSubViewContainer;
    // Subwindow WebView
    private WebView mSubView;
    // Saved bundle for when we are running low on memory. It contains the
    // information needed to restore the WebView if the user goes back to the
    // tab.
    private Bundle mSavedState;

    // If true, the tab is in page loading state (after onPageStarted,
    // before onPageFinsihed)
    private boolean mInPageLoad;

    // The last reported progress of the current page
    private int mPageLoadProgress;

    // The time the load started, used to find load page time
    private long mLoadStartTime;

    private int mCaptureWidth;
    private int mCaptureHeight;

    private Bitmap mCapture;
    private Handler mHandler;
    private boolean mUpdateThumbnail;

    public String mSavePageTitle;
    public String mSavePageUrl;
    // save page
    HashMap<Integer, Long> mSavePageJob;
    private PageState mCurrentState;

    // 用来存储页面信息
    static final String ID = "_id";
    static final String CURRENT_URL = "currentUrl";
    static final String CURRENT_TITLE = "currentTitle";
    private boolean mInForeground;
    private static Paint sAlphaPaint = new Paint();
    static {
        sAlphaPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        sAlphaPaint.setColor(Color.TRANSPARENT);
    }
    // 获取默认网页图标
    private static synchronized Bitmap getDefaultFavicon(Context context) {
        if (sDefaultFavicon == null) {
            sDefaultFavicon = BitmapFactory.decodeResource(
                    context.getResources(), R.drawable.ic_home);
        }
        return sDefaultFavicon;
    }

    // 构造WebViewClient
    private final WebViewClient mWebViewClient = new WebViewClient(){
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            mInPageLoad = true;
            mUpdateThumbnail = true;
            mPageLoadProgress = INITIAL_PROGRESS;
            mCurrentState = new PageState(url,favicon);
            mLoadStartTime = SystemClock.uptimeMillis();
            mWebViewController.onPageStarted(Tab.this,view,favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            syncCurrentState(view,url);
            if(url != null && url.equals(mSavePageUrl)){
                mCurrentState.mTitle = mSavePageTitle;
                mCurrentState.mUrl = mSavePageUrl;
            }
            mWebViewController.onPageFinished(Tab.this);
        }
    };

    // 构造 WebChromeClient
    private WebChromeClient mWebChromeClient = new WebChromeClient(){
        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
        }
    };
    public Tab(WebViewController webViewController,WebView view){
        this(webViewController,view,null);
    }
    public Tab(WebViewController webViewController,Bundle state){
        this(webViewController,null,state);
    }
    public Tab(WebViewController webViewController,WebView view,Bundle state){
        mSavePageJob = new HashMap<Integer, Long>();
        mWebViewController = webViewController;
        mContext = mWebViewController.getContext();
        mCurrentState = new PageState(mContext);
        mInPageLoad = false;
        mCaptureWidth = mContext.getResources().getDimensionPixelSize(
                R.dimen.tab_thumbnail_width);
        mCaptureHeight = mContext.getResources().getDimensionPixelSize(
                R.dimen.tab_thumbnail_height);
        updateShouldCaptureThumbnails();
        restoreState(state);
        if (getId() == -1) {
            mId = TabController.getNextId();
        }
        setWebView(view);
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case MSG_CAPTURE:
                        capture();
                        break;
                }
            }
        };
    }

    private void restoreState(Bundle state){
        mSavedState = state;
        if(mSavedState == null){
            return;
        }
        mId = state.getLong(ID);
        String url = state.getString(CURRENT_URL);
        String title = state.getString(CURRENT_TITLE);
        mCurrentState = new PageState(mContext,url,title);
    }
    public boolean shouldUpdateThumbnail() {
        return mUpdateThumbnail;
    }

    /**
     * This is used to get a new ID when the tab has been preloaded, before it is displayed and
     * added to TabControl. Preloaded tabs can be created before restoreInstanceState, leading
     * to overlapping IDs between the preloaded and restored tabs.
     */
    public void refreshIdAfterPreload() {
        mId = TabController.getNextId();
    }
    public void updateShouldCaptureThumbnails() {
        Log.e(TAG,"updateShouldCaptureThumbnails ;; + " + mWebViewController.shouldCaptureThumbnails());
        if (mWebViewController.shouldCaptureThumbnails()) {
            synchronized (Tab.this) {
                if (mCapture == null) {
                    mCapture = Bitmap.createBitmap(mCaptureWidth, mCaptureHeight,
                            Bitmap.Config.RGB_565);
                    mCapture.eraseColor(Color.WHITE);
                    /*
                    if (mInForeground) {
                        postCapture();
                    }
                    */
                }
            }
        } else {
            synchronized (Tab.this) {
                mCapture = null;
               //  deleteThumbnail();
            }
        }
    }

    public void setController(WebViewController ctl) {
        mWebViewController = ctl;
        updateShouldCaptureThumbnails();
    }
    public long getId() {
        return mId;
    }
    void setWebView(WebView w) {
        setWebView(w, true);
    }

    /**
     * Sets the WebView for this tab, correctly removing the old WebView from
     * the container view.
     */
    void setWebView(WebView w, boolean restore) {
        if (mMainView == w) {
            return;
        }
        mWebViewController.onSetWebView(this, w);

        if (mMainView != null) {
            mMainView.setPictureListener(null);
            if (w != null) {
                syncCurrentState(w, null);
            } else {
                mCurrentState = new PageState(mContext);
            }
        }
        // set the new one
        mMainView = w;
        // attach the WebViewClient, WebChromeClient and DownloadListener
        if (mMainView != null) {
            mMainView.setWebViewClient(mWebViewClient);
            mMainView.setWebChromeClient(mWebChromeClient);
            TabController tc = mWebViewController.getTabController();
            /*
            if (tc != null && tc.getOnThumbnailUpdatedListener() != null) {
                mMainView.setPictureListener(this);
            }
            */
            if (restore && (mSavedState != null)) {
                // restoreUserAgent();
                WebBackForwardList restoredState
                        = mMainView.restoreState(mSavedState);
                if (restoredState == null || restoredState.getSize() == 0) {
                    Log.w(TAG, "Failed to restore WebView state!");
                    loadUrl(mCurrentState.mOriginalUrl, null);
                }
                mSavedState = null;
            }
        }
    }

    /**
     * Destroy the tab's main WebView and subWindow if any
     */
    void destroy() {
        if (mMainView != null) {
            dismissSubWindow();
            // save the WebView to call destroy() after detach it from the tab
            WebView webView = mMainView;
            setWebView(null);
            webView.destroy();
        }
        /// M: add for save page @ {
        if (mSavePageJob == null) {
            return;
        }
        if (mSavePageJob.size() != 0) {
            // new CancelSavePageTask().execute();
        }
        /// @ }
    }
    /// @ }

    /**
     * Remove the tab from the parent
     */
    void removeFromTree() {
        // detach the children
        /*
        if (mChildren != null) {
            for(Tab t : mChildren) {
                t.setParent(null);
            }
        }
        // remove itself from the parent list
        if (mParent != null) {
            mParent.mChildren.remove(this);
        }
        */
        // deleteThumbnail();
    }
    /**
     * Dismiss the subWindow for the tab.
     */
    void dismissSubWindow() {
        if (mSubView != null) {
            mSubView.destroy();
            mSubView = null;
            mSubViewContainer = null;
        }
    }

    void resume() {
        if (mMainView != null) {
            setupHwAcceleration(mMainView);
            mMainView.onResume();
            if (mSubView != null) {
                mSubView.onResume();
            }
        }
    }
    void pause() {
        if (mMainView != null) {
            mMainView.onPause();
            if (mSubView != null) {
                mSubView.onPause();
            }
        }
    }
    void putInForeground() {
        if (mInForeground) {
            return;
        }
        mInForeground = true;
        resume();
    }
    void putInBackground() {
        if (!mInForeground) {
            return;
        }
        capture();
        mInForeground = false;
        pause();
        mMainView.setOnCreateContextMenuListener(null);
        if (mSubView != null) {
            mSubView.setOnCreateContextMenuListener(null);
        }
    }

    boolean inForeground() {
        return mInForeground;
    }

    /**
     * Return the main window of this tab. Note: if a tab is freed in the
     * background, this can return null. It is only guaranteed to be
     * non-null for the current tab.
     * @return The main WebView of this tab.
     */
    public WebView getWebView() {
        return mMainView;
    }

    void setViewContainer(View container) {
        mContainer = container;
    }
    public String getUrl() {
        return mCurrentState.mUrl;
    }

    public String getOriginalUrl() {
        if (mCurrentState.mOriginalUrl == null) {
            return getUrl();
        }
        return mCurrentState.mOriginalUrl;
    }
    /**
     * Get the title of this tab.
     */
    public String getTitle() {
        if (mCurrentState.mTitle == null && mInPageLoad) {
            return mContext.getString(R.string.title_bar_loading);
        }
        return mCurrentState.mTitle;
    }
    /**
     * Get the favicon of this tab.
     */
    public Bitmap getFavicon() {
        if (mCurrentState.mFavicon != null) {
            return mCurrentState.mFavicon;
        }
        return getDefaultFavicon(mContext);
    }

    public void clearTabData(){
        mWillBeClosed = true;
        mCurrentState.mUrl = "";
        mCurrentState.mOriginalUrl = "";
        mCurrentState.mTitle = "";
    }
    /**
     * @return TRUE if onPageStarted is called while onPageFinished is not
     *         called yet.
     */
    boolean inPageLoad() {
        return mInPageLoad;
    }
    /**
     * @return The Bundle with the tab's state if it can be saved, otherwise null
     */
    public Bundle saveState() {
        // If the WebView is null it means we ran low on memory and we already
        // stored the saved state in mSavedState.
        if (mMainView == null) {
            return mSavedState;
        }

        if (TextUtils.isEmpty(mCurrentState.mUrl)) {
            return null;
        }

        mSavedState = new Bundle();
        WebBackForwardList savedList = mMainView.saveState(mSavedState);
        if (savedList == null || savedList.getSize() == 0) {
            Log.w(TAG, "Failed to save back/forward list for "
                    + mCurrentState.mUrl);
        }

        mSavedState.putLong(ID, mId);
        mSavedState.putString(CURRENT_URL, mCurrentState.mUrl);
        mSavedState.putString(CURRENT_TITLE, mCurrentState.mTitle);
        return mSavedState;
    }
    public Bitmap getScreenshot() {
        synchronized (Tab.this) {
            return mCapture;
        }
    }
    public boolean isSnapshot() {
        return false;
    }

    private void setupHwAcceleration(View web) {
        if (web == null) return;
        // 这里需要用户自己设置
        if (true) {
            web.setLayerType(View.LAYER_TYPE_NONE, null);
        } else {
            web.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    private void syncCurrentState(WebView view,String url){
        if(mWillBeClosed){
            return;
        }
        mCurrentState.mUrl = view.getUrl();
        if(mCurrentState.mUrl == null){
            mCurrentState.mUrl = "";
        }
        mCurrentState.mOriginalUrl = view.getOriginalUrl();
        mCurrentState.mTitle = view.getTitle();
        mCurrentState.mFavicon = view.getFavicon();
    }
    public void loadUrl(String url, Map<String, String> headers) {
        if (mMainView != null) {
            mPageLoadProgress = INITIAL_PROGRESS;
            mInPageLoad = true;
            mCurrentState = new PageState(mContext, url, null);
            mWebViewController.onPageStarted(this, mMainView, null);
            try{
                mMainView.loadUrl(url, headers);
            }catch(SecurityException e){
                e.printStackTrace();
            }
        }
    }
    protected void capture() {
        if (mMainView == null || mCapture == null) return;
        if (mMainView.getMeasuredWidth() <= 0 || mMainView.getContentHeight() <= 0) {
            return;
        }
        Canvas c = new Canvas(mCapture);
        final int left = mMainView.getScrollX();
        final int top = mMainView.getScrollY();
        int state = c.save();
        c.translate(-left, -top);
        float scale = mCaptureWidth / (float) mMainView.getWidth();
        c.scale(scale, scale, left, top);
        mMainView.draw(c);
        c.restoreToCount(state);
        // manually anti-alias the edges for the tilt
        c.drawRect(0, 0, 1, mCapture.getHeight(), sAlphaPaint);
        c.drawRect(mCapture.getWidth() - 1, 0, mCapture.getWidth(),
                mCapture.getHeight(), sAlphaPaint);
        c.drawRect(0, 0, mCapture.getWidth(), 1, sAlphaPaint);
        c.drawRect(0, mCapture.getHeight() - 1, mCapture.getWidth(),
                mCapture.getHeight(), sAlphaPaint);
        c.setBitmap(null);
        mHandler.removeMessages(MSG_CAPTURE);
        TabController tc = mWebViewController.getTabController();
        if (tc != null) {
            TabController.OnThumbnailUpdatedListener updateListener
                    = tc.getOnThumbnailUpdatedListener();
            if (updateListener != null) {
                updateListener.onThumbnailUpdated(this);
            }
        }
    }
    public boolean canGoBack() {
        return mMainView != null ? mMainView.canGoBack() : false;
    }
    public boolean canGoForward() {
        return mMainView != null ? mMainView.canGoForward() : false;
    }

    public void goBack() {
        if (mMainView != null) {
            mMainView.goBack();
        }
    }

    public void goForward() {
        if (mMainView != null) {
            mMainView.goForward();
        }
    }

    public static class PageState{
        String mUrl;
        String mOriginalUrl;
        String mTitle;
        Bitmap mFavicon;
        PageState(Context context){
            this("",getDefaultFavicon(context));
        }
        PageState(String url,Bitmap favicon){
            this(url,"",favicon);
        }
        PageState(Context context,String url,String title){
            this(url,title,getDefaultFavicon(context));
        }
        PageState(String url,String title,Bitmap favicon){
            mUrl = mOriginalUrl = url;
            mTitle = title;
            mFavicon = favicon;
        }
    }
}
