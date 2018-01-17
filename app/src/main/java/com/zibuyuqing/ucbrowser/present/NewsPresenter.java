package com.zibuyuqing.ucbrowser.present;

import android.util.Log;

import com.zibuyuqing.ucbrowser.api.ApiConstants;
import com.zibuyuqing.ucbrowser.base.BasePresenter;
import com.zibuyuqing.ucbrowser.model.NewsModel;
import com.zibuyuqing.ucbrowser.model.bean.news.NewsItem;
import com.zibuyuqing.ucbrowser.view.NewsView;

import java.util.List;

import io.reactivex.functions.Consumer;

/**
 * Created by Xijun.Wang on 2018/1/16.
 */

public class NewsPresenter implements BasePresenter<NewsView> {
    private static final String TAG ="HomePresenter";
    NewsView view;
    NewsModel model = new NewsModel();
    private int startPage = 0;
    private String newsType = ApiConstants.HEADLINE_TYPE;
    private String newsId = ApiConstants.HEADLINE_ID;
    private boolean isRefresh = true;
    public NewsPresenter() {

    }
    public void refresh(){
        isRefresh = true;
        loadNews();
    }
    public void loadMore(){
        isRefresh = false;
        startPage += 20;
        loadNews();
    }
    private void loadNews(){
        view.showDialog();
        Log.e(TAG,"refreshData startPage =:" + startPage);
        model.getNewsListObservable(newsType,newsId,startPage)
                .subscribe(new Consumer<List<NewsItem>>() {
                    @Override
                    public void accept(List<NewsItem> newsItems) throws Exception {
                        view.refreshNews(newsItems,isRefresh);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG,"throwable =:" + throwable.getMessage());
                    }
                });
    }
    @Override
    public void attachView(NewsView view) {
        Log.e(TAG,"attachView");
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }
}
