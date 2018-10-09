package com.venus.esb.lang;

import com.venus.esb.annotation.ESBDesc;

import java.io.Serializable;

/**
 * 客户端设备指纹信息
 * 完全兼容老版本网关的CallerInfo
 *
 * @author lingminjun
 */
@ESBDesc("客户端信息")
public final class ESBSecur implements Serializable {

    private static final long serialVersionUID = 2604737085290383159L;

    // 参与token计算
    @ESBDesc("application id")
    public int    aid;//应用id
    @ESBDesc("认证方式")
    public int    securityLevel;
    @ESBDesc("过期时间,单位秒")
    public long   expire;
    @ESBDesc("设备id")
    public long   did;//设备id
    @ESBDesc("user id")
    public long   uid;//用户id
    @ESBDesc("设备公钥(下发客服端),夹带在token中")
    public String key;                      // 设备身份公钥
    /*
    @Deprecated
    @ESBDesc(value = "三方id,完全可以忽略了,仅仅兼容老版本",ignore = true)
    public String oauthid;                  //三方给予的code
    */
    @ESBDesc("设备指纹")
    public String dna;//设备指纹信息 genetic fingerprint

    public void setAid(int aid) {
        this.aid = aid;
    }

    public void setSecurityLevel(int securityLevel) {
        this.securityLevel = securityLevel;
    }

    public void setExpire(long expire) {
        this.expire = expire;
    }

    public void setDid(long did) {
        this.did = did;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setDna(String dna) {
        this.dna = dna;
    }
}
