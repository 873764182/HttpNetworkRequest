package com.panxiong.down.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

/**
 * Created by panxi on 2016/4/22.
 * <p/>
 * 保存简单键值对信息(SharedPreferences)
 */
public class ShaPreUtil {
    private static final String NAME = "ydbgxt.config";
    private static SharedPreferences preferences = null;
    private static SharedPreferences.Editor editor = null;

    /**
     * 获取配置对象
     */
    public static SharedPreferences getPreferences(@NonNull Context context) {
        if (preferences == null) {
            preferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        }
        return preferences;
    }

    /**
     * 获取配置编辑对象
     */
    public static SharedPreferences.Editor getEditor(@NonNull Context context) {
        if (editor == null) {
            editor = getPreferences(context).edit();
        }
        return editor;
    }

    /**
     * 保存配置
     */
    public static boolean save(@NonNull Context context, @NonNull String key, @NonNull String value) {
        return getEditor(context).putString(key, value).commit();
    }

    /**
     * d读取配置
     */
    public static String get(@NonNull Context context, @NonNull String key) {
        return getPreferences(context).getString(key, "-1");
    }
}
