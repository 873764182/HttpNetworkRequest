package com.panxiong.down.utils;

/**
 * Created by Administrator on 2016/8/1.
 * <p/>
 * 字符串工具对象
 */
public class StringUtil {

    public static String getNameByUrl(String url) {
        if (!noNull(url)) return "";
        return url.substring(url.lastIndexOf("/") + 1, url.length());
    }

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
