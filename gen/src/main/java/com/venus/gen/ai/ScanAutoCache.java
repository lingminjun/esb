package com.venus.gen.ai;

import com.venus.esb.lang.ESBT;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: lingminjun
 * Date: 2019-04-29
 * Time: 2:34 PM
 */
public final class ScanAutoCache {
    private static final Pattern AUTO_CACHE = Pattern.compile("\\s*@\\s*AutoCache\\s*\\(");
    private static final Pattern METHOD_DEFINITION = Pattern.compile("\n\\s*public\\s+[\\w\\$]{1}[\\w\\$\\.]*\\s+[\\w\\$]{1}[\\w\\$\\.]*\\s*\\(");

    // 光正则暂时不好检查被注释情况（此处逻辑必须配合正则 AUTO_CACHE 使用）
    private static boolean isCommentOut(String content, int idx, String group) {
        int prx = idx;
        int cidx = group.indexOf("@");
        boolean newline = group.substring(0,cidx).contains("\n");

        char c1 = ' ';
        char c2 = ' ';
        do {
            c1 = content.charAt(prx-2);
            c2 = content.charAt(prx-1);
            if (c1 == '*' && c2 == '/') {//首先检查到结束注释，则停止
                return false;
            } else if (c1 == '/' && c2 == '*') {//当做被注释
                return true;
            } else if (!newline && (c1 == '/' && c2 == '/')) {//必须检查 没有换行
                return true;
            }
            prx--;
        } while (prx > 0 && c2 != '\n');
        return false;
    }


    public static Set<String> scanAutoCache(String content) {
        Set<String> methods = new HashSet<String>();

        if (ESBT.isEmpty(content)) {
            return methods;
        }

        // 仅仅检查被启用auto cache的项目
        // @AutoCache(key = "@foreach(item : #{models}) => 'SHOP_#{item.shopId}'", evict = true)
        Matcher matcher = AUTO_CACHE.matcher(content);
        while (matcher.find()) {
            int idx = matcher.start();
            int edx = matcher.end();

            //检查是否被注释
            boolean commentOut = isCommentOut(content,idx,matcher.group());
            if (commentOut) {//注释就不看了
                continue;
            }

            // 往后检查确定method
            String sub = content.substring(edx,content.length());
            Matcher m = METHOD_DEFINITION.matcher(sub);
            if (m.find()) {
                String group = m.group();
                group = group.substring(0,group.length() - 1);// 去掉括号
                String[] ss = group.split("\\s+");
                String methodName = ss[ss.length - 1];

//                System.out.println(methodName);
                if (!ESBT.isEmpty(methodName)) {
                    methods.add(methodName);
                }
            }
        }

        return methods;
    }
}
