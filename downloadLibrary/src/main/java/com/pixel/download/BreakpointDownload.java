package com.pixel.download;

import android.content.Context;
import android.util.Log;

import com.pixel.download.callback.OnDownloadProgressCallback;
import com.pixel.download.entity.DownEntity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Administrator on 2016/11/3.
 * <p>
 * 下载
 */

public class BreakpointDownload implements Runnable {
    public static final String TAG = "BreakpointDownload";
    private Context context = null;
    private OnDownloadProgressCallback onDownloadProgressCallback;
    private volatile RandomAccessFile currentPart = null;
    private volatile boolean canRunThread = false;
    private volatile boolean canDown = false;
    private volatile DownEntity downEntity;

    public BreakpointDownload(Context context, String downUrl, String savePath) {
        init(context, new DownEntity(0L, downUrl, savePath, 0L, 0L));
    }

    public BreakpointDownload(Context context, DownEntity downEntity) {
        init(context, downEntity);
    }

    private void init(Context context, DownEntity downEntity) {
        try {
            this.context = context;
            this.downEntity = downEntity;
            this.currentPart = new RandomAccessFile(downEntity.getSavePath(), "rw");

            DownEntity entity = DownEntity.getEntity(context, downEntity.getDownUrl(), downEntity.getSavePath());
            if (entity == null) {
                DownEntity.saveEntity(context, downEntity);
            }
            if (entity != null) {
                DownEntity.updateEntity(context, downEntity);
            }
        } catch (Exception e) {
            Log.e(TAG, "初始化方法异常", e);
        }
    }

    @Override
    public void run() {
        try {
            while (canRunThread) {
                HttpURLConnection conn = null;
                InputStream inputStream = null;
                byte[] buffer = null; // 缓存设置大点可以减少硬盘的读写与回调次数次数
                int hasRead = 0;
                if (canDown) {
                    // 再次从数据库拿最新的数据
                    this.downEntity = DownEntity.getEntity(context, downEntity.getDownUrl(), downEntity.getSavePath());
                    File file = new File(downEntity.getSavePath());
                    if (downEntity.getDownSize() > 0 && file.exists() && file.isFile()) {
                        currentPart.seek(this.downEntity.getDownSize());
                    }

                    // 获取网络文件大小
                    HttpURLConnection getSizeConn = getConnection(downEntity.getDownUrl());
                    downEntity.setFileSize((long) getSizeConn.getContentLength());
                    getSizeConn.disconnect();

                    // 执行下载
                    conn = getConnection(downEntity.getDownUrl());
                    conn.setRequestProperty("Range", "bytes=" + downEntity.getDownSize() + "-" + downEntity.getFileSize()); // 读取范围
                    inputStream = conn.getInputStream();
                    buffer = new byte[4096]; // 缓存设置大点可以减少硬盘的读写与回调次数次数
                }
                while (canDown && (hasRead = inputStream.read(buffer)) != -1) {
                    currentPart.write(buffer, 0, hasRead);
                    downEntity.setDownSize(downEntity.getDownSize() + hasRead);
                    DownEntity.updateEntity(context, downEntity.getDownSize(), downEntity.get_id());   // 更新进度到数据库

                    float percentage = (float) downEntity.getDownSize() / (float) downEntity.getFileSize();
                    if (onDownloadProgressCallback != null) {
                        onDownloadProgressCallback.onProgress(downEntity.getFileSize(), downEntity.getDownSize(), percentage);
                    }
                    if (percentage >= 1.0f) {
                        DownEntity.deleteEntity(context, downEntity.getDownUrl(), downEntity.getSavePath());    // 清空记录 避免下次下载不了
                        canRunThread = false;   // 停止下载线程
                    }
                }
                if (canDown) {
                    if (currentPart != null) currentPart.close();
                    if (inputStream != null) inputStream.close();
                    if (conn != null) conn.disconnect();
                }
                Thread.sleep(2000); // 不能让线程太快
                Log.e(TAG, "外层循环在执行");
            }
            Log.e(TAG, "外层循环在执行 - 结束");
        } catch (Exception e) {
            Log.e(TAG, "RUN方法异常", e);
        }
    }

    // 获取网络连接客户端
    private HttpURLConnection getConnection(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(30 * 1000);
        conn.setReadTimeout(60 * 1000);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept-Language", "zh_CN");    // 语言
        conn.setRequestProperty("Charset", "UTF-8");                // 字符
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Accept", "image/gif, iamge/jpeg, image/pjpeg, " +  // 接受内容
                "application/x-shockwave-flash, application/xaml+xml, " +
                "application/vnd.ms-xpsdocument, application/x-ms-xbap, " +
                "application/x-ms-application, application/vnd.ms-excel, " +
                "application/vnd.ms-powerpoint, application/msword, */*"
        );
        return conn;
    }

    // 开启任务 每一次开启都会新开线程
    public synchronized void startDownload() {
        if (canRunThread || canDown) return;
        canRunThread = true;
        canDown = true;
        new Thread(this).start();
    }

    // 暂定任务 线程会被停掉
    public synchronized void suspendedDownload() {
        canDown = false;
    }

    public synchronized void restartDownload() {
        canDown = true;
    }

    // 清除下载任务 清除之后 请重新创建一个下载对象
    public synchronized void removeDownload() {
        canRunThread = false;
        canDown = false;
        File file = new File(downEntity.getSavePath());
        if (file.exists() && file.isFile()) file.delete();
        DownEntity.deleteEntity(context, downEntity.getDownUrl(), downEntity.getSavePath());
    }

    // 设置回调函数
    public void setOnDownloadProgressCallback(OnDownloadProgressCallback onDownloadProgressCallback) {
        this.onDownloadProgressCallback = onDownloadProgressCallback;
    }
}
