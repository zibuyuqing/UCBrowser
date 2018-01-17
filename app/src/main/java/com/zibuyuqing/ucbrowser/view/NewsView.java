package com.zibuyuqing.ucbrowser.view;

import com.zibuyuqing.ucbrowser.base.BaseView;
import com.zibuyuqing.ucbrowser.model.bean.news.NewsItem;

import java.util.List;

/**
 * Created by Xijun.Wang on 2018/1/16.
 */

public interface NewsView extends BaseView{
    void refreshNews(List<NewsItem> newsSummaries, boolean isRefresh);
}
