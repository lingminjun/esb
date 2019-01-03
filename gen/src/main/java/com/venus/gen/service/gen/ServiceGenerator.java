package com.venus.gen.service.gen;

import com.venus.esb.ESBSecurityLevel;
import com.venus.esb.lang.ESBConsts;
import com.venus.esb.utils.FileUtils;
import com.venus.gen.Generator;
import com.venus.gen.SpringXMLConst;
import com.venus.gen.dao.gen.MybatisGenerator;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: lingminjun
 * Date: 2018-10-16
 * Time: 下午11:06
 */
public class ServiceGenerator extends Generator {

    public static final String SPRING_BEAN_XML_NAME = "application-bean.xml";
    public static final String DUBBO_CONTEXT_XML_NAME = "application-dubbo-context.xml";
    public static final String CONTEXT_XML_NAME_SUFFIX = "-dubbo-context.xml";
    public static final String DUBBO_PROVIDER_XML_NAME = "application-dubbo-provider.xml";
    public static final String AUTO_CONFIG_XML_NAME = "auto-config.xml";
    public static final String AUTO_CONFIG_VM_NAME = "config.properties.vm";
    public static final String CONFIG_PROPERTIES_NAME = "xconfig.properties";

    public final MybatisGenerator mybatisGenerator;
    public final APIGenerator apiGenerator;
    private List<MybatisGenerator.Table> tables;

    // 注意：SpringBoot工程若生成xml配置，则需要启动类添加@ImportResource(locations={"classpath:application-bean.xml"})注解
    // 注意：Servlert工程若生成xml配置，则需要在web.xml中添加DispatcherServlet参数配置：
    /*
    <servlet>
        <servlet-name>m</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>classpath:application-bean.xml</param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>
    <servlet-mapping>
        <servlet-name>m</servlet-name>
        <url-pattern>/</url-pattern>
    </servlet-mapping>
    */
    protected boolean genXmlConfig = false;//生成Spring Bean配置
    {
        // dubbo默认走配置
        if (this.project.projectType == ProjectType.dubbo) {
            this.genXmlConfig = true;
        }
    }
    protected boolean genAutoConfig = true;//生成配置

    protected String relativeBeanXmlDir;//自定义路径，相对地址
    protected boolean separateXmlContext;//分离bean的上下文
    protected String relativeMapperPath; //数据库mapper地址，相对地址


    // Builder函数
    public static class Builder {
        private String projectDir;
        private String tablePrefix;
        private String packageName;
        private String sqlsSourcePath;
        private Class exceptionsClass;
        private ESBSecurityLevel security = ESBSecurityLevel.userAuth;
        private String mapperPath;

        private boolean setGenXmlConfig = false;
        private boolean genXmlConfig = false;
        private boolean genAutoConfig = true;

        private String relativeBeanXmlDir; //自定义路径，相对地址

        private boolean separateXmlContext = true;//分离bean的上下文
        private String relativeMapperPath;

        public Builder setProjectDir(String projectDir) {
            this.projectDir = projectDir;
            return this;
        }

        public Builder setTablePrefix(String tablePrefix) {
            this.tablePrefix = tablePrefix;
            return this;
        }

        public Builder setPackageName(String packageName) {
            this.packageName = packageName;
            return this;
        }

        public Builder setSqlsSourcePath(String sqlsSourcePath) {
            this.sqlsSourcePath = sqlsSourcePath;
            return this;
        }

        public Builder setExceptionsClass(Class exceptionsClass) {
            this.exceptionsClass = exceptionsClass;
            return this;
        }

        public Builder setSecurity(ESBSecurityLevel security) {
            this.security = security;
            return this;
        }

        public Builder setMapperPath(String mapperPath) {
            this.mapperPath = mapperPath;
            return this;
        }

        // 是否自动生成sqlmap-config.xml
        // 注意：SpringBoot工程若生成xml配置，则需要启动类添加@ImportResource(locations={"classpath:application-bean.xml"})注解
        // 注意：Servlert工程若生成xml配置，则需要在web.xml中添加DispatcherServlet参数配置：
        /*
        <servlet>
            <servlet-name>m</servlet-name>
            <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
            <init-param>
                <param-name>contextConfigLocation</param-name>
                <param-value>classpath:application-bean.xml</param-value>
            </init-param>
            <load-on-startup>1</load-on-startup>
        </servlet>
        <servlet-mapping>
            <servlet-name>m</servlet-name>
            <url-pattern>/</url-pattern>
        </servlet-mapping>
        */
        public Builder setGenXmlConfig(boolean genXmlConfig) {
            this.genXmlConfig = genXmlConfig;
            this.setGenXmlConfig = true;
            return this;
        }

