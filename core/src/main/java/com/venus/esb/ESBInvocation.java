package com.venus.esb;

import com.alibaba.fastjson.annotation.JSONField;
import com.venus.esb.lang.ESBConsts;
import com.venus.esb.lang.ESBField;
import com.venus.esb.utils.MD5;

import java.io.Serializable;

/**
 * Created by lingminjun on 17/8/10.
 * 最终实现RPC基础类
 */
public final class ESBInvocation implements Serializable {

    private static final long serialVersionUID = -2784657834930449742L;

    //invoke基本描述
    public String protocol;//Dubbo、Http、其他自定义协议:如利用netty、tcp封装的自定义协议
    public String scheme;//协议头
    public String serverName;//服务名
    public int serverPort;//服务端口
    public String methodName;//调用方法

    //扩充调用方式说明：
    //1、protocol=Dubbo时，理论有如下方式
    //      a: generic(默认，忽略) ；
    //      b: dubbo://10.10.1.1：20880/com.xxx.xxx.sell.TestService ；//实际映射配置如<dubbo:referenceid="testService"interface="com.xxx.xxx.sell.TestService" group="A"url="dubbo://10.10.1.1：20880/com.xxx.xxx.sell.TestService"timeout="3000"/>
    //2、protocol=Http时，主要是get(默认),post,delete,put,[option,trace,connect](网关基本不支持)
    public String method;   //调用方式，

    public String version;//接口版本
    public int timeout = 30000;//默认30秒
    public int retries;//不重试

    public String encoding;//编码方式 utf8、asii、传输内容解码方式
    public String serialization = ESBConsts.JSON;//返回值序列化方式 json、xml、binary;

    public ESBField[] paramTypes;//参数类型列表


    /**
     * 拼接一个唯一表示key,作为RPC实现的uuid
     * scheme://serverName:serverPort/methodName;version?paramTypes
     * @return
     */
    @JSONField(serialize = false, deserialize = false)
    public String getURI() {
        StringBuilder builder = new StringBuilder();
        builder.append(scheme.toLowerCase());//不区分大小写
        builder.append("://");
        builder.append(serverName.toLowerCase());//不区分大小写
        builder.append(":");
        builder.append(serverPort);
        builder.append("/");
        builder.append(methodName);
        if (version != null) {
            builder.append(";" + version);
        }
        if (paramTypes != null && paramTypes.length > 0) {
            builder.append("?");
            for (int i = 0; i < paramTypes.length; i++) {
                ESBField param = paramTypes[i];
                if (i != 0) {
                    builder.append("&");
                }
                builder.append("var" + i + "=" + param.getFinalType());
            }
        }
        return builder.toString();
    }

    /**
     * 拼接一个唯一表示key,作为RPC实现的uuid
     * scheme://serverName:serverPort/methodName;version?paramTypes&timeout=timeout&retries=retries
     * @return
     */
    @JSONField(serialize = false, deserialize = false)
    public String getURL() {

        StringBuilder builder = new StringBuilder();
        builder.append(getURI());
        if (paramTypes == null || paramTypes.length == 0) {
            builder.append("?");
        } else {
            builder.append("&");
        }
        builder.append("timeout="+timeout + "&retries="+retries);

        return builder.toString();
    }

    public String getMethod() {
        if (protocol != null && protocol.contains("dubbo")) {
            if (method != null && method.length() > 0) {
                return method;
            } else {
                return "generic";
            }
        } else {
            if (method != null && method.length() > 0) {
                return method;
            } else {
                return "get";
            }
        }
    }

    /**
     * (protocol uri).md5
     * 返回唯一id
     * @return
     */
    @JSONField(serialize = false, deserialize = false)
    public String getMD5() {
        return MD5.md5(getMethod() + " " + protocol.toLowerCase() + " " + getURI());
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("\turi: " + getURI() + "&timeout="+timeout + "&retries="+retries+"\n");
        builder.append("\tmd5: " + getMD5()+"\n");
        builder.append("\tencoding: " + encoding+"\n");
        builder.append("\tserialization: " + serialization+"\n");
        builder.append("\r\n");
        builder.append("\tObject ");
        builder.append(serverName + "." + methodName + "(");
        if (paramTypes != null) {
            for (int i = 0; i < paramTypes.length; i++) {
                ESBField param = paramTypes[i];
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append(param.getDisplayType() + " " + (param.name != null ? param.name : "var" + i));
                if (param.defaultValue != null && param.defaultValue.length() > 0) {
                    builder.append(" = " + param.defaultValue);
                }
            }
        }
        builder.append(");\r\n");

        return builder.toString();
    }

}
