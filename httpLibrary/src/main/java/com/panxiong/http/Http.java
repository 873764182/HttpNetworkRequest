package com.panxiong.http;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.panxiong.http.callback.DownloadProgressCallback;
import com.panxiong.http.callback.RequestCallBack;
import com.panxiong.http.callback.UploadProgressCallback;
import com.panxiong.http.cookie.ClearableCookieJar;
import com.panxiong.http.cookie.PersistentCookieJar;
import com.panxiong.http.cookie.SetCookieCache;
import com.panxiong.http.cookie.SharedPrefsCookiePersistor;
import com.panxiong.http.entity.DownloadEntity;
import com.panxiong.http.entity.ParamEntity;
import com.panxiong.http.entity.UploadEntity;
import com.panxiong.http.utils.JsonUtil;
import com.panxiong.http.utils.StringUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Administrator on 2016/7/30.
 * <p/>
 * Http请求客户端
 */
public class Http {
    public static final String TAG = "com.panxiong.http";
    private static volatile Http http = null;

    private OkHttpClient okHttpClient = null;
    private ClearableCookieJar cookieJar = null;
    private Gson gson = null;
    private Handler handler = null;

    private Http() {
        this.okHttpClient = getOkHttpClient(null);
        this.gson = new GsonBuilder().disableHtmlEscaping().create();
        this.handler = new Handler(Looper.getMainLooper());
    }

    private OkHttpClient getOkHttpClient(Context context) {
        if (context == null) {
            return new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build();
        } else {
            this.cookieJar = new PersistentCookieJar(
                    new SetCookieCache(), new SharedPrefsCookiePersistor(context)); // 打开Cookie支持
            return new OkHttpClient.Builder()
                    .cookieJar(cookieJar)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build();
        }
    }

    private void runOnUi(Runnable runnable) {
        if (runnable != null) handler.post(runnable);
    }

