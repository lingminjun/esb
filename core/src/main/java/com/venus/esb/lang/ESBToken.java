package com.venus.esb.lang;

import com.venus.esb.annotation.ESBDesc;

import java.io.Serializable;

/**
 * Created by lingminjun on 17/4/28.
 * 标准的Authorization
 *
 * 以下对于token各个字段说明：
 * 1、token(含私钥)，每次请求提交需携带，客户端能够访问（httpOnly=false & secure=false）
 * 2、stoken(含私钥，含token验证信息)，web/h5每次请求需携带,客户端不能够访问（httpOnly=true & secure=true），sso场景必须携带（考虑到token不安全容易篡改）
 * 3、refresh(含私钥，含token验证信息)，只有在刷新token时传输，客户端保存（浏览器不建议有次功能）
 * 4、issu_key(颁发公钥)，永不传输，用于客户端加签请求，可防止csrf攻击（简单的）
 *
 * 注意：token参数有一部分是可以注入的，若服务返回ESBToken类型时，自动注入
 *
 * 其他说明：刷新token时不会返回user和ext信息，此类信息由原服务第一次登录返回
 */
@ESBDesc("令牌认证类型")
public final class ESBToken implements Serializable {

    private static final long serialVersionUID = 6546433347733366134L;

    @ESBDesc("认证是否成功")
    public boolean success;

    @ESBDesc(value = "access_token：表示访问令牌，必选项。将被写入到cookie，js可读（httpOnly=false）",entrust = true)
    public String token;

    // secret_token/csrf_token:保护token加强校验，跨域免登(sso)会更换csrftoken，免登场景必须上传secret_token | httpOnly=true&secure=true意味着域名绑定，无法伪造
    @ESBDesc(value = "secret_token: 用于在不同domain间传递csrftoken, 只能在https(secure=true)传入, 将被写入到cookie，js不可读（httpOnly=true）",entrust = true)
    public String stoken;

    @ESBDesc(value = "refresh_token：用于刷新access_token（同理会刷新secret_token），可选项，不写入cookie，客户端保留好。",entrust = true)
    public String refresh;

    @ESBDesc(value = "issu_key：颁发公钥，加签秘钥（私钥保存在token中），必选项，不写入cookie，客户端保留好。",entrust = true)
    public String key;

    @ESBDesc(value = "expires_in：表示过期时间，单位为秒。如果省略该参数，必须其他方式设置过期时间。",entrust = true)
    public long expire;

    @ESBDesc("scope：表示权限范围，如果与客户端申请的范围一致，此项可省略。推荐【global,user,device,token,temporary】,可自定义")
    public String scope;

    @ESBDesc("设备did")
    public String did;

    @ESBDesc("用户id")
    public String uid;

    @ESBDesc("用户信息")
    public String user;

    @ESBDesc("额外参数")
    public ESBExts exts; //额外的key value计算

    public void putExt(String key, String value) {
        if (key != null && key.length() > 0 && value != null) {
            if (exts == null) {
                exts = new ESBExts();
            }
            exts.put(key,value);
        }
    }

    public String getExt(String key) {
        if (exts != null && key != null && key.length() > 0) {
            return exts.get(key);
        }
        return null;
    }

    public void removeExt(String key) {
        if (exts != null && key != null && key.length() > 0) {
            exts.remove(key);
        }
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setStoken(String stoken) {
        this.stoken = stoken;
    }

    public void setRefresh(String refresh) {
        this.refresh = refresh;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setExpire(long expire) {
        this.expire = expire;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setExts(ESBExts exts) {
        this.exts = exts;
    }
}
