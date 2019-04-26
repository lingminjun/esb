package com.venus.esb.annotation;

import com.venus.esb.lang.ESBExceptionCodes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法错误码可以定义多个，由于来自不同的ESBExceptionCodes文件
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ESBError {
    /**
     * 当前域暴露的错误码，只需要简单定义code
     * 也可以在通过ESBException.isExposed来控制对外暴露
     * 所有错误定义描述将会查找 from 指定的文件中定义
     * 若没有找到,则忽略
     * @return
     */
    int[] value();

    /**
     * 其他域暴露的错误，需要额外指定codeDefine类
     * @return
     */
    @Deprecated
    ESBCode[] codes() default {};

    /**
     * 错误码定义类,定义格式如 @see ESBExceptionCodes
     *
     * @return
     */
    Class<?> from() default ESBExceptionCodes.class;
}
