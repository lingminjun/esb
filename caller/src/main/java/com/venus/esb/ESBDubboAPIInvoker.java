package com.venus.esb;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.venus.esb.ESBAPIInfo;
import com.venus.esb.ESBInvocation;
import com.venus.esb.config.ESBConfigCenter;
import com.venus.esb.dubbo.ESBGenericCaller;
import com.venus.esb.lang.*;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Description: 完成dubbo的泛型调用
 * User: lingminjun
 * Date: 2018-09-27
 * Time: 下午5:00
 */
public final class ESBDubboAPIInvoker {

    public static Object dubboInvoke(ESBAPIInfo info, ESBInvocation invocation, ESBAPIContext context, Map<String, String> params, Map<String, ESBCookie> cookies, int index) throws ESBException {
        //获得序列化的参数
        ESBGenericCaller.MethodParams methodParams;
        try {
            methodParams = convertMethodParams(info, invocation, context, params, cookies, index);
        } catch (Throwable e) {
            throw ESBExceptionCodes.PARAMETER_ERROR("参数无法正确解析").setCoreCause(e);
        }
        return ESBGenericCaller.getInstance().genericInvoke(
                invocation.serverName,
                invocation.methodName,
                methodParams,
                ESBConfigCenter.instance().getDubboVersion(),
                ESBConfigCenter.instance().getDubboTimeout(),
                ESBConfigCenter.instance().getDubboRetries());
    }

    //屏蔽数据转换的差别
    private static ESBAPISerializer getSerializer(ESBInvocation invocation) {
//        if ("json".equals(invocation.serialization)) {
//
//        }
        //暂时就支持这种
        return new ESBAPIJsonSerializer();
    }

    private static ESBGenericCaller.MethodParams convertMethodParams(ESBAPIInfo info, ESBInvocation invocation, ESBAPIContext context, Map<String,String> params, Map<String,ESBCookie> cookies, int index) throws ESBException {
        ESBGenericCaller.MethodParams methodParams = new ESBGenericCaller.MethodParams(invocation.paramTypes.length);

        //参数映射
        Map<Integer, String> map = info.getInject(invocation.getMD5());

        ESBAPISerializer serializer = getSerializer(invocation);

        for (int idx = 0; idx < invocation.paramTypes.length; idx++) {
            ESBField field = invocation.paramTypes[idx];
            if (ESBContext.class.equals(field.type)) {//直接赋值
                methodParams.add(ESBContext.class.getName(), context.toGenericPOJO());
            } else {
                //需要匹配key
                String key = null;

                if (map != null) {
                    key = map.get(idx);
                }

                if (key == null) {
                    key = field.name;
                }

                String value = ESBAPIContext.getRightValue(key, context, params, cookies, index);
                if (value == null && !StringUtils.isEmpty(field.defaultValue)) {
                    value = field.defaultValue;
                }

                // 针对基础数据类型做默认值处理
                if (value == null && (!field.isList && ESBT.isPrimitiveType(field.getDeclareType()))) {
                    value = ESBT.defaultPrimitiveValue(field.getDeclareType());
                }

                //序列化值
                Object obj = serializer.deserialized(value, field.getDeclareType(), field.isList);
                methodParams.add(field.getFinalType(), obj);//以finalType方式
            }
        }


        return methodParams;
    }
}
