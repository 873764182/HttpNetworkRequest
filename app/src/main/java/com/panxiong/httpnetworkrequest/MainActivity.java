package com.panxiong.httpnetworkrequest;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.panxiong.http.Http;
import com.panxiong.http.callback.RequestCallBack;

import java.io.File;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private Button mButton1;
    private Button mButton2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton1 = (Button) findViewById(R.id.button1);
        mButton2 = (Button) findViewById(R.id.button2);

        Http.getInstance().openCookie(this);    // 打开Cookie支持

        mButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 发送GET请求
                Http.getInstance().GET("http://www.baidu.com", new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(String result) {
                        Log.e("MainActivity", result);
                    }
                }, null);
            }
        });
        mButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 下载文件
                Http.getInstance().DOWNLOAD("http://sw.bos.baidu.com/sw-search-sp/software/573f5db9b1f6a/QQ_8.5.18600.0_setup.exe",
                        Environment.getExternalStorageDirectory().getPath() + "/qq_setup.exe", new ProgressDialog(MainActivity.this),
                        new RequestCallBack<Long>() {
                            @Override
                            public void onSuccess(Long result) {
                                Log.e("MainActivity", "下载用时（毫秒）：" + result + " ms");
                            }

                            @Override
                            public void onProgress(Long downProgress, Long contentLength, Boolean isComplete, Integer percentage) {
                                Log.e("MainActivity", "下载进度：" + percentage + " %");
                            }
                        });
            }
        });

        // 打开Cookie支持 （不需要Cookie可以不调用）
        Http.getInstance().openCookie(this);

        // 发送GET请求
        Http.getInstance().GET("http://www.baidu.com", new RequestCallBack<String>() {
            @Override
            public void onSuccess(String result) {
                Log.e("MainActivity", result);
            }
        }, null);

        // 发送POST请求
        Http.getInstance().POST("http://www.baidu.com", new HashMap<String, String>() {
            {
                put("username", "大雄");
                put("password", "123456");
            }
        }, new RequestCallBack<String>() {
            @Override
            public void onSuccess(String result) {
                Log.e("MainActivity", "服务端返回：" + result);
            }
        }, new ProgressDialog(this));

        // 下载文件
        Http.getInstance().DOWNLOAD("http://sw.bos.baidu.com/sw-search-sp/software/573f5db9b1f6a/QQ_8.5.18600.0_setup.exe",
                Environment.getExternalStorageDirectory().getPath() + "/qq_setup.exe", new ProgressDialog(MainActivity.this),
                new RequestCallBack<Long>() {
                    @Override
                    public void onSuccess(Long result) {
                        Log.e("MainActivity", "下载用时（毫秒）：" + result + " ms");
                    }

                    @Override
                    public void onProgress(Long downProgress, Long contentLength, Boolean isComplete, Integer percentage) {
                        Log.e("MainActivity", "下载进度：" + percentage + " %");
                    }
                });

        // 上传文件 支持多文件一起上传
        Http.getInstance().UPLOAD(url, params, new String[]{"文件名.jpg"}, new File[]{null}, new RequestCallBack<String>() {
            @Override
            public void onSuccess(String result) {

            }
        });
    }
}
