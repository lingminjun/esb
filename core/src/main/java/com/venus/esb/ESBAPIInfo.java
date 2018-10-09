package com.venus.esb;

import com.alibaba.fastjson.annotation.JSONField;
import com.venus.esb.idl.ESBAPIDef;
import com.venus.esb.lang.ESBField;
import com.venus.esb.utils.DateUtils;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by lingminjun on 17/4/11.
 * 描述一个完整接口和接口实现过程
 */
public final class ESBAPIInfo implements Serializable {
    private static final long serialVersionUID = -4413720531756865222L;

    public ESBAPIDef api;//标准接口

    public boolean mock;//接口打标 [用于扩展意义]

    public long createAt;//创建日期
    public long modifyAt;//修改日期



    //所有关联的 map<invocation.md5,invocation>
    public Map<String,ESBInvocation> invocations;

    //每个invocation参数映射 map<invocation.md5, map<param_index,api_param_key>>
    public Map<String,Map<Integer,String>> inject;

    //每一条规则,后面对应的invocations map<keypath, List<ESBRuleNode>>
    public Map<String,List<ESBRuleNode>> rules;//方法加载规则

    /**
     * 根据invoke
     * @param invokeMD5
     * @return
     */
    @JSONField(serialize = false, deserialize = false)
    public Map<Integer,String> getInject(String invokeMD5) {
        if (inject != null) {
            return inject.get(invokeMD5);
        }
        return null;
    }

    /**
     * 是否为开放平台接口,是必须有pid,否则不和发
     * @return
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isOpenAPI() {
        if (api != null) {
            return this.api.isOpenAPI();
        }
        return false;
    }

//    public String getInvocationParamKey(String invokeMD5, int idx) {
//        //先从map中取
//        Map<Integer,String> map = null;
//        if (inject != null) {
//            map = inject.get(invokeMD5);
//        }
//
//        String key = null;
//        if (map != null) {
//            key = map.get(idx);
//        }
//
//        //仍然没有取到,直接使用invocation中的name
//        ESBInvocation invocation = getInvocation(invokeMD5);
//        if (invocation != null) {
//            key = invocation.paramTypes[idx].name;
//        }
//
//        //若还取不到,没办法了
//        return key;
//    }
//
//    public String getInvocationParamKey(ESBInvocation invocation, int idx) {
//        if (invocation == null) {
//            return null;
//        }
//
//        //先从map中取
//        Map<Integer,String> map = null;
//        if (inject != null) {
//            map = inject.get(invocation.getMD5());
//        }
//
//        String key = null;
//        if (map != null) {
//            key = map.get(idx);
//        }
//
//        //仍然没有取到,直接使用invocation中的name
//        key = invocation.paramTypes[idx].name;
//
//        //若还取不到,没办法了
//        return key;
//    }

    /**
     *
     * @param md5
     * @return
     */
    @JSONField(serialize = false, deserialize = false)
    public ESBInvocation getInvocation(String md5) {
        if (invocations != null) {
            return invocations.get(md5);
        }
        return null;
    }

    @Override
    public String toString() {

        if (api == null) {
            return super.toString();
        }

        StringBuilder builder = new StringBuilder();

        //api部分
        builder.append("API:\n");
        builder.append("\t/**\n");
        builder.append("\t * createAt: " + DateUtils.toYYYY_MM_DD_HH_MM_SS(createAt) + "\n");
        builder.append("\t * modifyAt: " + DateUtils.toYYYY_MM_DD_HH_MM_SS(modifyAt) + "\n");
        builder.append(api.toString());
        builder.append("\r\n");
        builder.append("Invocations:\n");
        if (invocations != null) {
            Iterator<Map.Entry<String, ESBInvocation>> entries = invocations.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, ESBInvocation> entry = entries.next();
                ESBInvocation invoke = entry.getValue();
                builder.append("\turi: " + invoke.getURL()+"\n");
                builder.append("\tmd5: " + invoke.getMD5()+"\n");
                builder.append("\tencoding: " + invoke.encoding+"\n");
                builder.append("\tserialization: " + invoke.serialization+"\n");
                builder.append("\r\n");
                builder.append("\tObject ");
                builder.append(invoke.serverName + "." + invoke.methodName + "(");

                //取mapping
                Map<Integer,String> ijt = getInject(invoke.getMD5());
                if (invoke.paramTypes != null) {
                    for (int i = 0; i < invoke.paramTypes.length; i++) {
                        ESBField param = invoke.paramTypes[i];
                        if (i > 0) {
                            builder.append(", ");
                        }
                        if (ijt != null && ijt.containsKey(i)) {
                            builder.append("@Inject(" + ijt.get(i) + ") ");
                        }
                        builder.append(param.getDisplayType() + " " + (param.name != null ? param.name : "var" + i));
                        if (param.defaultValue != null && param.defaultValue.length() > 0) {
                            builder.append(" = " + param.defaultValue);
                        }
                    }
                }
                builder.append(");\r\n");
            }
        }
        builder.append("\r\n");

        //策略配置部分
        builder.append("Rules:\n");
        if (rules != null) {
            Iterator<Map.Entry<String,List<ESBRuleNode>>> entries = rules.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String,List<ESBRuleNode>> entry = entries.next();
                String path = entry.getKey();
                List<ESBRuleNode> list = entry.getValue();
                if (path != null) {
                    if (path.startsWith("this")) {
                        builder.append("\t" + path + " ==>\n");
                    } else {
                        builder.append("\tthis." + path + " ==>\n");
                    }
                } else {
                    builder.append("\tthis ==>\n");
                }
                for (ESBRuleNode node : list) {
                    builder.append("\t\t" + node.rule + " ==> " + getInvocation(node.invocation).getURL() +"\n");
                }
                builder.append("\n");
            }
        }
        builder.append("\r\n");

        return builder.toString();
    }
}
