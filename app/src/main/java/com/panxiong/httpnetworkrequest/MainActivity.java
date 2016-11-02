package com.panxiong.httpnetworkrequest;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.panxiong.down.Down;
import com.panxiong.down.callback.DownCallBack;
import com.panxiong.down.callback.ProgressCallback;
import com.panxiong.down.utils.BreakpointDownUtil;
import com.panxiong.http.Http;
import com.panxiong.http.callback.RequestCallBack;

public class MainActivity extends AppCompatActivity {
    private Button mButton1;
    private Button mButton2;
    private Button mButton3;
    private Button mButton4;
    private Button mButton5;
    private Button mButton6;

    private volatile BreakpointDownUtil.TaskEntity taskEntity = null;

    private String DOWN_URL = "http://sqdd.myapp.com/myapp/qqteam/Androidlite/qqlite_3.5.0.660_android_r108360_GuanWang_537047121_release_10000484.apk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton1 = (Button) findViewById(R.id.button1);
        mButton2 = (Button) findViewById(R.id.button2);
        mButton3 = (Button) findViewById(R.id.button3);
        mButton4 = (Button) findViewById(R.id.button4);
        mButton5 = (Button) findViewById(R.id.button5);
        mButton6 = (Button) findViewById(R.id.button6);

        Http.getInstance().openCookie(getApplicationContext());    // 打开Cookie支持

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
                Http.getInstance().DOWNLOAD(DOWN_URL,
                        Environment.getExternalStorageDirectory().getPath() + "/qq_setup.jpg", null,
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
                        DOWN_URL,
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
                Down.browserDownload(MainActivity.this, DOWN_URL);
            }
        });

        mButton5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                dialog.setCancelable(true);// 设置是否可以通过点击Back键取消
                dialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
                dialog.setMax(100);
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (taskEntity != null) taskEntity.closeDown(false);    // 不清除任务
                        Toast.makeText(MainActivity.this, "暂停下载", Toast.LENGTH_LONG).show();
                    }
                });
                dialog.show();

                taskEntity = Down.breakpointDownload(MainActivity.this,
                        DOWN_URL,
                        Environment.getExternalStorageDirectory().getPath() + "/qqlite_3.5.0.660.apk",
                        new ProgressCallback() {
                            @Override
                            public void onDownProgress(Long progress, Long contentLength, Boolean complete, Integer percentage) {
                                Log.e("breakpointDownload", progress + " 当前下载: " + percentage + " %");
                                dialog.incrementProgressBy(percentage - dialog.getProgress());
                                if (complete) {
                                    dialog.dismiss();
                                }
                            }
                        }
                );
            }
        });
        mButton6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }
}
