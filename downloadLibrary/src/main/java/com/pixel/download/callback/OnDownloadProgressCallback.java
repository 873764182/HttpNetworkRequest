package com.pixel.download.callback;

/**
 * Created by Administrator on 2016/11/3.
 * <p>
 * 下载进度回调
 */

public interface OnDownloadProgressCallback {
    void onProgress(long fileSize, long downSize, float percentage);
}
