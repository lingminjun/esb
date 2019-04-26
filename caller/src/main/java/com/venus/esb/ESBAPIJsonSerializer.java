package com.venus.esb;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.venus.esb.lang.ESBConsts;
import com.venus.esb.lang.ESBException;
import com.venus.esb.lang.ESBExceptionCodes;
import com.venus.esb.lang.ESBT;

import java.util.ArrayList;

/**
 * Created by lingminjun on 17/4/13.
 */
public final class ESBAPIJsonSerializer extends ESBAPISerializer {

    @Override
    public String serialized(Object pojo) throws ESBException {
        //dubbo自身支持
        if (pojo instanceof String) {//RawString
            return (String)pojo;
        } else if (pojo instanceof com.alibaba.dubbo.common.json.JSONArray) {
            try {
                return com.alibaba.dubbo.common.json.JSON.json(pojo);
            } catch (Throwable e) {
                throw ESBExceptionCodes.SERIALIZE_FAILED("序列化数据出错").setCoreCause(e);
            }
        } else if (pojo instanceof com.alibaba.dubbo.common.json.JSONObject) {
            try {
                return com.alibaba.dubbo.common.json.JSON.json(pojo);
            } catch (Throwable e) {
                throw ESBExceptionCodes.SERIALIZE_FAILED("序列化数据出错").setCoreCause(e);
            }
        }
        return JSON.toJSONString(pojo, ESBConsts.FASTJSON_SERIALIZER_FEATURES);
    }

    @Override
    public Object deserialized(String data,String type,boolean isList) {
        if (data == null) {
            return null;
        }

        if (type != null && ESBT.isBaseType(type)) {
            return ESBT.value(data,ESBT.classForName(type));
        }

        Object obj = JSON.parse(data);
//        if (obj instanceof JSONObject && type != null) {
//            ((JSONObject) obj).put("class",type);
//        }

        if (isList && !(obj instanceof JSONArray)) {
            ArrayList list =  new ArrayList();
            list.add(obj);
            return list;
        }

        return obj;
    }

}
