package com.venus.esb.lang;

import com.venus.esb.annotation.ESBDesc;

/**
 * Created by lingminjun on 17/4/22.
 */
@ESBDesc("float类型")
public class ESBFloat extends ESBResultWrapper {
    private static final long serialVersionUID = 2878450218561387527L;

    @ESBDesc("值")
    public float value;

    @Override
    public void setValue(Object value) throws ESBException {
        if (Float.class == value.getClass()) {
            this.value = ((Float)value).floatValue();
        } else {
            throw ESBExceptionCodes.PARSE_ERROR("解析float类型不对,输入类型"+value.getClass());
        }
    }

    public void setValue(float value) {
        this.value = value;
    }
}
