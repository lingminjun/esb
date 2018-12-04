package com.venus.gen;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: lingminjun
 * Date: 2018-10-20
 * Time: 下午2:34
 */
public final class SpringXMLConst {
    public static final String SPRING_XSD_VERSION = "4.0";

    public static final String SPRING_XML_CONFIG_HEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" +
            "       xmlns:context=\"http://www.springframework.org/schema/context\"\n" +
            "       xmlns:tx=\"http://www.springframework.org/schema/tx\"\n" +
            "       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "       xsi:schemaLocation=\"http://www.springframework.org/schema/beans\n" +
            "           http://www.springframework.org/schema/beans/spring-beans-" + SPRING_XSD_VERSION + ".xsd\n" +
            "           http://www.springframework.org/schema/context\n" +
            "           http://www.springframework.org/schema/context/spring-context-" + SPRING_XSD_VERSION + ".xsd\n" +
            "           http://www.springframework.org/schema/tx\n" +
            "           http://www.springframework.org/schema/tx/spring-tx-" + SPRING_XSD_VERSION + ".xsd\"\n" +
            "       default-lazy-init=\"true\">\n" +
            "\n" +
            "    <context:annotation-config/>\n" +
            "    <context:property-placeholder file-encoding=\"UTF-8\"\n" +
            "                                  ignore-unresolvable=\"true\"\n" +
            "                                  ignore-resource-not-found=\"true\"\n" +
            "                                  location=\"classpath:xconfig.properties\"/>\n\n" +
            "    <!-- 自动寻找注入bean -->\n" +
            "    <!--<context:component-scan base-package=\"com.venus.custom.beans\"/>-->\n\n    ";

    public static final String SPRING_MVC_XML_CONFIG_HEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" +
            "       xmlns:context=\"http://www.springframework.org/schema/context\"\n" +
            "       xmlns:mvc=\"http://www.springframework.org/schema/mvc\"\n" +
            "       xmlns:tx=\"http://www.springframework.org/schema/tx\"\n" +
            "       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "       xsi:schemaLocation=\"http://www.springframework.org/schema/beans\n" +
            "           http://www.springframework.org/schema/beans/spring-beans-" + SPRING_XSD_VERSION + ".xsd\n" +
            "           http://www.springframework.org/schema/context\n" +
            "           http://www.springframework.org/schema/context/spring-context-" + SPRING_XSD_VERSION + ".xsd\n" +
            "           http://www.springframework.org/schema/tx\n" +
            "           http://www.springframework.org/schema/tx/spring-tx-" + SPRING_XSD_VERSION + ".xsd\"\n" +
            "           http://www.springframework.org/schema/mvc\n" +
            "           http://www.springframework.org/schema/mvc/spring-mvc-" + SPRING_XSD_VERSION + ".xsd\"\n" +
            "       default-lazy-init=\"true\">\n" +
            "\n" +
            "    <context:annotation-config/>\n" +
            "    <context:property-placeholder file-encoding=\"UTF-8\"\n" +
            "                                  ignore-unresolvable=\"true\"\n" +
            "                                  ignore-resource-not-found=\"true\"\n" +
            "                                  location=\"classpath:xconfig.properties\"/>\n\n" +
            "    <!-- 自动寻找注入bean -->\n" +
            "    <!--<context:component-scan base-package=\"com.venus.custom.beans\"/>-->\n\n    ";

