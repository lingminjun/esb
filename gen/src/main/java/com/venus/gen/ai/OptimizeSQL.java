package com.venus.gen.ai;

import com.venus.esb.lang.ESBT;
import com.venus.gen.dao.gen.MybatisGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class OptimizeSQL {

    // 支持select * from table的优化，将*替换掉 (尽量不支持select`table`.* from `table`)
    // 支持select * from table 或者 select table1.*,table2.* from table1,table2
    // user index
    // private static final Pattern SELECT_ALL = Pattern.compile("select\\s+(((`\\w+`|\\w+)\\.(`\\w+`|\\w+|\\*)\\s*,)*(`\\w+`|\\w+)\\.\\*|\\*)\\s+from\\s+");
    private static final Pattern SELECT_ALL1 = Pattern.compile("select\\s+(((`\\w+`|\\w+)\\.(`\\w+`|\\w+|\\*)|\\*)\\s*,\\s*)*((`\\w+`|\\w+)\\.(`\\w+`|\\w+|\\*)|\\*)\\s+from[\\w|\\.`\\s\\,=]+?where",Pattern.CASE_INSENSITIVE);

    private static final Pattern SELECT_ALL2 = Pattern.compile("select\\s+[\\w|\\.`\\s\\,]*\\*[\\w|\\.`\\s\\,\\*]*\\s+from",Pattern.CASE_INSENSITIVE);
    private static final String WHERE_END_FLAG = " WHere "; // 尽量避免书写习惯

    // 光正则暂时不好检查被注释情况（此处逻辑必须配合正则 AUTO_CACHE 使用），查询必须带有where子句，没有where子句的有性能风险，不做优化
    public static String tidy(String sql, List<MybatisGenerator.Table> tables) {
        if (ESBT.isEmpty(sql)) {
            return sql;
        }

        // 正则无法完美匹配where子句的
        if (!sql.toUpperCase().contains(WHERE_END_FLAG.toUpperCase())) {
            sql = sql + WHERE_END_FLAG;
        }

        Matcher matcher = SELECT_ALL1.matcher(sql);

        List<Strs.Range> anns = Strs.substringRanges(sql);
        List<Strs.Range> ranges = new ArrayList<Strs.Range>();
        while (matcher.find()) {
            int idx = matcher.start();
            int edx = matcher.end();
            String group = matcher.group();
            if (SELECT_ALL2.matcher(group).find()) {
                if (notAnnotation(anns,idx,edx)) {
                    // 反向插入
                    ranges.add(0,new Strs.Range(idx, edx));
                }
            }
        }

        // 反向优化
        if (ranges.size() > 0) {
            StringBuilder builder = new StringBuilder(sql);
            for (Strs.Range range : ranges) {
                optSQL(builder,range,tables);
            }
            sql = builder.toString();
        }

        if (sql.endsWith(WHERE_END_FLAG)) {
            sql = sql.substring(0,sql.length() - WHERE_END_FLAG.length());
        }

        return sql;
    }

    private static boolean notAnnotation(List<Strs.Range> anns, int begin, int end) {
        for (Strs.Range range : anns) {
            if (begin >= range.begin && begin <= range.end) {
                return false;
            }
            if (end >= range.begin && end <= range.end) {
                return false;
            }
        }
        return true;
    }

    private static String SELECT_KEY = "SELECT";
    private static void optSQL(StringBuilder sql, Strs.Range range, List<MybatisGenerator.Table> tables) {
        String target = sql.substring(range.begin,range.end);
//        System.out.println(target);
        String[] ss = target.split("from");
        if (ss.length < 2) {
            return;
        }
        String select = ss[0];

        String from = ss[1];
        Map<String,String> alias = new HashMap<String, String>();
        List<String> fromTables = new ArrayList<String>();
        parserFromTables(from,fromTables,alias);

        // 反向查找
        int begin = select.length() - 1;
        int end = select.length();
        // 反向遍历
        boolean find = false;
        StringBuilder tableName = new StringBuilder();
        for (int idx = begin; idx >= 0; idx--) {
            char c = select.charAt(idx);
            if (c == '*') {
                find = true;
                end = idx + 1;
                tableName.delete(0,tableName.length());
                continue;
            }

            if (!find) {
                continue;
            }

            if (c == '.') {
                continue;
            }

            //结束
            if (c == ' ' || c == ',') {
                String columns = columnsFromTable(tableName.toString(),fromTables,alias,tables);

                if (!ESBT.isEmpty(columns)) {
                    sql.replace(idx + 1, end, columns);
                }

                // 还原数据
                find = true;
                end = idx;
                tableName.delete(0,tableName.length());
                continue;
            }

            if (c != '`') {
                tableName.insert(0, c);
            }
        }
    }

    private static void parserFromTables(String from, List<String> fromTables, Map<String,String> alias) {
        String[] ws = from.split("\\s+");
        List<String> words = new ArrayList<String>();
        for (String str : ws) {
            if (ESBT.isEmpty(str)) {
                continue;
            }
            String[] ss = str.split(",");
            for (String s : ss) {
                if (ESBT.isEmpty(s)) {
                    continue;
                }
                words.add(s);
            }
        }


        int idx = 0;
        String tableName = null;
        String aliasName = null;
        int status = 0; // 0开始表，1别名
        while (idx < words.size()) {
            String key = words.get(idx);
            idx++;
            if (key.equalsIgnoreCase("from")
                    || key.equalsIgnoreCase("where")
                    || key.equalsIgnoreCase(",")
                    || key.equalsIgnoreCase("inner")
                    || key.equalsIgnoreCase("left")
                    || key.equalsIgnoreCase("right")
                    || key.equalsIgnoreCase("outer")
                    || key.equalsIgnoreCase("join")
                    ) {
                continue;
            }
            if (key.equalsIgnoreCase("on")) {
                int kc = 0;
                while (idx < words.size()) {
                    String word = words.get(idx);
                    idx++;
                    kc += Strs.hitCount(word,".");
                    if (kc == 2) {
                        break;
                    }
                }
                continue;
            }

            if (key.equalsIgnoreCase("as")) {
                status = 1;
                continue;
            }

            if (key.startsWith("`")) {
                key = key.substring(1);
            }
            if (key.endsWith("`")) {
                key = key.substring(0,key.length()-1);
            }
            if (status == 0) {
                tableName = key;
                if (!fromTables.contains(tableName)) {
                    fromTables.add(tableName);
                }
            } else {
                alias.put(key,tableName);
            }

            if (status != 0) {
                tableName = null;
            }

            status = 0;
        }

    }

    private static String columnsFromTable(String aliasName, List<String> fromTables, Map<String,String> alias, List<MybatisGenerator.Table> tables) {
        List<String> tbs = new ArrayList<String>();
        Map<String,String> in_alias = new HashMap<String, String>();
        if (ESBT.isEmpty(aliasName)) {
            tbs.addAll(fromTables);
        } else if (alias.containsKey(aliasName)) {
            tbs.add(alias.get(aliasName));
            in_alias.put(alias.get(aliasName),aliasName);
        } else {
            tbs.add(aliasName);
        }

        if (tbs.isEmpty()) {
            return null;
        }

        StringBuilder str = new StringBuilder();
        for (String tb : tbs) {
            MybatisGenerator.Table table = getTable(tb,tables);
            if (table == null) {
                continue;
            }

            if (str.length() > 0) {
                str.append(",");
            }

            // 没有考虑去重逻辑
            if (in_alias.containsKey(tb)) {
                str.append(table.flatAllColumns(in_alias.get(tb)));
            } else if (fromTables.size() == 1) {
                str.append(table.flatAllColumns(null));
            } else {
                str.append(table.flatAllColumns(tb));
            }
        }

        return str.toString();
    }

    private static MybatisGenerator.Table getTable(String tableName, List<MybatisGenerator.Table> tables) {
        if (tables == null) {
            return null;
        }

        for (MybatisGenerator.Table table : tables) {
            if (table.getName().equals(tableName)) {
                return table;
            }
        }
        return null;
    }

    /*
    public static void main(String[] args) {

        List<MybatisGenerator.Table> tables = MybatisGenerator.parseSqlTables("" +
                "CREATE TABLE `table1` (\n" +
                "`id`  bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id' ,\n" +
                "`username`  varchar(255) NULL DEFAULT NULL COMMENT '用户名' ,\n" +
                "`create_at` datetime NULL DEFAULT NULL COMMENT '创建时间' ,\n" +
                "PRIMARY KEY (`id`),\n" +
                "INDEX `IDX_ID` (`username`) USING BTREE\n" +
                ")\n" +
                "ENGINE=InnoDB\n" +
                "DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci;\n" +
                "\n" +
                "\n" +
                "CREATE TABLE `table2` (\n" +
                "`skuid`  bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id' ,\n" +
                "`name`  varchar(255) NULL DEFAULT NULL COMMENT '用户名' ,\n" +
                "`create` datetime NULL DEFAULT NULL COMMENT '创建时间' ,\n" +
                "PRIMARY KEY (`skuid`)\n" +
                ")\n" +
                "ENGINE=InnoDB\n" +
                "DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci;",null);


        System.out.println(tidy("select table1.*,table2.* from table1, table2 where id = 1", tables));
        System.out.println(tidy("select `table1`.*,table2.* from table1,table2 where id = 1", tables));
        System.out.println(tidy("select table1.*,`table2`.* from `table1`,table2 where id = 1", tables));
        System.out.println(tidy("select `table1`.*,`table2`.* from table1,table2 where id = 1", tables));
        System.out.println(tidy("select table1.* from table1,table2 where id = 1", tables));
        System.out.println(tidy("select * from table1,table2 where id = 1", tables));
        System.out.println(tidy("select * from `table1` where id = 1", tables));
        System.out.println(tidy("select *,table1.* from `table1`,table2 where id = 1", tables));
        System.out.println(tidy("select table1.*,table2.www from `table1`, table2 where id = 1", tables));
        System.out.println(tidy("select t1.*,table2.www from table1 as t1 left join table2 WHERE id = 1", tables));
        System.out.println(tidy("select t1.*,table2.www from table1 as t1 left join table2 on id = 1", tables));
        System.out.println(tidy("select t1.*,table2.www from `table1` as t1 left join table2 on id = 1", tables));
        System.out.println(tidy("select table1.aaa,table2.www from table1,table2 where id = 1", tables));

    }
    */

}
