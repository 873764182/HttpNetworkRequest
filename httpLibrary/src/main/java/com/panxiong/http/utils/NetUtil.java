package com.panxiong.http.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;

/**
 * Created by Administrator on 2016/7/30.
 * <p/>
 * 网络工具
 */
public class NetUtil {

    /**
     * 检查网络状态的
     */
    public static boolean netConn(@NonNull Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();    // 23版本过时
            if (networkInfo != null) {
                for (NetworkInfo info : networkInfo) {
                    if (info.getState() == NetworkInfo.State.CONNECTED) { // 判断当前网络状态是否为连接状态
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