    private static final String DUBBO_PROVIDER_XML_CONFIG_HEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" +
            "       xmlns:context=\"http://www.springframework.org/schema/context\"\n" +
            "       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "       xmlns:dubbo=\"http://code.alibabatech.com/schema/dubbo\"\n" +
            "       xsi:schemaLocation=\"http://www.springframework.org/schema/beans\n" +
            "            http://www.springframework.org/schema/beans/spring-beans-" + SPRING_XSD_VERSION + ".xsd\n" +
            "            http://www.springframework.org/schema/context\n" +
            "            http://www.springframework.org/schema/context/spring-context-" + SPRING_XSD_VERSION + ".xsd\n" +
            "            http://code.alibabatech.com/schema/dubbo\n" +
            "            http://code.alibabatech.com/schema/dubbo/dubbo.xsd\"\n" +
            "       default-lazy-init=\"true\">\n" +
            "    <context:annotation-config/>\n" +
            "    <context:property-placeholder file-encoding=\"UTF-8\"\n" +
            "                                  ignore-unresolvable=\"true\"\n" +
            "                                  ignore-resource-not-found=\"true\"\n" +
            "                                  location=\"classpath:xconfig.properties\"/>\n" +
            "\n" +
            "    <bean id=\"dubboApplicationConfig\" class=\"com.alibaba.dubbo.config.ApplicationConfig\">\n" +
            "        <property name=\"name\" value=\"@ApplicationName@\"/>\n" +
            "    </bean>\n" +
            "\n" +
            "    <bean id=\"dubboRegistryConfig\" class=\"com.alibaba.dubbo.config.RegistryConfig\">\n" +
            "        <property name=\"address\" value=\"${dubbo.registry.url}\"/>\n" +
            "    </bean>\n" +
            "\n" +
            "    <bean id=\"dubboProtocolConfig\" class=\"com.alibaba.dubbo.config.ProtocolConfig\">\n" +
            "        <property name=\"port\" value=\"-1\"/>\n" +
            "    </bean>\n" +
            "\n" +
            "    <!--<import resource=\"classpath*:@ApplicationName@-application-persistence.xml\"/>-->\n" +
            "\n" +
            "    <!-- 统一配置provider -->\n" +
            "    <dubbo:provider application=\"dubboApplicationConfig\"\n" +
            "                    version=\"${dubbo.export.version}\"\n" +
            "                    protocol=\"dubboProtocolConfig\"\n" +
            "                    registry=\"dubboRegistryConfig\"\n" +
            "                    timeout=\"${dubbo.export.timeout}\"\n" +
            "                    retries=\"0\"/>\n\n    ";

    public static final String theDubboProviderXmlConfigHead(String application) {
        return DUBBO_PROVIDER_XML_CONFIG_HEAD.replaceAll(APPLICATION_NAME,application);
    }

    public static final String DUBBO_PROVIDER_CONFIG = "<!-- 统一配置provider -->\n" +
            "    <dubbo:provider application=\"dubboApplicationConfig\"\n" +
            "                    version=\"${dubbo.export.version}\"\n" +
            "                    protocol=\"dubboProtocolConfig\"\n" +
            "                    registry=\"dubboRegistryConfig\"\n" +
            "                    timeout=\"${dubbo.export.timeout}\"\n" +
            "                    retries=\"0\"/>\n\n    ";

    public static final String JDBC_DATASOURCE_MASK = "@Datasource@";
    public static final String SQL_SESSION_FACTORY_MASK = "@SqlSessionFactory@";
    public static final String MAPPER_XML_PATH_MASK = "@MapperXmlPath@";

