package com.venus.esb.lang;


import com.venus.esb.annotation.ESBDesc;

/**
 * Created by lingminjun on 17/4/22.
 */
@ESBDesc("String类型")
public class ESBString extends ESBResultWrapper {

    private static final long serialVersionUID = -7499491788148343061L;

    @ESBDesc("值")
    public String value;

    @Override
    public void setValue(Object value) throws ESBException {
        if (value instanceof String) {
            this.value = (String)value;
        } else {
            throw ESBExceptionCodes.PARSE_ERROR("解析String类型不对,输入类型"+value.getClass());
        }
    }

    public void setValue(String value) {
        this.value = value;
    }
}
