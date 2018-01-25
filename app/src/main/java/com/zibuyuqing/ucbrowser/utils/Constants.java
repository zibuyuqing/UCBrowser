package com.zibuyuqing.ucbrowser.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Xijun.Wang on 2017/11/29.
 */

public class Constants {
    public static final String[] NEWS_TITLE = new String[]
            { "推荐", "视频", "热点", "娱乐", "体育", "深圳", "社会", "财经",
                    "科技" ,"汽车", "图片", "搞笑", "军事", "历史", "涨知识", "我的","值得买","情感"};
    public static final Map<String,String> WEBSITES = new HashMap<String, String>(){
        {
            // 百度
            put("baidu","https://www.baidu.com/");
            // 新浪
            put("sina","http://www.sina.com.cn/");
            // 淘宝
            put("taobo","https://www.taobao.com/");
            // 天猫
            put("tmall","https://www.tmall.com/");
            // 爱奇艺
            put("iqiyi","http://www.iqiyi.com/");
            // 58
            put("58","http://jump.luna.58.com/");

            // 当当
            put("dangdang","http://book.dangdang.com/");
            // 豆瓣
            put("douban","https://www.douban.com/");
            // 优酷
            put("youku","http://www.youku.com/");
            // 京东
            put("jd","https://www.jd.com/");
            // 搜狐
            put("souhu","http://www.sohu.com/");
            // 更多
            put("more","http://www.hao123.com/");
        }
    };
}
