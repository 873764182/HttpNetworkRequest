package com.panxiong.down.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.panxiong.down.utils.DownUtil;

import java.util.Map;

/**
 * Created by Administrator on 2016/8/1.
 * <p>
 * 网络改变监听
 */
public class ConnectChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (netConn(context)) {
            for (Map.Entry<String, DownUtil.TaskEntity> entry : DownUtil.taskMap.entrySet()) {
                entry.getValue().closeDown(false);
                entry.getValue().download();
            }
        } else {
            for (Map.Entry<String, DownUtil.TaskEntity> entry : DownUtil.taskMap.entrySet()) {
                entry.getValue().closeDown(false);
            }
        }
    }

    /* 检查网络状态的 */
    public static boolean netConn(Context context) {
        NetworkInfo netInfo = ((ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (null != netInfo && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }
}
