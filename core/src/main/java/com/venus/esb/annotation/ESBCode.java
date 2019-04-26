package com.venus.esb.annotation;

import com.venus.esb.lang.ESBExceptionCodes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 请使用ESBError
 */
@Deprecated
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ESBCode {
    /**
     * 暴露的错误码，用于非当前ESBGroup.codeDefine定义的错误码
     * @return
     */
    int[] value();

    /**
     * 错误码定义类,定义格式如 @see ESBExceptionCodes
     *
     * @return
     */
    Class<?> codeDefine() default ESBExceptionCodes.class;
}