    private void doResponse(
            Call call, Response response, final RequestCallBack requestCallBack, final Dialog dialog) {
        if (response.isSuccessful()) {
            if (requestCallBack != null) {
                try {
                    final String string = response.body().string();
                    Type type = JsonUtil.getType(requestCallBack.getClass());
                    if (type == String.class) {
                        runOnUi(new Runnable() {
                            @Override
                            public void run() {
                                requestCallBack.onSuccess(string);
                            }
                        });
                    } else {
                        final Object object = gson.fromJson(string, type);
                        runOnUi(new Runnable() {
                            @Override
                            public void run() {
                                requestCallBack.onSuccess(object);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            response.body().close();
        } else {
            if (requestCallBack != null) {
                requestCallBack.onServiceError(call, response);
            }
        }
        if (dialog != null) {
            runOnUi(new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                }
            });
        }
    }

    private Call getCall(String url, Object param) {
        if (!StringUtil.noNull(url)) {
            throw new NullPointerException("URL IS NULL !");
        }
        url = url.replaceAll("\\s+", "");   // 去除敏感字符
        Call call = null;
        if (param == null) {
            call = okHttpClient.newCall(new Request.Builder()
                    .tag(TAG)
                    .url(url)
                    .build());
        } else {
            FormBody.Builder formBody = new FormBody.Builder();
            if (param instanceof Map) {
                for (Map.Entry<String, String> entry : ((Map<String, String>) param).entrySet()) {
                    formBody.add(entry.getKey(), entry.getValue());
                }
            } else {
                for (ParamEntity p : (List<ParamEntity>) param) {
                    formBody.add(p.key, p.value);
                }
            }
            call = okHttpClient.newCall(new Request.Builder()
                    .tag(TAG)
                    .post(formBody.build()) // 有参数则使用POST
                    .url(url)
                    .build());
        }
        return call;
    }

    private Callback getCallback(final Dialog dialog, final RequestCallBack requestCallBack) {
        if (dialog != null) dialog.show();
        return new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                runOnUi(new Runnable() {
                    @Override
                    public void run() {
                        if (dialog != null) dialog.cancel();
                        if (requestCallBack != null) requestCallBack.onClientError(call, e);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                doResponse(call, response, requestCallBack, dialog);
            }
        };
    }

    // --------------------------------------------------------------------------------------------------对外公开方法

    /**
     * 获取客户端实例
     */
    public static Http getInstance() {
        if (http == null) {
            synchronized (Http.class) {
                if (http == null) {
                    http = new Http();
                }
            }
        }
        return http;
    }

    /**
     * 打开Cookie支持
     */
    public void openCookie(Context context) {
        this.okHttpClient = getOkHttpClient(context);
    }

    /**
     * 清除Cookie
     */
    public void clearCookie() {
        if (cookieJar != null) cookieJar.clear();
    }


    /**
     * 获取Http客户端对象
     */
    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    /**
     * 获取GSON对象
     */
    public Gson getGson() {
        return gson;
    }

    /**
     * 清除Http缓存
     */
    public void clearCache() {
        try {
            okHttpClient.cache().evictAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 同步Cookie到WebView（实现WebView不再需要验证）
     */
    public void syncCookieToWebView(WebView webView, String url) {
        if (webView != null) {
            WebSettings settings = webView.getSettings();
            settings.setAllowFileAccess(true);  // 设置可以访问文件
            settings.setJavaScriptEnabled(true);//如果访问的页面中有Javascript，则webview必须设置支持Javascript
            settings.setUserAgentString(TAG);    // 用户字符串
            settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
            settings.setAllowFileAccess(true);
            settings.setAppCacheEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setDatabaseEnabled(true);
            settings.setSupportZoom(true);
            settings.setBuiltInZoomControls(true);
            settings.setDisplayZoomControls(false);
            settings.setDefaultFontSize(12);
            settings.setLoadWithOverviewMode(true);
            settings.setUseWideViewPort(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);// 5.0要求开启第三方Cookie支持
            }
        }
        // 核心设置代码
        HttpUrl httpUrl = HttpUrl.parse(url);
        List<Cookie> cookies = okHttpClient.cookieJar().loadForRequest(httpUrl);
        StringBuilder sb = new StringBuilder();
        for (Cookie cookie : cookies) {
            sb.append(cookie.toString()).append(";");
        }
        CookieManager.getInstance().setCookie(url, sb.toString());
    }

    /**
     * 根据标记取消请求 标记为空则取消所有请求
     */
    public void cancelRequest(Object tag) {
        synchronized (this) {
            for (Call call : okHttpClient.dispatcher().queuedCalls()) {
                if (tag != null) {
                    if (tag.toString().trim().equals(call.request().tag().toString().trim()))
                        call.cancel();  // 取消正在等待执行的
                } else {
                    call.cancel();  // 取消正在等待执行的
                }
            }
            for (Call call : okHttpClient.dispatcher().runningCalls()) {
                if (tag != null) {
                    if (tag.toString().trim().equals(call.request().tag().toString().trim()))
                        call.cancel();  // 取消正在执行的
                } else {
                    call.cancel();  // 取消正在执行的
                }
            }
        }
    }

    /**
     * 发送GET请求
     */
    public final void GET(
            String url, RequestCallBack requestCallBack, Dialog dialog) {
        Call call = getCall(url, null);
        call.enqueue(getCallback(dialog, requestCallBack));
    }

    /**
     * 发送POST请求
     */
    public final void POST(
            String url, Map<String, String> params, RequestCallBack requestCallBack, Dialog dialog) {
        Call call = getCall(url, params);
        call.enqueue(getCallback(dialog, requestCallBack));
    }

    /**
     * 发送POST请求
     */
    public final void POST(
            String url, List<ParamEntity> params, RequestCallBack requestCallBack, Dialog dialog) {
        Call call = getCall(url, params);
        call.enqueue(getCallback(dialog, requestCallBack));
    }

    /**
     * 提交JSON数据
     */
    public final void JSON(
            @NonNull String url, @NonNull String json, RequestCallBack requestCallBack, Dialog dialog) {
        Call call = okHttpClient.newCall(new Request.Builder()
                .url(url.replaceAll("\\s+", ""))
                .tag(TAG)
                .post(RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), json))
                .build());
        call.enqueue(getCallback(dialog, requestCallBack));
    }

    /**
     * 多文件上传 带参数
     */
    public final void UPLOAD(
            @NonNull String url, List<ParamEntity> params, String[] fileNames, File[] fileValues, final RequestCallBack requestCallBack) {
        /*创建统一参数编辑对象*/
        UploadEntity.Builder body = new UploadEntity.Builder();
        body.setType(MultipartBody.FORM);   // 表单类型
        /*编辑参数*/
        if (params != null) {
            for (ParamEntity p : params) {
                body.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + p.key + "\""), RequestBody.create(null, p.value));
            }
        }
        /*编辑文件*/
        if (fileValues.length > 0 && fileNames.length > 0 && fileNames.length == fileValues.length) {
            for (int i = 0; i < fileValues.length; i++) {
                String fileName = fileValues[i].getName();   // 获取文件URL
                String mime = URLConnection.getFileNameMap().getContentTypeFor(fileName);
                RequestBody fileBody = RequestBody.create(
                        MediaType.parse(mime != null ? mime : "application/x-zip-compressed"), fileValues[i]);
                body.addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"" + fileNames[i] + "\"; filename=\"" + fileName + "\""), fileBody);
            }
        }
        UploadEntity uploadEntity = body.build();
        uploadEntity.setUploadProgressInterface(new UploadProgressCallback() {
            @Override
            public void doProgress(final Long progress, final Long contentLength, final Boolean complete) {
                if (requestCallBack != null) {
                    runOnUi(new Runnable() {
                        @Override
                        public void run() {
                            requestCallBack.onProgress(
                                    progress, contentLength, complete, (int) ((double) progress / (double) contentLength));
                        }
                    });
                }
            }
        });
        Call call = okHttpClient.newCall(new Request.Builder()
                .tag(TAG)
                .url(url.replaceAll("\\s+", ""))
                .post(uploadEntity)
                .build()
        );
        call.enqueue(getCallback(null, requestCallBack));   /*发起请求*/
    }

