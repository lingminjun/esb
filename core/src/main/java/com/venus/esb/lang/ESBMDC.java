package com.venus.esb.lang;

import org.slf4j.MDC;

/**
 * Created by lingminjun on 17/5/10.
 */
public final class ESBMDC {
    private static boolean hasSlf4jMDC;
    private static boolean hasLog4jMDC;
    static {
        try {
            Class.forName("org.slf4j.MDC");
            hasSlf4jMDC = true;
        } catch (ClassNotFoundException cnfe) {
            hasSlf4jMDC = false;
        }
        try {
            if (!hasSlf4jMDC) {
                Class.forName("org.apache.log4j.MDC");
                hasLog4jMDC = true;
            }
        } catch (ClassNotFoundException cnfe) {
            hasLog4jMDC = false;
        }
    }

    public static String get(String key) {
        if (key != null) {
            if (hasSlf4jMDC) {
                return MDC.get(key);
            } else if (hasLog4jMDC) {
                return org.apache.log4j.MDC.get(key).toString();
            }
        }
        return null;
    }

    public static void put(String key, String value) {
        if (key != null && value != null) {
            if (hasSlf4jMDC) {
                MDC.put(key, value);
            } else if (hasLog4jMDC) {
                org.apache.log4j.MDC.put(key, value);
            }
        }
    }

    public static void remove(String key) {
        if (key != null) {
            if (hasSlf4jMDC) {
                MDC.remove(key);
            } else if (hasLog4jMDC) {
                org.apache.log4j.MDC.remove(key);
            }
        }
    }

    public static void clear() {
        if (hasSlf4jMDC) {
            MDC.clear();
        } else if (hasLog4jMDC) {
            org.apache.log4j.MDC.clear();
        }
    }
}
