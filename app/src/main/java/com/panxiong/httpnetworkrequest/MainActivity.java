package com.panxiong.httpnetworkrequest;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.panxiong.down.Down;
import com.panxiong.down.callback.DownCallBack;
import com.panxiong.http.Http;
import com.panxiong.http.callback.RequestCallBack;

public class MainActivity extends AppCompatActivity {
    private Button mButton1;
    private Button mButton2;
    private Button mButton3;
    private Button mButton4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton1 = (Button) findViewById(R.id.button1);
        mButton2 = (Button) findViewById(R.id.button2);
        mButton3 = (Button) findViewById(R.id.button3);
        mButton4 = (Button) findViewById(R.id.button4);

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
                Http.getInstance().DOWNLOAD("http://sqdd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk",
                        Environment.getExternalStorageDirectory().getPath() + "/qq_setup.exe", null,
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
        mButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Down.systemDownload(MainActivity.this,
                        "http://sqdd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk",
                        new DownCallBack() {
                            @Override
                            public void onComplete(String path) {
                                Log.e("MainActivity", "下载完成： " + path);
                            }
                        });
            }
        });
        mButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Down.browserDownload(MainActivity.this, "http://sqdd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk");
            }
        });

    }
}
