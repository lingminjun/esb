package com.venus.esb.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lingminjun on 17/8/16.
 */
public final class DateUtils {

    public static String toYYYY_MM_DD_HH_MM_SS(long utc) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(new Date(utc));
        return dateString;
    }

    public static String toYYYY_MM_DD_HH_MM_SS(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(date);
        return dateString;
    }

    public static String toYYYY_MM_DD_HH_MM_SS_SSS(long utc) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
        String dateString = formatter.format(new Date(utc));
        return dateString;
    }

    public static String toYYYY_MM_DD_HH_MM_SS_SSS(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
        String dateString = formatter.format(date);
        return dateString;
    }
}
