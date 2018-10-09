package com.venus.esb.factory;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * User: 凌敏均
 */
@Component
public class ESBBeanFactory implements ApplicationContextAware {

    private static ESBBeanFactory shared = null;
    public static ESBBeanFactory shared() {
        return shared;
    }

    // Spring应用上下文环境
    private static ApplicationContext context;

    /**
     * 实现ApplicationContextAware接口的回调方法，设置上下文环境
     *
     * @param applicationContext
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
        shared = this;
    }

    /**
     * 使用此方法注意,必须等待Bean完全加载,否则context可能为null
     * 获取一个以所给名字注册的bean的实例
     * @param name
     * @param <T>
     * @return
     * @throws BeansException
     */
    public static <T> T getBean(String name) throws BeansException {
        if (context == null) {/*在bean的depends-on方法或者init-method方法中调用此方法*/return null;}
        return (T)context.getBean(name);
    }

    public static <T> T getBean(Class<T> clazz) {
        if (context == null) {/*在bean的depends-on方法或者init-method方法中调用此方法*/return null;}
        return context.getBean(clazz);
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        if (context == null) {/*在bean的depends-on方法或者init-method方法中调用此方法*/return null;}
        return context.getBean(name, clazz);
    }
}