        public Builder setGenAutoConfig(boolean genAutoConfig) {
            this.genAutoConfig = genAutoConfig;
            return this;
        }

        public Builder setRelativeBeanXmlDir(String relativeBeanXmlDir) {
            this.relativeBeanXmlDir = relativeBeanXmlDir;
            return this;
        }

        public Builder setSeparateXmlContext(boolean separateXmlContext) {
            this.separateXmlContext = separateXmlContext;
            return this;
        }

        public Builder setRelativeMapperPath(String relativeMapperPath) {
            this.relativeMapperPath = relativeMapperPath;
            return this;
        }

        public ServiceGenerator build() {
            if (packageName == null || packageName.length() == 0) {
                return null;
            }
            if (sqlsSourcePath == null || sqlsSourcePath.length() == 0) {
                return null;
            }
            if (exceptionsClass == null) {
                return null;
            }
            ServiceGenerator generator = new ServiceGenerator(packageName,sqlsSourcePath,exceptionsClass,tablePrefix,projectDir,security);

            if (setGenXmlConfig) {
                generator.genXmlConfig = genXmlConfig;
            }
            generator.genAutoConfig = genAutoConfig;
            generator.relativeBeanXmlDir = relativeBeanXmlDir;
            generator.separateXmlContext = separateXmlContext;
            generator.relativeMapperPath = relativeMapperPath;
            return generator;
        }
    }

    /**
     *
     * @param packageName     项目包名【必填】
     * @param sqlsSourcePath  资源路径【必填】
     * @param exceptionsClass  异常类地址【必填】
     */
    public ServiceGenerator(String packageName, String sqlsSourcePath, Class exceptionsClass) {
        this(packageName,sqlsSourcePath,exceptionsClass,null,ESBSecurityLevel.userAuth);
    }

    /**
     *
     * @param packageName     项目包名【必填】
     * @param sqlsSourcePath  资源路径【必填】
     * @param exceptionsClass  异常类地址【必填】
     * @param tablePrefix  table命名前缀【可选】
     */
    public ServiceGenerator(String packageName, String sqlsSourcePath, Class exceptionsClass,String tablePrefix) {
        this(packageName,sqlsSourcePath,exceptionsClass,tablePrefix,ESBSecurityLevel.userAuth);
    }

    /**
     *
     * @param packageName     项目包名【必填】
     * @param sqlsSourcePath  资源路径【必填】
     * @param exceptionsClass  异常类地址【必填】
     * @param tablePrefix  table命名前缀【可选】
     * @param security 接口的验权等级，仅仅支持idl API时有用
     */
    public ServiceGenerator(String packageName, String sqlsSourcePath, Class exceptionsClass, String tablePrefix, ESBSecurityLevel security) {
        this(packageName,sqlsSourcePath,exceptionsClass,tablePrefix,null,security);
    }

