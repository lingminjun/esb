package com.venus.esb.lang;

import com.venus.esb.annotation.ESBDesc;

import java.io.Serializable;

/**
 * Created by lingminjun on 17/4/28.
 * 标准的Authorization
 *
 */
@ESBDesc(value = "sso令牌认证类型", entrust = true)
public final class ESBSSOToken implements Serializable {
    private static final long serialVersionUID = 8863666855067745215L;

    @ESBDesc(value = "认证是否成功",entrust = true)
    public boolean success;

    @ESBDesc(value = "sso_token：表示访问令牌，必选项。将被写入到cookie",entrust = true)
    public String ssoToken;

    @ESBDesc(value = "expires_in：表示过期时间，单位为秒。如果省略该参数，必须其他方式设置过期时间。",entrust = true)
    public long expire;

    @ESBDesc(value = "额外参数",entrust = true)
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

    public void setSsoToken(String ssoToken) {
        this.ssoToken = ssoToken;
    }

    public void setExpire(long expire) {
        this.expire = expire;
    }

    public void setExts(ESBExts exts) {
        this.exts = exts;
    }
}
