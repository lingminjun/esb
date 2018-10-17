package server.service.gen;

import com.venus.esb.ESBSecurityLevel;
import server.Generator;
import server.dao.gen.MybatisGenerator;

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
    private List<MybatisGenerator.Table> tables;
    public final Class exceptionClass;
    public final String groupName;
    public final ESBSecurityLevel security;
    public final String transactionManager;

    /**
     *
     * @param packageName     项目包名【必填】
     * @param sqlsSourcePath  资源路径【必填】
     * @param transactionManager  事务管理【必填】
     * @param exceptionClass  异常类地址【必填】
     */
    public ServiceGenerator(String packageName, String sqlsSourcePath, String transactionManager, Class exceptionClass) {
        this(packageName,sqlsSourcePath,transactionManager,exceptionClass,null,ESBSecurityLevel.userAuth);
    }

    /**
     *
     * @param packageName     项目包名【必填】
     * @param sqlsSourcePath  资源路径【必填】
     * @param transactionManager  事务管理【必填】
     * @param exceptionClass  异常类地址【必填】
     * @param tablePrefix  table命名前缀【可选】
     */
    public ServiceGenerator(String packageName, String sqlsSourcePath, String transactionManager, Class exceptionClass,String tablePrefix) {
        this(packageName,sqlsSourcePath,transactionManager,exceptionClass,tablePrefix,ESBSecurityLevel.userAuth);
    }

    /**
     *
     * @param packageName     项目包名【必填】
     * @param sqlsSourcePath  资源路径【必填】
     * @param transactionManager  事务管理【必填】
     * @param exceptionClass  异常类地址【必填】
     * @param tablePrefix  table命名前缀【可选】
     * @param security 接口的验权等级，仅仅支持idl API时有用
     */
    public ServiceGenerator(String packageName, String sqlsSourcePath, String transactionManager, Class exceptionClass, String tablePrefix, ESBSecurityLevel security) {
        this(packageName,sqlsSourcePath,transactionManager,exceptionClass,tablePrefix,null,security);
    }

    /**
     *
     * @param packageName     项目包名【必填】
     * @param sqlsSourcePath  资源路径【必填】
     * @param exceptionClass  异常类地址【必填】
     * @param tablePrefix      table命名前缀【可选】
     * @param projectDir      工程目录【可选】
     * @param security 接口的验权等级，仅仅支持idl API时有用
     */
    public ServiceGenerator(String packageName, String sqlsSourcePath, String transactionManager, Class exceptionClass, String tablePrefix, String projectDir, ESBSecurityLevel security) {
        super(packageName, projectDir);

        // doa包名处理
        String doaPackage = packageName;
        if (packageName.endsWith("service")) {
            doaPackage = packageName.substring(0,packageName.length() - "service".length()) + "db";
        } else {//直接放到其子目录
            doaPackage = packageName + File.separator + "db";
        }
        this.mybatisGenerator = new MybatisGenerator(doaPackage, projectDir, sqlsSourcePath, tablePrefix);
        this.tables = mybatisGenerator.getTables();
        this.exceptionClass = exceptionClass;
        String[] strs = sqlsSourcePath.split("/");
        String fileName = strs[strs.length - 1];
        int idx = fileName.lastIndexOf(".");
        this.groupName = fileName.substring(0,idx);
        this.security = security;
        this.transactionManager = transactionManager;
    }

    @Override
    public boolean gen() {

        //先生成DOA
        if (!mybatisGenerator.gen()) {
            return false;
        }

        // 构建目录
        String entitiesPath = this.packagePath + File.separator + "entities";
        new File(entitiesPath).mkdirs();
        String crudImplPath = this.packagePath + File.separator + "impl";
        new File(crudImplPath).mkdirs();


        //生成基类
        for (MybatisGenerator.Table table : tables) {
            //产生实体类
            File pojoFile = new File(entitiesPath + File.separator + table.getSimplePOJOClassName() + ".java");
            writeEntity(pojoFile,packageName,table);

            //产生实体类集合
            File pojosFile = new File(entitiesPath + File.separator + table.getSimplePOJOResultsClassName() + ".java");
            writeEntityResults(pojosFile,packageName,table);

            //产生服务申明类
            File serviceFile = new File(this.packagePath + File.separator + table.getSimpleCRUDServiceBeanName() + ".java");
            writeCRUDService(serviceFile,packageName,mybatisGenerator.packageName,groupName,exceptionClass,security,table);

            //生成服务实现类
            File serviceImplFile = new File(crudImplPath + File.separator + table.getSimpleCRUDServiceImplementationName() + ".java");
            writeServiceImpl(serviceImplFile,packageName,mybatisGenerator.packageName,mybatisGenerator.sqlsSourcePath,groupName,transactionManager,exceptionClass,security,table);
        }

        return true;
    }

    private static void writeEntity(File file, String packageName, MybatisGenerator.Table table) {
        StringBuilder pojoContent = new StringBuilder();
        pojoContent.append("package " + packageName + ".entities;\n\r\n\r");
        pojoContent.append("import com.lmj.stone.idl.annotation.IDLDesc;\n");
        pojoContent.append("import java.io.Serializable;\n\r\n\r");
        pojoContent.append("/**\n");
        pojoContent.append(" * Owner: Minjun Ling\n");
        pojoContent.append(" * Creator: ESB ServiceGenerator\n");
        pojoContent.append(" * Version: 1.0.0\n");
        pojoContent.append(" * GitHub: https://github.com/lingminjun/esb\n");
        pojoContent.append(" * Since: " + new Date() + "\n");
        pojoContent.append(" * Table: " + table.getName() + "\n");
        pojoContent.append(" */\n");
        pojoContent.append("@IDLDesc(\"" + table.getName() + "对象生成\")\n");
        pojoContent.append("public final class " + table.getSimplePOJOClassName() + " implements Serializable {\n");
        pojoContent.append("    private static final long serialVersionUID = 1L;\n");

        for (MybatisGenerator.Column cl : table.getColumns()) {
            if (cl.getName().equals("is_delete") || cl.getName().equals("delete")) {
                continue;
            }
            if (MybatisGenerator.MYSQL_LONG_TYPE.contains(cl.getType())) {
                pojoContent.append("    @IDLDesc(\"" + cl.getCmmt() + "\")\n");
                pojoContent.append("    public long    ");
            } else if (MybatisGenerator.MYSQL_BOOL_TYPE.contains(cl.getType())) {
                pojoContent.append("    @IDLDesc(\"" + cl.getCmmt() + "\")\n");
                pojoContent.append("    public boolean ");
            } else if (MybatisGenerator.MYSQL_DOUBLE_TYPE.contains(cl.getType())) {
                pojoContent.append("    @IDLDesc(\"" + cl.getCmmt() + "\")\n");
                pojoContent.append("    public double  ");
            } else if (MybatisGenerator.MYSQL_INT_TYPE.contains(cl.getType())) {
                pojoContent.append("    @IDLDesc(\"" + cl.getCmmt() + "\")\n");
                pojoContent.append("    public int     ");
            } else if (MybatisGenerator.MYSQL_STRING_TYPE.contains(cl.getType())) {
                pojoContent.append("    @IDLDesc(\"" + cl.getCmmt() + "\")\n");
                pojoContent.append("    public String  ");
            } else {
                continue;
            }
            pojoContent.append(toHumpString(cl.getName(),false) + ";\n");
        }

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
        resultContent.append("import com.lmj.stone.idl.annotation.IDLDesc;\n");
        resultContent.append("import com.lmj.stone.service.PageResults;\n\r\n\r");
        resultContent.append("/**\n");
        resultContent.append(" * Owner: Minjun Ling\n");
        resultContent.append(" * Creator: ESB ServiceGenerator\n");
        resultContent.append(" * Version: 1.0.0\n");
        resultContent.append(" * GitHub: https://github.com/lingminjun/esb\n");
        resultContent.append(" * Since: " + new Date() + "\n");
        resultContent.append(" * Description: " + table.getSimplePOJOClassName() + "结果集\n");
        resultContent.append(" */\n");
        resultContent.append("@IDLDesc(\"" + table.getSimplePOJOClassName() + "结果集\")\n");
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
        serviceContent.append("package " + packageName + ";\n\r\n\r");
        serviceContent.append("import " + exceptionClass.getName() + ";\n");
        serviceContent.append("import com.lmj.stone.idl.ESBSecurityLevel;\n");
        serviceContent.append("import com.lmj.stone.idl.IDLException;\n");
        serviceContent.append("import com.lmj.stone.idl.annotation.IDLAPI;\n");
        serviceContent.append("import com.lmj.stone.idl.annotation.IDLError;\n");
        serviceContent.append("import com.lmj.stone.idl.annotation.IDLGroup;\n");
        serviceContent.append("import com.lmj.stone.idl.annotation.IDLParam;\n");
        serviceContent.append("import java.util.List;\n");
//        serviceContent.append("import org.springframework.beans.factory.annotation.Autowired;\n");
        serviceContent.append("import " + table.getDAOClassName(doaPackagaeName) + ";\n");
//        serviceContent.append("import " + table.getDObjectClassName(doaPackagaeName) + ";\n");
        serviceContent.append("import " + table.getPOJOClassName(packageName) + ";\n");
        serviceContent.append("import " + table.getPOJOResultsClassName(packageName) + ";\n\r\n\r");
        serviceContent.append("/**\n");
        serviceContent.append(" * Owner: Minjun Ling\n");
        serviceContent.append(" * Creator: ESB ServiceGenerator\n");
        serviceContent.append(" * Version: 1.0.0\n");
        serviceContent.append(" * GitHub: https://github.com/lingminjun/esb\n");
        serviceContent.append(" * Since: " + new Date() + "\n");
        serviceContent.append(" * Table: " + table.getName() + "\n");
        serviceContent.append(" */\n");

        String tableModelName = toHumpString(table.getAlias(),true);

        serviceContent.append("@IDLGroup(domain = \"" + groupName + "\", desc = \"" + tableModelName + "的相关操作\", codeDefine = " + exceptionClass.getSimpleName() + ".class)\n");
        serviceContent.append("public interface " + table.getSimpleCRUDServiceBeanName() + " {\n\n");

        //获取doa接口
        writeGetDaoBean(serviceContent,table,false);

        String pojoName = table.getSimplePOJOClassName();
        //所有基本的增删修查

        //增加单个
        writeCreateMethod(tableModelName,groupName,pojoName,theSecurity,serviceContent,table,false);

        //批量增加
        writeBatchCreateMethod(tableModelName,groupName,pojoName,theSecurity,serviceContent,table,false);

        //删，单个删除
        writeDeleteMethod(tableModelName,groupName,pojoName,theSecurity,serviceContent,table,false);

        //更新某个数据，拆开每个字段
        writeUpdateMethod(tableModelName,groupName,pojoName,theSecurity,serviceContent,table,false);

        //主键查询
        writeFindByIdMethod(tableModelName, groupName, pojoName, theSecurity, serviceContent, table, false);

        //查询，索引查询，翻页
        Map<String,List<MybatisGenerator.Column>> queryMethods = table.allIndexQueryMethod();
        List<String> methodNames = new ArrayList<String>(queryMethods.keySet());
        Collections.sort(methodNames);
        for (String methodName : methodNames) {
            List<MybatisGenerator.Column> cols = queryMethods.get(methodName);

            writeQueryMethod(tableModelName,groupName,pojoName,methodName,cols,theSecurity,serviceContent,table,false);
        }

        serviceContent.append("}\n\r\n\r");

        try {
            writeFile(file,serviceContent.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeGetDaoBean(StringBuilder serviceContent, MybatisGenerator.Table table, boolean implement) {
        serviceContent.append("    /**\n");
        serviceContent.append("     * " + table.getSimpleDAOClassName() + "\n");
        serviceContent.append("     * @return \n");
        serviceContent.append("     */\n");
        if (implement) {
            serviceContent.append("    @Override\n");
        }
        serviceContent.append("    public " + table.getSimpleDAOClassName() + " get" + table.getSimpleDAOClassName() + "(");

        if (!implement) {
            serviceContent.append(");\n\n");
            return;
        } else {
            serviceContent.append(") {\n");
        }

        //实现代码
        String doa = toLowerHeadString(table.getSimpleDAOClassName());
        serviceContent.append("        return this." + doa + ";\n");

        serviceContent.append("    }\n\n");
    }

    private static void writeCreateMethod(String tableModelName, String groupName, String pojoName, String theSecurity, StringBuilder serviceContent, MybatisGenerator.Table table, boolean implement) {
        String param = toLowerHeadString(tableModelName);

        serviceContent.append("    /**\n");
        serviceContent.append("     * insert " + pojoName + "\n");
        serviceContent.append("     * @return \n");
        serviceContent.append("     */\n");
        String addMethod = "add" + tableModelName;
        if (!implement) {
            serviceContent.append("    @IDLAPI(module = \"" + groupName + "\",name = \"" + addMethod + "\", desc = \"插入" + pojoName + "\", security = " + theSecurity + ")\n");
        } else {
            serviceContent.append("    @Override\n");
        }

        String defineMethod = "    public long " + addMethod + "(@IDLParam(name = \"" + param + "\", desc = \"实体对象\", required = true) final " + pojoName + " " + param;
        serviceContent.append(defineMethod);

        if (!implement) {
            serviceContent.append(") throws IDLException;\n\n");
            return;
        } else {
            serviceContent.append(") throws IDLException {\n");
        }

        //实现代码
        String theDaoBean = "get" + table.getSimpleDAOClassName() + "()";
        String dataObj = table.getSimpleDObjectClassName();

        serviceContent.append("        return BlockUtil.en(transactionManager, new BlockUtil.Call<Long>() {\n");
        serviceContent.append("            @Override\n");
        serviceContent.append("            public Long run() throws Throwable {\n");
        serviceContent.append("                " + dataObj + " dobj = new " + dataObj + "();\n");
        serviceContent.append("                Injects.fill(" + param + ",dobj);\n");
        serviceContent.append("                if (" + theDaoBean + ".insert(dobj) > 0) {\n");
        serviceContent.append("                    return (Long)dobj.id;\n");
        serviceContent.append("                } else {\n");
        serviceContent.append("                    return -1l;\n");
        serviceContent.append("                }\n");
        serviceContent.append("            }\n");
        serviceContent.append("        });\n");


        serviceContent.append("    }\n\n");
    }


    private static void writeBatchCreateMethod(String tableModelName, String groupName, String pojoName, String theSecurity, StringBuilder serviceContent, MybatisGenerator.Table table, boolean implement) {
        serviceContent.append("    /**\n");
        serviceContent.append("     * batch insert " + pojoName + "\n");
        serviceContent.append("     * @return \n");
        serviceContent.append("     */\n");
        String batchAddMethod = "batchAdd" + tableModelName;
        if (!implement) {
            serviceContent.append("    @IDLAPI(module = \"" + groupName + "\",name = \"" + batchAddMethod + "\", desc = \"批量插入" + pojoName + "\", security = " + theSecurity + ")\n");
        } else {
            serviceContent.append("    @Override\n");
        }

        String defineMethod = "    public boolean " + batchAddMethod + "(@IDLParam(name = \"models\", desc = \"实体对象\", required = true) final List<" + pojoName + "> models,\n";
        String spacing = formatSpaceParam(defineMethod);
        serviceContent.append(defineMethod);
        serviceContent.append(spacing);
        serviceContent.append("@IDLParam(name = \"ignoreError\", desc = \"忽略错误，单个插入，但是效率低；若不忽略错误，批量提交，效率高\", required = true) final boolean ignoreError");

        if (!implement) {
            serviceContent.append(") throws IDLException;\n\n");
            return;
        } else {
            serviceContent.append(") throws IDLException {\n");
        }

        //实现代码
        String theDaoBean = "get" + table.getSimpleDAOClassName() + "()";
        String dataObj = table.getSimpleDObjectClassName();

        serviceContent.append("        if (ignoreError) {\n");
        serviceContent.append("            for (final " + pojoName + "  pojo : models) {\n");
        serviceContent.append("                BlockUtil.en(transactionManager, new BlockUtil.Call<Boolean>() {\n");
        serviceContent.append("                    @Override\n");
        serviceContent.append("                    public Boolean run() throws Throwable {\n");
        serviceContent.append("                        " + dataObj + " dobj = new " + dataObj + "();\n");
        serviceContent.append("                        Injects.fill(pojo,dobj);\n");
        serviceContent.append("                        " + theDaoBean + ".insert(dobj);\n");
        serviceContent.append("                        return true;\n");
        serviceContent.append("                    }\n");
        serviceContent.append("                });\n");
        serviceContent.append("            }\n");
        serviceContent.append("            return true;\n");
        serviceContent.append("        } else {\n");
        serviceContent.append("           return BlockUtil.en(transactionManager, new BlockUtil.Call<Boolean>() {\n");
        serviceContent.append("                @Override\n");
        serviceContent.append("                public Boolean run() throws Throwable {\n");
        serviceContent.append("                    for (" + pojoName + " pojo : models) {\n");
        serviceContent.append("                        " + dataObj + " dobj = new " + dataObj + "();\n");
        serviceContent.append("                        Injects.fill(pojo,dobj);\n");
        serviceContent.append("                        " + theDaoBean + ".insert(dobj);\n");
        serviceContent.append("                    }\n");
        serviceContent.append("                    return true;\n");
        serviceContent.append("                }\n");
        serviceContent.append("            });\n");
        serviceContent.append("        }\n\n");


        serviceContent.append("    }\n\n");
    }

    private static void writeDeleteMethod(String tableModelName, String groupName, String pojoName, String theSecurity, StringBuilder serviceContent, MybatisGenerator.Table table, boolean implement) {
        MybatisGenerator.Column column = table.getDeleteStateColumn();
        if (column == null) {
            return;
        }

        serviceContent.append("    /**\n");
        serviceContent.append("     * remove " + pojoName + "\n");
        serviceContent.append("     * @return \n");
        serviceContent.append("     */\n");
        String removeMethod = "removeThe" + tableModelName;
        if (!implement) {
            serviceContent.append("    @IDLAPI(module = \"" + groupName + "\",name = \"" + removeMethod + "\", desc = \"删除" + pojoName + "\", security = " + theSecurity + ")\n");
        } else {
            serviceContent.append("    @Override\n");
            serviceContent.append("    @AutoCache(key = \"" + table.getAlias().toUpperCase() + "_#{id}\", evict = true)\n");
        }
        serviceContent.append("    public boolean " + removeMethod + "(@IDLParam(name = \"id\", desc = \"对象id\", required = true) final long id");

        if (!implement) {
            serviceContent.append(") throws IDLException;\n\n");
            return;
        } else {
            serviceContent.append(") throws IDLException {\n");
        }

        //实现代码
        String theDaoBean = "get" + table.getSimpleDAOClassName() + "()";

        serviceContent.append("        return BlockUtil.en(transactionManager, new BlockUtil.Call<Boolean>() {\n");
        serviceContent.append("            @Override\n");
        serviceContent.append("            public Boolean run() throws Throwable {\n");
        serviceContent.append("                " + theDaoBean + ".deleteById(id);\n");
        serviceContent.append("                return true;\n");
        serviceContent.append("           }\n");
        serviceContent.append("        });\n");

        serviceContent.append("    }\n\n");

    }

    private static void writeUpdateMethod(String tableModelName, String groupName, String pojoName, String theSecurity, StringBuilder serviceContent, MybatisGenerator.Table table, boolean implement) {

        serviceContent.append("    /**\n");
        serviceContent.append("     * update " + pojoName + "\n");
        serviceContent.append("     * @return \n");
        serviceContent.append("     */\n");
        String updateMethod = "updateThe" + tableModelName;
        if (!implement) {
            serviceContent.append("    @IDLAPI(module = \"" + groupName + "\",name = \"" + updateMethod + "\", desc = \"更新" + pojoName + "，仅更新不为空的字段\", security = " + theSecurity + ")\n");
        } else {
            serviceContent.append("    @Override\n");
            serviceContent.append("    @AutoCache(key = \"" + table.getAlias().toUpperCase() + "_#{id}\", evict = true)\n");
        }

        String defineMethod = "    public boolean " + updateMethod + "(@IDLParam(name = \"id\", desc = \"更新对象的id\", required = true) final long id";
        String spacing = formatSpaceParam(defineMethod);

        serviceContent.append(defineMethod);//首段写入
        List<MybatisGenerator.Column> params = new ArrayList<MybatisGenerator.Column>();
        for (MybatisGenerator.Column cl : table.getColumns()) {
            //忽略字段
            if (cl.getName().equals("is_delete")
                    || cl.getName().equals("delete")
                    || cl.getName().equals("id")
                    || cl.getName().equals("create_at")
                    || cl.getName().equals("modified_at")
                    ) {
                continue;
            }

            String paramName = toHumpString(cl.getName(),false);
            params.add(cl);
            serviceContent.append(",\n");
            serviceContent.append(spacing);
            serviceContent.append("@IDLParam(name = \"" + paramName + "\", desc = \"" + cl.getCmmt() + "\", required = false) final " + cl.getDefinedType() + " " + paramName);
        }
        if (!implement) {
            serviceContent.append(") throws IDLException;\n\n");
            return;
        } else {
            serviceContent.append(") throws IDLException {\n");
        }

        //实现代码
        String theDaoBean = "get" + table.getSimpleDAOClassName() + "()";
        String dataObj = table.getSimpleDObjectClassName();

        serviceContent.append("        return BlockUtil.en(transactionManager, new BlockUtil.Call<Boolean>() {\n");
        serviceContent.append("            @Override\n");
        serviceContent.append("            public Boolean run() throws Throwable {\n");
        serviceContent.append("                " + dataObj + " dobj = new " + dataObj + "();\n");
        MybatisGenerator.Column primary = table.getPrimaryColumn();
        if (primary != null) {
            serviceContent.append("                dobj.id = (" + primary.getDataType() + ")id;\n");
        } else {
            serviceContent.append("                dobj.id = id;\n");
        }
        for (MybatisGenerator.Column cl : params) {
            String paramName = toHumpString(cl.getName(),false);
            serviceContent.append("                dobj." + paramName + " = " + paramName + ";\n");
        }
        serviceContent.append("                " + theDaoBean + ".update(dobj);\n");
        serviceContent.append("                return true;\n");
        serviceContent.append("            }\n");
        serviceContent.append("        });\n");

        serviceContent.append("    }\n\n");
    }

    private static void writeFindByIdMethod(String tableModelName, String groupName, String pojoName, String theSecurity, StringBuilder serviceContent, MybatisGenerator.Table table, boolean implement) {

        serviceContent.append("    /**\n");
        serviceContent.append("     * find " + pojoName + " by id\n");
        serviceContent.append("     * @return \n");
        serviceContent.append("     */\n");
        String findMethod = "findThe" + tableModelName;
        if (!implement) {
            serviceContent.append("    @IDLAPI(module = \"" + groupName + "\",name = \"" + findMethod + "\", desc = \"寻找" + pojoName + "\", security = " + theSecurity + ")\n");
        } else {
            serviceContent.append("    @Override\n");
            serviceContent.append("    @AutoCache(key = \"" + table.getAlias().toUpperCase() + "_#{id}\", async = true, condition=\"!#{noCache}\")\n");
        }

        serviceContent.append("    public " + pojoName + " " + findMethod + "(@IDLParam(name = \"id\", desc = \"对象id\", required = true) final long id");
        serviceContent.append(",@IDLParam(name = \"noCache\", desc = \"不走缓存\", required = false) final boolean noCache");

        if (!implement) {
            serviceContent.append(") throws IDLException;\n\n");
            return;
        } else {
            serviceContent.append(") throws IDLException {\n");
        }

        //实现代码
        String theDaoBean = "get" + table.getSimpleDAOClassName() + "()";
        String dataObj = table.getSimpleDObjectClassName();

        serviceContent.append("        " + dataObj + " dobj = " + theDaoBean + ".getById(id);\n");
        serviceContent.append("        " + pojoName + " pojo = new " + pojoName + "();\n");
        serviceContent.append("        Injects.fill(dobj,pojo);\n");
        serviceContent.append("        return pojo;\n");

        serviceContent.append("    }\n\n");

    }

    private static void writeQueryMethod(String tableModelName, String groupName, String pojoName, String queryMethodName, List<MybatisGenerator.Column> columns, String theSecurity, StringBuilder serviceContent, MybatisGenerator.Table table, boolean implement) {
        String methodName = "query" + tableModelName + queryMethodName.substring(5,queryMethodName.length());//.replace("query", "query" + tableModelName);

        String defineMethod = "    public " + table.getSimplePOJOResultsClassName() + " " + methodName + "(@IDLParam(name = \"pageIndex\", desc = \"页索引，从1开始，传入0或负数无数据返回\", required = true) final int pageIndex";
        String spacing = formatSpaceParam(defineMethod);

        //提前处理参数
        StringBuilder methodParams = new StringBuilder();
        StringBuilder methodParamsDef = new StringBuilder();
        StringBuilder cacheKeyDef = new StringBuilder();
        for (MybatisGenerator.Column column : columns) {

            String param = toHumpString(column.getName(),false);
            if (methodParams.length() > 0) {
                methodParams.append(",");
            }
            methodParams.append(param);

            methodParamsDef.append(",\n");
            methodParamsDef.append(spacing);
            methodParamsDef.append("@IDLParam(name = \"" + param + "\", desc = \"" + column.getCmmt() + "\", required = true) final " + column.getDataType() + " " + param);

            if (cacheKeyDef.length() > 0) {
                cacheKeyDef.append("_");
            }
            cacheKeyDef.append(column.getName().toUpperCase());
            cacheKeyDef.append(":#{");
            cacheKeyDef.append(param);
            cacheKeyDef.append("}");
        }

        //添加分页信息
        cacheKeyDef.append("_PAGE:#{pageIndex},#{pageSize}");

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
                methodParamsDef.append("@IDLParam(name = \"isDeleted\", desc = \"是否已经被标记删除的\", required = false) final boolean isDeleted");

                cacheKeyDef.append("_DEL:#{isDeleted}");

                if (theDelete.getDataType().equals("boolean")) {
                    delParamIn = "isDeleted";
                } else {
                    delParamIn = "(isDeleted ? 1 : 0)";
                }
            }
        }

        //开始编写函数代码
        serviceContent.append("    /**\n");
        serviceContent.append("     * query " + pojoName + "\n");
        serviceContent.append("     * @return \n");
        serviceContent.append("     */\n");
        if (!implement) {
            serviceContent.append("    @IDLAPI(module = \"" + groupName + "\",name = \"" + methodName + "\", desc = \"批量插入" + pojoName + "\", security = " + theSecurity + ")\n");
        } else {
            serviceContent.append("    @Override\n");
            if (hasDeleted) {
                serviceContent.append("    @AutoCache(key = \"" + table.getAlias().toUpperCase() + "_QUERY_BY_" + cacheKeyDef.toString() + "\", async = true, condition=\"!#{noCache} && !#{isDeleted}\")\n");
            } else {
                serviceContent.append("    @AutoCache(key = \"" + table.getAlias().toUpperCase() + "_QUERY_BY_" + cacheKeyDef.toString() + "\", async = true, condition=\"!#{noCache}\")\n");
            }
        }



        serviceContent.append(defineMethod);//首段写入
        serviceContent.append(",\n");
        serviceContent.append(spacing);
        serviceContent.append("@IDLParam(name = \"pageSize\", desc = \"一页最大行数\", required = true) final int pageSize");

        //定义所有参数
        serviceContent.append(methodParamsDef.toString());

        //是否走缓存
        serviceContent.append(",\n");
        serviceContent.append(spacing);
        serviceContent.append("@IDLParam(name = \"noCache\", desc = \"不走缓存\", required = false) final boolean noCache");


        if (!implement) {
            serviceContent.append(") throws IDLException;\n\n");
            return;
        } else {
            serviceContent.append(") throws IDLException {\n");
        }

        //实现代码
        String theDaoBean = "get" + table.getSimpleDAOClassName() + "()";
        String dataObj = table.getSimpleDObjectClassName();
        String resultsName = table.getSimplePOJOResultsClassName();
        String countMethodName = "count" + queryMethodName.substring(5,queryMethodName.length());
        String methodParamsString = methodParams.toString();


        serviceContent.append("        if (pageIndex <= 0 || pageSize <= 0) {\n");
        serviceContent.append("            throw new IDLException(\"参数错误\",\"" + groupName + "\",-1,\"翻页参数传入错误\");\n");
        serviceContent.append("        }\n");
        serviceContent.append("        " + resultsName + " rlt = new " + resultsName + "();\n");
        serviceContent.append("        rlt.index = pageIndex;\n");
        serviceContent.append("        rlt.size = pageSize;\n");
        serviceContent.append("        rlt.total = " + theDaoBean + "." + countMethodName + "(");
        serviceContent.append(methodParamsString);
        if (hasDeleted) {
            serviceContent.append(",");
            serviceContent.append(delParamIn);
        }
        serviceContent.append(");\n");
        serviceContent.append("        List<" + dataObj + "> list = " + theDaoBean + "." + queryMethodName + "(");
        serviceContent.append(methodParamsString);
        if (hasDeleted) {
            serviceContent.append(",");
            serviceContent.append(delParamIn);
        }
        serviceContent.append(",null,false,(pageSize * (pageIndex - 1)), pageSize);\n");
        serviceContent.append("        rlt.results = new ArrayList<" + pojoName + ">();\n");
        serviceContent.append("        for (" + dataObj + " dobj : list) {\n");
        serviceContent.append("            " + pojoName + " pojo = new " + pojoName + "();\n");
        serviceContent.append("            Injects.fill(dobj,pojo);\n");
        serviceContent.append("            rlt.results.add(pojo);\n");
        serviceContent.append("        }\n");
        serviceContent.append("       return rlt;\n");

        serviceContent.append("    }\n\n");
    }

    private static void writeServiceImpl(File file, String packageName,String doaPackagaeName, String sqlsSourcePath, String groupName, String transactionManager, Class exceptionClass, ESBSecurityLevel security, MybatisGenerator.Table table) {
        String theSecurity = "ESBSecurityLevel." + security.toString();

        StringBuilder serviceContent = new StringBuilder();
        serviceContent.append("package " + packageName + ".impl;\n\r\n\r");
        serviceContent.append("import com.lmj.stone.cache.AutoCache;\n");
        serviceContent.append("import com.lmj.stone.service.Injects;\n");
        serviceContent.append("import com.lmj.stone.service.BlockUtil;\n");
        serviceContent.append("import org.springframework.jdbc.datasource.DataSourceTransactionManager;\n");
        serviceContent.append("import " + exceptionClass.getName() + ";\n");
        serviceContent.append("import com.lmj.stone.idl.ESBSecurityLevel;\n");
        serviceContent.append("import com.lmj.stone.idl.IDLException;\n");
        serviceContent.append("import com.lmj.stone.idl.annotation.IDLError;\n");
        serviceContent.append("import com.lmj.stone.idl.annotation.IDLParam;\n");
        serviceContent.append("import java.util.ArrayList;\n");
        serviceContent.append("import java.util.List;\n");
        serviceContent.append("import org.springframework.beans.factory.annotation.Autowired;\n");
        serviceContent.append("import org.springframework.stereotype.Service;\n");
        serviceContent.append("import " + table.getDAOClassName(doaPackagaeName) + ";\n");
        serviceContent.append("import " + table.getDObjectClassName(doaPackagaeName) + ";\n");
        serviceContent.append("import " + table.getPOJOClassName(packageName) + ";\n");
        serviceContent.append("import " + table.getPOJOResultsClassName(packageName) + ";\n");
        serviceContent.append("import " + table.getCRUDServiceBeanName(packageName) + ";\n");
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

        serviceContent.append("@Service\n");
        serviceContent.append("public class " + table.getSimpleCRUDServiceImplementationName() + " implements " + table.getSimpleCRUDServiceBeanName() + " {\n\n");

        serviceContent.append("    @Resource(name = \"" + transactionManager + "\")\n");
        serviceContent.append("    protected DataSourceTransactionManager transactionManager;\n\n");

        // 定义DAO属性
        serviceContent.append("    @Autowired\n");
        String doaClassName = table.getSimpleDAOClassName();
        String doaPropertyName = toLowerHeadString(doaClassName);
        serviceContent.append("    private ");
        serviceContent.append(doaClassName);
        serviceContent.append(" ");
        serviceContent.append(doaPropertyName);
        serviceContent.append(";\n\n");

        // 实现下面一系列方法
        String tableModelName = toHumpString(table.getAlias(), true);

        //获取doa接口
        writeGetDaoBean(serviceContent, table, true);

        String pojoName = table.getSimplePOJOClassName();
        //所有基本的增删修查

        //增加单个
        writeCreateMethod(tableModelName, groupName, pojoName, theSecurity, serviceContent, table, true);

        //批量增加
        writeBatchCreateMethod(tableModelName, groupName, pojoName, theSecurity, serviceContent, table, true);

        //删，单个删除
        writeDeleteMethod(tableModelName, groupName, pojoName, theSecurity, serviceContent, table, true);

        //更新某个数据，拆开每个字段
        writeUpdateMethod(tableModelName, groupName, pojoName, theSecurity, serviceContent, table, true);

        //主键查询
        writeFindByIdMethod(tableModelName, groupName, pojoName, theSecurity, serviceContent, table, true);

        //查询，索引查询，翻页
        Map<String, List<MybatisGenerator.Column>> queryMethods = table.allIndexQueryMethod();
        List<String> methodNames = new ArrayList<String>(queryMethods.keySet());
        Collections.sort(methodNames);
        for (String methodName : methodNames) {
            List<MybatisGenerator.Column> cols = queryMethods.get(methodName);

            writeQueryMethod(tableModelName, groupName, pojoName, methodName, cols, theSecurity, serviceContent, table, true);
        }


        serviceContent.append("}\n\r\n\r");

        try {
            writeFile(file,serviceContent.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
