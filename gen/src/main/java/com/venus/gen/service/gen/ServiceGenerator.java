package com.venus.gen.service.gen;

import com.venus.esb.ESBSecurityLevel;
import com.venus.gen.Generator;
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

    public final MybatisGenerator mybatisGenerator;
    public final APIGenerator apiGenerator;
    private List<MybatisGenerator.Table> tables;

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
                }
                if (apiModule != null && daoModule != null) {
                    break;
                }
            }
        }

        packageName = packageName.endsWith(".service")?packageName.substring(0,packageName.length() - ".service".length()):packageName;
        MybatisGenerator tempMybatisGenerator = null;
        if (daoModule != null) {
            tempMybatisGenerator = new MybatisGenerator(this.rootProject,daoModule,sqlsSourcePath,tablePrefix,null);
        } else {//则直接放到当前目录下
            tempMybatisGenerator = new MybatisGenerator(this.rootProject, this.rootProject.copyModule(packageName + "." + "persistence"), sqlsSourcePath,tablePrefix,null);
        }
        tempMybatisGenerator.setAutoGenSqlmapConfig(true);

        APIGenerator tempAPIGenerator = null;
        if (apiModule != null) {
            tempAPIGenerator = new APIGenerator(this.rootProject, apiModule, tempMybatisGenerator, exceptionsClass, security);
        } else {
            tempAPIGenerator = new APIGenerator(this.rootProject, this.rootProject.copyModule(packageName + "." + "api"), tempMybatisGenerator, exceptionsClass, security);
        }

        this.apiGenerator = tempAPIGenerator;
        this.mybatisGenerator = tempMybatisGenerator;
        this.tables = mybatisGenerator.getTables();
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
        for (MybatisGenerator.Table table : tables) {
            //生成服务实现类
            File serviceImplFile = new File(crudImplPath + File.separator + table.getSimpleCRUDServiceImplementationName() + ".java");
            writeServiceImpl(serviceImplFile,
                    this.packageName(),
                    mybatisGenerator.packageName(),
                    apiGenerator.packageName(),
                    mybatisGenerator.sqlsSourcePath,
                    apiGenerator.groupName,
                    apiGenerator.exceptionsClass,
                    apiGenerator.security,
                    table,
                    this.project.projectType == ProjectType.dubbo);
        }

        return true;
    }

    private static void writeServiceImpl(File file, String currentPackageName,String doaPackagaeName,String apiPackagaeName,  String sqlsSourcePath, String groupName, Class exceptionClass, ESBSecurityLevel security, MybatisGenerator.Table table, boolean isDubboProject) {
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
        serviceContent.append("import " + table.getDAOClassName(doaPackagaeName) + ";\n");
        serviceContent.append("import " + table.getDObjectClassName(doaPackagaeName) + ";\n");
        serviceContent.append("import " + table.getPOJOClassName(apiPackagaeName) + ";\n");
        serviceContent.append("import " + table.getPOJOResultsClassName(apiPackagaeName) + ";\n");
        serviceContent.append("import " + table.getCRUDServiceBeanName(apiPackagaeName + ".api") + ";\n");
        serviceContent.append("import javax.annotation.Resource;\n");
        serviceContent.append("\n\r\n\r");

        serviceContent.append("/**\n");
        serviceContent.append(" * Owner: Minjun Ling\n");
        serviceContent.append(" * Creator: ESB ServiceGenerator\n");
        serviceContent.append(" * Version: 1.0.0\n");
        serviceContent.append(" * GitHub: https://github.com/lingminjun/esb\n");
        serviceContent.append(" * Since: " + new Date() + "\n");
        serviceContent.append(" * SQLFile: " + sqlsSourcePath + "\n");
        serviceContent.append(" */\n");

//        serviceContent.append("@Component\n");
        serviceContent.append("@Service\n");
        serviceContent.append("public class " + table.getSimpleCRUDServiceImplementationName() + " implements " + table.getSimpleCRUDServiceBeanName() + " {\n");
        serviceContent.append("    private static final org.slf4j.Logger logger    = LoggerFactory.getLogger(" + table.getSimpleCRUDServiceImplementationName() + ".class);\n\n");

        serviceContent.append("    // 默认加载transactionManager事务，若persistence未配置，请防止出现null point\n");
        serviceContent.append("    @Resource(name = \"transactionManager\")\n");
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

        //增加单个
        APIGenerator.writeCreateMethod(tableModelName, groupName, pojoName, theSecurity, serviceContent, table, true);

        //批量增加
        APIGenerator.writeBatchCreateMethod(tableModelName, groupName, pojoName, theSecurity, serviceContent, table, true);

        //删，单个删除
        APIGenerator.writeDeleteMethod(tableModelName, groupName, pojoName, theSecurity, serviceContent, table, true);

        //更新某个数据，拆开每个字段
        APIGenerator.writeUpdateMethod(tableModelName, groupName, pojoName, theSecurity, serviceContent, table, true);

        //主键查询
        APIGenerator.writeFindByIdMethod(tableModelName, groupName, pojoName, theSecurity, serviceContent, table, true,false);
        APIGenerator.writeFindByIdMethod(tableModelName, groupName, pojoName, theSecurity, serviceContent, table, true,true);

        //查询，索引查询，翻页
        Map<String, List<MybatisGenerator.Column>> queryMethods = table.allIndexQueryMethod();
        List<String> methodNames = new ArrayList<String>(queryMethods.keySet());
        Collections.sort(methodNames);
        for (String methodName : methodNames) {
            List<MybatisGenerator.Column> cols = queryMethods.get(methodName);

            APIGenerator.writeQueryMethod(tableModelName, groupName, pojoName, methodName, cols, theSecurity, serviceContent, table, true,false);
            APIGenerator.writeQueryMethod(tableModelName, groupName, pojoName, methodName, cols, theSecurity, serviceContent, table, true,true);
        }

        serviceContent.append("}\n\r\n\r");

        try {
            writeFile(file,serviceContent.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
