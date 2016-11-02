package com.panxiong.down;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

import com.panxiong.down.callback.DownCallBack;
import com.panxiong.down.callback.ProgressCallback;
import com.panxiong.down.receiver.DownCompleteReceiver;
import com.panxiong.down.utils.BreakpointDownUtil;
import com.panxiong.down.utils.MultiDownUtil;
import com.panxiong.down.utils.StringUtil;

import java.util.Hashtable;
import java.util.Map;

/**
 * Created by Administrator on 2016/8/1.
 * <p/>
 * 下载管理对象
 */
public class Down {

    public static final Map<Long, DownCallBack> dcbMap = new Hashtable<>();

    /**
     * 调用系统的下载管理器下载
     */
    public static void systemDownload(Context context, String url, DownCallBack downCallBack) {
        /*获取系统下载管理器*/
        DownloadManager downloadManager =
                (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        /*处理下载地址 获取下载对象*/
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        /*设置允许使用的网络类型，这里是移动网络和wifi都可以*/
        request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        /*显示下载界面*/
        request.setVisibleInDownloadsUi(true);
        /*允许MediaScanner扫描到这个文件*/
        request.allowScanningByMediaScanner();
        /*下载中通知栏提示的标题*/
        request.setTitle(StringUtil.getNameByUrl(url));
        request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        /*设置下载文件的mineType*/
        // request.setMimeType("application/vnd.android.package-archive");
        /*设置保存路径*/
        request.setDestinationInExternalPublicDir("Download", StringUtil.getNameByUrl(url));
        /*开始下载 且获得下载ID*/
        Long downId = downloadManager.enqueue(request);
        /*绑定回调接口*/
        dcbMap.put(downId, downCallBack);
        /*注册下载结束广播*/
        context.registerReceiver(new DownCompleteReceiver(), new IntentFilter("android.intent.action.DOWNLOAD_COMPLETE"));
    }

    /**
     * 调用浏览器下载功能 保存在系统下载目录
     */
    public static void browserDownload(Context context, String url) {
        try {
            Uri uri = Uri.parse(url);
            Intent downloadIntent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(downloadIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 断点下载
     */
    public static BreakpointDownUtil.TaskEntity breakpointDownload(Context context, String downUrl, String savePath, ProgressCallback progressCallback) {
        BreakpointDownUtil.TaskEntity taskEntity = new BreakpointDownUtil.TaskEntity(context, downUrl, savePath, progressCallback);
        taskEntity.start();
        return taskEntity;
    }

    /**
     * 多线程下载
     */
    public static MultiDownUtil multiDownload(String downUrl, String savePath, int threadNum, ProgressCallback progressCallback) {
        MultiDownUtil multiDownUtil = new MultiDownUtil(downUrl, savePath, threadNum, progressCallback);
        multiDownUtil.startDownload();
        return multiDownUtil;
    }
}
