package com.venus.esb.servlet.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

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
 *
/*
@WebFilter(urlPatterns={"/*"}, asyncSupported=true, dispatcherTypes={DispatcherType.REQUEST},
        initParams=@WebInitParam(name="param1", value="value1"))*/

@WebFilter(urlPatterns={"/*"}, asyncSupported=true, dispatcherTypes={DispatcherType.REQUEST}, filterName = "ESBRequestFilter")
public class ESBRequestFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            long before = System.currentTimeMillis();
            System.out.println("===========》》》》before doFilter");
            filterChain.doFilter(servletRequest, servletResponse);
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
}
