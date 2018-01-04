package com.zibuyuqing.ucbrowser;

import android.content.pm.ShortcutInfo;
import android.graphics.Bitmap;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zibuyuqing.common.utils.ViewUtil;
import com.zibuyuqing.stackview.widget.UCStackView;
import com.zibuyuqing.ucbrowser.adapter.NewsPageAdapter;
import com.zibuyuqing.ucbrowser.adapter.UCPagerAdapter;
import com.zibuyuqing.ucbrowser.base.BaseLayout;
import com.zibuyuqing.ucbrowser.base.BaseNewsFragment;
import com.zibuyuqing.ucbrowser.model.bean.favorite.FavoriteFolderInfo;
import com.zibuyuqing.ucbrowser.model.bean.favorite.FavoriteShortcutInfo;
import com.zibuyuqing.ucbrowser.model.bean.favorite.ItemInfo;
import com.zibuyuqing.ucbrowser.model.bean.pager.UCPager;
import com.zibuyuqing.ucbrowser.ui.fragment.NewsListFragment;
import com.zibuyuqing.ucbrowser.utils.Constants;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener, UCPagerView.CallBack, UCStackView.OnChildDismissedListener, FavoriteWorkspace.OnItemClickListener {
    private static final String TAG = "MainActivity";
    private BaseLayout mTopSearchBar;// 顶部搜索条
    private UCHeadLayout mUCHeadLayout;// 头部
    private UCNewsLayout mUCNewsLayout;//新闻列表
    private UCBottomBar mUCBottomBar; // 底部菜单
    private BaseLayout mUCFavorite; // 收藏页
    private BezierLayout mBezierLayout;
    private UCRootView mUCRootView;
    private ViewPager mNewsPager;
    private TabLayout mNewsTab;
    private NewsPageAdapter mNewsPageAdapter;
    private FrameLayout mPagersManagerLayout;
    private UCStackView mUCStackView;
    private UCPagerAdapter mPagerAdapter;

    private DragLayer mDragLayer;
    private FavoriteWorkspace mWorkspace;
    private DragController mDragController;

    private TextView mPagersNum;
    private List<UCPager> mPagers = new ArrayList<>();
    private List<Integer> mPagerIds = new ArrayList<>();
    private int mSelectPager = 0;
    private boolean mPagersManagerUIShown = false;
    private boolean mFolderOpened = false;
    private FavoriteFolder mOpenedFolder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWindow();
        initViews();
    }

    private void initViews() {
        mUCHeadLayout = (UCHeadLayout) findViewById(R.id.llUCHeadLayout);
        mUCHeadLayout.setTransYEnable(true);
        mUCHeadLayout.setTransXEnable(true);
        mUCHeadLayout.initTranslationY(0, -100);
        mUCHeadLayout.initTranslationX(0, -ViewUtil.getScreenSize(this).x);
        mTopSearchBar = (BaseLayout) findViewById(R.id.llTopSearchBar);

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

        mUCNewsLayout = (UCNewsLayout) findViewById(R.id.llUCNewsListLayout);
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
        mUCBottomBar = (UCBottomBar)findViewById(R.id.llUCBottomBar);
        mBezierLayout = (BezierLayout)findViewById(R.id.llBezierLayout);
        mNewsPager = (ViewPager) mUCNewsLayout.findViewById(R.id.vpUCNewsPager);
        mNewsTab = (TabLayout) mUCNewsLayout.findViewById(R.id.tlUCNewsTab);

        mPagersNum = (TextView)findViewById(R.id.tvPagerNum);

        mPagersManagerLayout = (FrameLayout) findViewById(R.id.flPagersManager);
        mUCStackView = (UCStackView)findViewById(R.id.ucStackView);
        mPagerAdapter = new UCPagerAdapter(this,this);
        mUCStackView.setAdapter(mPagerAdapter);
        mUCStackView.setOnChildDismissedListener(this);
        mDragLayer = (DragLayer) findViewById(R.id.dragLayer);
        mWorkspace = (FavoriteWorkspace) findViewById(R.id.favoriteWorkspace);
        mDragController = new DragController(this,mDragLayer);
        mDragLayer.setup(mDragController);
        mWorkspace.setup(mDragLayer);
        mWorkspace.setOnItemClickListener(this);
        mUCFavorite = (BaseLayout) findViewById(R.id.ucFavorite);
        mUCFavorite.setTransXEnable(true);
        mUCFavorite.initTranslationX(ViewUtil.getScreenSize(this).x, 0);

        mUCRootView = (UCRootView) findViewById(R.id.ucRootView);
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
                                ViewUtil.getScreenSize(MainActivity.this).x,
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
        info.setIcon(ViewUtil.drawableToBitmap(getDrawable(R.drawable.folder_bg)));
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
        if(mFolderOpened){
            if(mOpenedFolder != null){
                mOpenedFolder.close();
                mOpenedFolder = null;
                mFolderOpened = false;
            }
            return;
        }
        if(mUCRootView.getMode() == UCRootView.NEWS_MODE){
            mUCRootView.back2Normal();
        } else if(mUCRootView.getMode() == UCRootView.FAVORITE_MODE){
            mUCRootView.back2Home();
        } else if(mPagersManagerUIShown){
            hidePagers(true);
        } else {
            super.onBackPressed();
        }
    }
    /**
     * 更新视图
     */
    private void refreshPages(){
        int count = mPagers.size();
        int olderSelect = mSelectPager;
        if(count > 0){
            mPagers.clear();
            for(int i = 0; i < count; i++){
                mPagers.add(buildUCPager());
            }
        }
        mSelectPager = olderSelect;
    }
    /**
     * 进入页面管理界面，用动画改变选择页（可以理解为一张截图）的Y和scale
     */
    public void showPagers(){
        if(mUCStackView.isAnimating()){
            return;
        }
        if(mPagers.size() <= 0){
            addUCPager(false);
        }
        mPagersManagerLayout.setVisibility(View.VISIBLE);
        refreshPages();
        mPagerAdapter.updateData(mPagers);
        getWindow().setStatusBarColor(getResources().getColor(R.color.pureBlack, null));
        mUCStackView.animateShow(mSelectPager, mUCRootView,mPagersManagerLayout,true, new Runnable() {
            @Override
            public void run() {
                mUCRootView.setVisibility(View.GONE);
                mPagersManagerUIShown = true;
            }
        });
    }

    /**
     *
     * @param animate 是否有动画，有动画时即UCRootView从下往上移
     */
    private void addUCPager(boolean animate){
        mUCRootView.setVisibility(View.VISIBLE);
        if(animate) {
           mUCRootView.animateShowFromBottomToTop(new Runnable() {
               @Override
               public void run() {
                   UCPager pager = buildUCPager();
                   mPagers.add(pager);
                   mPagerIds.add(pager.getKey());
                   hidePagers(false); // 把页面管理页隐藏
                   mPagersNum.setText("" + mPagers.size()); // 更新页面数量
                   initWindow(); // 更改状态栏颜色
               }
           });
        } else {
            UCPager pager = buildUCPager();
            mPagers.add(pager);
            mPagerIds.add(pager.getKey());
        }
    }
    private void removeUCPager(int index){
        mPagers.remove(index);
        mPagerIds.remove(index);
    }

    /**
     * 新建一页并初始化页面，这里的ICON为网站图标，我没资源先用UC代替，title为网站title，key为定义的页面ID
     * previewBitmap 为页面的截图
     * 当新建一页时，让我们的选择页置为新建的这一页，然后下次进入页面管理页时初始化进度
     * @return
     */
    private UCPager buildUCPager(){
        Bitmap pagerPreview = getScreenShot();
        String title = "UC";
        int websiteIcon = R.drawable.ic_home;
        int key = mPagers.size();
        UCPager pager = new UCPager(title,websiteIcon,pagerPreview,key);
        mSelectPager = key;
        return pager;
    }

    /**
     * 获取页面截图
     * @return
     */
    private Bitmap getScreenShot() {
        View view =  getWindow().getDecorView().getRootView();
        view.setDrawingCacheEnabled(true);
        try {
            Bitmap bitmap = view.getDrawingCache();
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public void hidePagers(boolean animated){
        if(mUCStackView.isAnimating()){
            return;
        }
        int pagerNum = mPagers.size();
        if(animated){
            mUCStackView.animateShow(mSelectPager, mUCRootView,mPagersManagerLayout,false, new Runnable() {
                @Override
                public void run() {
                    mPagersManagerLayout.setVisibility(View.GONE);
                    mUCRootView.setVisibility(View.VISIBLE);
                    initWindow();
                }
            });
        } else {
            mPagersManagerLayout.setVisibility(View.GONE);
            mUCBottomBar.setVisibility(View.VISIBLE);
            initWindow();
        }
        mPagersManagerUIShown = false;
        mPagersNum.setText("" + pagerNum);
    }

    private void bindNewsPage() {
        List<BaseNewsFragment> fragments = new ArrayList<>(Constants.NEWS_TITLE.length);
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
                    showPagers();
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
                addUCPager(true);
            }
        }
    }

    @Override
    public void onSelect(int key) {
        if(mUCStackView.isAnimating()){
            return;
        }
        int index = mPagerIds.indexOf(key);
        mSelectPager = index;
        mUCStackView.selectPager(index, new Runnable() {
            @Override
            public void run() {
                mUCRootView.setVisibility(View.VISIBLE);
                mPagersManagerLayout.setVisibility(View.GONE);
                initWindow();
            }
        });
        Log.e(TAG,"onSelect :: key =:" + key);
    }

    @Override
    public void onClose(int key) {
        Log.e(TAG,"onClose :: key = :" + key);
        if(!mPagerIds.contains(key)){
            return;
        }
        Log.e(TAG,"onClose ::  start mSelectPager =:" + mSelectPager +",mUCStackView.getChildCount() =:" +mUCStackView.getChildCount() +",key =:" + key);
        int index = mPagerIds.indexOf(key);
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
            addUCPager(true);
        }
        removeUCPager(index);
    }

    @Override
    public void onChildDismissed(int index) {
        onUCPagerClosed(index);
    }

    @Override
    public void onItemClick(View view) {
        if(view instanceof FavoriteFolderIcon){
            openFolder((FavoriteFolderIcon) view);
        } else if(view instanceof FavoriteShortcut){
            ItemInfo info = (ItemInfo) view.getTag();
            Toast.makeText(MainActivity.this,info.description,Toast.LENGTH_SHORT).show();
        }
    }
}
