package com.venus.esb.servlet.filter;

import com.alibaba.fastjson.JSON;
import com.venus.esb.config.ESBConfigCenter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
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

    Set<String> excludes = new HashSet<String>();
    {
        excludes.add("/");
        excludes.add(".ico");
        excludes.add(".jsp");
        excludes.add(".html");
        excludes.add(".htm");
        excludes.add(".css");
        excludes.add(".js");
        excludes.add(".jpg");
        excludes.add(".png");
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            long before = System.currentTimeMillis();
            System.out.println("===========》》》》before doFilter");
            String requestUri = ((HttpServletRequest) servletRequest).getRequestURI();
            System.out.println("requestUri: " + requestUri);
            if (!ESBConfigCenter.instance().isEsbCloseFilter() || checkUri(requestUri)) {
                filterChain.doFilter(servletRequest, servletResponse);
            } else {
                // filter逻辑
            }
            long after = System.currentTimeMillis();
            System.out.println("===========》》》》after doFilter cost=" + (after-before));
        } catch (Exception ex) {
            servletRequest.setAttribute("errorMessage", ex);
            servletRequest.getRequestDispatcher("/WEB-INF/views/jsp/error.jsp")
                    .forward(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {

    }

    private boolean checkUri (String requestUri) {
        boolean flag = false;
        for (String execlude : excludes) {
            if (execlude.equals(requestUri) || requestUri.endsWith(execlude)) {
                flag = true;
                break;
            }
        }
        return flag;
    }
}