    private static final String SPRING_JDBC_DATASOURCE = "    <tx:annotation-driven transaction-manager=\"transactionManager\"/>\n" +
            "    \n" +
            "    <!-- Datasource配置：jdbc链接池配置 -->\n" +
            "    <bean id=\"@Datasource@\" class=\"org.apache.tomcat.jdbc.pool.DataSource\" destroy-method=\"close\">\n" +
            "        <property name=\"poolProperties\">\n" +
            "            <bean class=\"org.apache.tomcat.jdbc.pool.PoolProperties\">\n" +
            "                <property name=\"driverClassName\" value=\"com.mysql.jdbc.Driver\"/>\n" +
            "                <property name=\"url\" value=\"${com.venus.@ApplicationName@.mysql.datasource.url}\"/>\n" +
            "                <property name=\"username\" value=\"${com.venus.@ApplicationName@.mysql.datasource.username}\"/>\n" +
            "                <property name=\"password\" value=\"${com.venus.@ApplicationName@.mysql.datasource.password}\"/>\n" +
            "                <property name=\"jmxEnabled\" value=\"false\"/>\n" +
            "                <property name=\"testWhileIdle\" value=\"false\"/>\n" +
            "                <property name=\"initialSize\" value=\"10\"/>\n" +
            "                <property name=\"maxActive\" value=\"100\"/>\n" +
            "                <property name=\"maxIdle\" value=\"30\"/>\n" +
            "                <property name=\"minIdle\" value=\"15\"/>\n" +
            "                <property name=\"defaultAutoCommit\" value=\"true\"/>\n" +
            "                <property name=\"maxWait\" value=\"50000\"/>\n" +
            "                <property name=\"removeAbandoned\" value=\"true\"/>\n" +
            "                <property name=\"removeAbandonedTimeout\" value=\"60\"/>\n" +
            "                <property name=\"testOnBorrow\" value=\"true\"/>\n" +
            "                <property name=\"testOnReturn\" value=\"false\"/>\n" +
            "                <property name=\"validationQuery\" value=\"SELECT 1\"/>\n" +
            "                <property name=\"validationInterval\" value=\"60000\"/>\n" +
            "                <property name=\"validationQueryTimeout\" value=\"3\"/>\n" +
            "                <property name=\"timeBetweenEvictionRunsMillis\" value=\"300000\"/>\n" +
            "                <property name=\"minEvictableIdleTimeMillis\" value=\"1800000\"/>\n" +
            "                <property name=\"jdbcInterceptors\"\n" +
            "                          value=\"org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer\"/>\n" +
            "            </bean>\n" +
            "        </property>\n" +
            "    </bean>\n" +
            "\n" +
            "    <!-- 注意：若只读Datasource，则需要注释以下事务（tomcat jdbc pool,读写库需要使用事务）-->\n" +
            "    <bean id=\"transactionManager\" class=\"org.springframework.jdbc.datasource.DataSourceTransactionManager\">\n" +
            "        <property name=\"dataSource\" ref=\"@Datasource@\"/>\n" +
            "    </bean>\n" +
            "    <bean id=\"transactionTemplate\" class=\"org.springframework.transaction.support.TransactionTemplate\">\n" +
            "        <property name=\"transactionManager\" ref=\"transactionManager\"/>\n" +
            "    </bean>\n" +
            "    <!-- 注意：若只读Datasource，则需要注释以上事务（tomcat jdbc pool,读写库需要使用事务）-->\n" +
            "\n" +
            "    <!-- SQL Session -->\n" +
            "    <bean id=\"@SqlSessionFactory@\" class=\"org.mybatis.spring.SqlSessionFactoryBean\">\n" +
            "        <property name=\"dataSource\" ref=\"@Datasource@\"/>\n" +
            "        <property name=\"configLocation\" value=\"classpath:@MapperXmlPath@\"/>\n" +
            "    </bean>\n\n    ";
    public static final String theJdbcDatasource(String project, String datasource, String sessionFactory, String mapperPath) {
        String str = SPRING_JDBC_DATASOURCE.replaceAll(JDBC_DATASOURCE_MASK,datasource);
        str = str.replaceAll(SQL_SESSION_FACTORY_MASK,sessionFactory);
        str = str.replaceAll(MAPPER_XML_PATH_MASK,mapperPath);
        str = str.replaceAll(APPLICATION_NAME,(project == null || project.length() == 0) ? "default" : project);
        return str;
    }

