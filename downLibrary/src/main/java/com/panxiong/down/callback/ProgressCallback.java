package com.panxiong.down.callback;

/**
 * Created by Administrator on 2016/8/1.
 * <p>
 * 下载进度回调
 */
public interface ProgressCallback {
    void onDownProgress(Long progress, Long contentLength, Boolean complete, Integer percentage);
}
