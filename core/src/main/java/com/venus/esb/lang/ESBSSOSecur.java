package com.venus.esb.lang;

import com.venus.esb.annotation.ESBDesc;

import java.io.Serializable;

/**
 * Created by lingminjun on 17/5/11.
 * 跨域分为scheme和host的跨域
 * 跨域主要是通过from的token后生产一个一次性token给客服端
 */
@ESBDesc("跨域安全信息")
public final class ESBSSOSecur implements Serializable {

    private static final long serialVersionUID = 5845355812508340798L;

    @ESBDesc("过期时间")
    public long   expire;

    @ESBDesc("user id")
    public long uid;//用户id

    @ESBDesc("to 设备id")
    public long   tdid;//设备id

//    @ESBDesc("from aid")
//    public int   faid;//起始id

    @ESBDesc("to aid")
    public int   taid;//目标id

//    @ESBDesc("to scheme")
//    public String tscheme;

    @ESBDesc("to domain")
    public String tdomain;

//    @ESBDesc("额外参数,需要返回给客户端的数据")
//    public ESBExts fexts; //额外的key value计算


    public void setExpire(long expire) {
        this.expire = expire;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public void setTdid(long tdid) {
        this.tdid = tdid;
    }

    public void setTaid(int taid) {
        this.taid = taid;
    }

    public void setTdomain(String tdomain) {
        this.tdomain = tdomain;
    }
}
