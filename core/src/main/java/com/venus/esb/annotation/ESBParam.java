package com.venus.esb.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ESBParam {
    /**
     * 参数名称,字母和下划线开头,只能是包含字母、数字和下划线
     */
    String name();

    /**
     * 是否为必要参数
     */
    boolean required() default true;

    /**
     * 自动注入
     */
    boolean autoInjected() default false;

    /**
     * 默认值
     */
    String defaultValue() default "";

    /**
     * 不允许在日志中输出
     */
    boolean quiet() default false;

    /**
     * 参数注释
     */
    String desc() default "";
}
