package com.zibuyuqing.ucbrowser.api;

import com.zibuyuqing.ucbrowser.model.bean.news.NewsItem;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

/**
 * Created by Xijun.Wang on 2017/11/20.
 */

public interface ApiService {
    @GET("nc/article/{type}/{id}/{startPage}-20.html")
    Observable<Map<String, List<NewsItem>>> getNewsList(
            @Header("Cache-Control") String cacheControl,
            @Path("type") String type, @Path("id") String id,
            @Path("startPage") int startPage);
}
