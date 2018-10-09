package com.venus.esb.lang;

import com.venus.esb.annotation.ESBDesc;

import java.io.Serializable;

/**
 * Created by lingminjun on 17/4/28.
 * 用于记录设备唯一性校验，其token签署了 did+ua+dna+imei+idfa+idfv等等
 *
 * 注意：token参数有一部分是可以注入的，若服务返回ESBDeviceToken类型时，自动注入
 */
@ESBDesc("令牌认证类型")
public final class ESBDeviceToken implements Serializable {

    private static final long serialVersionUID = 6546433347733366134L;

    @ESBDesc("认证是否成功")
    public boolean success;

    @ESBDesc(value = "device token", entrust = true)
    public String token;

    @ESBDesc(value = "device issu key", entrust = true)
    public String key;

    @ESBDesc("scope：表示权限范围，如果与客户端申请的范围一致，此项可省略。推荐【global,user,device,token,temporary】,可自定义")
    public String scope;

    @ESBDesc("设备did")
    public String did;

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setDid(String did) {
        this.did = did;
    }
}
