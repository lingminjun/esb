package com.venus.esb;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.venus.esb.ESBAPIInfo;
import com.venus.esb.ESBInvocation;
import com.venus.esb.lang.*;
import com.venus.esb.utils.HTTP;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Description: 完成HTTP的调用，注意参数顺序影响填充 methodName = "/link/get/{name}/{pwd}"这种形式的url
 *              参数填充，优先填充methodName中的参数
 *              get、delete请求，一律表单模式写入，对应参数@RequestParam，@ModelAttribute:则使用get set平铺
 *                  注意，get请不要使用@RequestBody注解
 *                  // application/x-www-form-urlencoded; charset=utf-8
 *              post、put请求，最后一个参数一律采用json写入，前面的参数一律作为query
 *                  // application/json; charset=utf-8
 * User: lingminjun
 * Date: 2018-09-27
 * Time: 下午5:05
 */
public final class ESBHTTPAPIInvoker {


    public static Object httpInvoke(ESBAPIInfo info, ESBInvocation invocation, ESBAPIContext context, Map<String, String> params, Map<String, ESBCookie> cookies, int index) throws ESBException {
        return http(info,invocation,context,params,cookies,index);
    }

    //屏蔽数据转换的差别
    private static ESBAPISerializer getDecodeSerializer(ESBAPIContext context, ESBInvocation invocation) {
        if (((context.contentType == null || context.contentType.length() == 0)
                    && (invocation.serialization == null || invocation.serialization.length() == 0))
                || context.contentType.equalsIgnoreCase(invocation.serialization)) {
            return null;
        }
//        if ("json".equals(context.contentType)) {
//
//        }
        //暂时就支持这种
        return new ESBAPIJsonSerializer();
    }
    private static ESBAPISerializer getEncodeSerializer(ESBInvocation invocation) {
//        if ("json".equals(invocation.serialization)) {
//
//        }
        //暂时就支持这种
        return new ESBAPIJsonSerializer();
    }

    // 解析
    private static ESBAPISerializer getDecodeSerializer(ESBAPIContext context) {
//        if ("json".equals(context.contentType)) {
//
//        }
        //暂时就支持这种
        return new ESBAPIJsonSerializer();
    }


    // get 一律表单
    private static Object http(ESBAPIInfo info, ESBInvocation invocation, ESBAPIContext context, Map<String, String> params, Map<String, ESBCookie> cookies, int index) throws ESBException {

        ESBAPISerializer decode = getDecodeSerializer(context,invocation);
        ESBAPISerializer encode = getEncodeSerializer(invocation);

        //参数映射列表,如果配置了映射关系的话
        Map<Integer, String> map = info.getInject(invocation.getMD5());
        Map<String, ParamDesc> fields = new HashMap<>();
        String lastKey = "";//记录最后一个key，如果post和put时做pojo写入
        for (int idx = 0; idx < invocation.paramTypes.length; idx++) {
            ESBField field = invocation.paramTypes[idx];
            //需要匹配key
            String key = null;

            if (map != null) {
                key = map.get(idx);
            }

            if (key == null) {
                key = field.name;
            }

            String value = ESBAPIContext.getRightValue(key, context, params, cookies, index);
            if (ESBContext.class.equals(field.type)) {//直接赋值
                value = context.toJson();
            } else if (value == null && !StringUtils.isEmpty(field.defaultValue)) {
                value = field.defaultValue;
            }

            //除非必要，否则不传入
            if (value != null) {
                ParamDesc paramDesc = new ParamDesc();
                paramDesc.key = key;
                paramDesc.field = field;

                if (decode != null) {
                    paramDesc.value = encode.serialized(decode.deserialized(value,field.getDeclareType(), field.isList));
                } else {
                    paramDesc.value = value;
                }
                fields.put(key,paramDesc);
            }

            lastKey = key;
        }

        String url = "";
        if (HTTP.Method.POST.name().equalsIgnoreCase(invocation.method)
                || HTTP.Method.PUT.name().equalsIgnoreCase(invocation.method)) {
            url = buildUrl(invocation,context,fields,lastKey);
        } else {//get请求
            url = buildUrl(invocation,context,fields,"");
        }

        //是否添加authority
        String authorization = null;
        if (context.utoken != null && context.utoken.length() > 0) {
            authorization = context.utoken;
        } else if (context.ttoken != null && context.ttoken.length() > 0) {
            authorization = context.ttoken;
        }

        String result = null;
        try {
            if (HTTP.Method.POST.name().equalsIgnoreCase(invocation.method)) {
                ParamDesc paramDesc = fields.get(lastKey);
                String body = null;
                if (paramDesc != null && paramDesc.value != null) {
                    body = paramDesc.value;
                }
                result = HTTP.postJSON(url, body, authorization);
            } else if (HTTP.Method.PUT.name().equalsIgnoreCase(invocation.method)) {
                ParamDesc paramDesc = fields.get(lastKey);
                String body = null;
                if (paramDesc != null && paramDesc.value != null) {
                    body = paramDesc.value;
                }
                result = HTTP.putJSON(url, body, authorization);
            } else if (HTTP.Method.DELETE.name().equalsIgnoreCase(invocation.method)) {
                result = HTTP.delete(url, null, authorization);
            } else {
                result = HTTP.get(url, null, authorization);
            }
        } catch (Throwable e) {
            throw ESBExceptionCodes.INTERNAL_SERVER_ERROR("HTTP服务转调错误").setCoreCause(e);
        }

        ESBAPISerializer fdecode = getDecodeSerializer(context);
        return fdecode.deserialized(result,null,false);
    }

