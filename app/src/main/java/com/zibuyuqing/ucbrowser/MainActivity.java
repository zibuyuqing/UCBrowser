package com.zibuyuqing.ucbrowser;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;

import com.zibuyuqing.ucbrowser.widget.BaseLayout;
import com.zibuyuqing.ucbrowser.widget.BezierLayout;
import com.zibuyuqing.ucbrowser.widget.UCBottomBar;
import com.zibuyuqing.ucbrowser.widget.UCHeadLayout;
import com.zibuyuqing.ucbrowser.widget.UCRootView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private BaseLayout mTopSearchBar;// 顶部搜索条
    private UCHeadLayout mUCHeadLayout;// 头部
    private BaseLayout mNewsListLayout;//新闻列表
    private UCBottomBar mUCBottomBar; // 底部菜单
    private BezierLayout mBezierLayout;
    private UCRootView mUCRootView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWindow();
        initViews();
    }

    private void initViews() {
        mUCHeadLayout = (UCHeadLayout) findViewById(R.id.llUCHeadLayout);
        mUCHeadLayout.setTranslateEnable(true);
        mUCHeadLayout.initTranslationY(0, -100);

        mTopSearchBar = (BaseLayout) findViewById(R.id.llTopSearchBar);
        mTopSearchBar.setTranslateEnable(true);
        mTopSearchBar.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mTopSearchBar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        mTopSearchBar.initTranslationY(-mTopSearchBar.getHeight(), 0);
                    }
                }
        );

        mNewsListLayout = (BaseLayout) findViewById(R.id.llUCNewsListLayout);
        mNewsListLayout.setTranslateEnable(true);
        mNewsListLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mNewsListLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        mNewsListLayout.initTranslationY(0, -mUCHeadLayout.getHeight() + mTopSearchBar.getHeight());
                    }
                }
        );

        mUCBottomBar = (UCBottomBar)findViewById(R.id.llUCBottomBar);
        mUCBottomBar.findViewById(R.id.ivHome).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mUCRootView.getMode() == UCRootView.NEWS_MODE){
                    mUCRootView.back2Normal();
                }
            }
        });
        mBezierLayout = (BezierLayout)findViewById(R.id.llBezierLayout);
        mUCRootView = (UCRootView) findViewById(R.id.ucRootView);
        mUCRootView.attachScrollStateListener(mTopSearchBar);
        mUCRootView.attachScrollStateListener(mUCHeadLayout);
        mUCRootView.attachScrollStateListener(mNewsListLayout);
        mUCRootView.attachScrollStateListener(mUCBottomBar);
        mUCRootView.attachScrollStateListener(mBezierLayout);
        mUCRootView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mNewsListLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        mUCRootView.setFinalDistance(mUCHeadLayout.getMeasuredHeight());
                    }
                }
        );
    }

    private void initWindow() {
        getWindow().setStatusBarColor(getResources().getColor(R.color.themeBlue, null));
    }

}