    public static final String MAPPER_XML_CONFIG_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<!DOCTYPE configuration PUBLIC \"-//mybatis.org//DTD Config 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-config.dtd\" >\n" +
            "<configuration>\n" +
            "    <settings>\n" +
            "        <!-- 全局映射器启用缓存 -->\n" +
            "        <setting name=\"cacheEnabled\" value=\"false\"/>\n" +
            "        <!-- 查询时，关闭关联对象即时加载以提高性能 -->\n" +
            "        <setting name=\"lazyLoadingEnabled\" value=\"false\"/>\n" +
            "        <!-- 设置关联对象加载的形态，此处为按需加载字段(加载字段由SQL指 定)，不会加载关联表的所有字段，以提高性能 -->\n" +
            "        <setting name=\"aggressiveLazyLoading\" value=\"false\"/>\n" +
            "        <!-- 对于未知的SQL查询，允许返回不同的结果集以达到通用的效果 -->\n" +
            "        <setting name=\"multipleResultSetsEnabled\" value=\"true\"/>\n" +
            "        <!-- 允许使用列标签代替列名 -->\n" +
            "        <setting name=\"useColumnLabel\" value=\"true\"/>\n" +
            "        <!-- 允许使用自定义的主键值(比如由程序生成的UUID 32位编码作为键值)，数据表的PK生成策略将被覆盖 -->\n" +
            "        <setting name=\"useGeneratedKeys\" value=\"true\"/>\n" +
            "        <!-- 给予被嵌套的resultMap以字段-属性的映射支持 -->\n" +
            "        <setting name=\"autoMappingBehavior\" value=\"FULL\"/>\n" +
            "        <!-- 对于批量更新操作缓存SQL以提高性能 -->\n" +
            "        <setting name=\"defaultExecutorType\" value=\"SIMPLE\"/>\n" +
            "        <!-- 数据库超过25000秒仍未响应则超时 -->\n" +
            "        <setting name=\"defaultStatementTimeout\" value=\"25000\"/>\n" +
            "        <!-- 日志输出，便于调试 -->\n" +
            "        <!-- <setting name=\"logImpl\" value=\"STDOUT_LOGGING\"/> -->\n" +
            "    </settings>\n" +
            "    <!-- 全局别名设置，在映射文件中只需写别名，而不必写出整个类路径 别名声明写这里 -->\n" +
            "    <typeAliases>\n" +
            "        <!-- 非注解的sql映射文件配置，如果使用mybatis注解，该mapper无需配置，但是如果mybatis注解中包含@resultMap注解，则mapper必须配置，给resultMap注解使用 -->\n" +
            "    </typeAliases>\n" +
            "    <mappers>\n" +
            "    </mappers>\n" +
            "</configuration>";

    public static final String MAPPER_SQLMAP_PATH = "@sqlmap@";
    private static final String MAPPER_RESOURCE_CONFIG = "        <mapper resource=\"@sqlmap@\"  />\n";
    public static final String theMapper(String sqlmap) {
        return MAPPER_RESOURCE_CONFIG.replaceAll(MAPPER_SQLMAP_PATH,"sqlmap" + File.separator + sqlmap);
    }

    public static final String BEAN_NAME = "@BeanName@";
    public static final String BEAN_CLASS_NAME = "@BeanClassName@";
    private static final String MAPPER_BEAN = "    <bean id=\"@BeanName@\" class=\"org.mybatis.spring.mapper.MapperFactoryBean\">\n" +
            "        <property name=\"sqlSessionFactory\" ref=\"@SqlSessionFactory@\"/>\n" +
            "        <property name=\"mapperInterface\" value=\"@BeanClassName@\"/>\n" +
            "    </bean>\n\n";
    public static final String theMapperBean(String beanName, String beanClass, String sessionFactory) {
        String str = MAPPER_BEAN.replaceAll(BEAN_NAME,beanName);
        str = str.replaceAll(BEAN_CLASS_NAME,beanClass);
        str = str.replaceAll(SQL_SESSION_FACTORY_MASK,sessionFactory);
        return str;
    }

    public static final String IMPL_CLASS_NAME = "@BeanImplClassName@";
    private static final String DUBBO_PROVIDER_BEAN = "<bean id=\"@BeanName@Impl\" class=\"@BeanImplClassName@\"/>\n" +
            "    <bean id=\"@BeanName@\" class=\"com.alibaba.dubbo.config.spring.ServiceBean\">\n" +
            "        <property name=\"interface\" value=\"@BeanClassName@\"/>\n" +
            "        <property name=\"ref\" ref=\"@BeanName@Impl\"/>\n" +
            "    </bean>\n\n    ";
    public static final String theDubboProvider(String beanName, String beanClass, String implClass) {
        String str = DUBBO_PROVIDER_BEAN.replaceAll(BEAN_NAME,beanName);
        str = str.replaceAll(BEAN_CLASS_NAME,beanClass);
        str = str.replaceAll(IMPL_CLASS_NAME,implClass);
        return str;
    }

