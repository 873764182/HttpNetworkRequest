# HttpNetworkRequest
android下一个基于OkHttp与Gson还有一个第三方Cookie库封装的网络请求库，为项目提供快速网络访问支持。


使用时用androidstudio直接导入httpLibrary即可(如果不需要修改源码,可以直接导入httpLibrary/pixel-http.jar与httpLibrary/libs下的三个jar包即可)

        // 打开Cookie支持 （不需要Cookie可以不调用，如需要Cookie支持推荐在Application的onCreate中调用。）
        Http.getInstance().openCookie(this.getApplicationContext());
        
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
        
        如果需要直接返回对象可以使用如下方式,UserEntity是一个普通实体，需要服务端返回的json数据格式与UserEntity对应。
        // 发送GET请求
        Http.getInstance().GET("http://www.baidu.com", new RequestCallBack<UserEntity>() {
            @Override
            public void onSuccess(UserEntity result) {
                Log.e("MainActivity", result.toString());
            }
        }, null);
        
        最后一个参数是一个对话框（等待框），需要在每次请求时显示一个等待对话框可以使用如下方式，具体对话框的样式可以自己定义，只要是从Dialog派生的都可以。请求库会自动打开与关闭等待对话框。
        // 发送GET请求
        Http.getInstance().GET("http://www.baidu.com", new RequestCallBack<UserEntity>() {
            @Override
            public void onSuccess(UserEntity result) {
                Log.e("MainActivity", result.toString());
            }
        }, new ProgressDialog(MainActivity.this));
        
        代码中都有详细注释，需要自己定义可以直接修改代码。
