package com.panxiong.http.callback;

/**
 * Created by Administrator on 2016/7/30.
 * <p/>
 * 下载回调接口
 */
public interface DownloadProgressCallback {
    void onDownProgress(Long progress, Long contentLength, Boolean complete, Integer percentage);
}
