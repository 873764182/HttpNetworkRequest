package com.panxiong.down;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;

import com.panxiong.down.callback.DownCallBack;
import com.panxiong.down.receiver.DownCompleteReceiver;
import com.panxiong.down.utils.StringUtil;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
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
     * 多线程下载
     */
    public static class MultiDown {
        /*下载路径*/
        private String downUrl = null;
        /*保存路径*/
        private String savePath = null;
        /*线程数量*/
        private int threadNum = 0;
        /*下载线程*/
        private DownThread[] threads;
        /*下载文件的总大小*/
        private long fileSize = 0;
        /*是否初始化完成*/
        private boolean isInit = false;

        /**
         * 初始化多线程下载
         *
         * @param downUrl   下载地址
         * @param savePath  保存路径
         * @param threadNum 线程数量
         */
        public MultiDown(String downUrl, String savePath, int threadNum) {
            this.downUrl = downUrl;
            this.threadNum = threadNum;
            this.savePath = getSdPath() + savePath;
            this.threads = new DownThread[threadNum];
        }

        /*开始下载*/
        public void startDownload() {
            if (threadNum <= 0) return;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(downUrl);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setConnectTimeout(5 * 1000);
                        conn.setReadTimeout(10 * 1000);
                        conn.setRequestMethod("GET");
                        conn.setRequestProperty("Accept", "image/gif, iamge/jpeg, image/pjpeg, " +  // 接受内容
                                "application/x-shockwave-flash, application/xaml+xml, " +
                                "application/vnd.ms-xpsdocument, application/x-ms-xbap, " +
                                "application/x-ms-application, application/vnd.ms-excel, " +
                                "application/vnd.ms-powerpoint, application/msword, */*"
                        );
                        conn.setRequestProperty("Accept-Language", "zh_CN");    // 语言
                        conn.setRequestProperty("Charset", "UTF-8");                // 字符
                        conn.setRequestProperty("Connection", "Keep-Alive");
                        fileSize = conn.getContentLength(); // 得到要下载文件的大小
                        conn.disconnect();
                        if (fileSize <= 0) throw new RuntimeException("文件大小： " + fileSize);
                        long currentPartSize = (fileSize / threadNum);    // 每条线程下载的大小 + 1
                        File tempFile = new File(savePath);
                        if (tempFile.exists() || tempFile.isFile()) {
                            tempFile.delete();
                        }
                        RandomAccessFile file = new RandomAccessFile(savePath, "rw"); // 获取具有读写的功能的文件随机存储对象
                        file.setLength(fileSize);
                        file.close();
                        for (int i = 0; i < threads.length; i++) {
                            long startPos = i * currentPartSize;    // 每条线程开始下载的地方
                            RandomAccessFile currentPart = new RandomAccessFile(savePath, "rw");
                            currentPart.seek(startPos); // 跳过指定大小
                            threads[i] = new DownThread(startPos, currentPartSize, currentPart);
                            threads[i].start();
                        }
                        isInit = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        /*获取下载进度*/
        public double getCompleteRate() {
            long sumSize = 0;
            if (isInit) {
                for (int i = 0; i < threads.length; i++) {
                    sumSize += threads[i].length;
                }
            }
            return sumSize * 1.0 / fileSize;
        }

        /* 获取内置SD卡路径 storage/sdcard */
        private String getSdPath() {
            try {
                return Environment.getExternalStorageDirectory().getPath();
            } catch (Exception e) {
                return "/mnt/sdcard";
            }
        }

        /*下载线程*/
        private class DownThread extends Thread {
            /*当前线程的下载位置*/
            private long startPos = 0;
            /*定义当前线程负责下载的文件大小*/
            private long currentPartSize = 0;
            /*当前线程需要下载的文件块*/
            private RandomAccessFile currentPart = null;
            /*定义当前线程已经下载的字节数*/
            public long length = 0;

            public DownThread(long startPos, long currentPartSize, RandomAccessFile currentPart) {
                this.startPos = startPos;
                this.currentPartSize = currentPartSize;
                this.currentPart = currentPart;
            }

            @Override
            public void run() {
                try {
                    URL url = new URL(downUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Range", "bytes=" + startPos + "-" + startPos + currentPartSize); // 读取范围
                    conn.setRequestProperty("Accept", "image/gif, iamge/jpeg, image/pjpeg, " +  // 接受内容
                            "application/x-shockwave-flash, application/xaml+xml, " +
                            "application/vnd.ms-xpsdocument, application/x-ms-xbap, " +
                            "application/x-ms-application, application/vnd.ms-excel, " +
                            "application/vnd.ms-powerpoint, application/msword, */*"
                    );
                    conn.setRequestProperty("Accept-Language", "zh_CN");    // 语言
                    conn.setRequestProperty("Charset", "UTF-8");                // 字符
                    InputStream inputStream = conn.getInputStream();
                    byte[] buffer = new byte[1024];
                    int hasRead = 0;
                    while (length < currentPartSize && (hasRead = inputStream.read(buffer)) > 0) {
                        currentPart.write(buffer, 0, hasRead);
                        length += hasRead;
                    }
                    currentPart.close();
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
