package com.venus.esb.utils;

import com.venus.esb.lang.ESBT;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: lingminjun
 * Date: 2018-06-22
 * Time: 下午6:00
 */
public final class Pickup {


    /**
     * 拾取器，从数据源中取值
     * @param source
     * @param fieldPath 支持级联，如 child.name; 支持数组，如 children[0].name
     * @return
     */
    public static Object get(Object source, String fieldPath) {
        if (source == null) {return null;}
        if (fieldPath == null || fieldPath.length() == 0) {return source;}

        //整理输入
        fieldPath = trim(fieldPath);

        if (fieldPath.length() == 0) {return source;}

        int idx = fieldPath.indexOf(".");
        String nextPath = null;
        if (idx >= 0 && idx < fieldPath.length()) {
            nextPath = fieldPath.substring(idx+1);
            fieldPath = fieldPath.substring(0,idx);
        }

        //根据类型取值
        if (source instanceof Map) {
            Map map = (Map)source;
            return get(map.get(fieldPath),nextPath);
        } else if (source instanceof List) {
            List list = (List)source;
            int i = index(fieldPath);
            if (i >= 0 && i < list.size()) {
                return get(list.get(i),nextPath);
            }
        } else if (source.getClass().isArray()) {
            int i = index(fieldPath);
            int count = Array.getLength(source);
            if (i >= 0 && i < count) {
                return get(Array.get(source,i),nextPath);
            }
        } else {//采用反射的方式取值
            Field field = ESBT.getDeclaredField(source,fieldPath);
            if (field != null) {
                Object obj = null;
                try {
                    field.setAccessible(true);
                    obj = field.get(source);
                } catch (Throwable e) {}
                return get(obj,nextPath);
            }
        }

        return null;
    }

    private static int index(String string) {
        if (!string.startsWith("[") || !string.endsWith("]")) {
            return -1;
        }
        string = string.substring(1,string.length() - 1);
        try {
            return Integer.parseInt(string);
        } catch (Throwable e) {
            return -1;
        }
    }

    private static String trim(String string) {
        string = string.trim();
        while (string.length() > 0) {
            int begin = 0;
            int end = string.length();
            if (string.charAt(0) == '.') {
                begin = 1;
            }

            if (string.charAt(end - 1) == '.') {
                end = end - 1;
            }

            if (begin == 0 && end == string.length()) {
                break;
            } else if (begin < end) {
                string = string.substring(begin,end);
            } else {
                string = "";
                break;
            }
        }
        return string;
    }
}
