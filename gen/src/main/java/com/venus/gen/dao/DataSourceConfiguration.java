package com.venus.gen.dao;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Configuration
@MapperScan
public @interface DataSourceConfiguration {
    String[] value() default {};// 覆盖value参数

    // 链接地址
    Value url();

    // 链接用户名
    Value user();

    // 链接密码
    Value password();

    // 连接池驱动
    String driverClass() default "com.mysql.jdbc.Driver";

    // DAO 扫描目录
    @AliasFor(
            annotation = MapperScan.class,
            attribute = "basePackages"
    )
    String[] basePackages();

    // mapper对应的配置目录
    String mapperPath() default "classpath:sqlmap/*.xml";

    // session名字 扫描目录
    @AliasFor(
            annotation = MapperScan.class,
            attribute = "sqlSessionFactoryRef"
    )
    String sqlSessionFactoryRef();
}