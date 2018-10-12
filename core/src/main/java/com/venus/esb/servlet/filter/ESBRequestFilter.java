package com.venus.esb.servlet.filter;

import com.github.kristofa.brave.Brave;
import com.venus.esb.brave.ESBBraveFactory;
import com.venus.esb.brave.Utils;
import com.venus.esb.config.ESBConfigCenter;
import com.venus.esb.lang.*;
import com.venus.esb.servlet.brave.HTTPResponseAdapter;
import com.venus.esb.servlet.brave.HTTPServerRequestAdapter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by lingminjun on 17/8/10.
 * 监控所有请求，TODO : 补充完整
 *
 * 自行在web.xml中配置均可
 *
 <filter>
    <filter-name>ESBServletFilter</filter-name>
    <filter-class>com.venus.esb.servlet.filter.ESBRequestFilter</filter-class>
 </filter>
 <filter-mapping>
    <filter-name>ESBServletFilter</filter-name>
    <url-pattern>/*</url-pattern>
 </filter-mapping>
 *
 *
 * 或者启动配置扫描目录（SpringBoot工程）
 * @ServletComponentScan("com.venus.esb.servlet.filter")
 */

@WebFilter(urlPatterns={"/*"}, asyncSupported=true, dispatcherTypes={DispatcherType.REQUEST}, filterName = "ESBRequestFilter")
public class ESBRequestFilter implements Filter {

