package com.zibuyuqing.ucbrowser.model;

import android.util.Log;

import com.zibuyuqing.common.utils.TimeUtil;
import com.zibuyuqing.ucbrowser.api.Api;
import com.zibuyuqing.ucbrowser.api.ApiService;
import com.zibuyuqing.ucbrowser.base.BaseModel;
import com.zibuyuqing.ucbrowser.model.bean.news.NewsItem;
import com.zibuyuqing.ucbrowser.rx.RxSchedulers;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;


/**
 * Created by Xijun.Wang on 2017/11/20.
 */

public class NewsModel implements BaseModel {
    private ApiService apiService;
    private final static String TAG = "BaseModel";
    public NewsModel(){
        apiService = Api.getApiService();
    };
    /**
     * example：http://c.m.163.com/nc/article/headline/T1348647909107/0-20.html
     *
     * @param newsType ：headline为头条,house为房产，list为其他
     */
    public Single<List<NewsItem>> getNewsListObservable(
            String newsType, final String newsId, int startPage) {
        return apiService.getNewsList(Api.getCacheControl(), newsType, newsId, startPage)
                .flatMap(new Function<Map<String, List<NewsItem>>, ObservableSource<NewsItem>>() {
                    @Override
                    public ObservableSource<NewsItem> apply(@NonNull Map<String, List<NewsItem>> map) throws Exception {
                        return Observable.fromIterable(map.get(newsId));
                    }
                }).map(new Function<NewsItem, NewsItem>() {
                    @Override
                    public NewsItem apply(@NonNull NewsItem newsSummary) throws Exception {
                        Log.e(TAG,"newsSummary +:" + newsSummary.getTitle());
                        String ptime = TimeUtil.formatDate(newsSummary.getPtime());
                        newsSummary.setPtime(ptime);
                        return newsSummary;
                    }
                }).distinct()
                .toSortedList(new Comparator<NewsItem>() {
                    @Override
                    public int compare(NewsItem o1, NewsItem o2) {
                        return o2.getPtime().compareTo(o1.getPtime());
                    }
                })
                .compose(RxSchedulers.<List<NewsItem>>io_main());
    }
}
