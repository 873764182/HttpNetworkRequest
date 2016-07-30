package com.panxiong.http.utils;

import com.google.gson.internal.$Gson$Types;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by Administrator on 2016/7/30.
 * <p/>
 * JSON工具
 */
public class JsonUtil {

    /**
     * 获取对象JSON类型
     */
    public static Type getType(Class<?> subclass) {
        if (subclass == null) {
            return new TypeToken<String>() {
            }.getType();
        }
        Type superclass = subclass.getGenericSuperclass();
        if (superclass instanceof Class) {
            return new TypeToken<String>() {
            }.getType();
        } else {
            return $Gson$Types.canonicalize(
                    ((ParameterizedType) superclass)
                            .getActualTypeArguments()[0]);
        }
    }
}
