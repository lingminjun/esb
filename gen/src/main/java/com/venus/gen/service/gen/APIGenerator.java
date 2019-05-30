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
class APIGenerator extends Generator {

    public final MybatisGenerator mybatisGenerator;
    private List<MybatisGenerator.Table> tables;
    public final Class exceptionsClass;
    public final String groupName;
    public final ESBSecurityLevel security;
    private final boolean autoMybatisGenerator;

    public static class Builder {
        private String projectDir;
        private String packageName;
        private String sqlsSourcePath;
        private Class exceptionsClass;
        private String tablePrefix;
        private ESBSecurityLevel security = ESBSecurityLevel.userAuth;

        private ProjectModule rootProject;
        private ProjectModule project;
        private MybatisGenerator mybatisGenerator;

        public Builder setProjectDir(String projectDir) {
            this.projectDir = projectDir;
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

        public Builder setTablePrefix(String tablePrefix) {
            this.tablePrefix = tablePrefix;
            return this;
        }

        public Builder setSecurity(ESBSecurityLevel security) {
            this.security = security;
            return this;
        }

        public Builder setRootProject(ProjectModule rootProject) {
            this.rootProject = rootProject;
            return this;
        }

        public Builder setProject(ProjectModule project) {
            this.project = project;
            return this;
        }

        public Builder setMybatisGenerator(MybatisGenerator mybatisGenerator) {
            this.mybatisGenerator = mybatisGenerator;
            return this;
        }

        public APIGenerator build() {
            if (exceptionsClass == null) {
                return null;
            }

            if (security == null) {
                security = ESBSecurityLevel.userAuth;
            }

            APIGenerator generator = null;
            if (rootProject != null && project != null && mybatisGenerator != null) {
                generator = new APIGenerator(rootProject,project,mybatisGenerator,exceptionsClass,security);
            } else {
                if (packageName == null || packageName.length() == 0) {
                    return null;
                }

                if (sqlsSourcePath == null || sqlsSourcePath.length() == 0) {
                    return null;
                }

                if (exceptionsClass == null) {
                    return null;
                }

                generator = new APIGenerator(packageName,sqlsSourcePath,exceptionsClass,tablePrefix,projectDir,security);
            }

            return generator;
        }
    }

    /**
     *
     * @param packageName     项目包名【必填】
     * @param sqlsSourcePath  资源路径【必填】
     * @param exceptionsClass  异常码地址【必填】
     */
    public APIGenerator(String packageName, String sqlsSourcePath, Class exceptionsClass) {
        this(packageName,sqlsSourcePath,exceptionsClass,null,ESBSecurityLevel.userAuth);
    }