    /**
     *
     * @param packageName     项目包名【必填】
     * @param sqlsSourcePath  资源路径【必填】
     * @param exceptionsClass  异常类地址【必填】
     * @param tablePrefix      table命名前缀【可选】
     * @param projectDir      工程目录【可选】
     * @param security 接口的验权等级，仅仅支持idl API时有用
     */
    public ServiceGenerator(String packageName, String sqlsSourcePath, Class exceptionsClass, String tablePrefix, String projectDir, ESBSecurityLevel security) {
        super(packageName.endsWith(".service")?packageName.substring(0,packageName.length() - ".service".length()):packageName, projectDir);

        // 复合工程情况
        ProjectModule daoModule = null;
        ProjectModule apiModule = null;
        if (this.rootProject.projectType == ProjectType.parent && this.rootProject.modules != null) {
            for (ProjectModule module : this.rootProject.modules) {
                if (apiModule == null && module.moduleName.contains("api") && !module.moduleName.contains("test")) {
                    apiModule = module;
                } else if (daoModule == null && !module.moduleName.contains("test") &&
                        (module.moduleName.contains("persistence") || module.moduleName.contains("dao"))) {
                    daoModule = module;
                    if (!module.packageName.endsWith("persistence")) {
                        daoModule = module.copyModule(packageName + "." + "persistence");
                    }
                }
                if (apiModule != null && daoModule != null) {
                    break;
                }
            }
        }

        packageName = packageName.endsWith(".service")?packageName.substring(0,packageName.length() - ".service".length()):packageName;

        MybatisGenerator.Builder mybatisBuilder = new MybatisGenerator.Builder();
        mybatisBuilder.setRootProject(this.rootProject);
        if (daoModule != null) {
            mybatisBuilder.setProject(daoModule);
        } else {
            mybatisBuilder.setProject(this.rootProject.copyModule(packageName + "." + "persistence"));
        }
        mybatisBuilder.setSqlsSourcePath(sqlsSourcePath);
        mybatisBuilder.setTablePrefix(tablePrefix);
        mybatisBuilder.setRelativeMapperPath(this.relativeBeanXmlDir);
        mybatisBuilder.setGenXmlConfig(true);

        MybatisGenerator tempMybatisGenerator = mybatisBuilder.build();

        APIGenerator.Builder apiBuilder = new APIGenerator.Builder();
        apiBuilder.setRootProject(this.rootProject);
        if (apiModule != null) {
            apiBuilder.setProject(apiModule);
        } else {
            apiBuilder.setProject(this.rootProject.copyModule(packageName + "." + "api"));
        }
        apiBuilder.setMybatisGenerator(tempMybatisGenerator);
        apiBuilder.setExceptionsClass(exceptionsClass);
        apiBuilder.setSecurity(security);

        APIGenerator tempAPIGenerator = apiBuilder.build();

        this.apiGenerator = tempAPIGenerator;
        this.mybatisGenerator = tempMybatisGenerator;
        this.tables = mybatisGenerator.getTables();
    }

    // 是否自动生成sqlmap-config.xml
    // 注意：SpringBoot工程若生成xml配置，则需要启动类添加@ImportResource(locations={"classpath:application-bean.xml"})注解
    // 注意：Servlert工程若生成xml配置，则需要在web.xml中添加DispatcherServlet参数配置：
    /*
    <servlet>
        <servlet-name>m</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>classpath:application-bean.xml</param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>
    <servlet-mapping>
        <servlet-name>m</servlet-name>
        <url-pattern>/</url-pattern>
    </servlet-mapping>
    */
    public boolean autoGenXmlConfig() {
        return this.genXmlConfig;
    }

    public boolean autoGenConfig() {
        return this.genAutoConfig;
    }


    public String getRelativeBeanXmlDir() {
        return relativeBeanXmlDir;
    }

