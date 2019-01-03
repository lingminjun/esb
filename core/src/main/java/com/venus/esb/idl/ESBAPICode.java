package com.venus.esb.idl;

import com.venus.esb.annotation.ESBDesc;

import java.io.Serializable;

/**
 * Created by lingminjun on 17/8/11.
 * 用于ESB接口返回码描述(idl一部分)
 */
@ESBDesc("错误码描述")
public final class ESBAPICode implements Serializable {
    private static final long serialVersionUID = -3231070107106910462L;

    @ESBDesc("错误码")
    public int code;        //错误码
    @ESBDesc("错误码所在域")
    public String domain;   //所属域名

    @ESBDesc("错误码名称")
    public String name;     //错误名称
    @ESBDesc("错误码说明")
    public String desc;     //错误码描述

    @Override
    public int hashCode() {
        return ("" + domain + "." + code).hashCode();
    }

    public final String getCodeId() {
        return  "" + domain + "." + code;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {return true;}
        if (obj instanceof ESBAPICode) {
            return this.code == ((ESBAPICode) obj).code && (this.domain != null ? this.domain.equals(((ESBAPICode) obj).domain) : false);
        }
        return false;
    }

    @Override
    public String toString() {
        return "error{domain:" +domain+ ",code:"+ code+",desc:"+desc+"}";
    }
}
