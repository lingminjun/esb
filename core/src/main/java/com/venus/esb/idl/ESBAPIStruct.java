package com.venus.esb.idl;

import com.venus.esb.annotation.ESBDesc;

import java.io.Serializable;

/**
 * Created by lingminjun on 17/8/11.
 * 用于ESB接口参数返回值对象描述(IDL一部分)
 */
@ESBDesc("对象结构描述")
public final class ESBAPIStruct implements Serializable {

    private static final long serialVersionUID = 1312133579930085713L;

    @ESBDesc("结构类型")
    public String type;//类型 java中尽量取包名全称, 自行配置,给自定义报名
    @ESBDesc("结构说明")
    public String desc;//对象描述

    @ESBDesc("属性列表")
    public ESBAPIParam[] fields; //属性列表

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("struct{type:"+type+",desc"+desc+",");
        builder.append("fields[");
        if (fields != null) {
            for (int i = 0; i < fields.length; i++) {
                ESBAPIParam field = fields[i];
                if (i > 0) {
                    builder.append(",");
                }
                builder.append(field.getDisplayType());
            }
        }
        builder.append("]");
        return "";
    }
}
