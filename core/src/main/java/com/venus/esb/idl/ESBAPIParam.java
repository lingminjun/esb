package com.venus.esb.idl;

import com.alibaba.fastjson.annotation.JSONField;
import com.venus.esb.annotation.ESBDesc;
import com.venus.esb.lang.ESBT;

import java.io.Serializable;

/**
 * Created by lingminjun on 17/8/11.
 * 用于ESB接口参数描述(IDL一部分)
 */
@ESBDesc("参数描述")
public final class ESBAPIParam implements Serializable {

    private static final long serialVersionUID = 171493272155466671L;

    @ESBDesc("参数类型")
    public String type;//参数类型描述
    @ESBDesc("参数名称")
    public String name;//参数名称
    @ESBDesc("参数描述")
    public String desc;//参数描述
    @ESBDesc("是否为必须参数")
    public boolean required;//是否必传
    @ESBDesc("参数默认值")
    public String defaultValue;//默认值【必传时忽略默认值】
    @ESBDesc("是array形式（包括list）")
    public boolean isArray;//数组支持,标准接口定义,只需要一种形式

    @JSONField(serialize = false, deserialize = false)
    public String getCoreType() {return ESBT.convertCoreType(type);}

    @JSONField(serialize = false, deserialize = false)
    public String getFinalType() {
        return ESBT.convertFinalType(type,false,isArray);
    }

    @JSONField(serialize = false, deserialize = false)
    public String getDeclareType() {
        return ESBT.convertDeclareType(getFinalType());
    }

    @JSONField(serialize = false, deserialize = false)
    public String getDisplayType() {
        return ESBT.convertDisplayType(getFinalType());
    }

    @Override
    public String toString() {
        return "param{type:"+type+",name:"+name+",desc"+desc+",isArray:"+isArray
                +",required:"+required+",default:"+defaultValue+"}";
    }
}
