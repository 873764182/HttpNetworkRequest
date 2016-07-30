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

public class MainActivity extends AppCompatActivity {
    private Button mButton1;
    private Button mButton2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton1 = (Button) findViewById(R.id.button1);
        mButton2 = (Button) findViewById(R.id.button2);

        Http.getInstance().openCookie(this);

        mButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
    }
}