    @Override
    public boolean gen() {

        //先生成DOA
        if (!mybatisGenerator.gen()) {
            return false;
        }

        if (!apiGenerator.gen()) {
            return false;
        }

        // 构建目录api
        String crudImplPath = this.packagePath() + File.separator + "service";
        new File(crudImplPath).mkdirs();

        //生成基类
        String application = this.getProjectSimpleName();
        for (MybatisGenerator.Table table : tables) {
            //生成服务实现类
            File serviceImplFile = new File(crudImplPath + File.separator + table.getSimpleCRUDServiceImplementationName() + ".java");
            writeServiceImpl(serviceImplFile,
                    application,
                    this.packageName(),
                    mybatisGenerator.packageName(),
                    apiGenerator.packageName(),
                    mybatisGenerator.sqlsSourcePath,
                    apiGenerator.groupName,
                    apiGenerator.exceptionsClass,
                    apiGenerator.security,
                    table,
                    this.project.projectType == ProjectType.dubbo,
                    this.genXmlConfig);
        }

        //自动生成配置
        if (!this.genXmlConfig) {
            return true;
        }
        String xmlPath;
        //自定义provider地址
        String xmlRelativeDir;
        if (this.relativeBeanXmlDir != null && this.relativeBeanXmlDir.length() > 0) {
            if (this.relativeBeanXmlDir.startsWith(File.separator) && this.relativeBeanXmlDir.endsWith(File.separator)) {
                xmlPath = this.resourcesPath() + this.relativeBeanXmlDir + DUBBO_PROVIDER_XML_NAME;
                xmlRelativeDir = this.relativeBeanXmlDir.substring(1);
            } else if (this.relativeBeanXmlDir.startsWith(File.separator)) {
                xmlPath = this.resourcesPath() + this.relativeBeanXmlDir + File.separator + DUBBO_PROVIDER_XML_NAME;
                xmlRelativeDir = this.relativeBeanXmlDir.substring(1) + File.separator;
            } else if (this.relativeBeanXmlDir.endsWith(File.separator)) {
                xmlPath = this.resourcesPath() + File.separator + this.relativeBeanXmlDir + DUBBO_PROVIDER_XML_NAME;
                xmlRelativeDir = this.relativeBeanXmlDir;
            } else {
                xmlPath = this.resourcesPath() + File.separator + this.relativeBeanXmlDir + File.separator + DUBBO_PROVIDER_XML_NAME;
                xmlRelativeDir = this.relativeBeanXmlDir + File.separator;
            }
        } else {
            if (this.project.projectType == ProjectType.dubbo) {//spring启动容器目录
                xmlPath = this.resourcesPath() + File.separator + "META-INF" + File.separator + "spring" + File.separator + DUBBO_PROVIDER_XML_NAME;
                new File(this.resourcesPath() + File.separator + "META-INF" + File.separator + "spring").mkdirs();
                xmlRelativeDir = "META-INF" + File.separator + "spring" + File.separator;
            } else {
                xmlPath = this.resourcesPath() + File.separator + SPRING_BEAN_XML_NAME;
                xmlRelativeDir = "";
            }
        }
        File providerXmlFile = new File(xmlPath);
        File contextXmlFile = null;
        if (this.separateXmlContext) {
            if (application != null && application.length() > 0) {
                contextXmlFile = new File(providerXmlFile.getParentFile().getAbsolutePath() + File.separator + application + CONTEXT_XML_NAME_SUFFIX);
            } else {
                contextXmlFile = new File(providerXmlFile.getParentFile().getAbsolutePath() + File.separator + DUBBO_CONTEXT_XML_NAME);
            }
        }
        writeXmlConfig(providerXmlFile,contextXmlFile,xmlRelativeDir,application,this.packageName(),mybatisGenerator.packageName(),apiGenerator.packageName(),tables,this.project.projectType == ProjectType.dubbo);

        if (!this.genAutoConfig) {
            return true;
        }

        String autoXmlPath;
        String autoVMPath;
        String configPath;
        String autoConfDir = "autoconf";
        if (this.project.projectType == ProjectType.servlet) {//servlet 启动容器目录
            autoXmlPath = this.webappPath() + File.separator + "META-INF" + File.separator + autoConfDir + File.separator + AUTO_CONFIG_XML_NAME;
            autoVMPath = this.webappPath() + File.separator + "META-INF" + File.separator + autoConfDir + File.separator + AUTO_CONFIG_VM_NAME;
            new File(this.webappPath() + File.separator + "META-INF" + File.separator + autoConfDir).mkdirs();
            configPath = this.resourcesPath() + File.separator + CONFIG_PROPERTIES_NAME;
        } else {
            autoXmlPath = this.resourcesPath() + File.separator + "META-INF" + File.separator + autoConfDir + File.separator + AUTO_CONFIG_XML_NAME;
            autoVMPath = this.resourcesPath() + File.separator + "META-INF" + File.separator + autoConfDir + File.separator + AUTO_CONFIG_VM_NAME;
            new File(this.resourcesPath() + File.separator + "META-INF" + File.separator + autoConfDir).mkdirs();
            configPath = this.resourcesPath() + File.separator + CONFIG_PROPERTIES_NAME;
        }

        writeAutoXmlConfig(new File(autoXmlPath),application,this.project.projectType == ProjectType.servlet,this.project.projectType == ProjectType.dubbo);
        writeConfigVM(new File(autoVMPath),application,this.project.projectType == ProjectType.dubbo);
        writeConfig(new File(configPath),application,this.project.projectType == ProjectType.dubbo);

        return true;
    }

