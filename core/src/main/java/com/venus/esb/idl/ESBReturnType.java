package com.venus.esb.idl;

import com.venus.esb.annotation.ESBDesc;
import com.venus.esb.lang.ESBT;

import java.io.Serializable;

/**
 * Created by lingminjun on 17/8/11.
 */
@ESBDesc("返回类型描述")
public final class ESBReturnType implements Serializable {
    private static final long serialVersionUID = 645098260044178465L;

    @ESBDesc("返回类型")
    public String type;//参数类型描述
    @ESBDesc("说明")
    public String desc;//参数描述
    @ESBDesc("是否为Array形式（包含list）")
    public boolean isArray;//数组支持,标准接口定义,只需要一种形式

    public String getCoreType() {return ESBT.convertCoreType(type);}

    public String getFinalType() {
        return ESBT.convertFinalType(type,false, isArray);
    }

    public String getDeclareType() {
        return ESBT.convertDeclareType(getFinalType());
    }

    public String getDisplayType() {
        return ESBT.convertDisplayType(getFinalType());
    }

    @Override
    public String toString() {
        return "return{type:"+type+",desc"+desc+",isArray:"+isArray+"}";
    }
}
