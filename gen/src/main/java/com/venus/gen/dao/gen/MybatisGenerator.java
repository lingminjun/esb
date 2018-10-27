package com.venus.gen.dao.gen;

import com.venus.esb.lang.ESBConsts;
import com.venus.esb.utils.FileUtils;
import com.venus.gen.SpringXMLConst;
import com.venus.gen.dao.SQL;
import com.venus.gen.dao.TableDAO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.venus.gen.Generator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MybatisGenerator extends Generator {

    public static final String SQLMAP_CONFIG_NAME = "mybatis-sqlmap-config.xml";
    public static final String SPRING_BEAN_XML_NAME = "application-persistence.xml";

    public final static HashSet<String> MYSQL_LONG_TYPE = new HashSet<String>();
    static {
        MYSQL_LONG_TYPE.add("BIGINT");
    }

    public final static HashSet<String> MYSQL_BOOL_TYPE = new HashSet<String>();
    static {
        MYSQL_BOOL_TYPE.add("BOOL");
    }

    public final static HashSet<String> MYSQL_DOUBLE_TYPE = new HashSet<String>();
    static {
        MYSQL_DOUBLE_TYPE.add("FLOAT");
        MYSQL_DOUBLE_TYPE.add("DOUBLE");
        MYSQL_DOUBLE_TYPE.add("REAL");

        //另外使用java.math.BigDecimal存储
        MYSQL_DOUBLE_TYPE.add("DECIMAL");
        MYSQL_DOUBLE_TYPE.add("DEC");
        MYSQL_DOUBLE_TYPE.add("NUMERIC");
    }

    public final static HashSet<String> MYSQL_INT_TYPE = new HashSet<String>();
    static {
        MYSQL_INT_TYPE.add("TINYINT");
        MYSQL_INT_TYPE.add("BIT");
        MYSQL_INT_TYPE.add("SMALLINT");
        MYSQL_INT_TYPE.add("INT");
        MYSQL_INT_TYPE.add("INTEGER");
    }

    public final static HashSet<String> MYSQL_STRING_TYPE = new HashSet<String>();
    static {
        MYSQL_STRING_TYPE.add("CHAR");
        MYSQL_STRING_TYPE.add("VARCHAR");
        MYSQL_STRING_TYPE.add("TINYBLOB");
        MYSQL_STRING_TYPE.add("TINYTEXT");
        MYSQL_STRING_TYPE.add("BLOB");
        MYSQL_STRING_TYPE.add("TEXT");
        MYSQL_STRING_TYPE.add("MEDIUMBLOB");
        MYSQL_STRING_TYPE.add("MEDIUMTEXT");
        MYSQL_STRING_TYPE.add("LONGBLOB");
        MYSQL_STRING_TYPE.add("LONGTEXT");
        MYSQL_STRING_TYPE.add("ENUM");
        MYSQL_STRING_TYPE.add("SET");
    }

    public final static HashSet<String> MYSQL_DATE_TYPE = new HashSet<String>();
    static {
        MYSQL_DATE_TYPE.add("DATETIME");
        MYSQL_DATE_TYPE.add("DATE");
        MYSQL_DATE_TYPE.add("TIMESTAMP");
        MYSQL_DATE_TYPE.add("TIME");
        MYSQL_DATE_TYPE.add("YEAR");
    }

    public final static HashSet<String> MYSQL_INDEX_TYPE = new HashSet<String>();
    static {
        MYSQL_INDEX_TYPE.add("PRIMARY");
        MYSQL_INDEX_TYPE.add("UNIQUE");
        MYSQL_INDEX_TYPE.add("INDEX");
        MYSQL_INDEX_TYPE.add("KEY");
    }


    public static class Column {
        String name;
        String type;
        String cmmt;

        String defaultValue; //默认值为NULL注意
        boolean notNull; //虽然是两个含义，此处简单处理，只要有default时就默认可以传入空，毕竟业务接口上只能以空来做判断

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getCmmt() {
            return cmmt;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public boolean isNotNull() {
//            return defaultValue == null;//此处简单处理,只要有default时就默认可以传入空，毕竟业务接口上只能以空来做判断
            return notNull;
        }

        public String getDefinedType() {
            if (MYSQL_LONG_TYPE.contains(type)) {
                return "Long";
            } else if (MYSQL_BOOL_TYPE.contains(type)) {
                return "Boolean";
            } else if (MYSQL_DOUBLE_TYPE.contains(type)) {
                return "Double";
            } else if (MYSQL_INT_TYPE.contains(type)) {
                return "Integer";
            } else if (MYSQL_STRING_TYPE.contains(type)) {
                return "String";
            } else if (MYSQL_DATE_TYPE.contains(type)) {
                return "Date";
            } else {
                return "";
            }
        }

        public String getDataType() {
            if (MYSQL_LONG_TYPE.contains(type)) {
                return "long";
            } else if (MYSQL_BOOL_TYPE.contains(type)) {
                return "boolean";
            } else if (MYSQL_DOUBLE_TYPE.contains(type)) {
                return "double";
            } else if (MYSQL_INT_TYPE.contains(type)) {
                return "int";
            } else if (MYSQL_STRING_TYPE.contains(type)) {
                return "String";
            } else if (MYSQL_DATE_TYPE.contains(type)) {
                return "Date";
            } else {
                return "";
            }
        }
    }

    public static class ColumnIndex {
        String name;
        boolean isPrimary;
        boolean isUnique;
        List<Column> columns = new ArrayList<Column>();

        public String getName() {
            return name;
        }

        public boolean isPrimary() {
            return isPrimary;
        }

        public boolean isUnique() {
            return isUnique;
        }

        public Column[] getColumns() {
            return columns.toArray(new Column[0]);
        }

//        public String getQueryMethodName() {
//            StringBuilder queryMethodName = new StringBuilder("queryBy");
//            boolean first = true;
//            for (Column col : columns) {
//                if (first) {first = false;}
//                else {queryMethodName.append("And");}
//                queryMethodName.append(toHumpString(col.name,true));
//            }
//            return queryMethodName.toString();
//        }
    }

    private static class MapperInfo {
        String daoClassName; //全称
        String daoSimpleClassName; //全称
        String daoPath;
        String mapperFileName;
        String mapperFilePath;
    }

    public static class Table {
        String name;
        String alias;
        List<Column> columns = new ArrayList<Column>();
        List<ColumnIndex> indexs = new ArrayList<ColumnIndex>();

        public String getName() {
            return name;
        }

        public String getAlias() {
            return alias;
        }

        public Column[] getColumns() {
            return columns.toArray(new Column[0]);
        }

        public Column[] getIndexs() {
            return columns.toArray(new Column[0]);
        }

        public Column getPrimaryColumn() {
            for (ColumnIndex column : indexs) {
                if (column.isPrimary) { return column.columns.get(0); }
            }
            return null;
        }

        //除了主键以外的索引
        public boolean hasIndexQuery() {
            for (ColumnIndex column : indexs) {
                if (!column.isPrimary) { return true; }
            }
            return false;
        }

        public Map<String,List<Column>> allIndexQueryMethod() {

            HashMap<String,List<Column>> methods = new HashMap<String, List<Column>>();
            for (ColumnIndex column : indexs) {
                if (column.isPrimary) { continue; }

                buildMethods(column.columns,"queryBy",methods);
            }

            return methods;
        }

        public Map<String,List<Column>> allIndexCountMethod() {

            HashMap<String,List<Column>> methods = new HashMap<String, List<Column>>();
            for (ColumnIndex column : indexs) {
                if (column.isPrimary) { continue; }

                buildMethods(column.columns,"countBy",methods);
            }

            return methods;
        }

        private static void buildMethods(List<Column> columns, String methodHead, HashMap<String,List<Column>> methods) {
            for (int i = 0; i < columns.size(); i++) {

                StringBuilder queryMethodName = new StringBuilder(methodHead);
                boolean first = true;
                List<Column> cols = new ArrayList<Column>();
                for (int j = 0; j <= i; j++) {
                    Column col = columns.get(j);
                    cols.add(col);
                    if (first) {first = false;}
                    else {queryMethodName.append("And");}
                    queryMethodName.append(toHumpString(col.name,true));
                }

                String methodName = queryMethodName.toString();
                if (!methods.containsKey(methodName)) {
                    methods.put(methodName,cols);
                }
            }
        }

        public Column getDeleteStateColumn() {
            for (Column col : columns) {
                if (col.name.equals("is_delete") || col.name.equals("delete")) {
                    return col;
                }
            }
            return null;
        }

        public static boolean hasDeleteStateColumn(List<Column> columns) {
            for (Column col : columns) {
                if (col.name.equals("is_delete") || col.name.equals("delete")) {
                    return true;
                }
            }
            return false;
        }

        public String getDAOClassName(String packageName) {
            return packageName + ".dao." + getSimpleDAOClassName();
        }

        public String getSimpleDAOClassName() {
            return toHumpString(alias,true) + "DAO";
        }

        public String getIncDAOClassName(String packageName) {
            return packageName + ".dao.inc." + getSimpleIncDAOClassName();
        }

        public String getSimpleIncDAOClassName() {
            return toHumpString(alias,true) + "IndexQueryDAO";
        }

        public String getDObjectClassName(String packageName) {
            return packageName + ".vo." + getSimpleDObjectClassName();
        }

        public String getSimpleDObjectClassName() {
            return toHumpString(alias,true) + "DO";
        }

        public String getPOJOClassName(String packageName) {
            return packageName + ".entities." + getSimplePOJOClassName();
        }

        public String getSimplePOJOClassName() {
            return toHumpString(alias,true) + "POJO";
        }

        public String getPOJOResultsClassName(String packageName) {
            return packageName + ".entities." + getSimplePOJOResultsClassName();
        }

        public String getSimplePOJOResultsClassName() {
            return toHumpString(alias,true) + "Results";
        }

        public String getCRUDServiceBeanName(String packageName) {
            return packageName + ".api." + getSimpleCRUDServiceBeanName();
        }

        public String getSimpleCRUDServiceBeanName() {
            return toHumpString(alias,true) + "CRUDService";
        }

        public String getSimpleCRUDServiceImplementationName() {
            return toHumpString(alias,true) + "CRUDServiceBean";
        }

        public String getCRUDServiceImplementationName(String packageName) {
            return packageName + ".service." + getSimpleCRUDServiceImplementationName();
        }

        public String getSimpleRestControllerName() {
            return toHumpString(alias,true) + "RestController";
        }

        public String getRestControllerName(String packageName) {
            return packageName + "." + getSimpleRestControllerName();
        }



        private static String getSqlWhereFragment(List<Column> tcols, Table table) {
            StringBuilder queryWhere = new StringBuilder();
            boolean first = true;
            for (Column cl : tcols) {
                if (first) {
                    first = false;
                } else {
                    queryWhere.append(" and ");
                }
                queryWhere.append("`"+ cl.name +"` = #{"+ toHumpString(cl.name,false) + "}");
            }
            //对is_delete字段处理
            if (table != null) {
                Column theDelete = table.getDeleteStateColumn();
                if (!Table.hasDeleteStateColumn(tcols) && theDelete != null) {
                    if (first) {
                        first = false;
                    } else {
                        queryWhere.append(" and ");
                    }
                    queryWhere.append("`" + theDelete.name + "` = #{" + toHumpString(theDelete.name, false) + "}");
                }
            }

            queryWhere.append("\n");

            return queryWhere.toString();
        }


    }


    public final String sqlsSourcePath;
    public final String mapperPath;
    public final String tablePrefix;
    protected final List<Table> tables;

    protected boolean genXmlConfig = true;//生成sqlmap配置

    /**
     * 生成DAO层代码
     * @param packageName 指定包名【必填】
     * @param sqlsSourcePath    sqls文件资源路径:sqls/xxx.sqls【必填】
     */
    public MybatisGenerator(String packageName, String sqlsSourcePath) {
        this(packageName,null,sqlsSourcePath,null,null);
    }

    /**
     * 生成DAO层代码
     * @param packageName 指定包名【必填】
     * @param sqlsSourcePath    sqls文件资源路径:sqls/xxx.sqls【必填】
     * @param tablePrefix  表定义前缀,只有匹配前缀有效时起作用
     */
    public MybatisGenerator(String packageName, String sqlsSourcePath, String tablePrefix) {
        this(packageName,null,sqlsSourcePath,tablePrefix,null);
    }

    /**
     * 生成DAO层代码
     * @param packageName 指定包名【必填】
     * @param projectDir  项目目录，可以不填
     * @param sqlsSourcePath    sqls文件资源路径:sqls/xxx.sqls【必填】
     * @param tablePrefix  表定义前缀,只有匹配前缀有效时起作用
     */
    public MybatisGenerator(String packageName, String projectDir,  String sqlsSourcePath, String tablePrefix) {
        this(packageName,projectDir,sqlsSourcePath,tablePrefix,null);
    }

    /**
     * 生成DAO层代码
     * @param packageName 指定包名【必填】
     * @param projectDir  项目目录，可以不填
     * @param sqlsSourcePath    sqls文件资源路径:sqls/xxx.sqls【必填】
     * @param tablePrefix  表定义前缀,只有匹配前缀有效时起作用
     * @param mapperPath  Mybatis Configuration配置文件路径:资源路径
     */
    public MybatisGenerator(String packageName, String projectDir,  String sqlsSourcePath, String tablePrefix,String mapperPath) {
        super(packageName,projectDir);

        if (mapperPath == null || mapperPath.length() == 0) {
            mapperPath = SQLMAP_CONFIG_NAME;
        }

        this.sqlsSourcePath = sqlsSourcePath;
        this.mapperPath = mapperPath;
        this.tablePrefix = tablePrefix;
        this.tables = parseSqlTables(sqlsSourcePath,tablePrefix);//解析sqls中的tables
    }

    public MybatisGenerator(ProjectModule rootProject, ProjectModule project,  String sqlsSourcePath, String tablePrefix,String mapperPath) {
        super(rootProject,project);
        if (mapperPath == null || mapperPath.length() == 0) {
            mapperPath = SQLMAP_CONFIG_NAME;
        }

        this.sqlsSourcePath = sqlsSourcePath;
        this.mapperPath = mapperPath;
        this.tablePrefix = tablePrefix;
        this.tables = parseSqlTables(sqlsSourcePath,tablePrefix);//解析sqls中的tables
    }

    // 是否自动生成sqlmap-config.xml
    public void setAutoGenXmlConfig(boolean auto) {
        this.genXmlConfig = auto;
    }
    public boolean autoGenXmlConfig() {
        return this.genXmlConfig;
    }

    @Override
    public boolean gen() {

        String dobjDir = null;
        String daoDir = null;
        String mapDir = null;

        //因为考虑有些工程，并不是main/java目录，可能直接就上是java目录[暂时不去兼容]
        mapDir = this.resourcesPath() + File.separator + "sqlmap";
        new File(mapDir).mkdirs();


        //包名
        dobjDir = this.packagePath() + File.separator + "vo";
        new File(dobjDir).mkdirs();
        daoDir = this.packagePath() + File.separator + "dao";
        new File(daoDir).mkdirs();


        List<MapperInfo> mappers = new ArrayList<MapperInfo>();
        for (Table table : tables) {
            MapperInfo mapperInfo = genTheTable(table,this.packageName(),dobjDir,daoDir,mapDir);
            if (mapperInfo != null) {
                mappers.add(mapperInfo);
            }
        }

        //开启xml自动配置
        if (this.genXmlConfig && mappers.size() > 0) {
            String mapperName = mapperPath;
            String mapperConf = null;
            //mybatis配置路径
            if (mapperPath != null && mapperPath.length() > 0) {
                mapperConf = this.resourcesPath() + File.separator + mapperPath;
            } else {
                mapperName = SQLMAP_CONFIG_NAME;
                mapperConf = this.resourcesPath() + File.separator + SQLMAP_CONFIG_NAME;
            }
            writeMapperSetting(mapperConf,mappers);

            //spring bean配置
            String springConf = this.resourcesPath() + File.separator + SPRING_BEAN_XML_NAME;
            writeSpringXml(springConf,mapperName,mappers,this.getProjectSimpleName());

        }

        return true;
    }

    /**
     * 获取数据表结构 [拷贝]
     * @return
     */
    public List<Table> getTables() {
        return new ArrayList<Table>(tables);
    }


    private static List<Table> parseSqlTables(String sqlsSourcePath, String tablePrefix) {

        //读取sql文件
        String sqlsContent = getSqlsContent(sqlsSourcePath);

        List<Table> tables = new ArrayList<Table>();

        //采用";"分割
        String[] sqls = specialSplit(sqlsContent,';');
        for (String sql : sqls) {

            //不能分割
            Pattern p = Pattern.compile("create\\s+table", Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(sql);
            if (!m.find()) {
                continue;
            }

            //匹配到一组
            int end = m.end();
            int idx = sql.indexOf("(", end);//从匹配命令后开始
            //解析表头
            StringBuilder builder = new StringBuilder();
            for (int i = idx - 1; i >= 0; i--) {
                char c = sql.charAt(i);
                if (isLetterChar(c) || isNumberChar(c)) {
                    builder.insert(0, c);
                } else if (c == '_') {//是否采用驼峰
                    builder.insert(0, c);
                } else {
                    if (builder.length() > 0) {
                        break;
                    }
                }
            }

            System.out.println("TableName:" + builder.toString());

            Table table = new Table();
            table.name = builder.toString();
            if (tablePrefix != null && tablePrefix.length() > 0 && table.name.startsWith(tablePrefix)) {
                table.alias = table.name.substring(tablePrefix.length(),table.name.length());
            } else {
                table.alias = table.name;
            }

            sql = sql.substring(idx + 1);

            parseSqlTable(sql,table);

            //有效的table
            if (table.columns.size() > 0) {
                tables.add(table);
            }
        }

        return tables;
    }

    private static void parseSqlTable(String sql,Table table) {
        String[] lines = specialSplit(sql,',');

        for (String line : lines) {
            line = line.trim();

            //判断是否为索引
            String[] strs = specialSplit(line,' ');
            if (strs.length <= 0) {
                continue;
            }

            boolean isIndex = false;
            String head = strs[0].toUpperCase();
            for (String key : MYSQL_INDEX_TYPE) {
                if (head.startsWith(key)) {
                    isIndex = true;
                    break;
                }
            }

            if (!isIndex && strs.length > 2) {
                addColumnToTable(strs,table);
            } else if (isIndex && strs.length >= 3){
                addIndexToTable(strs,line,table);
            }

        }
    }

    private static final String REPLACE_STRING_1 = "@~……#1";
    private static final String REPLACE_STRING_2 = "@~……#2";
    private static String[] specialSplit(String s, char split) {

        s = s.replaceAll("\\\\\"",REPLACE_STRING_1);//先替换掉可能嵌套的字符
        s = s.replaceAll("\\\\'",REPLACE_STRING_2);//先替换掉可能嵌套的字符

        List<String> result = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();

        int parenCount = 0;
        int quotCount = 0;
        int squotCount = 0;
        for (int i = 0; i < s.length(); i++) { // go from 1 to length -1 to discard the surrounding ()
            char c = s.charAt(i);
            if (squotCount > 0 && c == '\'') { squotCount--; }
            else if (c == '\'') { squotCount++; }
            else if (quotCount > 0 && c == '\"') { quotCount--; }
            else if (c == '\"') { quotCount++; }
            else if (quotCount == 0 && squotCount == 0 && c == '(') { parenCount++; }
            else if (quotCount == 0 && squotCount == 0 && c == ')') { parenCount--; }

            if (quotCount == 0 && squotCount == 0 && parenCount == 0 && c == split) {
                String subString = sb.toString();
                if (subString.length() > 0) {
                    subString = subString.replaceAll(REPLACE_STRING_1, "\\\\\"");
                    subString = subString.replaceAll(REPLACE_STRING_2, "\\\\'");
                    result.add(subString);
                }
                sb.setLength(0); // clear string builder
            } else {
                sb.append(c);
            }
        }

        String subString = sb.toString();
        if (subString.length() > 0) {
            subString = subString.replaceAll(REPLACE_STRING_1, "\\\\\"");
            subString = subString.replaceAll(REPLACE_STRING_2, "\\\\'");
            result.add(subString);
        }
        return result.toArray(new String[0]);
    }


    private static boolean isSpacingChar(char c) {
        if (c == '\f' || c == '\n' || c == '\r' || c == '\t' || c == ' ') {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isLetterChar(char c) {
        if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isNumberChar(char c) {
        if (c >= '0' && c <= '9') {
            return true;
        } else {
            return false;
        }
    }

    private static void addIndexToTable(String[] strs, String line, Table table) {
        boolean primary = strs[0].equalsIgnoreCase("PRIMARY");
        boolean unique = primary || strs[0].equalsIgnoreCase("UNIQUE");

        int xbegin = line.indexOf("(");
        int xend = line.indexOf(")");
        if (xbegin >= 0 && xbegin < line.length() && xend > 0 && xend < line.length()) {
            String columns = line.substring(xbegin + 1,xend);

            ColumnIndex columnIndex = new ColumnIndex();
            columnIndex.isPrimary = primary;
            columnIndex.isUnique = unique;

            String ss[] = columns.trim().split(",");
            for (String s : ss) {
                if (s.startsWith("`") && s.endsWith("`")) {
                    s = s.substring(1,s.length() - 1);
                }

                for (Column col : table.columns) {
                    if (col.name.equals(s)) {
                        columnIndex.columns.add(col);
                        break;
                    }
                }
            }

            table.indexs.add(columnIndex);

            System.out.println("PRIMARY:" + primary + "; UNIQUE:" + unique + "; COLUMNS:" + columns);
        }
    }

    private static void addColumnToTable(String[] strs, Table table) {

        String column = strs[0];
        String type = strs[1];

        boolean notNull = false;

        //检查default和comment
        String defaultValue = "NULL";
        String comment = "";
        for (int i = 2; i < strs.length; i++) {
            if (strs[i].equalsIgnoreCase("DEFAULT") && i + 1 < strs.length) {
                defaultValue = strs[i + 1];
            } else if (strs[i].equalsIgnoreCase("COMMENT") && i + 1 < strs.length) {
                comment = strs[i + 1];
            } else if (strs[i].equalsIgnoreCase("NOT") && i + 1 < strs.length) {
                notNull = strs[i + 1].equalsIgnoreCase("NULL");
            }
        }

        // 字段处理
        if (column.startsWith("`") && column.endsWith("`")) {
            column = column.substring(1,column.length() - 1);
        }

        // type处理
        int idx = type.indexOf("(");
        if (idx > 0 && idx < type.length()) {
            type = type.substring(0,idx);
        }

        //注释处理
        if ((comment.startsWith("\"") && comment.endsWith("\"")) || (comment.startsWith("\'") && comment.endsWith("\'"))) {
            comment = comment.substring(1,comment.length() - 1);
        }

        // 默认值处理
        if (notNull && defaultValue.equalsIgnoreCase("NULL")) {//此处直接矫正，虽然理论存在不为空，但是又设置默认值情况，因为update可以设置空的情况还是有的
            defaultValue = null;
        } else if ((defaultValue.startsWith("\"") && defaultValue.endsWith("\"")) || (defaultValue.startsWith("\'") && defaultValue.endsWith("\'"))) {
            defaultValue = defaultValue.substring(1,defaultValue.length() - 1);
        }

        Column col = new Column();
        col.name = column;
        col.type = type.toUpperCase();
        col.cmmt = comment;
        col.notNull = notNull;
        col.defaultValue = defaultValue;

        table.columns.add(col);

        System.out.println("Column:" + column + "; Type:" + type + "; NOT_NULL:" + notNull + "; DEFAULT:" + defaultValue + "; COMMENT:" + comment);
    }

    private static class MapperMethod {
        String id;//方法名
        String returnType;//返回值类型
        String sql;//对应的sql
    }

    private static MapperInfo genTheTable(Table table,String packName, String dobjDir,String daoDir,String mapDir) {
        if (table == null || table.columns.size() == 0) {
            return null;
        }

        MapperInfo mapperInfo = new MapperInfo();

        //采用别名 驼峰法
        String name = toHumpString(table.alias,true);

        String dobjFileName = name + "DO.java";
        String daoFileName = name + "DAO.java";

        //保留原名
        String mapperFileName = table.name.replaceAll("_","-") + "-sqlmap.xml";

        File dobjFile = new File(dobjDir + File.separator + dobjFileName);
        File daoFile = new File(daoDir + File.separator + daoFileName);
        File dmapFile = new File(mapDir + File.separator + mapperFileName);


        writeDObject(dobjFile,name,packName,table);
        List<MapperMethod> methods = writeDAObject(daoFile,name,packName,table);
        writeMapper(dmapFile,name,packName,table, methods);

        //记录mapper信息
        mapperInfo.daoClassName = packName + ".dao." + name + "DAO";
        mapperInfo.daoSimpleClassName = name + "DAO";
        mapperInfo.mapperFileName = mapperFileName;
        mapperInfo.daoPath = daoFile.getAbsolutePath();
        mapperInfo.mapperFilePath = dmapFile.getAbsolutePath();
        return mapperInfo;
    }

    private static void writeDObject(File file, String className, String packageName, Table table) {
        StringBuilder dobjContent = new StringBuilder();
        dobjContent.append("package " + packageName + ".vo;\n\r\n\r");
        dobjContent.append("import java.io.Serializable;\n");
        dobjContent.append("import java.util.*;\n\r\n\r");
        dobjContent.append("/**\n");
        dobjContent.append(" * Owner: Minjun Ling\n");
        dobjContent.append(" * Creator: ESB MybatisGenerator\n");
        dobjContent.append(" * Version: 1.0.0\n");
        dobjContent.append(" * GitHub: https://github.com/lingminjun/esb\n");
        dobjContent.append(" * Since: " + new Date() + "\n");
        dobjContent.append(" * Table: " + table.name + "\n");
        dobjContent.append(" */\n");
        dobjContent.append("public final class " + className + "DO implements Serializable {\n");
        dobjContent.append("    private static final long serialVersionUID = 1L;\n");

        for (Column cl : table.columns) {
            if (MYSQL_LONG_TYPE.contains(cl.type)) {
                dobjContent.append("    public Long    ");
            } else if (MYSQL_BOOL_TYPE.contains(cl.type)) {
                dobjContent.append("    public Boolean ");
            } else if (MYSQL_DOUBLE_TYPE.contains(cl.type)) {
                dobjContent.append("    public Double  ");
            } else if (MYSQL_INT_TYPE.contains(cl.type)) {
                dobjContent.append("    public Integer ");
            } else if (MYSQL_STRING_TYPE.contains(cl.type)) {
                dobjContent.append("    public String  ");
            } else if (MYSQL_DATE_TYPE.contains(cl.type)) {
                dobjContent.append("    public Date    ");
            } else {
                continue;
            }


            dobjContent.append(toHumpString(cl.name,false) + ";");
            if (cl.cmmt != null && cl.cmmt.length() > 0) {
                dobjContent.append(" // " + cl.cmmt);
            }
            dobjContent.append("\n");
        }

        dobjContent.append("}\n\r\n\r");

        try {
            writeFile(file,dobjContent.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<String,String> getImportLineFromJavaSource(String content) {
        //找import语句
        Pattern p = Pattern.compile("import\\s+[\\w$.]+\\s*;");
        Matcher m = p.matcher(content);
        Map<String,String> map = new HashMap<String, String>();
        while (m.find())
        {
//            System.out.println(m.group(0));
            String imp = m.group(0);
            String[] strs = imp.trim().split("\\s+");
            if (strs.length == 2) {
                map.put(strs[1].substring(0,strs[1].length() - 1),imp);
            } else {
                map.put(imp,imp);
            }
        }
        return map;
    }

    private static String getBodyFromJavaSource(String content) {
        //找import语句
        Pattern p = Pattern.compile("public\\s+(interface|class)\\s+[\\w$.]+[\\w$.<>\\s]*\\{[\\S\\s]*}");
        Matcher m = p.matcher(content);
        while (m.find())
        {
            return m.group(0);
        }
        return null;
    }

    private static List<MapperMethod> getInterfaceMapperMetthods(Class clazz) {
        List<MapperMethod> list = new ArrayList<MapperMethod>();

        Method[] methods = clazz.getMethods();
        if (methods == null || methods.length == 0) {
            return list;
        }

        for (int i = 0; i < methods.length; i++) {
            Method md = methods[i];

            SQL mapper = md.getAnnotation(SQL.class);
            if (mapper == null) {
                continue;
            }

            MapperMethod mapperMethod = new MapperMethod();
            mapperMethod.id = md.getName();
            mapperMethod.sql = mapper.value();

            //返回值
            String type = md.getGenericReturnType().toString();
            if (type.contains("<")) {
                type = type.split("<")[1];
                type = type.substring(0,type.length() - 1);
            } else if (type.startsWith("class ")) {
                type = type.substring("class ".length(), type.length());
            }

            //转包装类型
            if (type.equals("int")) {
                type = Integer.class.getName();
            } else if (type.equals("short")) {
                type = Short.class.getName();
            } else if (type.equals("long")) {
                type = Long.class.getName();
            } else if (type.equals("boolean")) {
                type = Boolean.class.getName();
            } else if (type.equals("byte")) {
                type = Byte.class.getName();
            } else if (type.equals("char")) {
                type = Character.class.getName();
            } else if (type.equals("float")) {
                type = Float.class.getName();
            } else if (type.equals("double")) {
                type = Double.class.getName();
            }
            mapperMethod.returnType = type;
//            System.out.println(type);
            list.add(mapperMethod);
        }

        return list;
    }

    private static List<MapperMethod> writeDAObject(File file, String className, String packageName, Table table) {

        //注意 索引查询需要重新生成类
        boolean hasIndexQuery = table.hasIndexQuery();
        String daoDir = file.getParent();//父目录
        File idxDaoFile = new File(daoDir + File.separator + "inc" + File.separator + className + "IndexQueryDAO.java");
        if (idxDaoFile.exists()) {//先删除
            idxDaoFile.delete();
        }

        if (hasIndexQuery) {
            writeIndexQueryDAObject(idxDaoFile,className,packageName,table);
        }

        List<MapperMethod> methods = null;

        //此类全称
        String daobj = table.getDAOClassName(packageName);      //
        String idxDaobj = table.getIncDAOClassName(packageName);//

        //如果文件本身存在，则保留文件体
        Map<String,String> imports = new HashMap<String, String>();
        String body = null;
        if (file.exists()) {

            //先获取要执行的额外内容
            try {
                methods = getInterfaceMapperMetthods(Class.forName(daobj));
            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
                System.out.println("抱歉！没有加载到原类，请采用单元测试执行Generator！");
            }

            //保留java代码
            try {
                String old = readFile(file.getAbsolutePath());
                imports = getImportLineFromJavaSource(old);
                body = getBodyFromJavaSource(old);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        imports.put(TableDAO.class.getName(),"import " + TableDAO.class.getName() + ";");
        String dobj = table.getDObjectClassName(packageName);   //
        imports.put(dobj,"import " + dobj + ";");
        imports.put(Mapper.class.getName(),"import " + Mapper.class.getName() + ";");
        if (hasIndexQuery) {
            imports.put(idxDaobj,"import " + idxDaobj + ";");
        }

        StringBuilder content = new StringBuilder();
        content.append("package " + packageName + ".dao;\n\r\n\r");

        //imports
        Iterator<Map.Entry<String, String>> entries = imports.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, String> entry = entries.next();
            content.append(entry.getValue() + "\n");
        }
        content.append("\n\n");
        content.append("/**\n");
        content.append(" * Owner: Minjun Ling\n");
        content.append(" * Creator: ESB MybatisGenerator\n");
        content.append(" * Version: 1.0.0\n");
        content.append(" * GitHub: https://github.com/lingminjun/esb\n");
        content.append(" * Since: " + new Date() + "\n");
        content.append(" * Table: " + table.name + "\n");
        content.append(" */\n");

        //类定义

//        content.append("@Mapper\n"); // 扫描方式支持，此处不需要，因为xml中会定义
        content.append("public interface " + className + "DAO ");
        if (hasIndexQuery) {
            content.append("extends " + className + "IndexQueryDAO ");
        } else {
            content.append("extends TableDAO<" + className + "DO> ");
        }

        //保留body
        if (body != null && body.length() > 0) {
            int idx = body.indexOf("{");
            body = body.substring(idx);
            content.append(body);
        } else {
            content.append("{ /* Add custom methods */ }");
            content.append("\n\r\n\r");
        }

        try {
            writeFile(file,content.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return methods;
    }

    private static void writeIndexQueryDAObject(File file, String className, String packageName, Table table) {

        File dic = file.getParentFile();
        if (!dic.exists()) {
            dic.mkdirs();
        }

        //如果文件本身存在，则保留文件体
        Map<String,String> imports = new HashMap<String, String>();

        imports.put(TableDAO.class.getName(),"import " + TableDAO.class.getName() + ";");
        String dobj = table.getDObjectClassName(packageName);
        imports.put(dobj,"import " + dobj + ";");
        imports.put(Mapper.class.getName(),"import " + Mapper.class.getName() + ";");
        imports.put(Param.class.getName(),"import " + Param.class.getName() + ";");
        imports.put(List.class.getName(),"import " + List.class.getName() + ";");

        //开始写入
        StringBuilder content = new StringBuilder();
        content.append("package " + packageName + ".dao.inc;\n\r\n\r");

        //imports
        Iterator<Map.Entry<String, String>> entries = imports.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, String> entry = entries.next();
            content.append(entry.getValue() + "\n");
        }
        content.append("\n\n");
        content.append("/**\n");
        content.append(" * Owner: Minjun Ling\n");
        content.append(" * Creator: ESB MybatisGenerator\n");
        content.append(" * Version: 1.0.0\n");
        content.append(" * GitHub: https://github.com/lingminjun/esb\n");
        content.append(" * Since: " + new Date() + "\n");
        content.append(" * Table: " + table.name + "\n");
        content.append(" */\n");

        //类定义
        content.append("public interface " + className + "IndexQueryDAO extends TableDAO<" + className + "DO> { \n");

        //查询方法
        Map<String,List<Column>> queryMethods = table.allIndexQueryMethod();
        List<String> methodNames = new ArrayList<String>(queryMethods.keySet());
        Collections.sort(methodNames);
        for (String methodName : methodNames) {

            List<Column> cols = queryMethods.get(methodName);

            content.append("    /**\n");
            content.append("     * 根据以下索引字段查询实体对象集\n");

            StringBuilder params = new StringBuilder();
            buildMethodParams(cols,table,content,params);

            // 排序与limit
            content.append("     * @param sortField 排序字段，传入null时表示不写入sql\n");
            content.append("     * @param isDesc 排序为降序\n");
            content.append("     * @param offset 其实位置\n");
            content.append("     * @param limit  返回条数\n");
            params.append(",@Param(\"sortField\") String sortField");
            params.append(",@Param(\"isDesc\") boolean isDesc");
            params.append(",@Param(\"offset\") int offset");
            params.append(",@Param(\"limit\") int limit");

            content.append("     * @return\n");
            content.append("     */\n");
            content.append("    public List<" + className + "DO> " + methodName + "(");
            content.append(params.toString());
            content.append(");\n\r\n\r");
        }

        //求总数方法
        Map<String,List<Column>> countMethods = table.allIndexCountMethod();
        List<String> countMethodNames = new ArrayList<String>(countMethods.keySet());
        Collections.sort(countMethodNames);
        for (String methodName : countMethodNames) {

            List<Column> cols = countMethods.get(methodName);

            content.append("    /**\n");
            content.append("     * 根据以下索引字段计算count\n");

            StringBuilder params = new StringBuilder();
            buildMethodParams(cols,table,content,params);

            content.append("     * @return\n");
            content.append("     */\n");
            content.append("    public long " + methodName + "(");
            content.append(params.toString());
            content.append(");\n\r\n\r");
        }

        //结束
        content.append("}\n\r\n\r");


        try {
            writeFile(file,content.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void buildMethodParams(List<Column> cols,Table table, StringBuilder content, StringBuilder params) {
        boolean first = true;
        for (Column col : cols) {
            String type = col.getDataType();
            if (type == null || type.length() == 0) {
                continue;
            }
            String colName = toHumpString(col.name,false);
            content.append("     * @param " + colName + "  " + (col.cmmt == null ? "" : col.cmmt) + "\n");
            if (first) { first = false; }
            else {
                params.append(", ");
            }
            params.append("@Param(\"" + colName + "\") " + type + " " + colName);
        }

        //判断delete字段
        Column theDeleteColumn = table.getDeleteStateColumn();
        if (!Table.hasDeleteStateColumn(cols) && theDeleteColumn != null) {
            String colName = toHumpString(theDeleteColumn.name,false);
            content.append("     * @param " + colName + "  " + (theDeleteColumn.cmmt == null ? "" : theDeleteColumn.cmmt) + "\n");
            if (first) { first = false; }
            else {
                params.append(", ");
            }
            params.append("@Param(\"" + colName + "\") " + theDeleteColumn.getDataType() + " " + colName);
        }
    }

    private static void writeMapper(File file, String className, String packageName, Table table, List<MapperMethod> methods) {

        String doName = table.getDObjectClassName(packageName); //
        String daoName = table.getDAOClassName(packageName);    //
        StringBuilder content = new StringBuilder();
        content.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD SQL 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\" >\n" +
                "<mapper namespace=\"" + daoName + "\">\n\n");
        String resultEntity = className.substring(0,1).toLowerCase() + className.substring(1) + "DOResult";
        content.append("    <resultMap id=\"" + resultEntity + "\" type=\"" + doName + "\">\n");
        for (Column cl : table.columns) {
            content.append("        <result column=\"" + cl.name + "\" property=\"" + toHumpString(cl.name,false) + "\"/>\n");
        }
        content.append("    </resultMap>\n\n");

        //将column修改
        StringBuilder flds = new StringBuilder();
        StringBuilder cols = new StringBuilder();

        StringBuilder inflds = new StringBuilder();
        StringBuilder item_inflds = new StringBuilder();
        StringBuilder incols = new StringBuilder();

        for (Column cl : table.columns) {

            if (cols.length() > 0) {
                flds.append(",");
                cols.append(",");
            }

            String field;
            String itemField;
            if (cl.name.equals("create_at") || cl.name.equals("modified_at")) {
                // 优化
                if (MYSQL_DATE_TYPE.contains(cl.type)) {//日期类型
                    /*
                    ---------------------------------------------------------------------------
                    类型	        字节	格式	                用途	                是否支持设置系统默认值
                    date	    3	YYYY-MM-DD	        日期值	                不支持
                    time	    3	HH:MM:SS	        时间值或持续时间	        不支持
                    year	    1	YYYY	            年份	                    不支持
                    datetime	8	YYYY-MM-DD HH:MM:SS	日期和时间混合值	        不支持
                    timestamp	4	YYYYMMDD HHMMSS	    混合日期和时间，可作时间戳	支持
                    ---------------------------------------------------------------------------
                    */
                    if ("date".equalsIgnoreCase(cl.type)) {
                        field = "curdate()";
                        itemField = "curdate()";
                    } else if ("time".equalsIgnoreCase(cl.type)) {
                        field = "curtime()";
                        itemField = "curtime()";
                    } else if ("year".equalsIgnoreCase(cl.type)) {//FIXME:此处可能不成功
                        field = "now()";
                        itemField = "now()";
                    } else {
                        field = "now()";
                        itemField = "now()";
                    }
                } else {//说明采用long记录日期
                    field = "(unix_timestamp() * 1000)";
                    itemField = "(unix_timestamp() * 1000)";
                }
            } else {
                field = "#{" + toHumpString(cl.name,false) + "}";
                itemField = "#{item." + toHumpString(cl.name,false) + "}";
            }
            cols.append("`" + cl.name + "`");
            flds.append(field);

            if (cl.name.equals("id")) {
                continue;
            }
            if (incols.length() > 0) {
                incols.append(",");
                inflds.append(",");
                item_inflds.append(",");
            }
            incols.append("`" + cl.name + "`");
            inflds.append(field);
            item_inflds.append(itemField);

        }

        StringBuilder upBuilder = new StringBuilder();
        boolean hasModify = false;
        for (Column cl : table.columns) {
            if (cl.name.equals("id") || cl.name.equals("create_at")) {
                continue;
            }

            if (cl.name.equals("modified_at")) {
                hasModify = true;
                if (MYSQL_DATE_TYPE.contains(cl.type)) {
                    if ("date".equalsIgnoreCase(cl.type)) {
                        upBuilder.insert(0, "            modified_at = curdate() \n");
                    } else if ("time".equalsIgnoreCase(cl.type)) {
                        upBuilder.insert(0, "            modified_at = curtime() \n");
                    } else if ("year".equalsIgnoreCase(cl.type)) {//FIXME:此处可能不成功
                        upBuilder.insert(0, "            modified_at = now() \n");
                    } else {
                        upBuilder.insert(0, "            modified_at = now() \n");
                    }
                } else {
                    upBuilder.insert(0,"            modified_at = (unix_timestamp() * 1000) \n");
                }
            } else {
                upBuilder.append("        <if test=\""+ toHumpString(cl.name,false) + " != null\">\n");
                    upBuilder.append("            ,");
                upBuilder.append("`"+ cl.name +"` = #{"+ toHumpString(cl.name,false) + "}\n");
                upBuilder.append("        </if>\n");
            }
        }
        if (!hasModify) {
            upBuilder.insert(0,"            id = id \n");//为了语法不错，故意设置id作为第一项
        }

        //默认的sql文件编写
//        public void insert(DO entity) throws DataAccessException;
        content.append("    <insert id=\"insert\" useGeneratedKeys=\"true\" keyProperty=\"id\" parameterType=\"" + doName + "\">\n");
        content.append("        insert into `"+table.name+"` (" + incols.toString() +") values (" + inflds.toString() + ")\n");
        content.append("    </insert>\n\n");

//        public void insertOrUpdate(DO entity) throws DataAccessException;
        content.append("    <insert id=\"insertOrUpdate\" useGeneratedKeys=\"true\" keyProperty=\"id\" parameterType=\"" + doName + "\">\n");
        content.append("        insert into `"+table.name+"` (" + incols.toString() +") values (" + inflds.toString() + ") on duplicate key update \n");
        content.append(upBuilder.toString());
        content.append("    </insert>\n\n");

        //public long batchInsert(List<DO> entities) throws DataAccessException;
        content.append("    <insert id=\"batchInsert\" useGeneratedKeys=\"true\" parameterType=\"java.util.List\">\n");
        content.append("        <selectKey resultType=\"long\" keyProperty=\"id\" order=\"AFTER\">\n");
        content.append("            SELECT LAST_INSERT_ID()\n");
        content.append("        </selectKey>\n");
        content.append("        insert into `"+table.name+"` (" + incols.toString() +") values \n");
        content.append("        <foreach collection=\"list\" item=\"item\" index=\"index\" separator=\",\" >\n");
        content.append("            (" + item_inflds.toString() + ")\n");
        content.append("        </foreach>\n");
        content.append("    </insert>\n\n");

//        public int update(DO entity) throws DataAccessException;
        content.append("    <update id=\"update\" parameterType=\"" + doName + "\">\n");
        content.append("        update `" + table.name + "` set \n");
        content.append(upBuilder.toString());
        content.append("        where id = #{id} \n");
        content.append("    </update>\n\n");

//        public int deleteById(Long pk) throws DataAccessException;
        content.append("    <delete id=\"deleteById\">\n");
        content.append("        delete from `" + table.name + "` where id = #{id} \n");
        content.append("    </delete>\n\n");

//        public DO getById(Long pk) throws DataAccessException;
        content.append("    <select id=\"getById\" resultMap=\"" + resultEntity + "\">\n");
        content.append("        select " + cols.toString() + " \n");
        content.append("        from `" + table.name + "` \n");
        content.append("        where id = #{id} \n");
        content.append("    </select>\n\n");

//        public DO getByIdForUpdate(Long pk) throws DataAccessException;
        content.append("    <select id=\"getByIdForUpdate\" resultMap=\"" + resultEntity + "\">\n");
        content.append("        select " + cols.toString() + " \n");
        content.append("        from `" + table.name + "` \n");
        content.append("        where id = #{id} \n");
        content.append("        for update \n");
        content.append("    </select>\n\n");

        //public List<DO> queryByIds(List<Long> pks);
        content.append("    <select id=\"queryByIds\" resultMap=\"" + resultEntity + "\">\n");
        content.append("        select " + cols.toString() + " \n");
        content.append("        from `" + table.name + "` \n");
        content.append("        where id en \n");
        content.append("        <foreach collection=\"list\" item=\"theId\" index=\"index\" \n");
        content.append("             open=\"(\" close=\")\" separator=\",\"> \n");
        content.append("             #{theId}  \n");
        content.append("        </foreach>  \n");
        content.append("    </select>\n\n");

        // 针对索引建查询语句
        Map<String,List<Column>> queryMethods = table.allIndexQueryMethod();
        List<String> methodNames = new ArrayList<String>(queryMethods.keySet());
        Collections.sort(methodNames);
        for (String methodName : methodNames) {

            List<Column> tcols = queryMethods.get(methodName);
            String queryWhere = Table.getSqlWhereFragment(tcols,table);

            content.append("    <select id=\"" + methodName + "\" resultMap=\"" + resultEntity + "\">\n");
            content.append("        select " + cols.toString() + " \n");
            content.append("        from `" + table.name + "` \n");
            content.append("        where ");
            content.append(queryWhere);
            content.append("        <if test=\"sortField != null and sortField != ''\">\n");
            content.append("            order by `${sortField}` ");//注意参数为字符替换，而不是"?"掩码
            //MySQL中默认排序是acs(可省略)：从小到大 ; desc ：从大到小，也叫倒序排列。
            content.append("<if test=\"isDesc\"> desc </if> \n");
            content.append("        </if>\n");
            content.append("        limit #{offset},#{limit}\n");//发现limit可以掩码"?"
            content.append("    </select>\n\n");
        }

        // 针对索引求count
        Map<String,List<Column>> countMethods = table.allIndexCountMethod();
        List<String> countMethodNames = new ArrayList<String>(countMethods.keySet());
        Collections.sort(countMethodNames);
        for (String methodName : countMethodNames) {

            List<Column> tcols = countMethods.get(methodName);
            String queryWhere = Table.getSqlWhereFragment(tcols,table);

            content.append("    <select id=\"" + methodName + "\" resultType=\"java.lang.Long\">\n");
            content.append("        select count(1) from `" + table.name + "` \n");
            content.append("        where ");
            content.append(queryWhere);
            content.append("    </select>\n\n");
        }


        //自定的mapper添加
        if (methods != null && methods.size() > 0) {
            content.append("    <!-- Custom sqls mapper -->\n");
            for (MapperMethod mapperMethod : methods) {
                String sql = mapperMethod.sql.trim();//.toLowerCase();

                //处理特殊字符
                sql = sql.replaceAll("<\\!\\[((?i)cdata)\\[\\s+<>\\s+\\]\\]>"," <> ");
                sql = sql.replaceAll("<\\!\\[((?i)cdata)\\[\\s+<=\\s+\\]\\]>"," <= ");
                sql = sql.replaceAll("<\\!\\[((?i)cdata)\\[\\s+>=\\s+\\]\\]>"," >= ");
                sql = sql.replaceAll("<\\!\\[((?i)cdata)\\[\\s+<\\s+\\]\\]>"," < ");
                sql = sql.replaceAll("<\\!\\[((?i)cdata)\\[\\s+>\\s+\\]\\]>"," > ");

                sql = sql.replaceAll("<>","_@!#0#!@_");
                sql = sql.replaceAll("<=","_@!#1#!@_");
                sql = sql.replaceAll(">=","_@!#2#!@_");
                sql = sql.replaceAll(" < ","_@!#3#!@_");//防止把sql已有脚本提出
                sql = sql.replaceAll(" > ","_@!#4#!@_");//防止把sql已有脚本提出


                sql = sql.replaceAll("_@!#0#!@_"," <![CDATA[ <> ]]> ");
                sql = sql.replaceAll("_@!#1#!@_"," <![CDATA[ <= ]]> ");
                sql = sql.replaceAll("_@!#2#!@_"," <![CDATA[ >= ]]> ");
                sql = sql.replaceAll("_@!#3#!@_"," <![CDATA[ < ]]> ");
                sql = sql.replaceAll("_@!#4#!@_"," <![CDATA[ > ]]> ");

                if (sql.toLowerCase().startsWith("insert")) {
                    content.append("    <insert id=\"" + mapperMethod.id + "\" useGeneratedKeys=\"true\" keyProperty=\"id\" >\n");
                    content.append("        " + sql + "\n");
                    content.append("    </insert>\n\n");
                } else if (sql.toLowerCase().startsWith("update")) {
                    content.append("    <update id=\"" + mapperMethod.id + "\" >\n");
                    content.append("        " + sql + "\n");
                    content.append("    </update>\n\n");
                } else if (sql.toLowerCase().startsWith("delete")) {
                    content.append("    <delete id=\"" + mapperMethod.id + "\">\n");
                    content.append("        " + sql + "\n");
                    content.append("    </delete>\n\n");
                } else {
                    //已经映射过返回值，直接使用
                    if (mapperMethod.returnType.equals(doName)) {
                        content.append("    <select id=\"" + mapperMethod.id + "\" resultMap=\"" + resultEntity + "\">\n");
                    } else {
                        content.append("    <select id=\"" + mapperMethod.id + "\" resultType=\"" + mapperMethod.returnType + "\">\n");
                    }
                    content.append("        " + sql + "\n");
                    content.append("    </select>\n\n");
                }
            }
        }

        content.append("</mapper>\n\n");

        try {
            writeFile(file,content.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeMapperSetting(String mapperPath, List<MapperInfo> mappers) {
//        File file = new File(mapperPath);
        //判断是否为更新
        StringBuilder content = new StringBuilder();
        boolean fileHeader = false;
        try {
            String old = FileUtils.readFile(mapperPath, ESBConsts.UTF8);
            if (old != null) {
                int idx = old.indexOf("<mappers>");
                if (idx > 0 && idx < old.length()) {
                    content.append(old.substring(0,idx));
                    fileHeader = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //默认写入
        if (!fileHeader) {
            content.append(SpringXMLConst.MAPPER_XML_CONFIG_HEAD);
            content.append("    ");
        }

        //开始写入sql-mapper
        content.append("<mappers>\n");
        for (MapperInfo mapperInfo : mappers) {
            content.append(SpringXMLConst.theMapper(mapperInfo.mapperFileName));
        }
        content.append("\n    </mappers>\n");
        content.append(SpringXMLConst.MAPPER_XML_CONFIG_END);

        try {
            writeFile(mapperPath,content.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String parseSqlSessionFromSpringXml(String xml) {
        int idx = xml.lastIndexOf("\"org.mybatis.spring.SqlSessionFactoryBean\"");
        if (idx > 0 && idx < xml.length()) {
            String target = xml.substring(0,idx);
            int begin = target.lastIndexOf("<bean");
            if (begin > 0 && begin < target.length()) {
                target = target.substring(begin + "<bean".length());
            }
            // id="searchSqlSessionFactory" class=
            StringBuilder builder = new StringBuilder();
            int cidx = target.length() - 1;
            //反向遍历
            boolean start = false;
            while (cidx > 0) {
                char c = target.charAt(cidx);
                if (c == '\"') {
                    if (start) {
                        break;
                    } else {
                        start = true;
                    }
                } else if (start) {
                    builder.insert(0,c);
                }

                cidx--;
            }

            return builder.toString();
        }

        return "";
    }

    private static void writeSpringXml(String springPath, String mapperFileName, List<MapperInfo> mappers, String projectName) {

        String datasource = "dataSource";
        String sqlSession = "sqlSessionFactory";
        if (projectName != null && projectName.length() > 0) {
            sqlSession = projectName + "SqlSessionFactory";
            datasource = projectName + "DataSource";
        }

        //判断是否为更新
        StringBuilder content = new StringBuilder();
        boolean fileHeader = false;
        try {
            String old = FileUtils.readFile(springPath, ESBConsts.UTF8);
            if (old != null) {
                int idx = old.indexOf("\"org.mybatis.spring.mapper.MapperFactoryBean\"");
                if (idx > 0 && idx < old.length()) {
                    old = old.substring(0,idx);
                    idx = old.lastIndexOf("<bean");
                    if (idx > 0 && idx < old.length()) {
                        old = old.substring(0,idx);
                        content.append(old);
                        fileHeader = true;

                        //查找sqlSessionName
                        String str = parseSqlSessionFromSpringXml(old);
                        if (str != null && str.length() > 0) {
                            sqlSession = str;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //写入默认配置
        if (!fileHeader) {
            content.append(SpringXMLConst.SPRING_XML_CONFIG_HEAD);
            content.append(SpringXMLConst.theJdbcDatasource(projectName,datasource,sqlSession,mapperFileName));
            content.append("    <!-- mapper beans -->\n    ");

        }

        for (MapperInfo mapperInfo : mappers) {
            String beanName = toLowerHeadString(mapperInfo.daoSimpleClassName);
            content.append(SpringXMLConst.theMapperBean(beanName,mapperInfo.daoClassName,sqlSession));
        }

        content.append("\n</beans>");

        try {
            writeFile(springPath,content.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
