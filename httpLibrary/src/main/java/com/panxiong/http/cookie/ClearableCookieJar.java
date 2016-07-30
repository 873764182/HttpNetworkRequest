package com.panxiong.http.cookie;

import okhttp3.CookieJar;

public interface ClearableCookieJar extends CookieJar {

    void clear();
}
