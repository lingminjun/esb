package com.venus.esb;

import com.venus.esb.lang.*;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;

/**
 * Created by lingminjun on 17/8/13.
 * 方法选择实现的规则【策略请使用规则来保证】
 */
public final class ESBRule {
    public static final String DEFAULT = "-";//默认规则,也就是没有什么规则
    public static final String PROPORTION_PREFIX = "%";//百分比设置
    public static final String APPOINT_PREFIX = "$";//指定规则
    public static final String SEPARATOR = "|";
    public static final String ESPECIAL_SPLITTER   = new String(new char[] { 2, 3 });
    public static final String THIS_PATH = "this";

    //优先级
    public static final int DEFAULT_SORT_VALUE = 0;
    public static final int PROPORTION_SORT_VALUE = 10;
    public static final int APPOINT_SORT_VALUE = 100;

    //倒序
    public static Comparator RuleComparator = new Comparator<ESBRuleNode>() {
        @Override
        public int compare(ESBRuleNode o1, ESBRuleNode o2) {
            return o2.level - o1.level;//倒序
        }
    };

    /**
     * 比例规则,可用于灰度场景,
     * 比例规则仅仅选用一种参照key
     * @param begin 比例,仅仅取值集合【0,100) 的整数
     * @param end 比例,仅仅取值集合【0,100) 的整数
     * @param key 必须是 @see ESBSTDKeys 中定义的key 或者其 输入必传参数, 实际常用是aid、did、uid、pid
     * @return 不符合构造条件时都返回 DEFAULT
     */
    public static String buildProportion(int begin, int end, String key) {
        if (begin < 0 || begin >= 100 || end < 0 || end >= 100) {
            return DEFAULT;
        }
        if (begin > end) {
            return DEFAULT;
        }

        return PROPORTION_PREFIX + SEPARATOR + begin + SEPARATOR + end + SEPARATOR + key;
    }

    /**
     * 指定值键
     * @param key
     * @param values
     * @return
     */
    public static String buildAppoint(String key, Set<String> values) {
        if (key == null || key.length() == 0 || values == null || values.size() == 0) {
            return DEFAULT;
        }

        StringBuilder builder = new StringBuilder();//不做拆分,直接增加内容
        builder.append(ESPECIAL_SPLITTER);
        for (String v : values) {
            builder.append(v);
            builder.append(ESPECIAL_SPLITTER);
        }
        return APPOINT_PREFIX + SEPARATOR + key + SEPARATOR + builder.toString();
    }

    /**
     * 是否符合规则
     * @param rule 规则
     * @param context header参数不需要再提供,都会在context中提供
     * @param params 参数
     * @param cookies cookie
     * @param index 调用顺序【组合请求时使用】
     * @return
     */
    public static boolean conformToRule(String rule, ESBContext context, Map<String,String> params, Map<String,ESBCookie> cookies, int index) {
        if (rule == null || rule.length() == 0) {return true;}
        if (DEFAULT.equals(rule)) {return true;}

        //先指定
        if (rule.startsWith(APPOINT_PREFIX)) {
            return conformToAppointRule(rule,context,params,cookies,index);
        }

        //百分比
        if (rule.startsWith(PROPORTION_PREFIX)) {
            return conformToProportionRule(rule,context,params,cookies,index);
        }

        return false;
    }

    /**
     * 给予合适的rule
     * @param rule
     * @return
     */
    public static int judgeSortValue(String rule) {
        if (rule == null || rule.length() == 9) {return DEFAULT_SORT_VALUE;}

        if (rule.startsWith(APPOINT_PREFIX)) {
            return APPOINT_SORT_VALUE;
        }

        if (rule.startsWith(PROPORTION_PREFIX)) {
            return PROPORTION_SORT_VALUE;
        }

        return DEFAULT_SORT_VALUE;
    }

    /**
     * 组合最后的root
     * @param root
     * @param path
     * @param child
     * @return
     */
    public static Object assembleResult(Object root, String path, Object child) throws ESBException {
        //raw string直接取value内容
        if (child instanceof ESBRawString) {
            ESBRawString rawString = (ESBRawString) child;
            child = rawString.value;
            ESBContext.getContext().transmit = true; //前置设置isTransmit
            //需要设置contentType
            if (rawString.contentType != null && rawString.contentType.length() > 0) {
                ESBContext.getContext().putExt(ESBContext.TRANSMIT_CONTENT_TYPE_KEY,rawString.contentType);
            }
        }

        if (THIS_PATH.equals(path)) {
            return child;
        }
        if (root == null) {
            return null;
        }

        boolean rt = ESBT.setValueForFieldPath(root,path,child);
        if (!rt) {
            throw ESBExceptionCodes.ILLEGAL_MULTIAPI_ASSEMBLY("组装失败,说明是非法的组合");
//            System.out.println("组装失败,需要抛异常");
        }
        return root;
    }

    private static boolean conformToProportionRule(String rule, ESBContext context, Map<String,String> params, Map<String,ESBCookie> cookies, int index) {
        String[] keys = rule.split("\\|");
        if (keys.length != 4) {
            return false;
        }
        int b = ESBT.integer(keys[1],-1);
        int e = ESBT.integer(keys[2],-1);
        if (b < 0 || e < 0) {
            return false;
        }

        String value = ESBContext.getRightValue(keys[3],context,params,cookies,index);
        if (value == null || value.length() == 0) {
            return false;
        }

        //crc32计算
        CRC32 crc32 = new CRC32();
        crc32.update(value.getBytes());
        long v = crc32.getValue();
        v = v % 100;
        //有问题,必须所有比例瓜分
        return b <= v && e >= v;
    }

    private static boolean conformToAppointRule(String rule, ESBContext context, Map<String,String> params, Map<String,ESBCookie> cookies, int index) {
        String[] keys = rule.split("\\|");
        if (keys.length != 3) {
            return false;
        }

        String value = ESBContext.getRightValue(keys[1],context,params,cookies,index);
        if (value == null || value.length() == 0) {
            return false;
        }

        return keys[2].contains(ESPECIAL_SPLITTER + value + ESPECIAL_SPLITTER);
    }
}