    private static void writeServiceImpl(File file, String projectName, String currentPackageName,String daoPackageName,String apiPackageName,  String sqlsSourcePath, String groupName, Class exceptionClass, ESBSecurityLevel security, MybatisGenerator.Table table, boolean isDubboProject, boolean genXmlConfig) {
        String theSecurity = "ESBSecurityLevel." + security.toString();

        StringBuilder serviceContent = new StringBuilder();
        serviceContent.append("package " + currentPackageName + ".service;\n\r\n\r");
//        serviceContent.append("import com.lmj.stone.cache.AutoCache;\n");
        serviceContent.append("import com.venus.esb.utils.Injects;\n");
        serviceContent.append("import org.slf4j.LoggerFactory;\n");
//        serviceContent.append("import com.lmj.stone.service.BlockUtil;\n");
        serviceContent.append("import org.springframework.jdbc.datasource.DataSourceTransactionManager;\n");
        serviceContent.append("import " + exceptionClass.getName() + ";\n");
        serviceContent.append("import com.venus.esb.ESBSecurityLevel;\n");
        serviceContent.append("import com.venus.esb.lang.ESBException;\n");
        serviceContent.append("import com.venus.esb.annotation.ESBError;\n");
        serviceContent.append("import com.venus.esb.annotation.ESBParam;\n");
        serviceContent.append("import java.util.*;\n");
        serviceContent.append("import org.springframework.beans.factory.annotation.Autowired;\n");

//        serviceContent.append("import org.springframework.stereotype.Component;\n");
        if (isDubboProject) {
            serviceContent.append("import com.alibaba.dubbo.config.annotation.Service;\n");
        } else {
            serviceContent.append("import org.springframework.stereotype.Service;\n");
        }
        serviceContent.append("import " + table.getDAOClassName(daoPackageName) + ";\n");
        serviceContent.append("import " + table.getDObjectClassName(daoPackageName) + ";\n");
        serviceContent.append("import " + table.getPOJOClassName(apiPackageName) + ";\n");
        serviceContent.append("import " + table.getPOJOResultsClassName(apiPackageName) + ";\n");
        serviceContent.append("import " + table.getCRUDServiceBeanName(apiPackageName) + ";\n");
        serviceContent.append("import javax.annotation.Resource;\n");
        serviceContent.append("\n\r\n\r");

        serviceContent.append("/**\n");
        serviceContent.append(" * Owner: Minjun Ling\n");
        serviceContent.append(" * Creator: ESB ServiceGenerator\n");
        serviceContent.append(" * Version: 1.0.0\n");
        serviceContent.append(" * GitHub: https://github.com/lingminjun/esb\n");
//        serviceContent.append(" * Since: " + new Date() + "\n");
        serviceContent.append(" * SQLFile: " + sqlsSourcePath + "\n");
        serviceContent.append(" */\n");

//        serviceContent.append("@Component\n");
        if (genXmlConfig) {//自动生成xml配置
            serviceContent.append("@Service\n");
        }
        serviceContent.append("public class " + table.getSimpleCRUDServiceImplementationName() + " implements " + table.getSimpleCRUDServiceBeanName() + " {\n");
        serviceContent.append("    private static final org.slf4j.Logger logger    = LoggerFactory.getLogger(" + table.getSimpleCRUDServiceImplementationName() + ".class);\n\n");

        serviceContent.append("    // 默认加载transactionManager事务，若persistence未配置，请防止出现null point\n");
        if (projectName != null && projectName.length() > 0) {
            serviceContent.append("    @Resource(name = \"" + projectName + "TransactionManager\")\n");
        } else {
            serviceContent.append("    @Resource(name = \"transactionManager\")\n");
        }
        serviceContent.append("    protected DataSourceTransactionManager transactionManager;\n\n");

        // 定义DAO属性
        serviceContent.append("    @Autowired\n");
        String doaClassName = table.getSimpleDAOClassName();
        String doaPropertyName = toLowerHeadString(doaClassName);
        serviceContent.append("    protected ");
        serviceContent.append(doaClassName);
        serviceContent.append(" ");
        serviceContent.append(doaPropertyName);
        serviceContent.append(";\n\n");

        // 实现下面一系列方法
        String tableModelName = toHumpString(table.getAlias(), true);

        String pojoName = table.getSimplePOJOClassName();
        //所有基本的增删修查

        if (!table.justViewTable()) {
            //增加单个
            APIGenerator.writeCreateMethod(tableModelName, groupName, pojoName, theSecurity, serviceContent, table, true);

            //批量增加
            APIGenerator.writeBatchCreateMethod(tableModelName, groupName, pojoName, theSecurity, serviceContent, table, true);

            //删，单个删除
            APIGenerator.writeDeleteMethod(tableModelName, groupName, pojoName, theSecurity, serviceContent, table, true);

            //更新某个数据，拆开每个字段
            APIGenerator.writeUpdateMethod(tableModelName, groupName, pojoName, theSecurity, serviceContent, table, true);

            //主键查询
            APIGenerator.writeFindByIdMethod(tableModelName, groupName, pojoName, theSecurity, serviceContent, table, true, false);
            APIGenerator.writeFindByIdMethod(tableModelName, groupName, pojoName, theSecurity, serviceContent, table, true, true);

            //主键批量查询
            APIGenerator.writeQueryByIdsMethod(tableModelName, groupName, pojoName, theSecurity, serviceContent, table, true, false);
            APIGenerator.writeQueryByIdsMethod(tableModelName, groupName, pojoName, theSecurity, serviceContent, table, true, true);
        }

        //查询，索引查询，翻页
        {
            Map<String, List<MybatisGenerator.Column>> queryMethods = table.allIndexQueryMethod();
            List<String> methodNames = new ArrayList<String>(queryMethods.keySet());
            Collections.sort(methodNames);
            for (String methodName : methodNames) {
                List<MybatisGenerator.Column> cols = queryMethods.get(methodName);

                APIGenerator.writeQueryMethod(tableModelName, groupName, pojoName, methodName, cols, theSecurity, serviceContent, table, true, false, false);
                APIGenerator.writeQueryMethod(tableModelName, groupName, pojoName, methodName, cols, theSecurity, serviceContent, table, true, true, false);
            }
        }

        //查询，视图查询，翻页
        {
            Map<String, MybatisGenerator.SQLSelect> viewMethods = table.allViewQueryMethod();
            List<String> methodNames = new ArrayList<String>(viewMethods.keySet());
            Collections.sort(methodNames);
            for (String methodName : methodNames) {
                MybatisGenerator.SQLSelect sqlSelect = viewMethods.get(methodName);
                List<MybatisGenerator.Column> cols = sqlSelect.getBinds();

                APIGenerator.writeQueryMethod(tableModelName, groupName, pojoName, methodName, cols, theSecurity, serviceContent, table, true, false, true);
                APIGenerator.writeQueryMethod(tableModelName, groupName, pojoName, methodName, cols, theSecurity, serviceContent, table, true, true, true);
            }
        }

        serviceContent.append("}\n\r\n\r");

        try {
            writeFile(file,serviceContent.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeXmlConfig(File file, File contextFile, String relativeXmlDir, String applicationName, String currentPackageName, String daoPackageName, String apiPackagaeName, List<MybatisGenerator.Table> tables, boolean isDubboProject) {
        //表示分离context配置
        if (contextFile != null) {//查看原来配置是否存在，如果存在，则不重写，如果不存在，就重写
            writeContextXmlConfig(contextFile,applicationName,isDubboProject);
        }

        //判断是否为更新
        StringBuilder content = new StringBuilder();
        boolean fileHeader = false;
        try {
            String old = FileUtils.readFile(file.getAbsolutePath(), ESBConsts.UTF8);
            if (old != null) {
                int idx = old.lastIndexOf("</beans>");
                if (idx >= 0 && idx < old.length()) {
                    content.append(old.substring(0,idx).trim());
                    content.append("\n\n    ");
                    fileHeader = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //写入默认配置
        if (!fileHeader) {
            if (contextFile != null) {//单独引入
                if (isDubboProject) {
                    content.append(SpringXMLConst.theDubboProviderXmlConfigHead(applicationName,true));
                } else {
                    content.append(SpringXMLConst.SPRING_XML_PURE_CONFIG_HEAD);
                }

                //检查是否需要包含
                String contextFileName = contextFile.getName();
                int idx = content.toString().indexOf(contextFileName);
                if (idx < 0 || idx >= content.length()) {
                    content.append("    <!-- 引用XML公用的配置信息，防止重复引用，故注释，使用者按需打开 -->\n");
                    content.append("    <!-- <import resource=\"classpath:" + relativeXmlDir + contextFileName + "\"/> -->\n\n");
                }
            } else {
                if (isDubboProject) {
                    content.append(SpringXMLConst.theDubboProviderXmlConfigHead(applicationName,false));
                } else {
                    content.append(SpringXMLConst.SPRING_XML_CONFIG_HEAD);
                }
            }
        }

        //调整输入位置
        if (content.toString().endsWith("\n")) {
            content.append("    ");
        }

        // 添加为bean的
        for (MybatisGenerator.Table table : tables) {
            String beanName = toLowerHeadString(table.getSimpleCRUDServiceBeanName());
            String beanClass = table.getCRUDServiceBeanName(apiPackagaeName);
            int idx = content.toString().indexOf(beanClass);
            if (idx < 0 || idx >= content.length()) {
                if (isDubboProject) {
                    String implClass = table.getCRUDServiceImplementationName(currentPackageName);
                    content.append(SpringXMLConst.theDubboProvider(beanName, beanClass, implClass));
                } else {
                    content.append(SpringXMLConst.theSpringBean(beanName, beanClass));
                }
            }
        }

        content.append("\n</beans>");

        try {
            writeFile(file,content.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeContextXmlConfig(File contextFile, String applicationName, boolean isDubboProject) {
        if (!contextFile.exists()) {
            //写新的配置文件
            try {
                if (isDubboProject) {
                    writeFile(contextFile,SpringXMLConst.theDubboContextXmlConfig(applicationName).toString());
                } else {
                    writeFile(contextFile,SpringXMLConst.SPRING_XML_CONTEXT_CONFIG);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return;
        }

        StringBuilder contextString = new StringBuilder();
        try {
            String old = FileUtils.readFile(contextFile.getAbsolutePath(), ESBConsts.UTF8);
            int eidx = old.lastIndexOf("</beans>");
            if (eidx >= 0 && eidx < old.length()) {
                contextString.append(old.substring(0,eidx).trim());
                contextString.append("\n\n    ");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean changed = false;
        if (isDubboProject) {
            int idx = contextString.toString().indexOf("\"com.alibaba.dubbo.config.ApplicationConfig\"");
            if (idx < 0 || idx >= contextString.length()) {
                contextString.append(SpringXMLConst.theDubboApplicationConfig(applicationName));
                changed = true;
            }

            idx = contextString.toString().indexOf("\"com.alibaba.dubbo.config.RegistryConfig\"");
            if (idx < 0 || idx >= contextString.length()) {
                contextString.append(SpringXMLConst.DUBBO_REGISTRY_CONFIG);
                changed = true;
            }

            idx = contextString.toString().indexOf("\"com.alibaba.dubbo.config.ProtocolConfig\"");
            if (idx < 0 || idx >= contextString.length()) {
                contextString.append(SpringXMLConst.DUBBO_PROTOCOL_CONFIG);
                changed = true;
            }

            idx = contextString.toString().indexOf("<dubbo:provider");
            if (idx < 0 || idx >= contextString.length()) {
                contextString.append(SpringXMLConst.DUBBO_PROVIDER_CONFIG);
                changed = true;
            }
        }

        contextString.append("\n</beans>");

        if (changed) {
            try {
                writeFile(contextFile,contextString.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void writeAutoXmlConfig(File file, String applicationName, boolean isServlet, boolean isDubbo) {
        //判断是否为更新
        String relativePath = isServlet ? "WEB-INF/classes/" : "";
        String old = SpringXMLConst.theAutoConfigXml(applicationName, relativePath);
        try {
            String told = FileUtils.readFile(file.getAbsolutePath(), ESBConsts.UTF8);
            if (told != null && told.length() > 0) {
                old = told;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        StringBuilder content = new StringBuilder(old);

        // application name
        {
            int idx = content.indexOf("application.name");
            if (idx < 0 || idx >= content.length()) {
                //插入
                idx = content.indexOf("<config");
                if (idx >= 0 && idx < content.length()) {
                    idx = content.indexOf(">", (idx + "<config".length()));
                    content.insert(idx + 1, "\n" + SpringXMLConst.ADD_APPLICATION_NAME_CONFIG_GROUP);
                }

            }
        }

        //如果是dubbo
        if (isDubbo) {
            int idx = content.indexOf("dubbo.registry.url");
            if (idx < 0 || idx >= content.length()) {
                //插入
                idx = content.indexOf("<config");
                if (idx >= 0 && idx < content.length()) {
                    idx = content.indexOf(">", (idx + "<config".length()));
                    content.insert(idx + 1, "\n" + SpringXMLConst.ADD_DUBBO_AUTO_CONFIG_GROUP);
                }
            }
        }

        // dao配置
        {
            int idx = content.indexOf("com.venus." + applicationName + ".mysql.datasource.url");
            if (idx < 0 || idx >= content.length()) {
                //插入
                idx = content.indexOf("<config");
                if (idx >= 0 && idx < content.length()) {
                    idx = content.indexOf(">", (idx + "<config".length()));
                    content.insert(idx + 1, "\n" + SpringXMLConst.theDatasourceAutoConfigGroup(applicationName));
                }
            }
        }

        // 配置script作用
        {
            int idx = content.indexOf("config.properties.vm");
            if (idx < 0 || idx >= content.length()) {
                //插入
                idx = content.lastIndexOf("</script>");
                int edx = content.lastIndexOf("</config>");
                if (idx >= 0 && idx < content.length()) {
                    content.insert(idx, "    <generate template=\"config.properties.vm\" destfile=\"" + relativePath + CONFIG_PROPERTIES_NAME + "\" charset=\"UTF-8\"/>\n    ");
                } else if (edx >= 0 && edx < content.length()) {
                    content.insert(edx, "    <script>\n" +
                            "        <generate template=\"config.properties.vm\" destfile=\"" + relativePath + CONFIG_PROPERTIES_NAME + "\" charset=\"UTF-8\"/>\n" +
                            "    </script>\n");
                }
            }
        }

        try {
            writeFile(file,content.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeConfigVM(File file, String applicationName, boolean isDubbo) {
        //判断是否为更新
        String old = SpringXMLConst.theAutoConfigVMGroup(applicationName);
        try {
            String told = FileUtils.readFile(file.getAbsolutePath(), ESBConsts.UTF8);
            if (told != null && told.length() > 0) {
                old = told;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        StringBuilder content = new StringBuilder(old);

        // application name
        {
            int idx = content.indexOf("application.name");
            if (idx < 0 || idx >= content.length()) {
                content.insert(0, SpringXMLConst.ADD_APPLICATION_NAME_CONFIG);
            }
        }

        //如果是dubbo
        if (isDubbo) {
            int idx = content.indexOf("dubbo.registry.url");
            if (idx < 0 || idx >= content.length()) {
                content.insert(0, SpringXMLConst.ADD_DUBBO_PROPERTIES_CONFIG);
            }
        }

        // dao配置
        {
            int idx = content.indexOf("com.venus." + applicationName + ".mysql.datasource.url");
            if (idx < 0 || idx >= content.length()) {
                content.append("\n" + SpringXMLConst.theDatasourcePropertiesConfig(applicationName,true));
            }
        }


        // 配置script作用
        {
            int idx = content.indexOf("com.venus." + applicationName + ".log.home");
            if (idx < 0 || idx >= content.length()) {
                content.append("\ncom.venus." + applicationName + ".log.home=${com.venus." + applicationName + ".log.home}\n");
            }
        }

        try {
            writeFile(file,content.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeConfig(File file, String applicationName, boolean isDubbo) {
        //判断是否为更新
        String old = SpringXMLConst.theConfigProperties(applicationName);
        try {
            String told = FileUtils.readFile(file.getAbsolutePath(), ESBConsts.UTF8);
            if (told != null && told.length() > 0) {
                old = told;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        StringBuilder content = new StringBuilder(old);

        {
            int idx = content.indexOf("application.name");
            if (idx < 0 || idx >= content.length()) {
                content.insert(0, SpringXMLConst.theApplicationPropertiesConfig(applicationName));
            }
        }

        //如果是dubbo
        if (isDubbo) {
            int idx = content.indexOf("dubbo.registry.url");
            if (idx < 0 || idx >= content.length()) {
                content.insert(0, SpringXMLConst.ADD_DUBBO_PROPERTIES_CONFIG_DEMO);
            }
        }

        // dao配置
        {
            int idx = content.indexOf("com.venus." + applicationName + ".mysql.datasource.url");
            if (idx < 0 || idx >= content.length()) {
                content.append("\n" + SpringXMLConst.theDatasourcePropertiesConfig(applicationName,false));
            }
        }


        // 配置script作用
        {
            int idx = content.indexOf("com.venus." + applicationName + ".log.home");
            if (idx < 0 || idx >= content.length()) {
                content.append("\n# 日志目录配置\ncom.venus." + applicationName + ".log.home=/Home/admin/logs/" + applicationName + "-service/\n");
            }
        }

        try {
            writeFile(file,content.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
