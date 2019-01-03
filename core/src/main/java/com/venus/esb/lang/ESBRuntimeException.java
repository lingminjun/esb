package com.venus.esb.lang;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.fastjson.annotation.JSONField;
import com.venus.esb.l10n.ESBI18N;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Created by lingminjun on 17/2/7.
 * rpc过程所有错误形式定义,兼容网关1.0 AbstractReturnCode数据
 */
public final class ESBRuntimeException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = -4533417417926672855L;

    private final int code;   //错误码
    private final String msg; //错误描述,用于展示, 多语言支持
    private String l10n; //本地化展示
    private final String domain;//错误域
    private transient String reason;    //错误原因描述,真实原因
    private transient Throwable inner;  //内部错误
    private boolean isExposed = true;//server的异常是否要暴露给esb使用端

    @JSONField(serialize = false, deserialize = false)
    private transient HashMap<String,Object> info;//携带的自定义参数

    @JSONField(serialize = false, deserialize = false)
    private transient String codeId;  //用于esb唯一id描述

    @JSONField(serialize = false, deserialize = false)
    public transient String name;//异常码名字,仅仅为文档需要使用,意义不是很大

    public ESBRuntimeException(String message, String domain, int code, Throwable cause) {
        super(message, cause);
        this.msg = message;
        this.l10n = ESBI18N.l10n(message);
        this.code = code;
        this.domain = domain;
        this.inner = cause;
        this.reason = cause != null ? cause.getMessage() : message;
        this.codeId = domain+"."+code;
    }

    public ESBRuntimeException(String message, String domain, int code, String reason) {
        super((reason != null && reason.length() > 0) ? reason : message);
        this.msg = message;
        this.l10n = ESBI18N.l10n(message);
        this.code = code;
        this.domain = domain;
        this.reason = reason != null ? reason : message;
        this.codeId = domain+"."+code;
    }

    public final String getCodeId() {
        return codeId;
    }

    public int getCode() {
        return code;
    }

    public final ESBException getException() {
        if (this.reason != null) {
            return new ESBException(this.msg, this.domain, this.code, this.reason);
        } else {
            return new ESBException(this.msg, this.domain, this.code, this.inner);
        }
    }

    @Override
    public String getMessage() {
        String m = super.getMessage();
        if (m != null && m.length() > 0) {
            return m;
        }
        if (reason != null && reason.length() > 0) {
            return reason;
        }
        return msg;
    }

    /**
     * 本地化文案返回
     * @return
     */
    @Override
    public String getLocalizedMessage() {
        return l10n != null ? l10n : msg;
    }

    public String getDomain() {
        return domain;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setLocalizedMessage(String l10nMsg) {
        this.l10n = l10nMsg;
    }
    /**
     * 日志时,可以看core cause内容来定位问题
     * @return
     */
    public Throwable getCoreCause() {
        return inner != null ? inner : this;
    }

    public ESBRuntimeException setCoreCause(Throwable inner) {
        this.inner = inner;
        //内置cause原因,打印日志时可以将真实原因打印全
        if (getCause() == null && inner != null) {
            try {
                Field cause = Throwable.class.getDeclaredField("cause");
                cause.setAccessible(true);
                cause.set(this,inner);
            } catch (Throwable e) {}
        }
        return this;
    }

    public final HashMap<String, Object> getUserInfo() {
        return info;
    }

    public final ESBRuntimeException addUserInfo(String key, Object obj) {
        if (info == null) {
            info = new HashMap<String,Object>();
        }

        if (StringUtils.isEmpty(key)) {
            return this;
        }

        if (obj == null) {
            info.remove(key);
        } else {
            info.put(key,obj);
        }

        return this;
    }

    public final void removeUserInfo(String key) {
        if (info == null) {
            return;
        }
        if (StringUtils.isEmpty(key)) {
            return;
        }
        info.remove(key);
    }

    public boolean isExposed() {
        return isExposed;
    }

    public void setExposed(boolean exposed) {
        isExposed = exposed;
    }

    @Override
    public String toString() {
        return "ESBException{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", l10n='" + l10n + '\'' +
                ", domain='" + domain + '\'' +
                ", reason='" + reason + '\'' +
                ", exposed='" + isExposed + '\'' +
                '}';
    }
}
