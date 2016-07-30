package com.panxiong.http.callback;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by Administrator on 2016/7/30.
 * <p/>
 * http请求回调函数
 */
public abstract class RequestCallBack<T> {
    /*回调函数*/
    public abstract void onSuccess(T result);

    /*客户端异常 可选*/
    public void onClientError(Call call, IOException e) {
        if (call.isExecuted()) call.cancel();
        e.printStackTrace();
    }

    /*服务端异常 可选*/
    public void onServiceError(Call call, Response response) {
        if (call.isExecuted()) call.cancel();
        response.body().close();
    }

    /*上传/下载进度 可选*/
    public void onProgress(Long downProgress, Long contentLength, Boolean isComplete, Integer percentage) {
    }
}