    /**
     * 下载文件
     */
    public final void DOWNLOAD(
            String url, @NonNull final String path, final ProgressDialog dialog, final RequestCallBack requestCallBack) {
        // 开始下载的时间
        final long startDownTime = System.currentTimeMillis();
        final long[] fileSize = {0};    // 文件大小
        final long[] downSize = {0};    // 下载进度

        final Call call = getCall(url, null);
        call.enqueue(new Callback() {

            @Override
            public void onFailure(final Call call, final IOException e) {
                runOnUi(new Runnable() {
                    @Override
                    public void run() {
                        if (dialog != null) dialog.cancel();
                        if (requestCallBack != null) requestCallBack.onClientError(call, e);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    InputStream inputStream = null;
                    FileOutputStream outputStream = null;
                    try {
                        outputStream = new FileOutputStream(path);
                        inputStream = response.body().byteStream();
                        fileSize[0] = response.body().contentLength();  // 获取文件大小
                        byte[] buffer = new byte[1024];
                        int hasRead = 0;
                        while ((hasRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, hasRead);
                            downSize[0] += hasRead;
                        }
                        outputStream.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (outputStream != null) {
                            outputStream.close();
                        }
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    }
                    response.body().close();
                } else {
                    requestCallBack.onServiceError(call, response); // 提示错误
                }
                if (dialog != null) {
                    dialog.cancel();
                }
            }
        });
        /*定时对象*/
        final Timer timer = new Timer();
        /*设置进度条*/
        if (dialog != null) {
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setCancelable(false);// 设置是否可以通过点击Back键取消
            dialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
            dialog.setMax(100);
            dialog.show();
        }
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUi(new Runnable() {
                    @Override
                    public void run() {
                        double percentage = ((double) downSize[0] / (double) fileSize[0]) * 100;
                        if (dialog != null) {
                            dialog.incrementProgressBy((int) (percentage - dialog.getProgress()));  // 更新进度计算公式：(thisValue + X)/ 100 = percentage;
                        }
                        if (requestCallBack != null) {
                            requestCallBack.onProgress(downSize[0], fileSize[0], fileSize[0] == downSize[0], (int) percentage); // 回传进度
                        }
                        if (percentage >= 100.0d) {
                            if (requestCallBack != null) {
                                requestCallBack.onSuccess(System.currentTimeMillis() - startDownTime);  // 返回下载用时
                            }
                            timer.cancel();
                        }
                    }
                });
            }
        }, 100, 100);  // 延迟0.1s 每0.1s调用一次
    }

    /**
     * 下载文件 在一个新的客户端 (在下载的资源是需要身份验证的时候请不要使用)
     */
    public final void DOWNLOAD_BY_NEW(
            String url, @NonNull final String path, final ProgressDialog dialog, final RequestCallBack requestCallBack) {
        /*开始下载的时间*/
        final long startDownTime = System.currentTimeMillis();
        /*设置进度条*/
        if (dialog != null) {
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setCancelable(false);// 设置是否可以通过点击Back键取消
            dialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
            dialog.setMax(100);
            dialog.show();
        }
        /*添加下载拦截*/
        final Interceptor downProgressInterceptor = DownloadEntity.getDownProgressInterceptor(new DownloadProgressCallback() {
            @Override
            public void onDownProgress(final Long progress, final Long contentLength, final Boolean complete, final Integer percentage) {
                if (requestCallBack != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (requestCallBack != null) {
                                requestCallBack.onProgress(progress, contentLength, complete, percentage); // 回传进度
                                if (complete) {
                                    requestCallBack.onSuccess(System.currentTimeMillis() - startDownTime);  // 返回下载所用时间长度
                                }
                            }
                            if (dialog != null) {
                                dialog.incrementProgressBy(percentage - dialog.getProgress());  // 更新进度计算公式：(thisValue + X)/ 100 = percentage;
                            }
                        }
                    });
                }
            }
        });
        final OkHttpClient client = new OkHttpClient.Builder().addInterceptor(downProgressInterceptor).build(); // 构建客户端且添加拦截器
        final Call call = client.newCall(new Request.Builder().url(url).build());
        call.enqueue(new Callback() {

            @Override
            public void onFailure(final Call call, final IOException e) {
                runOnUi(new Runnable() {
                    @Override
                    public void run() {
                        if (dialog != null) dialog.cancel();
                        if (requestCallBack != null) requestCallBack.onClientError(call, e);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    InputStream inputStream = null;
                    FileOutputStream outputStream = null;
                    try {
                        outputStream = new FileOutputStream(path);
                        inputStream = response.body().byteStream();
                        byte[] buffer = new byte[1024];
                        int hasRead = 0;
                        while ((hasRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, hasRead);
                        }
                        outputStream.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (outputStream != null) {
                            outputStream.close();
                        }
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    }
                    response.body().close();
                } else {
                    if (requestCallBack != null)
                        requestCallBack.onServiceError(call, response); // 提示错误
                }
                if (dialog != null) {
                    dialog.cancel();
                }
            }
        });
    }
}