    private static void parseQuery(String queryString, Map<String, String> query) {
        String[] ss = queryString.split("&");
        for (String str : ss) {
            int idx = str.indexOf("=");
            //存在key,value
            if (idx > 0 && idx + 1 < str.length()) {
                query.put(str.substring(0,idx),str.substring(idx+1));
            }
        }
    }

    private static String buildUrl(ESBInvocation invocation, ESBAPIContext context, Map<String, ParamDesc> params, String ignoreKey) throws ESBException {
        StringBuilder builder = new StringBuilder();
        builder.append(invocation.scheme.toLowerCase());//不区分大小写
        builder.append("://");
        builder.append(invocation.serverName.toLowerCase());//不区分大小写
        //默认端口
//        if ((invocation.serverPort == 80 && invocation.scheme.equalsIgnoreCase("http"))
//                || (invocation.serverPort == 443 && invocation.scheme.equalsIgnoreCase("https"))) {
//            //nothing
//        } else {
        builder.append(":");
        builder.append(invocation.serverPort);
//        }

        //截取
        HashMap<String,String> query = new HashMap<>();
        int idx = invocation.methodName.indexOf("?");
        String path = invocation.methodName.trim();
        if (idx >= 0 && idx < invocation.methodName.length()) {
            path = invocation.methodName.substring(0,idx);
            String queryStr =  invocation.methodName.substring(idx + 1);
            //必须保留
            parseQuery(queryStr,query);
        }

        //查找@PathVariable的参数
        int pidx = -1;
        do{
            pidx = path.indexOf("{");
            if (pidx >= 0 && pidx < path.length()) {
                int eidx = path.indexOf("}");
                if (eidx > pidx && eidx < path.length()) {
                    String key = path.substring(pidx + 1, eidx);
                    ParamDesc param = params.get(key);
                    if (param == null || param.value == null || param.value.length() == 0) {
                        throw ESBExceptionCodes.PARAMETER_ERROR("缺少参数" + key);
                    }

                    //是否需要对参数进行编码
                    String value;
                    try {
                        //参数是否需要转换
                        value = URLEncoder.encode(param.value,ESBConsts.UTF8_STR);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        value = param.value;
                    }
                    path = path.replace("{" + key + "}",value);
                } else {
                    break;
                }
            } else {
                break;
            }
        } while (true);

        if (!path.startsWith("/")) {
            builder.append("/");
        }

        builder.append(path);

        //拼接参数
        for (Map.Entry<String,ParamDesc> entry : params.entrySet()) {
            if (entry.getKey().equals(ignoreKey)) {
                continue;
            }

            // 防止覆盖前面const参数：methodName中直接又query,如methodName="/city/{id}?province=湖南"
            if (query.containsKey(entry.getKey())) {
                continue;
            }

            ParamDesc paramDesc = entry.getValue();
            if (paramDesc.value != null && paramDesc.value.length() > 0) {
                String value;
                try {
                    //参数是否需要转换
                    value = URLEncoder.encode(paramDesc.value,ESBConsts.UTF8_STR);
                } catch (Throwable e) {
                    e.printStackTrace();
                    value = paramDesc.value;
                }
                query.put(entry.getKey(),value);
            }
        }

        //拼接query
        String q = HTTP.getFormParamsString(query,ESBConsts.UTF8_STR);
        if (q != null && q.length() > 0) {
            builder.append("?");
            builder.append(q);
        }

        return builder.toString();
    }

    private static class ParamDesc {
        public String key;//参数名
        public String value; //转json
        public ESBField field;
    }
}
