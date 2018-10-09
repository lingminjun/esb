package com.venus.esb.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述
 * @author lingminjun
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.PACKAGE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ESBDesc {
    String value() default "";

    /**
     * 此属性只有作用于属性时有意义,忽略其字段
     * @return
     */
    boolean ignore() default false;

    /**
     * 表示此属性不对外开放,只在网关解析,仅仅属性起作用
     * @return
     */
    boolean inner() default false;

    /**
     * 可以委托网关设置的,仅仅属性起作用,暂时仅仅支持token相关的补充
     * @return
     */
    boolean entrust() default false;
}
