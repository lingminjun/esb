package com.venus.esb.utils;

/**
 * Created by lingminjun on 17/4/23.
 */
public final class JavaCodeAssist {
    public static String[] methodParamNames(Class<?> clazz, String methodName, int varCount) {
        try {
            return FileUtils.methodParamNames(clazz,methodName,varCount);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}