    //可能的文件资源，不做filter
    Set<String> excludes = new HashSet<String>();
    {
        excludes.add("/");

        excludes.add(".jsp");
        excludes.add(".html");
        excludes.add(".htm");
        excludes.add(".css");
        excludes.add(".js");

        excludes.add(".ico");
        excludes.add(".bmp");
        excludes.add(".jpg");
        excludes.add(".ppeg");
        excludes.add(".png");
        excludes.add(".gif");
        excludes.add(".webp");//

        excludes.add(".mp4");
        excludes.add(".3gp");
        excludes.add(".avi");
        excludes.add(".mkv");
        excludes.add(".wmv");
        excludes.add(".mpg");
        excludes.add(".vob");
        excludes.add(".flv");
        excludes.add(".mov");
        excludes.add(".xv");
        excludes.add(".rm");
        excludes.add(".rmvb");
        excludes.add(".mpeg");
        excludes.add(".webm");//

        excludes.add(".mp3");

        excludes.add(".xsl");
        excludes.add(".xml");
        excludes.add(".txt");
        excludes.add(".rtf");
        excludes.add(".doc");
        excludes.add(".ttf");
        excludes.add(".pdf");

//        excludes.add(".com");
        excludes.add(".exe");
        excludes.add(".zip");
        excludes.add(".apk");
        excludes.add(".ipa");
        excludes.add(".xap");

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        String err = null;
        String result = "hide";
        boolean success = true;
        try {

            if (ESBConfigCenter.instance().isEsbCloseFilter() || excludedRequest(servletRequest)) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }

            ESBContext.getContext().clear();//防止脏数据,效率上不合理
            TracingInfo tracing = logContext(servletRequest);


            //zipkin监控
            try {
                braveInvoking(tracing);
            } catch (Throwable e) {e.printStackTrace();}

            long before = System.currentTimeMillis();
            System.out.println("===========》》》》before doFilter");

            filterChain.doFilter(servletRequest, servletResponse);

            //获取servletResponse状态
            if (servletResponse instanceof HttpServletResponse) {
                if (((HttpServletResponse) servletResponse).getStatus() == 200) {
                    success = true;

                    //简单处理，不记录返回值
                } else {
                    success = false;
                }
            }

            long after = System.currentTimeMillis();
            System.out.println("===========》》》》after doFilter cost=" + (after-before));

        } catch (Throwable e) {
            success = false;
            err = Utils.getExceptionStackTrace(e);
            servletRequest.setAttribute("errorMessage", e);
            servletRequest.getRequestDispatcher("/WEB-INF/views/jsp/error.jsp")
                    .forward(servletRequest, servletResponse);
        } finally {
            //必须清理，放置下次进入脏数据
            ESBContext.getContext().clear();

            //日志处理
            try {
                braveInvoked(success,result,err);
            } catch (Throwable e) {e.printStackTrace();}
        }
    }

    @Override
    public void destroy() {

    }

    private String getPath(ServletRequest servletRequest) {
        if (servletRequest instanceof HttpServletRequest) {
            String path = ((HttpServletRequest) servletRequest).getRequestURI();
            return path;
        }
        return "";
    }

    private String extPath(String path) {
        if (path == null) {
            return "";
        }
        int idx = path.lastIndexOf(".");
        if (idx > 0 && idx < path.length()) {
            return path.substring(idx, path.length() - 1).toLowerCase();
        }
        return "";
    }

    private boolean excludedRequest(ServletRequest servletRequest) {
        String path = getPath(servletRequest);
        if ("/".equals(path)) {
            return true;
        }
        String ext = extPath(path);
        return excludes.contains(ext);
    }

    private static TracingInfo logContext(ServletRequest servletRequest) {

        TracingInfo info = new TracingInfo();

        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest request = ((HttpServletRequest) servletRequest);

            info.method = request.getMethod();
            info.url = request.getRequestURI();
            info.path = request.getContextPath();
            info.clientIp = Utils.getClientIp(request);
            info.clientPort = request.getRemotePort();
            info.serverIp = request.getLocalAddr();

            //请参考写入head时记录
            String tracing = request.getHeader(ESBConsts.HTTP_ESB_TRACING_HEADER);
            if (tracing != null) {
                String[] strs = tracing.split(ESBConsts.HTTP_ESB_SPLIT, -1);//防止尾部丢失
                if (strs.length == 4) {
                    ESBThreadLocal.put(ESBSTDKeys.PARENT_CID_KEY,strs[0]);//从传过来的cid来做parent cid
                    ESBThreadLocal.put(ESBSTDKeys.TID_KEY,strs[1]);//记录tid,继续透传

                    //确保日志能打印
                    ESBMDC.put(ESBSTDKeys.CID_KEY,strs[0]);//仅仅用于日志打印
                    ESBMDC.put(ESBSTDKeys.TID_KEY,strs[1]);

                    info.cid = strs[0];
                    info.tid = strs[1];
                    info.parentCid = strs[2];
                    info.clientName = strs[3];
                }
            }

            String ctxstr = request.getHeader(ESBConsts.HTTP_ESB_CONTEXT_HEADER);
            if (ctxstr != null) {
                String[] strs = ctxstr.split(ESBConsts.HTTP_ESB_SPLIT, -1);//防止尾部丢失
                if (strs.length == 5) {
                    ESBThreadLocal.put(ESBSTDKeys.L10N_KEY,strs[0]);//保留上一次的
                    //必要的业务数据传递
                    ESBThreadLocal.put(ESBSTDKeys.AID_KEY,strs[1]);
                    ESBThreadLocal.put(ESBSTDKeys.DID_KEY,strs[2]);
                    ESBThreadLocal.put(ESBSTDKeys.UID_KEY,strs[3]);
                    ESBThreadLocal.put(ESBSTDKeys.PID_KEY,strs[4]);
                    ESBMDC.put(ESBSTDKeys.AID_KEY,strs[1]);
                    ESBMDC.put(ESBSTDKeys.DID_KEY,strs[2]);
                    ESBMDC.put(ESBSTDKeys.UID_KEY,strs[3]);
                    ESBMDC.put(ESBSTDKeys.PID_KEY,strs[4]);
                }
            }

            info.spanId = request.getHeader(ESBConsts.ZIPKIN_BRAVE_SPAN_ID_KEY);
            if (info.spanId == null) {//兼容 slueth zipkin（spring-cloud-sleuth-zipkin）以下两个字段，是否兼容处理
                info.spanId = request.getHeader("X-B3-SpanId");
            }

        }

        return info;
    }

    private void braveInvoking(TracingInfo tracing) {
        Brave brave = ESBBraveFactory.getBrave();
        if (brave == null) {
            return;
        }

        brave.serverRequestInterceptor().handle(new HTTPServerRequestAdapter(brave,tracing.method,tracing.url,tracing.path,tracing.spanId,tracing.serverIp,tracing.clientName,tracing.clientIp,tracing.clientPort));
    }

    private void braveInvoked(boolean success, String result, String errorMessage) {
        Brave brave = ESBBraveFactory.getBrave();
        if (brave == null) {
            return;
        }

        brave.serverResponseInterceptor().handle(new HTTPResponseAdapter(false,success,result,errorMessage));
    }


    private static class TracingInfo {
        public String method;
        public String url;
        public String path;
        public String tid;
        public String cid;
        public String parentCid;
        public String clientName;
        public String spanId;
        public String clientIp;
        public int clientPort;
        public String serverIp;
    }
}
