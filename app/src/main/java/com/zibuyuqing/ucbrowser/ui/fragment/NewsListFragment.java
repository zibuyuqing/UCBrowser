package com.zibuyuqing.ucbrowser.ui.fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.zibuyuqing.ucbrowser.R;
import com.zibuyuqing.ucbrowser.adapter.NewsItemAdapter;
import com.zibuyuqing.ucbrowser.base.BaseFragment;
import com.zibuyuqing.ucbrowser.model.bean.news.NewsItem;
import com.zibuyuqing.ucbrowser.present.NewsPresenter;
import com.zibuyuqing.ucbrowser.view.NewsView;
import com.zibuyuqing.ucbrowser.widget.xrecyclerview.XRecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by Xijun.Wang on 2017/11/29.
 */

public class NewsListFragment extends BaseFragment implements NewsView,XRecyclerView.LoadingListener{
    private static final String TAG = NewsListFragment.class.getSimpleName();
    @BindView(R.id.rvNewsList)
    XRecyclerView mNewsList;
    private RecyclerView.Adapter mNewsAdapter;
    private ArrayList<NewsItem> mNewsItems = new ArrayList<>();
    private NewsPresenter mPresenter;
    @Override
    protected int layoutId() {
        return R.layout.fragment_news_list;
    }

    @Override
    protected void init() {
        mNewsList.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext().getApplicationContext());
        mNewsList.setLayoutManager(layoutManager);
        mNewsList.setLoadingListener(this);
        mPresenter = new NewsPresenter();
        mPresenter.attachView(this);
        mNewsAdapter = new NewsItemAdapter(mNewsItems,getContext());
        mNewsList.setAdapter(mNewsAdapter);
        mPresenter.refresh();
    }

    @Override
    public void onDestroyView() {
        if(mPresenter != null){
            mPresenter.detachView();
        }
        super.onDestroyView();
    }

    @Override
    public void showDialog() {
        showTips("加载....");
    }

    @Override
    public void hideDialog() {
        showTips("加载完成....");
    }

    @Override
    public void showMsg(String msg) {
        showTips(msg);
    }

    @Override
    public void refreshNews(List<NewsItem> newsSummaries, boolean isRefresh) {
        Log.e(TAG,"refreshNews--- isRefresh =;" + isRefresh);
        if(isRefresh) {
            mNewsItems.clear();
        }
        mNewsItems.addAll(newsSummaries);
        mNewsAdapter.notifyDataSetChanged();
        mNewsList.refreshComplete();
        mNewsList.loadMoreComplete();
    }

    @Override
    public void onRefresh() {
        Log.e(TAG,"onRefresh -----");
        mPresenter.refresh();
    }

    @Override
    public void onLoadMore() {
        Log.e(TAG,"onRefresh -----");
        mPresenter.loadMore();
    }
}
