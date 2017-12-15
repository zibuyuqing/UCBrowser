package com.zibuyuqing.ucbrowser;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.zibuyuqing.ucbrowser.adapter.NewsPageAdapter;
import com.zibuyuqing.ucbrowser.adapter.UCPagerAdapter;
import com.zibuyuqing.ucbrowser.base.BaseLayout;
import com.zibuyuqing.ucbrowser.base.BaseNewsFragment;
import com.zibuyuqing.ucbrowser.model.bean.UCPager;
import com.zibuyuqing.ucbrowser.ui.fragment.NewsListFragment;
import com.zibuyuqing.ucbrowser.utils.Constants;
import com.zibuyuqing.ucbrowser.utils.ViewUtil;
import com.zibuyuqing.ucbrowser.widget.layout.BezierLayout;
import com.zibuyuqing.ucbrowser.widget.layout.UCBottomBar;
import com.zibuyuqing.ucbrowser.widget.layout.UCHeadLayout;
import com.zibuyuqing.ucbrowser.widget.layout.UCNewsLayout;
import com.zibuyuqing.ucbrowser.widget.root.UCRootView;
import com.zibuyuqing.ucbrowser.widget.stackview.UCPagerView;
import com.zibuyuqing.ucbrowser.widget.stackview.UCStackView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, UCPagerView.CallBack {
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
    private List<UCPager> mPagers = new ArrayList<>();
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

        mPagersManagerLayout = (FrameLayout) findViewById(R.id.flPagersManager);
        mUCStackView = (UCStackView)findViewById(R.id.ucStackView);
        mPagerAdapter = new UCPagerAdapter(this,this);
        mUCStackView.setAdapter(mPagerAdapter);

        findViewById(R.id.flWindowsNum).setOnClickListener(this);
        findViewById(R.id.tvBack).setOnClickListener(this);
        findViewById(R.id.ivHome).setOnClickListener(this);
        findViewById(R.id.ivAddPager).setOnClickListener(this);
        bindNewsPage();
    }

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

    public void showPagers(){
        if(mPagers.size() <= 0){
            mPagers.add(buildUCPager());
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
    private UCPager buildUCPager(){
        Bitmap pagerPreview = getScreenShot();
        String title = "UC";
        int websiteIcon = R.drawable.ic_home;
        int key = mPagers.size() + 1;
        UCPager pager = new UCPager(title,websiteIcon,pagerPreview,key);
        mSelectPager = key;
        return pager;
    }
    private Bitmap getScreenShot() {
        View view =  getWindow().getDecorView().getRootView();
        view.setDrawingCacheEnabled(true);
        try {
            Bitmap bitmap = view.getDrawingCache();
            return bitmap;
            // scale : 10   coverColor: 0x1f000000   radius : 6
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public void hidePagers(boolean animated){
        if(animated){
            mUCStackView.animateShow(mSelectPager, mUCRootView,mPagersManagerLayout,false, new Runnable() {
                @Override
                public void run() {
                    mPagersManagerLayout.setVisibility(View.GONE);
                    initWindow();
                }
            });
        } else {
            mUCRootView.setVisibility(View.VISIBLE);
            mPagersManagerLayout.setVisibility(View.GONE);
            initWindow();
        }

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
                hidePagers(true);
                break;
            }
            case R.id.ivAddPager:{
                mPagers.add(buildUCPager());
                hidePagers(false);
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
        mUCStackView.closePager(key);
    }
}
