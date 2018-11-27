package com.venus.esb.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lingminjun on 17/8/16.
 */
public final class DateUtils {

    public static String toShortYYYY_MM_DD(long utc) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        String dateString = formatter.format(new Date(utc));
        return dateString;
    }

    public static String toShortYYYY_MM_DD(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = formatter.format(date);
        return dateString;
    }

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

    public static String toYYYYMMDDHHMMSSSSS(long utc) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String dateString = formatter.format(new Date(utc));
        return dateString;
    }

    public static String toYYYYMMDDHHMMSSSSS(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String dateString = formatter.format(date);
        return dateString;
    }

    public static long dateYYYYMMDDHHMMSSSSS(String timestamp) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Date date = null;
        try {
            date = formatter.parse(timestamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date != null ? date.getTime() : 0;
    }

    // MYSQL Date类型支持
    /*
    ---------------------------------------------------------------------------
    类型	        字节	格式	                用途	                是否支持设置系统默认值
    date	    3	YYYY-MM-DD	        日期值	                不支持
    time	    3	HH:MM:SS	        时间值或持续时间	        不支持
    year	    1	YYYY	            年份	                    不支持
    datetime	8	YYYY-MM-DD HH:MM:SS	日期和时间混合值	        不支持
    timestamp	4	YYYYMMDD HHMMSS	    混合日期和时间，可作时间戳	支持
    ---------------------------------------------------------------------------
     */
    public static String toMYSQLDateSuitable(long utc, String type) {
        SimpleDateFormat formatter = null;
        if ("date".equalsIgnoreCase(type)) {
            formatter = new SimpleDateFormat("yyyy-MM-dd");
        } else if ("time".equalsIgnoreCase(type)) {
            formatter = new SimpleDateFormat("HH:mm:ss");
        } else if ("year".equalsIgnoreCase(type)) {
            formatter = new SimpleDateFormat("yyyy");
        } else if ("timestamp".equalsIgnoreCase(type)) {
            formatter = new SimpleDateFormat("yyyyMMdd HHmmss");
        } else {
            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
        String dateString = formatter.format(new Date(utc));
        return dateString;
    }

    public static String toMYSQLDate(long utc) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = formatter.format(new Date(utc));
        return dateString;
    }

    public static String toMYSQLDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = formatter.format(date);
        return dateString;
    }

    public static String toMYSQLTime(long utc) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        String dateString = formatter.format(new Date(utc));
        return dateString;
    }

    public static String toMYSQLTime(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        String dateString = formatter.format(date);
        return dateString;
    }

    public static String toMYSQLYear(long utc) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
        String dateString = formatter.format(new Date(utc));
        return dateString;
    }

    public static String toMYSQLYear(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
        String dateString = formatter.format(date);
        return dateString;
    }

    public static String toMYSQLDatetime(long utc) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(new Date(utc));
        return dateString;
    }

    public static String toMYSQLDatetime(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(date);
        return dateString;
    }

    public static String toMYSQLTimestamp(long utc) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd HHmmss");
        String dateString = formatter.format(new Date(utc));
        return dateString;
    }

    public static String toMYSQLTimestamp(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd HHmmss");
        String dateString = formatter.format(date);
        return dateString;
    }
}
