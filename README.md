一、Dubbo工程依赖可不再依赖dubbo库

二、SpringMVC、Servlet工程依赖注意web.xml配置需要监控的请求<br>

如下：web.xml中<br>
``` 
 <filter>
    <filter-name>ESBServletFilter</filter-name>
    <filter-class>com.venus.esb.servlet.filter.ESBRequestFilter</filter-class>
 </filter>
 <filter-mapping>
    <filter-name>ESBServletFilter</filter-name>
    <url-pattern>/*</url-pattern>
 </filter-mapping>
 

```

三、SpringBoot工程请添加Scan包名
```

// Spring Boot 应用的标识
@SpringBootApplication
@MapperScan("org.spring.springboot.dao")// mapper 接口类扫描包配置
@ServletComponentScan("com.venus.esb.servlet.filter") //filter加载
public class Application {

    public static void main(String[] args) {
        // 程序启动入口
        // 启动嵌入式的 Tomcat 并初始化 Spring 环境及其各 Spring 组件
        SpringApplication.run(Application.class,args);
    }
}

```
