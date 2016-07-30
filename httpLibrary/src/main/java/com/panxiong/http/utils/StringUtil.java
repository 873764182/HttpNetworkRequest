package com.panxiong.http.utils;

/**
 * Created by Administrator on 2016/7/30.
 * <p/>
 * String工具
 */
public class StringUtil {

    /**
     * 字符串非空
     */
    public static boolean noNull(String string) {
        if (string == null || string.length() <= 0 || "null".equals(string)) {
            return false;
        } else {
            return true;
        }
    }
}
