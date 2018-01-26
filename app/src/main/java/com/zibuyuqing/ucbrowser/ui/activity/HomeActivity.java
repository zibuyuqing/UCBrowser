package com.zibuyuqing.ucbrowser.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.zibuyuqing.ucbrowser.web.UiController;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class HomeActivity extends BaseActivity implements View.OnClickListener,
        UCStackView.OnChildDismissedListener, FavoriteWorkspace.OnItemClickListener,UiController {
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

    private boolean mTabsManagerUIShown = false;
    private boolean mFolderOpened = false;
    private FavoriteFolder mOpenedFolder;

    TabController mTabController;
    private Tab mActiveTab;
    private WebViewFactory mFactory;
    private boolean mIsAnimating = false;
    private boolean mIsInMain = true;
    @Override
    protected int layoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void init() {
        initWindow();
        initViews();
        mTabController = new TabController(this, this);
        mFactory = new BrowserWebViewFactory(this);
        // 先建立一个tab标记主页
        if (mTabController.getTabCount() <= 0) {
            addTab(false);
        }
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

        mTabAdapter = new UCTabAdapter(this, this);
        mUCStackView.setAdapter(mTabAdapter);
        mUCStackView.setOnChildDismissedListener(this);
        mDragController = new DragController(this, mDragLayer);
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

    private void bindFavoriteItems() {
        ArrayList<ItemInfo> infos = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            infos.add(buildFavoriteShortcutItem());
        }
        infos.addAll(bindFavoriteFolders());
        mWorkspace.bindItems(infos);
    }

    private ArrayList<ItemInfo> bindFavoriteFolders() {
        ArrayList<ItemInfo> folders = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            folders.add(buildFavoriteFolderItem());
        }
        return folders;
    }

    private int mCurrentCount = 0;

    private FavoriteShortcutInfo buildFavoriteShortcutItem() {
        FavoriteShortcutInfo shortcutInfo = new FavoriteShortcutInfo();
        shortcutInfo.setIcon(ViewUtil.drawableToBitmap(getDrawable(R.drawable.ic_favorite_baidu)));
        shortcutInfo.cellX = shortcutInfo.cellY = shortcutInfo.rank = -1;
        mCurrentCount++;
        shortcutInfo.setDescription("百度" + mCurrentCount);
        shortcutInfo.setUrl("http://baidu.com");
        return shortcutInfo;
    }

    private FavoriteFolderInfo buildFavoriteFolderItem() {
        FavoriteFolderInfo info = new FavoriteFolderInfo();
        for (int i = 0; i < 10; i++) {
            info.addItem(buildFavoriteShortcutItem());
        }
        mCurrentCount++;
        info.setDescription("文件夹" + mCurrentCount);
        info.setIcon(ViewUtil.drawableToBitmap(getDrawable(R.drawable.folder_icon_bg)));
        return info;
    }

    public boolean isAnimating() {
        return mUCRootView.isAnimating() || mUCStackView.isAnimating() || mIsAnimating;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.e(TAG, "dispatchTouchEvent ::isAnimating =: " + isAnimating());
        if (isAnimating()) {
            return false;
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 重写back键逻辑
     */
    @Override
    public void onBackPressed() {
        if (mOpenedFolder != null) {
            mOpenedFolder.close();
            mOpenedFolder = null;
            mFolderOpened = false;
            return;
        }
        if (mTabsManagerUIShown) {
            hideTabs(true);
            return;
        }
        if (mActiveTab != null) {
            if (mActiveTab.canGoBack()) {
                mActiveTab.goBack();
                return;
            } else {
                if(!mIsInMain) {
                    mActiveTab.clearTabData();
                    switchToMain();
                    return;
                }
            }
        }
        if (mUCRootView.getMode() == UCRootView.NEWS_MODE) {
            mUCRootView.back2Normal();
        } else if (mUCRootView.getMode() == UCRootView.FAVORITE_MODE) {
            mUCRootView.back2Home();
        } else {
            super.onBackPressed();
        }
    }


    /**
     * 进入页面管理界面，用动画改变选择页（可以理解为一张截图）的Y和scale
     */
    public void showTabs() {
        if (mUCStackView.isAnimating()) {
            return;
        }
        mActiveTab.capture();
        mTabAdapter.updateData(mTabController.getTabs());
        mTabsManagerLayout.bringToFront();
        mTabsManagerLayout.setVisibility(View.VISIBLE);
        getWindow().setStatusBarColor(getResources().getColor(R.color.pureBlack, null));
        mUCStackView.animateShow(mTabController.getCurrentPosition(), mContentWrapper, mTabsManagerLayout, true, new Runnable() {
            @Override
            public void run() {
                mContentWrapper.setVisibility(View.GONE);
                mTabsManagerUIShown = true;
            }
        });
    }
    public void animateShowFromBottomToTop(View view,final Runnable onCompleteRunnable){
        mContentWrapper.setVisibility(View.VISIBLE);
        ObjectAnimator animator = ObjectAnimator.ofFloat(
                view,
                "translationY",
                ViewUtil.getScreenSize(this).y,
                0);
        animator.setDuration(500);
        animator.start();
        mIsAnimating = true;
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mIsAnimating = false;
                if(onCompleteRunnable != null){
                    onCompleteRunnable.run();
                }
            }
        });
    }
    /**
     * @param animate 是否有动画，有动画时即UCRootView从下往上移
     */
    private void addTab(boolean animate) {
        Log.e(TAG,"addTab = ;-----------");
        if (animate) {
            switchToMain();
            mContentWrapper.bringToFront();
            animateShowFromBottomToTop(mContentWrapper,new Runnable() {
                @Override
                public void run() {
                    hideTabs(false); // 把页面管理页隐藏
                    mUCBottomBar.bringToFront();
                    initWindow(); // 更改状态栏颜色
                }
            });
        }
        Tab tab = buildTab();
        mActiveTab = tab;
        mTabController.setActiveTab(mActiveTab);
    }

    private void removeTab(int index) {
        mTabController.removeTab(index);
    }

    /**
     * 新建一页并初始化页面，这里的ICON为网站图标，我没资源先用UC代替，title为网站title，key为定义的页面ID
     * previewBitmap 为页面的截图
     * 当新建一页时，让我们的选择页置为新建的这一页，然后下次进入页面管理页时初始化进度
     *
     * @return
     */
    private Tab buildTab() {
        Log.e(TAG,"buildTab = ;-----------");
        return mTabController.createNewTab();
    }

    public void hideTabs(boolean animated) {
        if (mUCStackView.isAnimating()) {
            return;
        }
        if (animated) {
            mUCStackView.animateShow(mTabController.getCurrentPosition(), mContentWrapper, mTabsManagerLayout, false, new Runnable() {
                @Override
                public void run() {
                    mTabsManagerLayout.setVisibility(View.GONE);
                    mContentWrapper.setVisibility(View.VISIBLE);
                    initWindow();
                }
            });
        } else {
            mTabsManagerLayout.setVisibility(View.GONE);
            mUCBottomBar.setVisibility(View.VISIBLE);
            initWindow();
        }
        mTabsManagerUIShown = false;
    }

    private void bindNewsPage() {
        List<BaseFragment> fragments = new ArrayList<>(Constants.NEWS_TITLE.length);
        for (String title : Constants.NEWS_TITLE) {
            NewsListFragment fragment = new NewsListFragment();
            Bundle bundle = new Bundle();
            bundle.putString("title", title);
            fragment.setArguments(bundle);
            fragments.add(fragment);
        }
        mNewsPageAdapter = new NewsPageAdapter(getSupportFragmentManager(), fragments);
        mNewsPager.setAdapter(mNewsPageAdapter);
        mNewsTab.setupWithViewPager(mNewsPager);
    }

    private void initWindow() {
        getWindow().setStatusBarColor(getResources().getColor(R.color.themeBlue, null));
    }

    public void openFolder(FavoriteFolderIcon icon) {
        if (!mFolderOpened) {
            FavoriteFolder folder = FavoriteFolder.fromXml(this);
            folder.setup(mDragLayer);
            folder.open(icon, true, null);
            mFolderOpened = true;
            mOpenedFolder = folder;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ivHome: {
                Log.e(TAG, " onClick mode = :" + mUCRootView.getMode());
                if (mUCRootView.getMode() == UCRootView.NEWS_MODE) {
                    mUCRootView.back2Normal();
                } else {
                    mUCRootView.back2Home();
                }
                break;
            }
            case R.id.flWindowsNum: {
                if (mUCRootView.getMode() != UCRootView.NEWS_MODE)
                    showTabs();
                break;
            }
            case R.id.tvBack: {
                hideTabs(true);
                break;
            }
            case R.id.ivAddPager: {
                addTab(true);
                break;
            }
            case R.id.tvBaidu: {
                load(Constants.WEBSITES.get("baidu"));
                break;
            }
        }
    }

    private void load(String url) {
        if (mActiveTab != null) {
            mActiveTab.loadUrl(url, null);
            switchToTab();
        }
    }
    private void switchToTab(){
        if(mUCRootView.getParent() != null) {
            mContentWrapper.removeView(mUCRootView);
        }
        WebView view = mActiveTab.getWebView();
        Log.e(TAG,"switchToTab ----------" + mUCRootView.getParent() +",view.getParent()= ;" + view.getParent() +",view =:" + view.getTitle());
        if(view != null && view.getParent() == null) {
            mContentWrapper.addView(view);
        }
        mIsInMain = false;
    }
    private void switchToMain(){
        Log.e(TAG,"switchToMain :: mUCRootView.getParent() =:" + mUCRootView.getParent());
        if(mUCRootView.getParent() == null){
            mContentWrapper.addView(mUCRootView);
        }
        mUCRootView.bringToFront();
        WebView view = mActiveTab.getWebView();
        if(view != null) {
            mContentWrapper.removeView(view);
        }
        mIsInMain = true;
    }

    private void onTabClosed(int index) {
        removeTab(index);
        if (mUCStackView.getChildCount() <= 0) {
            addTab(true);
        }
    }

    @Override
    public void onChildDismissed(int index) {
        Log.e(TAG, "onChildDismissed :: index =: " + index);
        onTabClosed(index);
    }

    @Override
    public void onItemClick(View view) {
        if (view instanceof FavoriteFolderIcon) {
            openFolder((FavoriteFolderIcon) view);
        } else if (view instanceof FavoriteShortcut) {
            ItemInfo info = (ItemInfo) view.getTag();
            Toast.makeText(HomeActivity.this, info.description, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public Activity getActivity() {
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

    }

    @Override
    public void onPageFinished(Tab tab) {
        Toast.makeText(HomeActivity.this, "加载完毕", Toast.LENGTH_SHORT).show();
        tab.shouldUpdateThumbnail(true);
        mTabAdapter.notifyDataSetChanged();
    }

    @Override
    public void onProgressChanged(Tab tab) {
        Log.e(TAG, "onProgressChanged --------------");
    }

    @Override
    public void onReceivedTitle(Tab tab, String title) {
        Log.e(TAG, "onReceivedTitle --------------title =:" + title);

    }

    @Override
    public void onFavicon(Tab tab, WebView view, Bitmap icon) {

    }

    @Override
    public void selectTab(Tab tab) {
        if (mUCStackView.isAnimating()) {
            return;
        }
        int index = mTabController.getTabPosition(tab);
        mActiveTab = tab;
        mTabController.setActiveTab(mActiveTab);
        if(mActiveTab.checkUrlNotNull()) {
            switchToTab();
        } else {
            switchToMain();
        }
        mUCStackView.selectTab(index, new Runnable() {
            @Override
            public void run() {
                Log.e(TAG,"onSelect ----- mActiveTab.checkUrlNotNull() =:" + mActiveTab.checkUrlNotNull() +"mActiveTab "+ mActiveTab.getTitle() +"," + mActiveTab.getUrl());
                mContentWrapper.setVisibility(View.VISIBLE);
                mTabsManagerLayout.setVisibility(View.GONE);
                initWindow();
            }
        });
        Log.e(TAG, "onSelect :: key =:" + tab.getId());
    }

    @Override
    public void closeTab(Tab tab) {
        mUCStackView.closeTab(mTabController.getTabPosition(tab));
    }

    @Override
    public void onTabCountChanged() {
        mTabNum.setText("" + mTabController.getTabCount()); // 更新页面数量
    }

    @Override
    public void onTabDataChanged(Tab tab) {
        mTabAdapter.notifyDataSetChanged();
    }
}
