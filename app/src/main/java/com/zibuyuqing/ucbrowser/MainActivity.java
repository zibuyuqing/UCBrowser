package com.zibuyuqing.ucbrowser;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewTreeObserver;

import com.zibuyuqing.ucbrowser.widget.BaseLayout;
import com.zibuyuqing.ucbrowser.widget.UCRootView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private BaseLayout mTopSearchBar;// 顶部搜索条
    private BaseLayout mUCHeadLayout;// 头部
    private BaseLayout mNewsListLayout;//新闻列表
    private UCRootView mUCRootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWindow();
        initViews();
    }

    private void initViews() {
        mTopSearchBar = findViewById(R.id.llTopSearchBar);
        mUCHeadLayout = findViewById(R.id.llUCHeadLayout);
        mNewsListLayout = findViewById(R.id.llUCNewsListLayout);
        mUCHeadLayout.setDirection(BaseLayout.SHRINK_WHEN_SCROLL_UP);
        mUCHeadLayout.initScale(1.0f, 0.9f);
        mTopSearchBar.setDirection(BaseLayout.DOWN_WHEN_SCROLL_UP);
        mTopSearchBar.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mTopSearchBar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        mTopSearchBar.initTranslationY(-mTopSearchBar.getHeight(), 0);
                    }
                }
        );

        mNewsListLayout.setDirection(BaseLayout.UP_WHEN_SCROLL_UP);
        mNewsListLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mNewsListLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        mNewsListLayout.initTranslationY(0, -mUCHeadLayout.getHeight());
                    }
                }
        );
        mUCRootView = findViewById(R.id.ucRootView);
        mUCRootView.addScrollStateListener(mTopSearchBar);
        mUCRootView.addScrollStateListener(mUCHeadLayout);
        mUCRootView.addScrollStateListener(mNewsListLayout);
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