    private static final String SPRING_BEAN = "<bean id=\"@BeanName@\" class=\"@BeanClassName@\"/>\n\n    ";
    public static final String theSpringBean(String beanName, String beanClass) {
        String str = SPRING_BEAN.replaceAll(BEAN_NAME,beanName);
        str = str.replaceAll(BEAN_CLASS_NAME,beanClass);
        return str;
    }

    public static final String APPLICATION_NAME = "@ApplicationName@";
    public static final String DUBBO_APPLICATION_CONFIG = "<bean id=\"dubboApplicationConfig\" class=\"com.alibaba.dubbo.config.ApplicationConfig\">\n" +
            "        <property name=\"name\" value=\"@ApplicationName@\"/>\n" +
            "    </bean>\n\n    ";
    public static final String theDubboApplicationConfig(String applicationName) {
        return DUBBO_APPLICATION_CONFIG.replaceAll(APPLICATION_NAME,applicationName);
    }

    public static final String DUBBO_REGISTRY_CONFIG = "<bean id=\"dubboRegistryConfig\" class=\"com.alibaba.dubbo.config.RegistryConfig\">\n" +
            "        <property name=\"address\" value=\"${dubbo.registry.url}\"/>\n" +
            "    </bean>\n\n    ";

    public static final String DUBBO_PROTOCOL_CONFIG = "<bean id=\"dubboProtocolConfig\" class=\"com.alibaba.dubbo.config.ProtocolConfig\">\n" +
            "        <property name=\"port\" value=\"-1\"/>\n" +
            "    </bean>\n\n    ";



    public final static String CONFIG_PROPERTIES_RELATIVE_PATH_MASK = "@RelativePath@";//WEB-INF/classes/
    private final static String AUTO_CONFIG_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n" +
            "<config description=\"autoConf配置信息\">\n" +
            "    <group name=\"com.venus.@ApplicationName@.log.setting\">\n" +
            "        <property name=\"com.venus.@ApplicationName@.log.home\" description=\"日志目录\" required=\"true\"/>\n" +
            "    </group>\n" +
            "\n    <script>\n" +
            "        <generate template=\"config.properties.vm\" destfile=\"@RelativePath@config.properties\" charset=\"UTF-8\"/>\n" +
            "    </script>\n" +
            "</config>";
    public final static String theAutoConfigXml(String project, String configRelativePath) {
        String str =  AUTO_CONFIG_XML.replaceAll(APPLICATION_NAME,project);
        str = str.replaceAll(CONFIG_PROPERTIES_RELATIVE_PATH_MASK,configRelativePath == null ? "" : configRelativePath);
        return str;
    }

    public final static String ADD_DUBBO_AUTO_CONFIG_GROUP = "    <group name=\"com.venus.dubbo.setting\">\n" +
            "        <property name=\"dubbo.registry.url\" description=\"Dubbo服务注册地址\" required=\"true\"/>\n" +
            "        <property name=\"dubbo.reference.version\" description=\"Dubbo服务引用版本号\" required=\"true\"/>\n" +
            "        <property name=\"dubbo.export.version\" description=\"Dubbo服务暴露版本号\" required=\"true\"/>\n" +
            "        <property name=\"dubbo.export.timeout\" description=\"Dubbo服务暴露超时时间\" required=\"true\"/>\n" +
            "    </group>";

    private final static String ADD_DATASOURCE_AUTO_CONFIG_GROUP = "    <group name=\"com.venus.@ApplicationName@.datasource.setting\">\n" +
            "        <property name=\"com.venus.@ApplicationName@.mysql.datasource.url\" description=\"数据库地址\" required=\"true\"/>\n" +
            "        <property name=\"com.venus.@ApplicationName@.mysql.datasource.username\" description=\"数据库用户名\" required=\"true\"/>\n" +
            "        <property name=\"com.venus.@ApplicationName@.mysql.datasource.password\" description=\"数据库密码\" required=\"true\"/>\n" +
            "    </group>";
    public final static String theDatasourceAutoConfigGroup(String application) {
        return ADD_DATASOURCE_AUTO_CONFIG_GROUP.replaceAll(APPLICATION_NAME,application);
    }

