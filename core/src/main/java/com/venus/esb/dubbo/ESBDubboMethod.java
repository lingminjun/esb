package com.venus.esb.dubbo;

import com.venus.esb.ESBInvocation;
import com.venus.esb.lang.ESBConsts;
import com.venus.esb.lang.ESBField;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lingminjun on 17/4/23.
 */
public final class ESBDubboMethod implements Serializable {
    private static final long serialVersionUID = 888087124025783857L;

    public String dubbo;//服务名
    public String method;//方法
    public String version;//dubbo接口版本
    public int timeout = 30000;//默认30秒
    public int retries;//不重试
//    public long modifyAt;//最后一次编辑时间

    public List<ESBField> params = new ArrayList<ESBField>();//参数列表,顺序一致

    public ESBInvocation getInvocation() {
        ESBInvocation invocation = new ESBInvocation();
        invocation.protocol = "dubbo 4.0.5";
        invocation.scheme = "dubbo";
        invocation.serverName = this.dubbo;
        invocation.serverPort = 20880;
        invocation.methodName = this.method;
        invocation.timeout = this.timeout;
        invocation.retries = this.retries;
        invocation.version = this.version;
        invocation.encoding = ESBConsts.UTF8_STR;
        invocation.serialization = ESBConsts.JSON;

        //参数部分
        invocation.paramTypes = this.params.toArray(new ESBField[0]);

        return invocation;
    }

    /**
     * 实际是要看方法签名,此处简化逻辑,仅仅看名字加参数个数,并不看参数类型,类型配合ESBAPIInfo来用
     * @return
     */
//    public String getUUID() {
//        if ((this.dubbo)) {
//            return "";
//        }
//        //方法签名,
//        return dubbo + "." + method + "_" + (params == null ? 0 : params.length);
//    }

//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj) {return true;}
//        if (obj == null) {return false;}
//        if (ESBDubboMethod.class != obj.getClass()) {return false;}
//        String typeDesc = ((ESBDubboMethod)obj).getUUID();
//        if (typeDesc == null || typeDesc.equals("")) {
//            return false;
//        }
//        return typeDesc.equals(this.getUUID());
//    }
//
//    @Override
//    public int hashCode() {
//        return ("" + ESBDubboMethod.class + this.getUUID()).hashCode();
//    }
}
