package com.venus.esb.lang;

import com.alibaba.fastjson.annotation.JSONField;
import com.venus.esb.idl.ESBAPIParam;

import java.io.Serializable;

/**
 * Created by lingminjun on 17/4/12.
 * 用于ESB接口结构属性描述
 */
public final class ESBField implements Serializable {
    private static final long serialVersionUID = 968739920098447637L;

    public String type;//属性元素类型 class 全称
    public String name;//属性名
    public String desc;//描述
    public boolean isArray;//数组支持
    public boolean isList;//ArrayList支持

    @JSONField(serialize = false, deserialize = false)
    private transient Class<?> typeClass;//值,用于

    //记录属性时需要
//    public boolean isInner;
//    public boolean canEntrust;
//
//    //记录参数时需要
//    public boolean required = true;//
//    public boolean autoInjected = false;
    public String defaultValue; //默认值不为空,则认为是必传参数
//    public boolean isQuiet = false;

    @JSONField(serialize = false, deserialize = false)
    private transient Object value;//值,用于

//    @JSONField(serialize = false, deserialize = false)
//    public transient boolean isRequestBody;//值,用于
//    @JSONField(serialize = false, deserialize = false)
//    public transient boolean isPathVariable;//值,用于


    public void setTypeClass(Class typeClass) {
        this.typeClass = typeClass;
        this.type = typeClass.getName();
        this.isArray = typeClass.isArray();
//        this.isList = List.class.isAssignableFrom(typeClass);
    }

    public Class<?> getTypeClass() {
        if (typeClass != null) {
            return typeClass;
        }
        try {
            typeClass = ESBT.classForName(type);
        } catch (Throwable e) {}
        return typeClass;
    }

    @JSONField(serialize = false, deserialize = false)
    public Object getTransientValue() {
        return value;
    }

    @JSONField(serialize = false, deserialize = false)
    public void setTransientValue(Object value) {
        this.value = value;
    }

    @JSONField(serialize = false, deserialize = false)
    public String getCoreType() {return ESBT.convertCoreType(type);}

    /**
     * ESB中java类型标准描述
     * @return
     */
    @JSONField(serialize = false, deserialize = false)
    public String getFinalType() {
        return ESBT.convertFinalType(type,isList,isArray);
    }

    /**
     * 方法申明时需要的类型描述
     * @return
     */
    @JSONField(serialize = false, deserialize = false)
    public String getDeclareType() {
        return ESBT.convertDeclareType(getFinalType());
    }

    /**
     * 展示的java类型描述
     * @return
     */
    @JSONField(serialize = false, deserialize = false)
    public String getDisplayType() {
        return ESBT.convertDisplayType(getFinalType());
    }

    public ESBAPIParam convertParam() {
        ESBAPIParam param = new ESBAPIParam();
        param.type = this.type;
        param.name = this.name;
        param.desc = this.desc;
//        param.required = this.re
        param.defaultValue = this.defaultValue;
        if (this.isList) {
            param.isArray = true;
            param.type = ESBT.packArrayType(this.type);
        } else if (this.isArray || this.type.startsWith("[")) {
            param.isArray = true;
        }
        return param;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {return true;}
        if (obj == null) {return false;}
        if (ESBField.class != obj.getClass()) {return false;}
        String typeDesc = ((ESBField)obj).getFinalType();
        if (typeDesc == null || typeDesc.equals("")) {
            return false;
        }
        return typeDesc.equals(this.getFinalType());
    }

    @Override
    public int hashCode() {
        return ("" + ESBField.class + this.getFinalType()).hashCode();
    }

    public ESBField copy() {
        ESBField field = new ESBField();
        field.type = this.type;//属性元素类型 class 全称
        field.isArray = this.isArray;//数组支持
        field.isList = this.isList;//ArrayList支持
        field.name = this.name;//属性名
        field.desc = this.desc;//描述
//        field.required = this.required;//
//        field.autoInjected = this.autoInjected;
        field.defaultValue = this.defaultValue; //默认值不为空,则认为是比传参数
//        field.isQuiet = this.isQuiet;
        return field;
    }
}
