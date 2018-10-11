package com.venus.esb.brave;

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
}