    /**
     *
     * @param packageName     项目包名【必填】
     * @param sqlsSourcePath  资源路径【必填】
     * @param exceptionsClass  异常类地址【必填】
     * @param tablePrefix  table命名前缀【可选】
     */
    public APIGenerator(String packageName, String sqlsSourcePath, Class exceptionsClass, String tablePrefix) {
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
    public APIGenerator(String packageName, String sqlsSourcePath, Class exceptionsClass, String tablePrefix, ESBSecurityLevel security) {
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
    public APIGenerator(String packageName, String sqlsSourcePath, Class exceptionsClass, String tablePrefix, String projectDir, ESBSecurityLevel security) {
        super(packageName, projectDir);

        // doa包名处理
        String doaPackage = packageName;
        if (packageName.endsWith("service")) {
            doaPackage = packageName.substring(0,packageName.length() - "service".length()) + "persistence";
        } else {//直接放到其子目录
            doaPackage = packageName + "." + "persistence";
        }

        this.mybatisGenerator = new MybatisGenerator(doaPackage, projectDir, sqlsSourcePath, tablePrefix);
        this.tables = mybatisGenerator.getTables();
        this.exceptionsClass = exceptionsClass;
        this.groupName = getProjectSimpleName();
        this.security = security;
        this.autoMybatisGenerator = true;
    }

    public APIGenerator(ProjectModule rootProject, ProjectModule project, MybatisGenerator mybatisGenerator, Class exceptionsClass, ESBSecurityLevel security) {
        super(rootProject,project);
        this.mybatisGenerator = mybatisGenerator;
        this.tables = mybatisGenerator.getTables();
        this.exceptionsClass = exceptionsClass;
        this.groupName = getProjectSimpleName();
        this.security = security;
        this.autoMybatisGenerator = false;
    }

    @Override
    public boolean gen() {

        //先生成DOA
        if (this.autoMybatisGenerator) {
            if (!mybatisGenerator.gen()) {
                return false;
            }
        }

        // 构建目录api
        String entitiesPath = this.packagePath() + File.separator + "entities";
        new File(entitiesPath).mkdirs();
        // 构建目录api
        String apisPath = this.packagePath() + File.separator + "api";
        new File(apisPath).mkdirs();


        //生成基类
        for (MybatisGenerator.Table table : tables) {
            //产生实体类
            File pojoFile = new File(entitiesPath + File.separator + table.getSimplePOJOClassName() + ".java");
            writeEntity(pojoFile,this.packageName(),table);

            //产生实体类集合
            File pojosFile = new File(entitiesPath + File.separator + table.getSimplePOJOResultsClassName() + ".java");
            writeEntityResults(pojosFile,this.packageName(),table);

            //产生服务申明类
            File serviceFile = new File(apisPath + File.separator + table.getSimpleCRUDServiceBeanName() + ".java");
            writeCRUDService(serviceFile,this.packageName(),mybatisGenerator.packageName(),groupName, exceptionsClass,security,table);
        }

        return true;
    }

    private static void writeEntity(File file, String packageName, MybatisGenerator.Table table) {
        StringBuilder pojoContent = new StringBuilder();
        pojoContent.append("package " + packageName + ".entities;\n\r\n\r");
        pojoContent.append("import com.venus.esb.annotation.ESBDesc;\n");
        pojoContent.append("import java.util.*;\n");
        pojoContent.append("import java.io.Serializable;\n\r\n\r");
        pojoContent.append("/**\n");
        pojoContent.append(" * Owner: Minjun Ling\n");
        pojoContent.append(" * Creator: ESB ServiceGenerator\n");
        pojoContent.append(" * Version: 1.0.0\n");
        pojoContent.append(" * GitHub: https://github.com/lingminjun/esb\n");
//        pojoContent.append(" * Since: " + new Date() + "\n");
        pojoContent.append(" * Table: " + table.getName() + "\n");
        pojoContent.append(" */\n");
        pojoContent.append("@ESBDesc(\"" + table.getName() + "对象生成\")\n");
        pojoContent.append("public class " + table.getSimplePOJOClassName() + " implements Serializable {\n");
        pojoContent.append("    private static final long serialVersionUID = 1L;\n");

        StringBuilder getset = new StringBuilder();
        for (MybatisGenerator.Column cl : table.getColumns()) {
            if (cl.getName().equals("is_delete") || cl.getName().equals("delete")) {
                continue;
            }

            String get = null;
            String set = null;
            if (MybatisGenerator.MYSQL_LONG_TYPE.contains(cl.getType())) {
                pojoContent.append("    @ESBDesc(\"" + cl.getCmmt() + "\")\n");
                pojoContent.append("    private long    ");

                get = "    public long get";
                set = "(long value)";//"    public void set";
            } else if (MybatisGenerator.MYSQL_BOOL_TYPE.contains(cl.getType())) {
                pojoContent.append("    @ESBDesc(\"" + cl.getCmmt() + "\")\n");
                pojoContent.append("    private boolean ");
                get = "    public boolean get";
                set = "(boolean value)";
            } else if (MybatisGenerator.MYSQL_DOUBLE_TYPE.contains(cl.getType())) {
                pojoContent.append("    @ESBDesc(\"" + cl.getCmmt() + "\")\n");
                pojoContent.append("    private double  ");
                get = "    public double get";
                set = "(double value)";
            } else if (MybatisGenerator.MYSQL_INT_TYPE.contains(cl.getType())) {
                pojoContent.append("    @ESBDesc(\"" + cl.getCmmt() + "\")\n");
                pojoContent.append("    private int     ");
                get = "    public int get";
                set = "(int value)";
            } else if (MybatisGenerator.MYSQL_STRING_TYPE.contains(cl.getType())) {
                pojoContent.append("    @ESBDesc(\"" + cl.getCmmt() + "\")\n");
                pojoContent.append("    private String  ");
                get = "    public String get";
                set = "(String value)";
            } else if (MybatisGenerator.MYSQL_DATE_TYPE.contains(cl.getType())) {
                // 注意，data一律转long
                pojoContent.append("    @ESBDesc(\"" + cl.getCmmt() + "\")\n");
                pojoContent.append("    private long    ");
                get = "    public long get";
                set = "(long value)";
            } else {
                continue;
            }
            String property = toHumpString(cl.getName(),false);
            pojoContent.append(property);
            pojoContent.append(";\n");

            // get
            getset.append(get);
            getset.append(toHumpString(cl.getName(),true));
            getset.append("() {\n        return this.");
            getset.append(property);
            getset.append(";\n    }\n");

            // set
            getset.append("    public void set");
            getset.append(toHumpString(cl.getName(),true));
            getset.append(set);
            getset.append(" {\n        this.");
            getset.append(property);
            getset.append(" = value;\n    }\n");
        }

        // 设置get set
        pojoContent.append("\n");
        pojoContent.append(getset);

        pojoContent.append("}\n\r\n\r");

        try {
            writeFile(file,pojoContent.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeEntityResults(File file, String packageName, MybatisGenerator.Table table) {
        //在增加一个结果集
        StringBuilder resultContent = new StringBuilder();
        resultContent.append("package " + packageName + ".entities;\n\r\n\r");
        resultContent.append("import com.venus.esb.annotation.ESBDesc;\n");
        resultContent.append("import com.venus.gen.service.PageResults;\n\r\n\r");
        resultContent.append("/**\n");
        resultContent.append(" * Owner: Minjun Ling\n");
        resultContent.append(" * Creator: ESB ServiceGenerator\n");
        resultContent.append(" * Version: 1.0.0\n");
        resultContent.append(" * GitHub: https://github.com/lingminjun/esb\n");
//        resultContent.append(" * Since: " + new Date() + "\n");
        resultContent.append(" * Description: " + table.getSimplePOJOClassName() + "结果集\n");
        resultContent.append(" */\n");
        resultContent.append("@ESBDesc(\"" + table.getSimplePOJOClassName() + "结果集\")\n");
        resultContent.append("public final class " + table.getSimplePOJOResultsClassName() + " extends PageResults<" + table.getSimplePOJOClassName() + "> { \n");
        resultContent.append("    /* nothing */\n");
        resultContent.append("}\n\r\n\r");

        try {
            writeFile(file,resultContent.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeCRUDService(File file, String packageName,String doaPackagaeName, String groupName, Class exceptionClass, ESBSecurityLevel security, MybatisGenerator.Table table) {

        String theSecurity = "ESBSecurityLevel." + security.toString();

        StringBuilder serviceContent = new StringBuilder();
        serviceContent.append("package " + packageName + ".api;\n\r\n\r");
        serviceContent.append("import " + exceptionClass.getName() + ";\n");
        serviceContent.append("import com.venus.esb.ESBSecurityLevel;\n");
        serviceContent.append("import com.venus.esb.lang.ESBException;\n");
        serviceContent.append("import com.venus.esb.annotation.ESBAPI;\n");
        serviceContent.append("import com.venus.esb.annotation.ESBError;\n");
        serviceContent.append("import com.venus.esb.annotation.ESBGroup;\n");
        serviceContent.append("import com.venus.esb.annotation.ESBParam;\n");
        serviceContent.append("import java.util.*;\n");
//        serviceContent.append("import org.springframework.beans.factory.annotation.Autowired;\n");
//        serviceContent.append("import " + table.getDAOClassName(doaPackagaeName) + ";\n");
//        serviceContent.append("import " + table.getDObjectClassName(doaPackagaeName) + ";\n");
        serviceContent.append("import " + table.getPOJOClassName(packageName) + ";\n");
        serviceContent.append("import " + table.getPOJOResultsClassName(packageName) + ";\n\r\n\r");
        serviceContent.append("/**\n");
        serviceContent.append(" * Owner: Minjun Ling\n");
        serviceContent.append(" * Creator: ESB ServiceGenerator\n");
        serviceContent.append(" * Version: 1.0.0\n");
        serviceContent.append(" * GitHub: https://github.com/lingminjun/esb\n");
//        serviceContent.append(" * Since: " + new Date() + "\n");
        serviceContent.append(" * Table: " + table.getName() + "\n");
        serviceContent.append(" */\n");

        String tableModelName = toHumpString(table.getAlias(),true);

        serviceContent.append("@ESBGroup(domain = \"" + groupName + "\", desc = \"" + tableModelName + "的相关操作\", codeDefine = " + exceptionClass.getSimpleName() + ".class)\n");
        serviceContent.append("public interface " + table.getSimpleCRUDServiceBeanName() + " {\n\n");

        String pojoName = table.getSimplePOJOClassName();
        //所有基本的增删修查

        if (!table.justViewTable()) {
            //增加单个
            writeCreateMethod(tableModelName, groupName, pojoName, theSecurity, serviceContent, table, false, null);

            //批量增加
            writeBatchCreateMethod(tableModelName, groupName, pojoName, theSecurity, serviceContent, table, false, null);

            //删，单个删除
            writeDeleteMethod(tableModelName, groupName, pojoName, theSecurity, serviceContent, table, false, null);

            //更新某个数据，拆开每个字段
            writeUpdatePojoMethod(tableModelName, groupName, pojoName, theSecurity, serviceContent, table, false, null);
            writeUpdateMethod(tableModelName, groupName, pojoName, theSecurity, serviceContent, table, false, null);

            //主键查询
            writeFindByIdMethod(tableModelName, groupName, pojoName, theSecurity, serviceContent, table, false, false, null);
            writeFindByIdMethod(tableModelName, groupName, pojoName, theSecurity, serviceContent, table, false, true, null);

            //主键批量查询
            writeQueryByIdsMethod(tableModelName, groupName, pojoName, theSecurity, serviceContent, table, false, false, false, null);
            writeQueryByIdsMethod(tableModelName, groupName, pojoName, theSecurity, serviceContent, table, false, true, false, null);

            writeQueryByIdsMethod(tableModelName, groupName, pojoName, theSecurity, serviceContent, table, false, false, true, null);
            writeQueryByIdsMethod(tableModelName, groupName, pojoName, theSecurity, serviceContent, table, false, true, true, null);
        }

        //查询唯一索引查询
        {
            // 与前面findBy冲突
            Map<String, List<MybatisGenerator.Column>> queryMethods = table.allUniqueIndexQueryMethod(false);
            List<String> methodNames = new ArrayList<String>(queryMethods.keySet());
            Collections.sort(methodNames);
            for (String methodName : methodNames) {
                List<MybatisGenerator.Column> cols = queryMethods.get(methodName);

                writeFindMethod(tableModelName, groupName, pojoName, methodName, cols, theSecurity, serviceContent, table, false, false, null);
                writeFindMethod(tableModelName, groupName, pojoName, methodName, cols, theSecurity, serviceContent, table, false, true, null);
            }
        }

        //查询，索引查询，翻页
        {
            Map<String, List<MybatisGenerator.Column>> queryMethods = table.allIndexQueryMethod();
            List<String> methodNames = new ArrayList<String>(queryMethods.keySet());
            Collections.sort(methodNames);
            for (String methodName : methodNames) {
                List<MybatisGenerator.Column> cols = queryMethods.get(methodName);

                // 不排序
                writeQueryMethod(tableModelName, groupName, pojoName, methodName, cols, theSecurity, serviceContent, table, false, false, false, false, null);
                writeQueryMethod(tableModelName, groupName, pojoName, methodName, cols, theSecurity, serviceContent, table, false, true, false, false, null);

                // 排序
                writeQueryMethod(tableModelName, groupName, pojoName, methodName, cols, theSecurity, serviceContent, table, false, false, true, false, null);
                writeQueryMethod(tableModelName, groupName, pojoName, methodName, cols, theSecurity, serviceContent, table, false, true, true, false, null);
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

                writeQueryMethod(tableModelName, groupName, pojoName, methodName, cols, theSecurity, serviceContent, table, false, false, false, true, null);
                writeQueryMethod(tableModelName, groupName, pojoName, methodName, cols, theSecurity, serviceContent, table, false, true, false, true, null);
            }
        }

        serviceContent.append("}\n\r\n\r");

        try {
            writeFile(file,serviceContent.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeCreateMethod(String tableModelName, String groupName, String pojoName, String theSecurity, StringBuilder serviceContent, MybatisGenerator.Table table, boolean implement, Map<String,Set<String>> AI) {
        String param = toLowerHeadString(tableModelName);

        serviceContent.append("    /**\n");
        serviceContent.append("     * insert " + pojoName + "\n");
        serviceContent.append("     * @return \n");
        serviceContent.append("     */\n");
        String addMethod = "add" + tableModelName;

        //改名字，主键id
        String primaryKey = toHumpString(table.getPrimaryKeyName(),false);

        if (!implement) {
            serviceContent.append("    @ESBAPI(module = \"" + groupName + "\",name = \"" + addMethod + "\", desc = \"插入" + pojoName + "\", security = " + theSecurity + ")\n");
        } else {
            serviceContent.append("    @Override\n");
            if (hasUsingAutoCache(addMethod,AI)) {
                serviceContent.append("    ");
            } else {
                serviceContent.append("    //");
            }
            serviceContent.append("@AutoCache(key = \"" + table.getAlias().toUpperCase() + "_#{" + param + "." + primaryKey + "}\", evict = true)\n");
        }

        String defineMethod = "    public long " + addMethod + "(@ESBParam(name = \"" + param + "\", desc = \"实体对象\", required = true) final " + pojoName + " " + param;
        serviceContent.append(defineMethod);

        if (!implement) {
            serviceContent.append(") throws ESBException;\n\n");
            return;
        } else {
            serviceContent.append(") throws ESBException {\n");
        }

        //实现代码
        String theDaoBean = "this." + toLowerHeadString(table.getSimpleDAOClassName());
        String dataObj = table.getSimpleDObjectClassName();
        String primaryField = toHumpString(table.getPrimaryKeyName(),false);

        serviceContent.append("        " + dataObj + " dobj = new " + dataObj + "();\n");
        serviceContent.append("        Injects.fill(" + param + ",dobj);\n");
        serviceContent.append("        if (" + theDaoBean + ".insertOrUpdate(dobj) > 0) {\n");
        serviceContent.append("            return (Long)dobj." + primaryField + ";\n");
        serviceContent.append("        } else {\n");
        serviceContent.append("            return -1l;\n");
        serviceContent.append("        }\n");

        serviceContent.append("    }\n\n");
    }


    public static void writeBatchCreateMethod(String tableModelName, String groupName, String pojoName, String theSecurity, StringBuilder serviceContent, MybatisGenerator.Table table, boolean implement, Map<String,Set<String>> AI) {
        serviceContent.append("    /**\n");
        serviceContent.append("     * batch insert " + pojoName + "\n");
        serviceContent.append("     * @return \n");
        serviceContent.append("     */\n");
        String batchAddMethod = "batchAdd" + tableModelName;

        //改名字，主键id
        String primaryKey = toHumpString(table.getPrimaryKeyName(),false);
        String param = toLowerHeadString(tableModelName);

        if (!implement) {
            serviceContent.append("    @ESBAPI(module = \"" + groupName + "\",name = \"" + batchAddMethod + "\", desc = \"批量插入" + pojoName + "\", security = " + theSecurity + ")\n");
        } else {
            serviceContent.append("    @Override\n");
            if (hasUsingAutoCache(batchAddMethod,AI)) {
                serviceContent.append("    ");
            } else {
                serviceContent.append("    //");
            }
            serviceContent.append("@AutoCache(key = \"@foreach(item : #{" + param + "s}) => '" + table.getAlias().toUpperCase() + "_#{item." + primaryKey + "}'\", evict = true)\n");
        }

        String defineMethod = "    public boolean " + batchAddMethod + "(@ESBParam(name = \"" + param + "s\", desc = \"实体对象集合\", required = true) final List<" + pojoName + "> " + param + "s,\n";
        String spacing = formatSpaceParam(defineMethod);
        serviceContent.append(defineMethod);
        serviceContent.append(spacing);
        serviceContent.append("@ESBParam(name = \"ignoreError\", desc = \"忽略错误，单个插入，但是效率低；若不忽略错误，批量提交，效率高\", required = true) final boolean ignoreError");

        if (!implement) {
            serviceContent.append(") throws ESBException;\n\n");
            return;
        } else {
            serviceContent.append(") throws ESBException {\n");
        }

        //实现代码
        String theDaoBean = "this." + toLowerHeadString(table.getSimpleDAOClassName());
        String dataObj = table.getSimpleDObjectClassName();

        serviceContent.append("        if (ignoreError) {\n");
        serviceContent.append("            for (final " + pojoName + "  pojo : " + param + "s) {\n");

        serviceContent.append("                " + dataObj + " dobj = new " + dataObj + "();\n");
        serviceContent.append("                Injects.fill(pojo,dobj);\n");
        serviceContent.append("                try {\n");
        serviceContent.append("                    " + theDaoBean + ".insertOrUpdate(dobj);\n");
        serviceContent.append("                } catch (Throwable e) {\n");
        serviceContent.append("                    logger.error(\"batch add " + tableModelName + " error!\", e);\n");
        serviceContent.append("                }\n");
        serviceContent.append("            }\n");
        serviceContent.append("            return true;\n");
        serviceContent.append("        } else {\n");

        serviceContent.append("            List<" + dataObj + "> lst = new ArrayList<" + dataObj + ">();\n");
        serviceContent.append("            for (" + pojoName + " pojo : " + param + "s) {\n");
        serviceContent.append("                " + dataObj + " dobj = new " + dataObj + "();\n");
        serviceContent.append("                Injects.fill(pojo,dobj);\n");
        serviceContent.append("                lst.add(dobj);\n");
        serviceContent.append("            }\n");
        serviceContent.append("            " + theDaoBean + ".batchInsert(lst);\n");
        serviceContent.append("            return true;\n");

        serviceContent.append("        }\n\n");


        serviceContent.append("    }\n\n");
    }

    public static void writeDeleteMethod(String tableModelName, String groupName, String pojoName, String theSecurity, StringBuilder serviceContent, MybatisGenerator.Table table, boolean implement, Map<String,Set<String>> AI) {
        MybatisGenerator.Column column = table.getDeleteStateColumn();
        if (column == null) {
            return;
        }

        //改名字，主键id
        String primaryKey = toHumpString(table.getPrimaryKeyName(),false);

        serviceContent.append("    /**\n");
        serviceContent.append("     * remove " + pojoName + "\n");
        serviceContent.append("     * @return \n");
        serviceContent.append("     */\n");
        String removeMethod = "removeThe" + tableModelName;
        if (!implement) {
            serviceContent.append("    @ESBAPI(module = \"" + groupName + "\",name = \"" + removeMethod + "\", desc = \"删除" + pojoName + "\", security = " + theSecurity + ")\n");
        } else {
            serviceContent.append("    @Override\n");
            if (hasUsingAutoCache(removeMethod,AI)) {
                serviceContent.append("    ");
            } else {
                serviceContent.append("    //");
            }
            serviceContent.append("@AutoCache(key = \"" + table.getAlias().toUpperCase() + "_#{" + primaryKey + "}\", evict = true)\n");
        }
        serviceContent.append("    public boolean " + removeMethod + "(@ESBParam(name = \"" + primaryKey + "\", desc = \"对象的" + primaryKey + "\", required = true) final long " + primaryKey);

        if (!implement) {
            serviceContent.append(") throws ESBException;\n\n");
            return;
        } else {
            serviceContent.append(") throws ESBException {\n");
        }

        //实现代码
        String theDaoBean = "this." + toLowerHeadString(table.getSimpleDAOClassName());

        serviceContent.append("        " + theDaoBean + ".deleteById(" + primaryKey + ");\n");
        serviceContent.append("        return true;\n");

        serviceContent.append("    }\n\n");

    }

    public static void writeUpdatePojoMethod(String tableModelName, String groupName, String pojoName, String theSecurity, StringBuilder serviceContent, MybatisGenerator.Table table, boolean implement, Map<String,Set<String>> AI) {

        serviceContent.append("    /**\n");
        serviceContent.append("     * update " + pojoName + "\n");
        serviceContent.append("     * @return \n");
        serviceContent.append("     */\n");
        String updateMethod = "update" + tableModelName;

        //改名字，主键id
        String primaryKey = toHumpString(table.getPrimaryKeyName(),false);
        String param = toLowerHeadString(tableModelName);

        if (!implement) {
            serviceContent.append("    @ESBAPI(module = \"" + groupName + "\",name = \"" + updateMethod + "\", desc = \"更新" + pojoName + "，仅更新不为空的属性(基础类型非零属性)，delete字段被忽略，故无法调用此接口删除数据\", security = " + theSecurity + ")\n");
        } else {
            serviceContent.append("    @Override\n");
            if (hasUsingAutoCache(updateMethod,AI)) {
                serviceContent.append("    ");
            } else {
                serviceContent.append("    //");
            }
            serviceContent.append("@AutoCache(key = \"" + table.getAlias().toUpperCase() + "_#{" + param + "." + primaryKey + "}\", evict = true)\n");
        }

        serviceContent.append("    public boolean " + updateMethod + "(@ESBParam(name = \"" + param + "\", desc = \"更新对象\", required = true) final " + pojoName + " " + param);//首段写入

        if (!implement) {
            serviceContent.append(") throws ESBException;\n\n");
            return;
        } else {
            serviceContent.append(") throws ESBException {\n");
        }

        serviceContent.append("        // 不合法的数据更新，必须要有id\n");
        serviceContent.append("        if (" + param + ".get" + toHumpString(primaryKey,true) + "() <= 0) {\n");
        serviceContent.append("            return false;\n");
        serviceContent.append("        }\n");

        //实现代码
        String theDaoBean = "this." + toLowerHeadString(table.getSimpleDAOClassName());

        String dataObj = table.getSimpleDObjectClassName();

        serviceContent.append("        " + dataObj + " dobj = new " + dataObj + "();\n");
        serviceContent.append("        Injects.fill(" + param + ",dobj);\n");

        // 清理不能update的数据
        serviceContent.append("        // 清理不需要更新字段，防止update做过多检查\n");
        for (MybatisGenerator.Column cl : table.getColumns()) {
            //忽略字段
            if (cl.getName().equals("is_delete")
                    || cl.getName().equals("delete")
                    || cl.getName().equals("is_deleted")
                    || cl.getName().equals("deleted")
                    || cl.getName().equals("create_at")
                    || cl.getName().equals("created_at")
                    || cl.getName().equals("modify_at")
                    || cl.getName().equals("modified_at")
                    ) {
                serviceContent.append("        dobj." + toHumpString(cl.getName(),false) + " = null;\n");
            } else if (!cl.isPrimary() && !cl.getName().equals("id")) {
                if (!cl.isStringType()) {
                    serviceContent.append("        if (dobj." + toHumpString(cl.getName(),false) + " != null &&  0 == dobj." + toHumpString(cl.getName(),false) + ") {\n");
                    serviceContent.append("            dobj." + toHumpString(cl.getName(),false) + " = null;\n");
                    serviceContent.append("        }\n\n");
                }
            }
        }
        serviceContent.append("        " + theDaoBean + ".update(dobj);\n");
        serviceContent.append("        return true;\n");

        serviceContent.append("    }\n\n");
    }

    @Deprecated
    public static void writeUpdateMethod(String tableModelName, String groupName, String pojoName, String theSecurity, StringBuilder serviceContent, MybatisGenerator.Table table, boolean implement, Map<String,Set<String>> AI) {

        serviceContent.append("    /**\n");
        serviceContent.append("     * update " + pojoName + "\n");
        serviceContent.append("     * @return \n");
        serviceContent.append("     */\n");
        String updateMethod = "updateThe" + tableModelName;

        //改名字，主键id
        String primaryKey = toHumpString(table.getPrimaryKeyName(),false);

        if (!implement) {
            serviceContent.append("    @ESBAPI(module = \"" + groupName + "\",name = \"" + updateMethod + "\", desc = \"更新" + pojoName + "，仅更新不为空的字段\", security = " + theSecurity + ")\n");
        } else {
            serviceContent.append("    @Override\n");
            if (hasUsingAutoCache(updateMethod,AI)) {
                serviceContent.append("    ");
            } else {
                serviceContent.append("    //");
            }
            serviceContent.append("@AutoCache(key = \"" + table.getAlias().toUpperCase() + "_#{" + primaryKey + "}\", evict = true)\n");
        }

        String defineMethod = "    public boolean " + updateMethod + "(@ESBParam(name = \"" + primaryKey + "\", desc = \"更新对象的" + primaryKey + "\", required = true) final long " + primaryKey;

        String spacing = formatSpaceParam(defineMethod);

        serviceContent.append(defineMethod);//首段写入
        List<MybatisGenerator.Column> params = new ArrayList<MybatisGenerator.Column>();
        for (MybatisGenerator.Column cl : table.getColumns()) {
            //忽略字段
            if (cl.isPrimary()
                    || cl.getName().equals("is_delete")
                    || cl.getName().equals("delete")
                    || cl.getName().equals("is_deleted")
                    || cl.getName().equals("deleted")
                    || cl.getName().equals("id")
                    || cl.getName().equals("create_at")
                    || cl.getName().equals("created_at")
                    || cl.getName().equals("modify_at")
                    || cl.getName().equals("modified_at")
                    ) {
                continue;
            }

            String paramName = toHumpString(cl.getName(),false);
            params.add(cl);
            serviceContent.append(",\n");
            serviceContent.append(spacing);
            serviceContent.append("@ESBParam(name = \"" + paramName + "\", desc = \"" + cl.getCmmt() + "\", required = false) final " + cl.getDefinedType() + " " + paramName);
        }
        if (!implement) {
            serviceContent.append(") throws ESBException;\n\n");
            return;
        } else {
            serviceContent.append(") throws ESBException {\n");
        }

        //实现代码
        String theDaoBean = "this." + toLowerHeadString(table.getSimpleDAOClassName());

        String dataObj = table.getSimpleDObjectClassName();

        serviceContent.append("        " + dataObj + " dobj = new " + dataObj + "();\n");
        MybatisGenerator.Column primary = table.getPrimaryColumn();
        if (primary != null) {
            serviceContent.append("        dobj." + primaryKey + " = (" + primary.getDataType() + ")" + primaryKey + ";\n");
        } else {
            serviceContent.append("        dobj." + primaryKey + " = " + primaryKey + ";\n");
        }
        for (MybatisGenerator.Column cl : params) {
            String paramName = toHumpString(cl.getName(),false);
            serviceContent.append("        dobj." + paramName + " = " + paramName + ";\n");
        }
        serviceContent.append("        " + theDaoBean + ".update(dobj);\n");
        serviceContent.append("        return true;\n");

        serviceContent.append("    }\n\n");
    }

    public static void writeFindByIdMethod(String tableModelName, String groupName, String pojoName, String theSecurity, StringBuilder serviceContent, MybatisGenerator.Table table, boolean implement, boolean hasCache, Map<String,Set<String>> AI) {

        //改名字，主键id
        String primaryKey = toHumpString(table.getPrimaryKeyName(),false);

        serviceContent.append("    /**\n");
        if (hasCache) {
            serviceContent.append("     * backend find " + pojoName + " by " + primaryKey + "\n");
        } else {
            serviceContent.append("     * find " + pojoName + " by " + primaryKey + "\n");
        }
        serviceContent.append("     * @return \n");
        serviceContent.append("     */\n");
        String findMethod = null;
        if (hasCache) {
            findMethod = "rawFindThe" + tableModelName;
        } else {
            findMethod = "findThe" + tableModelName;
        }
        if (!implement) {
            serviceContent.append("    @ESBAPI(module = \"" + groupName + "\",name = \"" + findMethod + "\", desc = \"寻找" + pojoName + "\", security = " + (hasCache ? "ESBSecurityLevel.integrated" : theSecurity) + ")\n");
        } else {
            serviceContent.append("    @Override\n");
            if (hasUsingAutoCache(findMethod,AI)) {
                serviceContent.append("    ");
            } else {
                serviceContent.append("    //");
            }
            if (hasCache) {
                serviceContent.append("@AutoCache(key = \"" + table.getAlias().toUpperCase() + "_#{" + primaryKey + "}\", async = true, condition=\"!#{noCache}\")\n");
            } else {// Spring AOP下，不支持内部转调走代理
                serviceContent.append("@AutoCache(key = \"" + table.getAlias().toUpperCase() + "_#{" + primaryKey + "}\", async = true)\n");
            }
        }

        String defineMethod = "    public " + pojoName + " " + findMethod + "(@ESBParam(name = \"" + primaryKey + "\", desc = \"对象的" + primaryKey + "\", required = true) final long " + primaryKey;
        serviceContent.append(defineMethod);
        String spacing = formatSpaceParam(defineMethod);

        //是否走缓存
        if (hasCache) {
            serviceContent.append(",\n");
            serviceContent.append(spacing);
            serviceContent.append("@ESBParam(name = \"noCache\", desc = \"是否走缓存\", required = false) final boolean noCache");
        }

        if (!implement) {
            serviceContent.append(") throws ESBException;\n\n");
            return;
        } else {
            serviceContent.append(") throws ESBException {\n");
        }

        //实现代码
        String theDaoBean = "this." + toLowerHeadString(table.getSimpleDAOClassName());

        String dataObj = table.getSimpleDObjectClassName();

        if (!hasCache) {
            serviceContent.append("        // If you wan't to use annotation caching automatically.\n");
            serviceContent.append("        // Please add the cached code here.\n");
            serviceContent.append("        // String cackeKey = \"" + table.getAlias().toUpperCase() + "_\" + " + primaryKey + ";\n\n");
            serviceContent.append("        return this.");
            serviceContent.append("raw");
            serviceContent.append(toUpperHeadString(findMethod));
            serviceContent.append("(" + primaryKey + ",false);\n");
        } else {
            serviceContent.append("        " + dataObj + " dobj = " + theDaoBean + ".getById(" + primaryKey + ");\n");
            serviceContent.append("        " + pojoName + " pojo = new " + pojoName + "();\n");
            serviceContent.append("        Injects.fill(dobj,pojo);\n");
            serviceContent.append("        return pojo;\n");
        }

        serviceContent.append("    }\n\n");

    }

    public static void writeQueryByIdsMethod(String tableModelName, String groupName, String pojoName, String theSecurity, StringBuilder serviceContent, MybatisGenerator.Table table, boolean implement, boolean hasCache, boolean hasSortKey, Map<String,Set<String>> AI) {

        //改名字，主键id
        String primaryKey = toHumpString(table.getPrimaryKeyName(),false);

        serviceContent.append("    /**\n");
        if (hasCache) {
            if (hasSortKey) {
                serviceContent.append("     * backend query " + pojoName + " by " + primaryKey + "s sorted\n");
            } else {
                serviceContent.append("     * backend query " + pojoName + " by " + primaryKey + "s\n");
            }
        } else {
            if (hasSortKey) {
                serviceContent.append("     * query " + pojoName + " by " + primaryKey + "s sorted\n");
            } else {
                serviceContent.append("     * query " + pojoName + " by " + primaryKey + "s\n");
            }
        }
        serviceContent.append("     * @return \n");
        serviceContent.append("     */\n");
        String findMethod = null;
        if (hasCache) {
            findMethod = "rawQuery" + tableModelName + "By" + toHumpString(primaryKey,true) + "s";
        } else {
            findMethod = "query" + tableModelName + "By" + toHumpString(primaryKey,true) + "s";
        }
        if (hasSortKey) {
            findMethod += "Sorted";
        }
        if (!implement) {
            serviceContent.append("    @ESBAPI(module = \"" + groupName + "\",name = \"" + findMethod + "\", desc = \"查找" + pojoName + "\", security = " + (hasCache ? "ESBSecurityLevel.integrated" : theSecurity) + ")\n");
        } else {
            serviceContent.append("    @Override\n");
            if (hasUsingAutoCache(findMethod,AI)) {
                serviceContent.append("    ");
            } else {
                serviceContent.append("    //");
            }
            if (hasSortKey) {
                if (hasCache) {
                    serviceContent.append("@AutoCache(key = \"BATCH_" + table.getAlias().toUpperCase() + "_#{" + primaryKey + "s}_SORTED_BY_#{sortBy}_DESC_#{isDesc}\", async = true, condition=\"!#{noCache}\")\n");
                } else {
                    serviceContent.append("@AutoCache(key = \"BATCH_" + table.getAlias().toUpperCase() + "_#{" + primaryKey + "s}_SORTED_BY_#{sortBy}_DESC_#{isDesc}\", async = true)\n");
                }
            } else {
                if (hasCache) {
                    serviceContent.append("@AutoCache(key = \"BATCH_" + table.getAlias().toUpperCase() + "_#{" + primaryKey + "s}\", async = true, condition=\"!#{noCache}\")\n");
                } else {
                    serviceContent.append("@AutoCache(key = \"BATCH_" + table.getAlias().toUpperCase() + "_#{" + primaryKey + "s}\", async = true)\n");
                }
            }
        }

        String defineMethod = "    public " + table.getSimplePOJOResultsClassName() + " " + findMethod + "(@ESBParam(name = \"" + primaryKey + "s\", desc = \"对象id集合\", required = true) final long[] " + primaryKey + "s";
        serviceContent.append(defineMethod);
        String spacing = formatSpaceParam(defineMethod);

        if (hasSortKey) {
            serviceContent.append(",\n");
            serviceContent.append(spacing);
            serviceContent.append("@ESBParam(name = \"sortBy\", desc = \"排序key，注意：此处需要参照存储表字段\", required = false) final String sortBy");

            serviceContent.append(",\n");
            serviceContent.append(spacing);
            serviceContent.append("@ESBParam(name = \"isDesc\", desc = \"对应sortBy参数，排序是否为降序，默认为否\", required = false) final boolean isDesc");
        }

        //是否走缓存
        if (hasCache) {
            serviceContent.append(",\n");
            serviceContent.append(spacing);
            serviceContent.append("@ESBParam(name = \"noCache\", desc = \"是否走缓存\", required = false) final boolean noCache");
        }

        if (!implement) {
            serviceContent.append(") throws ESBException;\n\n");
            return;
        } else {
            serviceContent.append(") throws ESBException {\n");
        }

        //实现代码
        String theDaoBean = "this." + toLowerHeadString(table.getSimpleDAOClassName());

        String dataObj = table.getSimpleDObjectClassName();
        String resultsName = table.getSimplePOJOResultsClassName();

        if (!hasCache) {
            serviceContent.append("        // If you wan't to use annotation caching automatically.\n");
            serviceContent.append("        // Please add the cached code here.\n");
            if (hasSortKey) {
                serviceContent.append("        // String cackeKey = \"BATCH_" + table.getAlias().toUpperCase() + "_\" + " + primaryKey + "s.toString() + \"_SORTED_BY_\" + sortBy + \"_DESC_\" + isDesc;\n\n");
            } else {
                serviceContent.append("        // String cackeKey = \"BATCH_" + table.getAlias().toUpperCase() + "_\" + " + primaryKey + "s.toString();\n\n");
            }
            serviceContent.append("        return this.");
            serviceContent.append("raw");
            serviceContent.append(toUpperHeadString(findMethod));
            if (hasSortKey) {
                serviceContent.append("(" + primaryKey + "s,sortBy,isDesc,false);\n");
            } else {
                serviceContent.append("(" + primaryKey + "s,false);\n");
            }
        } else {

            serviceContent.append("        if (" + primaryKey + "s == null || " + primaryKey + "s.length <= 0) {\n");
            serviceContent.append("            throw new ESBException(\"参数错误\",\"" + groupName + "\",-1,\"id集合不能为空\");\n");
            serviceContent.append("        }\n");

            serviceContent.append("        List<Long> idList = new ArrayList<Long>();\n");
            serviceContent.append("        for (long id : " + primaryKey + "s) {\n");
            serviceContent.append("            idList.add(id);\n");
            serviceContent.append("        }\n");

            if (hasSortKey) {
                serviceContent.append("        List<" + dataObj + "> list = " + theDaoBean + ".queryByIdsOnSort(idList,sortBy,isDesc);\n");
            } else {
                serviceContent.append("        List<" + dataObj + "> list = " + theDaoBean + ".queryByIds(idList);\n");
            }

            serviceContent.append("        List<" + pojoName + "> rs = new ArrayList<" + pojoName + ">();\n");
            serviceContent.append("        for (" + dataObj + " dobj : list) {\n");
            serviceContent.append("            " + pojoName + " pojo = new " + pojoName + "();\n");
            serviceContent.append("            Injects.fill(dobj,pojo);\n");
            serviceContent.append("            rs.add(pojo);\n");
            serviceContent.append("        }\n");

            serviceContent.append("        " + resultsName + " rlt = new " + resultsName + "();\n");
            serviceContent.append("        rlt.setIndex(1);\n");
            serviceContent.append("        rlt.setTotal(rs.size());\n");
            serviceContent.append("        rlt.setSize(rs.size());\n");
            serviceContent.append("        rlt.setResults(rs);\n");
            serviceContent.append("        return rlt;\n");
        }

        serviceContent.append("    }\n\n");
    }

    public static void writeFindMethod(String tableModelName, String groupName, String pojoName, String queryMethodName, List<MybatisGenerator.Column> columns, String theSecurity, StringBuilder serviceContent, MybatisGenerator.Table table, boolean implement, boolean hasCache, Map<String,Set<String>> AI) {

        String methodName = (hasCache ? "rawFindThe" : "findThe") + tableModelName + queryMethodName.substring("find".length());//.replace("find", "find" + tableModelName);

        String defineMethod = "    public " + pojoName + " " + methodName + "(";
        String spacing = formatSpaceParam(defineMethod);

        //提前处理参数
        StringBuilder methodParams = new StringBuilder();
        StringBuilder methodParamsDef = new StringBuilder();
        StringBuilder cacheKeyDef = new StringBuilder();
        StringBuilder cacheKeyAuto = new StringBuilder();
        StringBuilder paramsDesc = new StringBuilder();
        for (MybatisGenerator.Column column : columns) {

            String param = toHumpString(column.getName(),false);
            if (methodParams.length() > 0) {
                methodParams.append(",");
                methodParamsDef.append(",\n");
                paramsDesc.append(" and ");
                methodParamsDef.append(spacing);
            }
            methodParams.append(param);
            paramsDesc.append(param);
            methodParamsDef.append("@ESBParam(name = \"" + param + "\", desc = \"" + column.getCmmt() + "\", required = true) final " + column.getDataType() + " " + param);

            if (cacheKeyDef.length() > 0) {
                cacheKeyDef.append("_");
            }
            cacheKeyDef.append(column.getName().toUpperCase());
            cacheKeyDef.append(":\" + ");
            cacheKeyDef.append(param);
            cacheKeyDef.append(" + \"");

            if (cacheKeyAuto.length() > 0) {
                cacheKeyAuto.append("_");
            }
            cacheKeyAuto.append(column.getName().toUpperCase());
            cacheKeyAuto.append(":#{");
            cacheKeyAuto.append(param);
            cacheKeyAuto.append("}");
        }

        // 判断是否有delete参数
        boolean hasDeleted = false;
        String delParamIn = "";
        MybatisGenerator.Column theDelete = null;
        if (table != null) {
            theDelete = table.getDeleteStateColumn();
            if (!MybatisGenerator.Table.hasDeleteStateColumn(columns) && theDelete != null) {
                hasDeleted = true;

                methodParamsDef.append(",\n");
                methodParamsDef.append(spacing);
                methodParamsDef.append("@ESBParam(name = \"isDeleted\", desc = \"是否已经被标记删除的\", required = false) final boolean isDeleted");

                cacheKeyDef.append("_DEL:\" + isDeleted + \"");
                cacheKeyAuto.append("_DEL:#{isDeleted}");
                if (theDelete.getDataType().equals("boolean")) {
                    delParamIn = "isDeleted";
                } else {
                    delParamIn = "(isDeleted ? 1 : 0)";
                }
            }
        }

        //开始编写函数代码
        serviceContent.append("    /**\n");
        if (hasCache) {
            serviceContent.append("     * backend find the " + pojoName + " by " + paramsDesc + "\n");
        } else {
            serviceContent.append("     * find the " + pojoName + " by " + paramsDesc + "\n");
        }

        serviceContent.append("     * @return \n");
        serviceContent.append("     */\n");
        if (!implement) {
            serviceContent.append("    @ESBAPI(module = \"" + groupName + "\",name = \"" + methodName + "\", desc = \"查询" + pojoName + "\", security = " + (hasCache ? "ESBSecurityLevel.integrated" : theSecurity) + ")\n");
        } else {
            serviceContent.append("    @Override\n");
            if (hasUsingAutoCache(methodName,AI)) {
                serviceContent.append("    ");
            } else {
                serviceContent.append("    //");
            }
            if (hasCache) {
                serviceContent.append("@AutoCache(key = \"" + table.getAlias().toUpperCase() + "_FIND_BY_" + cacheKeyAuto.toString() + "\", async = true, condition=\"!#{noCache}\")\n");
            } else {
                serviceContent.append("@AutoCache(key = \"" + table.getAlias().toUpperCase() + "_FIND_BY_" + cacheKeyAuto.toString() + "\", async = true)\n");
            }
        }

        //首段写入
        serviceContent.append(defineMethod);

        //定义所有参数
        serviceContent.append(methodParamsDef.toString());

        //是否走缓存
        if (hasCache) {
            serviceContent.append(",\n");
            serviceContent.append(spacing);
            serviceContent.append("@ESBParam(name = \"noCache\", desc = \"是否走缓存\", required = false) final boolean noCache");
        }


        if (!implement) {
            serviceContent.append(") throws ESBException;\n\n");
            return;
        } else {
            serviceContent.append(") throws ESBException {\n");
        }

        //实现代码
        String theDaoBean = "this." + toLowerHeadString(table.getSimpleDAOClassName());
        String dataObj = table.getSimpleDObjectClassName();
        String methodParamsString = methodParams.toString();

        if (!hasCache) {
            serviceContent.append("        // If you wan't to use annotation caching automatically.\n");
            serviceContent.append("        // Please add the cached code here.\n");
            serviceContent.append("        // String cackeKey = \"" + table.getAlias().toUpperCase() + "_FIND_BY_" + cacheKeyDef.toString() + "\";\n\n");
            serviceContent.append("        return this.");
            serviceContent.append("raw");
            serviceContent.append(toUpperHeadString(methodName));

            serviceContent.append("(");
            serviceContent.append(methodParamsString);// 普通参数
            // 数据库已删除，放前面
            if (hasDeleted) {
                serviceContent.append(",isDeleted");
            }
            serviceContent.append(",false);\n");// 缓存参数
        } else {

            // 检查参数是否合法
            for (MybatisGenerator.Column column : columns) {
                if (column.isStringType()) {
                    String param = toHumpString(column.getName(),false);
                    serviceContent.append("        if (" + param + " == null) {\n");
                    serviceContent.append("            throw new ESBException(\"参数错误\",\"" + groupName + "\",-1,\"" + param + "参数传入错误\");\n");
                    serviceContent.append("        }\n");
                }
            }

            serviceContent.append("        " + dataObj + " dobj = " + theDaoBean + "." + queryMethodName + "(");
            serviceContent.append(methodParamsString);
            // 数据库已删除，放前面
            if (hasDeleted) {
                serviceContent.append(",");
                serviceContent.append(delParamIn);
            }
            serviceContent.append(");\n");
            serviceContent.append("        " + pojoName + " pojo = new " + pojoName + "();\n");
            serviceContent.append("        Injects.fill(dobj,pojo);\n");
            serviceContent.append("        return pojo;\n");
        }

        serviceContent.append("    }\n\n");
    }

    public static void writeQueryMethod(String tableModelName, String groupName, String pojoName, String queryMethodName, List<MybatisGenerator.Column> columns, String theSecurity, StringBuilder serviceContent, MybatisGenerator.Table table, boolean implement, boolean hasCache, boolean hasSortKey, boolean isViewQuery, Map<String,Set<String>> AI) {

        String methodName = (hasCache ? "rawQuery" : "query") + tableModelName + queryMethodName.substring("query".length());//.replace("query", "query" + tableModelName);

        // view query不支持定制排序
        if (hasSortKey && !isViewQuery) {
            methodName += "Sorted";
        }

        String defineMethod = "    public " + table.getSimplePOJOResultsClassName() + " " + methodName + "(@ESBParam(name = \"pageIndex\", desc = \"页索引，从1开始，传入0或负数无数据返回\", required = true) final int pageIndex";
        String spacing = formatSpaceParam(defineMethod);

        //提前处理参数
        StringBuilder methodParams = new StringBuilder();
        StringBuilder methodParamsDef = new StringBuilder();
        StringBuilder cacheKeyDef = new StringBuilder();
        StringBuilder cacheKeyAuto = new StringBuilder();
        for (MybatisGenerator.Column column : columns) {

            String param = toHumpString(column.getName(),false);
            if (methodParams.length() > 0) {
                methodParams.append(",");
            }
            methodParams.append(param);

            methodParamsDef.append(",\n");
            methodParamsDef.append(spacing);
            methodParamsDef.append("@ESBParam(name = \"" + param + "\", desc = \"" + column.getCmmt() + "\", required = true) final " + column.getDataType() + " " + param);

            if (cacheKeyDef.length() > 0) {
                cacheKeyDef.append("_");
            }
            cacheKeyDef.append(column.getName().toUpperCase());
            cacheKeyDef.append(":\" + ");
            cacheKeyDef.append(param);
            cacheKeyDef.append(" + \"");

            if (cacheKeyAuto.length() > 0) {
                cacheKeyAuto.append("_");
            }
            cacheKeyAuto.append(column.getName().toUpperCase());
            cacheKeyAuto.append(":#{");
            cacheKeyAuto.append(param);
            cacheKeyAuto.append("}");
        }

        // 排序
        if (hasSortKey && !isViewQuery) {

            methodParamsDef.append(",\n");
            methodParamsDef.append(spacing);
            methodParamsDef.append("@ESBParam(name = \"sortBy\", desc = \"排序key，注意：此处需要参照存储表字段\", required = false) final String sortBy");

            methodParamsDef.append(",\n");
            methodParamsDef.append(spacing);
            methodParamsDef.append("@ESBParam(name = \"isDesc\", desc = \"对应sortBy参数，排序是否为降序，默认为否\", required = false) final boolean isDesc");

            cacheKeyDef.append("_SORTED_BY_\" + sortBy + \"_DESC_\" + isDesc + \"");
            cacheKeyAuto.append("_SORTED_BY_#{sortBy}_DESC_#{isDesc}");
        }

        //添加分页信息
        cacheKeyDef.append("_PAGE:\" + pageIndex + \",\" + pageSize + \"");
        cacheKeyAuto.append("_PAGE:#{pageIndex},#{pageSize}");

        // 判断是否有delete参数
        boolean hasDeleted = false;
        String delParamIn = "";
        MybatisGenerator.Column theDelete = null;
        if (!isViewQuery && table != null) {
            theDelete = table.getDeleteStateColumn();
            if (!MybatisGenerator.Table.hasDeleteStateColumn(columns) && theDelete != null) {
                hasDeleted = true;

                methodParamsDef.append(",\n");
                methodParamsDef.append(spacing);
                methodParamsDef.append("@ESBParam(name = \"isDeleted\", desc = \"是否已经被标记删除的\", required = false) final boolean isDeleted");

                cacheKeyDef.append("_DEL:\" + isDeleted + \"");
                cacheKeyAuto.append("_DEL:#{isDeleted}");
                if (theDelete.getDataType().equals("boolean")) {
                    delParamIn = "isDeleted";
                } else {
                    delParamIn = "(isDeleted ? 1 : 0)";
                }
            }
        }

        //开始编写函数代码
        serviceContent.append("    /**\n");
        if (hasSortKey && !isViewQuery) {
            if (hasCache) {
                serviceContent.append("     * backend query " + pojoName + " on sort. \n");
            } else {
                serviceContent.append("     * query " + pojoName + " on sort. \n");
            }
        } else {
            if (hasCache) {
                serviceContent.append("     * backend query " + pojoName + "\n");
            } else {
                serviceContent.append("     * query " + pojoName + "\n");
            }
        }
        serviceContent.append("     * @return \n");
        serviceContent.append("     */\n");
        if (!implement) {
            serviceContent.append("    @ESBAPI(module = \"" + groupName + "\",name = \"" + methodName + "\", desc = \"查询" + pojoName + "\", security = " + (hasCache ? "ESBSecurityLevel.integrated" : theSecurity) + ")\n");
        } else {
            serviceContent.append("    @Override\n");
            if (hasUsingAutoCache(methodName,AI)) {
                serviceContent.append("    ");
            } else {
                serviceContent.append("    //");
            }
            if (hasCache) {
                if (hasDeleted) {
                    serviceContent.append("@AutoCache(key = \"" + table.getAlias().toUpperCase() + (isViewQuery ? "" : "_QUERY_BY_") + cacheKeyAuto.toString() + "\", async = true, condition=\"!#{noCache} && !#{isDeleted}\")\n");
                } else {
                    serviceContent.append("@AutoCache(key = \"" + table.getAlias().toUpperCase() + (isViewQuery ? "" : "_QUERY_BY_") + cacheKeyAuto.toString() + "\", async = true, condition=\"!#{noCache}\")\n");
                }
            } else {
                if (hasDeleted) {
                    serviceContent.append("@AutoCache(key = \"" + table.getAlias().toUpperCase() + (isViewQuery ? "" : "_QUERY_BY_") + cacheKeyAuto.toString() + "\", async = true, condition=\"!#{isDeleted}\")\n");
                } else {
                    serviceContent.append("@AutoCache(key = \"" + table.getAlias().toUpperCase() + (isViewQuery ? "" : "_QUERY_BY_") + cacheKeyAuto.toString() + "\", async = true)\n");
                }
            }
        }

        serviceContent.append(defineMethod);//首段写入
        serviceContent.append(",\n");
        serviceContent.append(spacing);
        serviceContent.append("@ESBParam(name = \"pageSize\", desc = \"一页最大行数\", required = true) final int pageSize");

        //定义所有参数
        serviceContent.append(methodParamsDef.toString());

        //是否走缓存
        if (hasCache) {
            serviceContent.append(",\n");
            serviceContent.append(spacing);
            serviceContent.append("@ESBParam(name = \"noCache\", desc = \"是否走缓存\", required = false) final boolean noCache");
        }


        if (!implement) {
            serviceContent.append(") throws ESBException;\n\n");
            return;
        } else {
            serviceContent.append(") throws ESBException {\n");
        }

        //实现代码
        String theDaoBean = "this." + toLowerHeadString(table.getSimpleDAOClassName());

        String dataObj = table.getSimpleDObjectClassName();
        String resultsName = table.getSimplePOJOResultsClassName();
        String countMethodName = "count" + queryMethodName.substring(5);
        String methodParamsString = methodParams.toString();

        if (!hasCache) {
            serviceContent.append("        // If you wan't to use annotation caching automatically.\n");
            serviceContent.append("        // Please add the cached code here.\n");
            serviceContent.append("        // String cackeKey = \"" + table.getAlias().toUpperCase() + (isViewQuery ? "" : "_QUERY_BY_") + cacheKeyDef.toString() + "\";\n\n");
            serviceContent.append("        return this.");
            serviceContent.append("raw");
            serviceContent.append(toUpperHeadString(methodName));

            serviceContent.append("(pageIndex,pageSize,");

            // 普通参数
            serviceContent.append(methodParamsString);

            // 排序参数
            if (hasSortKey && !isViewQuery) {
                serviceContent.append(",sortBy,isDesc");
            }

            // 已删除
            if (hasDeleted) {
                serviceContent.append(",isDeleted");
            }

            // 缓存参数
            serviceContent.append(",false);\n");
        } else {

            if (isViewQuery || hasSortKey) {
                serviceContent.append("        if (pageIndex <= 0 || pageSize <= 0) {\n");
                serviceContent.append("            throw new ESBException(\"参数错误\",\"" + groupName + "\",-1,\"翻页参数传入错误\");\n");
                serviceContent.append("        }\n");
                serviceContent.append("        " + resultsName + " rlt = new " + resultsName + "();\n");
                serviceContent.append("        rlt.setIndex(pageIndex);\n");
                serviceContent.append("        rlt.setSize(pageSize);\n");
                serviceContent.append("        rlt.setTotal(" + theDaoBean + "." + countMethodName + "(");
                serviceContent.append(methodParamsString);
                if (hasDeleted) {
                    serviceContent.append(",");
                    serviceContent.append(delParamIn);
                }
                serviceContent.append("));\n");
                serviceContent.append("        List<" + dataObj + "> list = " + theDaoBean + "." + queryMethodName + "(");

                // 普通参数
                serviceContent.append(methodParamsString);

                // 数据库已删除，放前面
                if (hasDeleted) {
                    serviceContent.append(",");
                    serviceContent.append(delParamIn);
                }

                // 排序参数
                if (hasSortKey && !isViewQuery) {
                    serviceContent.append(",sortBy,isDesc");
                }

                if (isViewQuery) {
                    serviceContent.append(",(pageSize * (pageIndex - 1)), pageSize);\n");
                } else {
                    if (hasSortKey) { // 参数已经传入
                        serviceContent.append(",(pageSize * (pageIndex - 1)), pageSize);\n");
                    } else {
                        serviceContent.append(",null,false,(pageSize * (pageIndex - 1)), pageSize);\n");
                    }
                }
                serviceContent.append("        List<" + pojoName + "> rs = new ArrayList<" + pojoName + ">();\n");
                serviceContent.append("        for (" + dataObj + " dobj : list) {\n");
                serviceContent.append("            " + pojoName + " pojo = new " + pojoName + "();\n");
                serviceContent.append("            Injects.fill(dobj,pojo);\n");
                serviceContent.append("            rs.add(pojo);\n");
                serviceContent.append("        }\n");
                serviceContent.append("        rlt.setResults(rs);\n");
                serviceContent.append("        return rlt;\n");
            } else {// 采用方法转调比较合适
                serviceContent.append("        return this.");
                serviceContent.append(methodName);
                serviceContent.append("Sorted");

                // 翻页参数
                serviceContent.append("(pageIndex,pageSize,");

                // 普通参数
                serviceContent.append(methodParamsString);

                // 排序参数
                serviceContent.append(",null,false");

                // 已删除
                if (hasDeleted) {
                    serviceContent.append(",isDeleted");
                }

                // 缓存参数
                serviceContent.append(",false);\n");
            }
        }

        serviceContent.append("    }\n\n");
    }
}
