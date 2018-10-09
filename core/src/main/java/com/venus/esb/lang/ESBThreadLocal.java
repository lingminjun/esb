package com.venus.esb.lang;

import java.util.HashMap;

/**
 * Created by lingminjun on 17/7/26.
 */
public final class ESBThreadLocal {
    public static String get(String key) {
        if (key == null) {
            return null;
        }
        return LOCAL.get().get(key);
    }

    public static String remove(String key) {
        if (key == null) {
            return null;
        }
        return LOCAL.get().remove(key);
    }

    public static void put(String key, String value) {
        if (key == null || value == null) {
            return;
        }
        LOCAL.get().put(key,value);
    }

    public static void clear() {
        LOCAL.get().clear();
    }

    private static final ThreadLocal<HashMap<String,String>> LOCAL = new ThreadLocal() {
        protected HashMap<String,String> initialValue() {
            return new HashMap<String,String>();
        }
    };
}
