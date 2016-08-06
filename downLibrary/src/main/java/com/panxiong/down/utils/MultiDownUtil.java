package com.panxiong.down.utils;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import com.panxiong.down.callback.ProgressCallback;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2016/8/6.
 * <p/>
 * 多线程下载
 */
public class MultiDownUtil {
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

    public MultiDownUtil(String downUrl, String savePath, int threadNum) {
        new MultiDownUtil(downUrl, savePath, threadNum, null);
    }

    /**
     * 初始化多线程下载
     *
     * @param downUrl   下载地址
     * @param savePath  保存路径
     * @param threadNum 线程数量
     */
    public MultiDownUtil(String downUrl, String savePath, int threadNum, final ProgressCallback progressCallback) {
        this.downUrl = downUrl;
        this.threadNum = threadNum;
        this.savePath = getSdPath() + savePath;
        this.threads = new DownThread[threadNum];

        if (progressCallback != null) {
            final Handler handler = new Handler(Looper.getMainLooper());
            final Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    final int percentage = getCompleteRate();
                    final boolean complete = percentage >= 100;
                    final long progress = percentage * fileSize;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressCallback.onDownProgress(progress, fileSize, complete, percentage);
                        }
                    });
                    if (complete) timer.cancel();
                }
            }, 100, 100);
        }
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
                    conn.setRequestProperty("Accept", "*/*");   // 接受内容
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
                        threads[i] = new DownThread(startPos, currentPartSize, currentPart, downUrl);
                        threads[i].start();
                    }
                    isInit = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /*获取多个线程的总下载进度百分比*/
    public int getCompleteRate() {
        long sumSize = 0;
        if (isInit) {
            for (int i = 0; i < threads.length; i++) {
                sumSize += threads[i].length;
            }
        }
        return (int) ((sumSize * 1.0 / fileSize) * 100);
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
    private static class DownThread extends Thread {
        /*下载路径*/
        private String downUrl = null;
        /*当前线程的下载位置*/
        private long startPos = 0;
        /*定义当前线程负责下载的文件大小*/
        private long currentPartSize = 0;
        /*当前线程需要下载的文件块*/
        private RandomAccessFile currentPart = null;
        /*定义当前线程已经下载的字节数*/
        public long length = 0;

        public DownThread(long startPos, long currentPartSize, RandomAccessFile currentPart, String downUrl) {
            this.startPos = startPos;
            this.currentPartSize = currentPartSize;
            this.currentPart = currentPart;
            this.downUrl = downUrl;
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
