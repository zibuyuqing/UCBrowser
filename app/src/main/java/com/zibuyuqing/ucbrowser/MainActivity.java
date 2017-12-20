package com.zibuyuqing.ucbrowser;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import com.zibuyuqing.ucbrowser.model.bean.pager.UCPager;
import com.zibuyuqing.ucbrowser.ui.fragment.NewsListFragment;
import com.zibuyuqing.ucbrowser.utils.Constants;
import com.zibuyuqing.ucbrowser.widget.layout.BezierLayout;
import com.zibuyuqing.ucbrowser.widget.layout.UCBottomBar;
import com.zibuyuqing.ucbrowser.widget.layout.UCHeadLayout;
import com.zibuyuqing.ucbrowser.widget.layout.UCNewsLayout;
import com.zibuyuqing.ucbrowser.widget.root.UCRootView;
import com.zibuyuqing.ucbrowser.widget.stackview.UCPagerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, UCPagerView.CallBack, UCStackView.OnChildDismissedListener {
    private static final String TAG = "MainActivity";
    private BaseLayout mTopSearchBar;// 顶部搜索条
    private UCHeadLayout mUCHeadLayout;// 头部
    private UCNewsLayout mUCNewsLayout;//新闻列表
    private UCBottomBar mUCBottomBar; // 底部菜单
    private BezierLayout mBezierLayout;
    private UCRootView mUCRootView;
    private ViewPager mNewsPager;
    private TabLayout mNewsTab;
    private NewsPageAdapter mNewsPageAdapter;
    private FrameLayout mPagersManagerLayout;
    private UCStackView mUCStackView;
    private UCPagerAdapter mPagerAdapter;
    private TextView mPagersNum;
    private List<UCPager> mPagers = new ArrayList<>();
    private List<Integer> mPagerIds = new ArrayList<>();
    private int mSelectPager = 0;
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
        mUCRootView = (UCRootView) findViewById(R.id.ucRootView);
        mUCRootView.attachScrollStateListener(mTopSearchBar);
        mUCRootView.attachScrollStateListener(mUCHeadLayout);
        mUCRootView.attachScrollStateListener(mUCNewsLayout);
        mUCRootView.attachScrollStateListener(mUCBottomBar);
        mUCRootView.attachScrollStateListener(mBezierLayout);
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
        mNewsPager = (ViewPager) mUCNewsLayout.findViewById(R.id.vpUCNewsPager);
        mNewsTab = (TabLayout) mUCNewsLayout.findViewById(R.id.tlUCNewsTab);

        mPagersNum = (TextView)findViewById(R.id.tvPagerNum);

        mPagersManagerLayout = (FrameLayout) findViewById(R.id.flPagersManager);
        mUCStackView = (UCStackView)findViewById(R.id.ucStackView);
        mPagerAdapter = new UCPagerAdapter(this,this);
        mUCStackView.setAdapter(mPagerAdapter);
        mUCStackView.setOnChildDismissedListener(this);

        findViewById(R.id.flWindowsNum).setOnClickListener(this);
        findViewById(R.id.tvBack).setOnClickListener(this);
        findViewById(R.id.ivHome).setOnClickListener(this);
        findViewById(R.id.ivAddPager).setOnClickListener(this);
        bindNewsPage();
    }

    /**
     * 重写back键逻辑
     */
    @Override
    public void onBackPressed() {
        if(mUCRootView.getMode() == UCRootView.NEWS_MODE){
            mUCRootView.back2Normal();
        } else if(mUCRootView.getMode() == UCRootView.WEBSITE_MODE){
            mUCRootView.back2Home();
        } else {
            super.onBackPressed();
        }
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
        mPagerAdapter.updateData(mPagers);
        getWindow().setStatusBarColor(getResources().getColor(R.color.pureBlack, null));
        mUCStackView.animateShow(mSelectPager, mUCRootView,mPagersManagerLayout,true, new Runnable() {
            @Override
            public void run() {
                mUCRootView.setVisibility(View.GONE);
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
            ObjectAnimator animator = ObjectAnimator.ofFloat(
                    mUCRootView,
                    "translationY",
                    ViewUtil.getScreenSize(this).y,
                    0);
            animator.setDuration(500);
            animator.start();
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    UCPager pager = buildUCPager();
                    mPagers.add(pager);
                    mPagerIds.add(pager.getKey());
                    hidePagers(false); // 把页面管理页隐藏
                    mPagersNum.setText("" + mPagers.size()); // 更新页面数量
                    initWindow(); // 更改状态栏颜色
                }

                @Override
                public void onAnimationStart(Animator animation) {

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
        int key = mPagers.size() + 1;
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
        mSelectPager = mUCStackView.getSelectPager();
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
                if(mSelectPager == 0){
                    Toast.makeText(this,"需要创建新的一页",Toast.LENGTH_SHORT).show();
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
        mSelectPager = key;
        mUCStackView.selectPager(key);
    }

    @Override
    public void onClose(int key) {
        if(!mPagerIds.contains(key)){
            return;
        }
        Log.e(TAG,"onClose ::  start mSelectPager =:" + mSelectPager +",mUCStackView.getChildCount() =:" +mUCStackView.getChildCount() +",key =:" + key);
        int index = mPagerIds.indexOf(key);
        mUCStackView.closePager(index);
        onUCPagerClosed(index);
        Log.e(TAG,"onClose ::  end mSelectPager =:" + mSelectPager +",mUCStackView.getChildCount() =:" +mUCStackView.getChildCount());
    }
    private void onUCPagerClosed(int index){
        if(mSelectPager >= 2){
            mSelectPager --;
        } else {
            if(mUCStackView.getChildCount() <= 1){
                mSelectPager = 0;
                addUCPager(true);
            }
        }
        removeUCPager(index);
    }

    @Override
    public void onChildDismissed(int index) {
        onUCPagerClosed(index);
    }
}
