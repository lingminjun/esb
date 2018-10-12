package com.venus.esb.brave;

import com.venus.esb.lang.ESBSTDKeys;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: lingminjun
 * Date: 2018-10-11
 * Time: 下午9:12
 */
public final class Utils {
    /**
     * 获取异常信息
     * @param e
     * @return
     */
    public static String getExceptionStackTrace(Throwable e) {
        String msg = e.getMessage();
        StringBuilder builder = new StringBuilder(msg == null ? "" : msg);
        for(StackTraceElement elem : e.getStackTrace()) {
            builder.append("\t" + elem.toString() + "\n");
        }
        String var = builder.toString();
        e.printStackTrace();
        return var;
    }

    public static String getClientIp(HttpServletRequest request) {
        String ip = null;
        if (request != null) {
            ip = request.getHeader("x-forwarded-for");
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("http-x-forwarded-for");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("remote-addr");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader(ESBSTDKeys.CIP_KEY);//request.getRemoteAddr();
            }
            //
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Remote Address");//request.getRemoteAddr();
            }
        }
        return ip;
    }
}
