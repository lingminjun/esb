package com.venus.esb.lang;

import com.venus.esb.annotation.ESBDesc;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: lingminjun
 * Date: 2018-09-21
 * Time: 下午10:06
 */
@ESBDesc("专门为http定制的cookie")
public class ESBCookie implements Serializable {
    private static final long serialVersionUID = 2604737085290383159L;

    @ESBDesc("名称key")
    public String name;
    @ESBDesc("具体值")
    public String value;
    @ESBDesc("所属域名")
    public String domain;
    @ESBDesc("有效期，单位为秒。如果没有设置有效期，则默认为-1（或者说小于0的值），表示在浏览器关闭时就删除Cookie内容；设置为0，则表示通知浏览器立即删除这个Cookie")
    public int maxAge = -1;
    @ESBDesc("只支持https")
    public boolean secure;
    @ESBDesc("针对路径filter")
    public String path = "/";
    @ESBDesc("版本")
    public int version = 0;
    @ESBDesc("不允许客服端读写，防XSS和CSRF攻击")
    public boolean httpOnly = false;

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }
}
