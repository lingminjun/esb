package com.venus.esb.lang;


import com.venus.esb.annotation.ESBDesc;

/**
 * Created by lingminjun on 17/4/22.
 */
@ESBDesc("boolean类型")
public final class ESBBoolean extends ESBResultWrapper {
    private static final long serialVersionUID = -9054823490873492068L;

    @ESBDesc("值")
    public boolean value;

    @Override
    public void setValue(Object value) throws ESBException {
        if (Boolean.class == value.getClass()) {
            this.value = ((Boolean)value).booleanValue();
        } else {
            throw ESBExceptionCodes.PARSE_ERROR("解析boolean类型不对,输入类型"+value.getClass());
        }
    }

    // spring mvc 数据注入必须
    public void setValue(boolean value) {
        this.value = value;
    }
}
