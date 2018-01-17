package com.zibuyuqing.common.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Xijun.Wang on 2018/1/16.
 */

public class NetworkUtils {
    /**
     * 检测网络是否可用
     * @param context
     * @return
     */
    public static boolean isNetWorkAvailable (Context context){
        ConnectivityManager cm = (ConnectivityManager) context.
                getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm != null){
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if(networkInfo != null && networkInfo.isAvailable()){
                return true;
            }
        }
        return false;
    }
    /**
     * 检测wifi是否连接
     */
    public static boolean isWifiAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.
                getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            }
        }
        return false;
    }
    /**
     * 判断网址是否有效
     */
    public static boolean isLinkAvailable(String link) {
        Pattern pattern = Pattern.compile("^(http://|https://)?((?:[A-Za-z0-9]+-[A-Za-z0-9]+|[A-Za-z0-9]+)\\.)+([A-Za-z]+)[/\\?\\:]?.*$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(link);
        if (matcher.matches()) {
            return true;
        }
        return false;
    }
}
