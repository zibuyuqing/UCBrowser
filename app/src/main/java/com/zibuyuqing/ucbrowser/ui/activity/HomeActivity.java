package com.zibuyuqing.ucbrowser.ui.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zibuyuqing.common.utils.ViewUtil;
import com.zibuyuqing.stackview.widget.UCStackView;
import com.zibuyuqing.ucbrowser.R;
import com.zibuyuqing.ucbrowser.adapter.NewsPageAdapter;
import com.zibuyuqing.ucbrowser.adapter.UCTabAdapter;
import com.zibuyuqing.ucbrowser.base.BaseActivity;
import com.zibuyuqing.ucbrowser.base.BaseLayout;
import com.zibuyuqing.ucbrowser.base.BaseFragment;
import com.zibuyuqing.ucbrowser.model.bean.favorite.FavoriteFolderInfo;
import com.zibuyuqing.ucbrowser.model.bean.favorite.FavoriteShortcutInfo;
import com.zibuyuqing.ucbrowser.model.bean.favorite.ItemInfo;
import com.zibuyuqing.ucbrowser.ui.fragment.NewsListFragment;
import com.zibuyuqing.ucbrowser.utils.Constants;
import com.zibuyuqing.ucbrowser.web.BrowserWebViewFactory;
import com.zibuyuqing.ucbrowser.web.Tab;
import com.zibuyuqing.ucbrowser.web.TabController;
import com.zibuyuqing.ucbrowser.web.WebViewController;
import com.zibuyuqing.ucbrowser.web.WebViewFactory;
import com.zibuyuqing.ucbrowser.widget.favorite.DragController;
import com.zibuyuqing.ucbrowser.widget.favorite.DragLayer;
import com.zibuyuqing.ucbrowser.widget.favorite.FavoriteFolder;
import com.zibuyuqing.ucbrowser.widget.favorite.FavoriteFolderIcon;
import com.zibuyuqing.ucbrowser.widget.favorite.FavoriteShortcut;
import com.zibuyuqing.ucbrowser.widget.favorite.FavoriteWorkspace;
import com.zibuyuqing.ucbrowser.widget.layout.BezierLayout;
import com.zibuyuqing.ucbrowser.widget.layout.UCBottomBar;
import com.zibuyuqing.ucbrowser.widget.layout.UCHeadLayout;
import com.zibuyuqing.ucbrowser.widget.layout.UCNewsLayout;
import com.zibuyuqing.ucbrowser.widget.root.UCRootView;
import com.zibuyuqing.ucbrowser.widget.stackview.UCPagerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class HomeActivity extends BaseActivity implements View.OnClickListener, UCPagerView.CallBack,
        UCStackView.OnChildDismissedListener, FavoriteWorkspace.OnItemClickListener ,WebViewController{
    private static final String TAG = "MainActivity";


    // 头部
    @BindView(R.id.llUCHeadLayout)
    UCHeadLayout mUCHeadLayout;

    //新闻列表
    @BindView(R.id.llUCNewsListLayout)
    UCNewsLayout mUCNewsLayout;

    @BindView(R.id.tlUCNewsTab)
    TabLayout mNewsTab;

    @BindView(R.id.tvPagerNum)
    TextView mTabNum;

    @BindView(R.id.flPagersManager)
    FrameLayout mTabsManagerLayout;

    @BindView(R.id.ucStackView)
    UCStackView mUCStackView;

    @BindView(R.id.dragLayer)
    DragLayer mDragLayer;

    @BindView(R.id.favoriteWorkspace)
    FavoriteWorkspace mWorkspace;

    @BindView(R.id.ucRootView)
    UCRootView mUCRootView;

    // 收藏页
    @BindView(R.id.ucFavorite)
    BaseLayout mUCFavorite;

    // 底部菜单
    @BindView(R.id.llUCBottomBar)
    UCBottomBar mUCBottomBar;

    @BindView(R.id.llBezierLayout)
    BezierLayout mBezierLayout;

    @BindView(R.id.vpUCNewsPager)
    ViewPager mNewsPager;

    // 顶部搜索条
    @BindView(R.id.llTopSearchBar)
    BaseLayout mTopSearchBar;

    @BindView(R.id.homeContentWrapper)
    FrameLayout mContentWrapper;

    NewsPageAdapter mNewsPageAdapter;


    UCTabAdapter mTabAdapter;



    DragController mDragController;


    private List<Tab> mTabs = new ArrayList<>();
    private List<Long> mTabIds = new ArrayList<>();
    private int mSelectPager = 0;
    private boolean mTabsManagerUIShown = false;
    private boolean mFolderOpened = false;
    private FavoriteFolder mOpenedFolder;

    TabController mTabController;
    private Tab mActiveTab;
    private WebViewFactory mFactory;
    @Override
    protected int layoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void init() {
        initWindow();
        initViews();
        mTabController = new TabController(this,this);
        mFactory = new BrowserWebViewFactory(this);
    }

    private void initViews() {
        mUCHeadLayout.setTransYEnable(true);
        mUCHeadLayout.setTransXEnable(true);
        mUCHeadLayout.initTranslationY(0, -100);
        mUCHeadLayout.initTranslationX(0, -ViewUtil.getScreenSize(this).x);

        //可移动
        mTopSearchBar.setTransYEnable(true);

        // 这方法是在view layout 之后获取大小，避免获取的大小全是 0
        mTopSearchBar.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mTopSearchBar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        // 初始化移动参数
                        mTopSearchBar.initTranslationY(-mTopSearchBar.getHeight(), 0);
                    }
                }
        );


        mUCNewsLayout.setTransYEnable(true);
        mUCNewsLayout.setTransXEnable(true);
        mUCNewsLayout.initTranslationX(0, -ViewUtil.getScreenSize(this).x);
        mUCNewsLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mUCNewsLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        mUCNewsLayout.initTranslationY(0, -mUCHeadLayout.getHeight() + mTopSearchBar.getHeight());
                    }
                }
        );

        mTabAdapter = new UCTabAdapter(this,this);
        mUCStackView.setAdapter(mTabAdapter);
        mUCStackView.setOnChildDismissedListener(this);
        mDragController = new DragController(this,mDragLayer);
        mDragLayer.setup(mDragController);
        mWorkspace.setup(mDragLayer);
        mWorkspace.setOnItemClickListener(this);

        mUCFavorite.setTransXEnable(true);
        mUCFavorite.initTranslationX(ViewUtil.getScreenSize(this).x, 0);

        mUCRootView.attachScrollStateListener(mTopSearchBar);
        mUCRootView.attachScrollStateListener(mUCHeadLayout);
        mUCRootView.attachScrollStateListener(mUCNewsLayout);
        mUCRootView.attachScrollStateListener(mUCBottomBar);
        mUCRootView.attachScrollStateListener(mBezierLayout);
        mUCRootView.attachScrollStateListener(mUCFavorite);
        mUCRootView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mUCNewsLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        mUCRootView.setFinalDistance(
                                ViewUtil.getScreenSize(HomeActivity.this).x,
                                mUCHeadLayout.getMeasuredHeight()
                        );
                    }
                }
        );
        mDragController.addDropTarget(mWorkspace);
        mDragController.addDragListener(mUCRootView);

        findViewById(R.id.flWindowsNum).setOnClickListener(this);
        findViewById(R.id.tvBack).setOnClickListener(this);
        findViewById(R.id.ivHome).setOnClickListener(this);
        findViewById(R.id.ivAddPager).setOnClickListener(this);

        findViewById(R.id.tvBaidu).setOnClickListener(this);

        bindNewsPage();
        bindFavoriteItems();
    }
    private void bindFavoriteItems(){
        ArrayList<ItemInfo> infos = new ArrayList<>();
        for(int i = 0; i < 10; i ++){
            infos.add(buildFavoriteShortcutItem());
        }
        infos.addAll(bindFavoriteFolders());
        mWorkspace.bindItems(infos);
    }
    private ArrayList<ItemInfo> bindFavoriteFolders(){
        ArrayList<ItemInfo> folders = new ArrayList<>();
        for(int i = 0; i < 3; i ++){
            folders.add(buildFavoriteFolderItem());
        }
        return folders;
    }
    private int mCurrentCount = 0;
    private FavoriteShortcutInfo buildFavoriteShortcutItem(){
        FavoriteShortcutInfo shortcutInfo = new FavoriteShortcutInfo();
        shortcutInfo.setIcon(ViewUtil.drawableToBitmap(getDrawable(R.drawable.ic_favorite_baidu)));
        shortcutInfo.cellX = shortcutInfo.cellY = shortcutInfo.rank = -1;
        mCurrentCount ++ ;
        shortcutInfo.setDescription("百度" + mCurrentCount);
        shortcutInfo.setUrl("http://baidu.com");
        return shortcutInfo;
    }
    private FavoriteFolderInfo buildFavoriteFolderItem(){
        FavoriteFolderInfo info = new FavoriteFolderInfo();
        for(int i = 0; i < 10; i ++){
            info.addItem(buildFavoriteShortcutItem());
        }
        mCurrentCount ++ ;
        info.setDescription("文件夹" + mCurrentCount);
        info.setIcon(ViewUtil.drawableToBitmap(getDrawable(R.drawable.folder_icon_bg)));
        return info;
    }
    public boolean isAnimating(){
        return mUCRootView.isAnimating() || mUCStackView.isAnimating();
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.e(TAG,"dispatchTouchEvent ::isAnimating =: " +isAnimating());
        if(isAnimating()){
            return false;
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 重写back键逻辑
     */
    @Override
    public void onBackPressed() {
        if(mOpenedFolder != null){
            mOpenedFolder.close();
            mOpenedFolder = null;
            mFolderOpened = false;
            return;
        }
        if(mActiveTab != null){
            if(mActiveTab.canGoBack()){
                mActiveTab.goBack();
                return;
            } else {
                WebView webView = mActiveTab.getWebView();
                if(webView != null && webView.getParent() != null) {
                    mContentWrapper.removeView(webView);
                    mContentWrapper.addView(mUCRootView);
                    return;
                }
            }
        }
        if(mUCRootView.getMode() == UCRootView.NEWS_MODE){
            mUCRootView.back2Normal();
        } else if(mUCRootView.getMode() == UCRootView.FAVORITE_MODE){
            mUCRootView.back2Home();
        } else if(mTabsManagerUIShown){
            hidePagers(true);
        } else {
            super.onBackPressed();
        }
    }
    /**
     * 更新视图
     */
    private void refreshPages(){
        int count = mTabs.size();
        int olderSelect = mSelectPager;
        if(count > 0){
            mTabs.clear();
            for(int i = 0; i < count; i++){
                mTabs.add(buildTab());
            }
        }
        mSelectPager = olderSelect;
    }
    /**
     * 进入页面管理界面，用动画改变选择页（可以理解为一张截图）的Y和scale
     */
    public void showTabs(){
        if(mUCStackView.isAnimating()){
            return;
        }
        Log.e(TAG,"showTabs :: mTabs.size() =:" + mTabs.size());
        if(mTabs.size() <= 0){
            addTab(false);
        }
        mTabsManagerLayout.setVisibility(View.VISIBLE);
        mTabsManagerLayout.bringToFront();
        // refreshPages();
        mTabAdapter.updateData(mTabs);
        getWindow().setStatusBarColor(getResources().getColor(R.color.pureBlack, null));
        mUCStackView.animateShow(mSelectPager, mUCRootView, mTabsManagerLayout,true, new Runnable() {
            @Override
            public void run() {
                mUCRootView.setVisibility(View.GONE);
                mTabsManagerUIShown = true;
            }
        });
    }

    /**
     *
     * @param animate 是否有动画，有动画时即UCRootView从下往上移
     */
    private void addTab(boolean animate){
        mUCRootView.setVisibility(View.VISIBLE);
        if(animate) {
           mUCRootView.animateShowFromBottomToTop(new Runnable() {
               @Override
               public void run() {
                   Tab tab = buildTab();
                   mTabs.add(tab);
                   mTabIds.add(tab.getId());
                   hidePagers(false); // 把页面管理页隐藏
                   mTabNum.setText("" + mTabs.size()); // 更新页面数量
                   mActiveTab = tab;
                   initWindow(); // 更改状态栏颜色
               }
           });
        } else {
            Tab tab = buildTab();
            mTabs.add(tab);
            mActiveTab = tab;
            mTabIds.add(tab.getId());
        }
    }
    private void removeUCPager(int index){
        if(mTabs.size() <=0 || index < 0 || index >= mTabs.size()){
            return;
        }
        mTabs.remove(index);
        mTabIds.remove(index);
    }

    /**
     * 新建一页并初始化页面，这里的ICON为网站图标，我没资源先用UC代替，title为网站title，key为定义的页面ID
     * previewBitmap 为页面的截图
     * 当新建一页时，让我们的选择页置为新建的这一页，然后下次进入页面管理页时初始化进度
     * @return
     */
    private Tab buildTab(){
        Tab tab = mTabController.createNewTab();
        return tab;
    }

    public void hidePagers(boolean animated){
        if(mUCStackView.isAnimating()){
            return;
        }
        int pagerNum = mTabs.size();
        if(animated){
            mUCStackView.animateShow(mSelectPager, mUCRootView, mTabsManagerLayout,false, new Runnable() {
                @Override
                public void run() {
                    mTabsManagerLayout.setVisibility(View.GONE);
                    mUCRootView.setVisibility(View.VISIBLE);
                    initWindow();
                }
            });
        } else {
            mTabsManagerLayout.setVisibility(View.GONE);
            mUCBottomBar.setVisibility(View.VISIBLE);
            initWindow();
        }
        mTabsManagerUIShown = false;
        mTabNum.setText("" + pagerNum);
    }

    private void bindNewsPage() {
        List<BaseFragment> fragments = new ArrayList<>(Constants.NEWS_TITLE.length);
        for(String title : Constants.NEWS_TITLE){
            NewsListFragment fragment = new NewsListFragment();
            Bundle bundle = new Bundle();
            bundle.putString("title",title);
            fragment.setArguments(bundle);
            fragments.add(fragment);
        }
        mNewsPageAdapter = new NewsPageAdapter(getSupportFragmentManager(),fragments);
        mNewsPager.setAdapter(mNewsPageAdapter);
        mNewsTab.setupWithViewPager(mNewsPager);
    }

    private void initWindow() {
        getWindow().setStatusBarColor(getResources().getColor(R.color.themeBlue, null));
    }
    public void openFolder(FavoriteFolderIcon icon){
        if(!mFolderOpened) {
            FavoriteFolder folder = FavoriteFolder.fromXml(this);
            folder.setup(mDragLayer);
            folder.open(icon, true, null);
            mFolderOpened = true;
            mOpenedFolder = folder;
        }
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ivHome: {
                Log.e(TAG," onClick mode = :" + mUCRootView.getMode());
                if(mUCRootView.getMode() == UCRootView.NEWS_MODE){
                    mUCRootView.back2Normal();
                } else {
                    mUCRootView.back2Home();
                }
                break;
            }
            case R.id.flWindowsNum:{
                if(mUCRootView.getMode() != UCRootView.NEWS_MODE)
                    showTabs();
                break;
            }
            case R.id.tvBack:{
                if(mSelectPager < 0){
                    return;
                }
                hidePagers(true);
                break;
            }
            case R.id.ivAddPager:{
                addTab(true);
                break;
            }
            case R.id.tvBaidu: {
                load(Constants.WEBSITES.get("baidu"));
                break;
            }
        }
    }
    private void load(String url){
        if(mActiveTab != null) {
            mContentWrapper.removeView(mUCRootView);
            mActiveTab.loadUrl(url, null);
            WebView webView = mActiveTab.getWebView();
            if(webView == null){
                return;
            }
            mContentWrapper.addView(webView);
        }
    }
    @Override
    public void onSelect(long key) {
        if(mUCStackView.isAnimating()){
            return;
        }
        int index = mTabIds.indexOf(key);
        mSelectPager = index;
        mUCStackView.selectPager(index, new Runnable() {
            @Override
            public void run() {
                mUCRootView.setVisibility(View.VISIBLE);
                mTabsManagerLayout.setVisibility(View.GONE);
                initWindow();
            }
        });
        Log.e(TAG,"onSelect :: key =:" + key);
    }

    @Override
    public void onClose(long key) {
        Log.e(TAG,"onClose :: key = :" + key);
        if(!mTabIds.contains(key) || mUCStackView.isAnimating()){
            return;
        }
        Log.e(TAG,"onClose ::  start mSelectPager =:" + mSelectPager +",mUCStackView.getChildCount() =:" +mUCStackView.getChildCount() +",key =:" + key);
        int index = mTabIds.indexOf(key);
        mUCStackView.closePager(index);
        Log.e(TAG,"onClose ::  end mSelectPager =:" + mSelectPager +",mUCStackView.getChildCount() =:" +mUCStackView.getChildCount());
    }
    private void onUCPagerClosed(int index){
        if(index <= mSelectPager){
            if(mSelectPager >= 1){
                mSelectPager --;
            }
        }
        Log.e(TAG,"onUCPagerClosed ---- mSelectPager =:" + mSelectPager);
        if(mUCStackView.getChildCount() <= 0){
            addTab(true);
        }
        removeUCPager(index);
    }

    @Override
    public void onChildDismissed(int index) {
        Log.e(TAG,"onChildDismissed :: index =: " + index);
        onUCPagerClosed(index);
    }

    @Override
    public void onItemClick(View view) {
        if(view instanceof FavoriteFolderIcon){
            openFolder((FavoriteFolderIcon) view);
        } else if(view instanceof FavoriteShortcut){
            ItemInfo info = (ItemInfo) view.getTag();
            Toast.makeText(HomeActivity.this,info.description,Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public TabController getTabController() {
        return mTabController;
    }

    @Override
    public WebViewFactory getWebViewFactory() {
        return mFactory;
    }

    @Override
    public void onSetWebView(Tab tab, WebView view) {

    }

    @Override
    public void onPageStarted(Tab tab, WebView webView, Bitmap favicon) {
        tab.shouldUpdateThumbnail();
    }

    @Override
    public void onPageFinished(Tab tab) {
        Toast.makeText(HomeActivity.this,"加载完毕",Toast.LENGTH_SHORT).show();
        tab.shouldUpdateThumbnail();
    }

    @Override
    public void onProgressChanged(Tab tab) {
        Log.e(TAG,"onProgressChanged --------------");
    }

    @Override
    public void onReceivedTitle(Tab tab, String title) {
        Log.e(TAG,"onReceivedTitle --------------title =:" + title);
    }

    @Override
    public void onFavicon(Tab tab, WebView view, Bitmap icon) {

    }

    @Override
    public Tab openTab(String url, boolean setActive, boolean useCurrent) {
        return null;
    }

    @Override
    public Tab openTab(String url, Tab parent, boolean setActive, boolean useCurrent) {
        return null;
    }

    @Override
    public boolean switchToTab(Tab tab) {
        return false;
    }

    @Override
    public void closeTab(Tab tab) {

    }

    @Override
    public boolean shouldCaptureThumbnails() {
        return false;
    }

    @Override
    public void setActiveTab(Tab tab) {

    }
}
