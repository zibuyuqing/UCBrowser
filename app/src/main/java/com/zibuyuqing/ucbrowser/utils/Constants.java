package com.zibuyuqing.ucbrowser.utils;

import com.zibuyuqing.ucbrowser.R;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Xijun.Wang on 2017/11/29.
 */

public class Constants {
    public static final String[] NEWS_TITLE = new String[]
            { "推荐", "视频", "热点", "娱乐", "体育", "深圳", "社会", "财经",
                    "科技" ,"汽车", "图片", "搞笑", "军事", "历史", "涨知识", "我的","值得买","情感"};
    public static final int[] WEBSITE_ICON_RES = {
            R.drawable.ic_baidu,
            R.drawable.ic_sina,
            R.drawable.ic_taobao,
            R.drawable.ic_tianmao,
            R.drawable.ic_aiqiyi,
            R.drawable.ic_58,
            R.drawable.ic_dangdang,
            R.drawable.ic_douban,
            R.drawable.ic_youku,
            R.drawable.ic_jingdong,
            R.drawable.ic_souhu,
            R.drawable.ic_more_site,
    };
    public static final Map<String,String> WEBSITES = new LinkedHashMap<String, String>(){
        {
            // 百度
            put("百度","https://www.baidu.com/");
            // 新浪
            put("新浪","http://www.sina.com.cn/");
            // 淘宝
            put("淘宝","https://www.taobao.com/");
            // 天猫
            put("天猫","https://www.tmall.com/");
            // 爱奇艺
            put("爱奇艺","http://www.iqiyi.com/");
            // 58
            put("58","http://jump.luna.58.com/");

            // 当当
            put("当当","http://book.dangdang.com/");
            // 豆瓣
            put("豆瓣","https://www.douban.com/");
            // 优酷
            put("优酷","http://www.youku.com/");
            // 京东
            put("京东","https://www.jd.com/");
            // 搜狐
            put("搜狐","http://www.sohu.com/");
            // 更多
            put("更多","http://www.hao123.com/");
        }
    };

    public static final String APP_CACHE_DIRNAME = "cache";
}