    private final static String AUTO_CONFIG_VM = "dubbo.registry.url=${dubbo.registry.url}\n" +
            "dubbo.reference.version=${dubbo.reference.version}\n" +
            "dubbo.export.version=${dubbo.export.version}\n" +
            "dubbo.export.timeout=${dubbo.export.timeout}\n\n" +
            "com.venus.@ApplicationName@.mysql.datasource.url=${com.venus.@ApplicationName@.mysql.datasource.url}\n" +
            "com.venus.@ApplicationName@.mysql.datasource.username=${com.venus.@ApplicationName@.mysql.datasource.username}\n" +
            "com.venus.@ApplicationName@.mysql.datasource.password=${com.venus.@ApplicationName@.mysql.datasource.password}\n\n" +
            "com.venus.@ApplicationName@.log.home=${com.venus.@ApplicationName@.log.home}";
    public final static String theAutoConfigVMGroup(String application) {
        return AUTO_CONFIG_VM.replaceAll(APPLICATION_NAME,application);
    }

    public final static String ADD_DUBBO_PROPERTIES_CONFIG = "dubbo.registry.url=${dubbo.registry.url}\n" +
            "dubbo.reference.version=${dubbo.reference.version}\n" +
            "dubbo.export.version=${dubbo.export.version}\n" +
            "dubbo.export.timeout=${dubbo.export.timeout}\n";

    private final static String ADD_DATASOURCE_PROPERTIES_CONFIG = "com.venus.@ApplicationName@.mysql.datasource.url=${com.venus.@ApplicationName@.mysql.datasource.url}\n" +
            "com.venus.@ApplicationName@.mysql.datasource.username=${com.venus.@ApplicationName@.mysql.datasource.username}\n" +
            "com.venus.@ApplicationName@.mysql.datasource.password=${com.venus.@ApplicationName@.mysql.datasource.password}\n";

    private final static String ADD_DATASOURCE_PROPERTIES_CONFIG_DEMO = "# @ApplicationName@数据库连接配置\n" +
            "com.venus.@ApplicationName@.mysql.datasource.url=jdbc:mysql://127.0.0.1:3306/demo?autoReconnect=true&useUnicode=true&characterEncoding=utf8\n" +
            "com.venus.@ApplicationName@.mysql.datasource.username=root\n" +
            "com.venus.@ApplicationName@.mysql.datasource.password=root\n";

    public final static String theDatasourcePropertiesConfig(String application, boolean vm) {
        if (vm) {
            return ADD_DATASOURCE_PROPERTIES_CONFIG.replaceAll(APPLICATION_NAME, application);
        } else {
            return ADD_DATASOURCE_PROPERTIES_CONFIG_DEMO.replaceAll(APPLICATION_NAME, application);
        }
    }

    private final static String SERVICE_CONFIG_DEMO = "# dubbo配置\n" +
            "dubbo.registry.url = zookeeper://127.0.0.1:2181\n" +
            "dubbo.reference.version=DEV1\n" +
            "dubbo.export.version=DEV1\n" +
            "dubbo.export.timeout=30000\n\n" +
            "# @ApplicationName@数据库连接配置\n" +
            "com.venus.@ApplicationName@.mysql.datasource.url=jdbc:mysql://127.0.0.1:3306/demo?autoReconnect=true&useUnicode=true&characterEncoding=utf8\n" +
            "com.venus.@ApplicationName@.mysql.datasource.username=root\n" +
            "com.venus.@ApplicationName@.mysql.datasource.password=root\n\n" +
            "# 基本配置\n" +
            "com.venus.@ApplicationName@.log.home=/Users/lingminjun/logs/@ApplicationName@-service/\n";

    public final static String ADD_DUBBO_PROPERTIES_CONFIG_DEMO = "# dubbo配置\n" +
            "dubbo.registry.url = zookeeper://127.0.0.1:2181\n" +
            "dubbo.reference.version=DEV1\n" +
            "dubbo.export.version=DEV1\n" +
            "dubbo.export.timeout=30000\n\n";

    public final static String theConfigProperties(String application) {
        return AUTO_CONFIG_VM.replaceAll(APPLICATION_NAME,application);
    }

}
