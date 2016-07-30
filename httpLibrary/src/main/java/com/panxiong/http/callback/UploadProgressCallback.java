package com.panxiong.http.callback;

/**
 * Created by Administrator on 2016/7/30.
 * <p/>
 * 上传回调接口
 */
public interface UploadProgressCallback {
    void doProgress(Long progress, Long contentLength, Boolean complete);
}
