# HttpNetworkRequest
一个基于OkHttp与Gson还有一个第三方Cookie库封装的网络请求库



Http.getInstance().openCookie(this);    // 打开Cookie支持需要在所有请求方法前调用才能有效
                
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
                        
// 上传文件 支持多文件同时上传 且可以附带参数
Http.getInstance().UPLOAD("http://www.xxx.com", null, new String[]{"文件名.jpg"}, new File[]{file}, new RequestCallBack<String>() {
           @Override
          public void onSuccess(String result) {
                Log.e("MainActivity", "上传完成：" + result);
         }
      });
