package com.venus.esb.lang;


import com.venus.esb.annotation.ESBDesc;

/**
 * Created by lingminjun on 17/4/22.
 */
@ESBDesc("double类型")
public class ESBDouble extends ESBResultWrapper {
    private static final long serialVersionUID = 2878450218561387527L;

    @ESBDesc("值")
    public double value;

    @Override
    public void setValue(Object value) throws ESBException {
        if (Double.class == value.getClass()) {
            this.value = ((Double)value).doubleValue();
        } else if (Float.class == value.getClass()) {
            this.value = ((Float)value).doubleValue();
        } else {
            throw ESBExceptionCodes.PARSE_ERROR("解析double类型不对,输入类型"+value.getClass());
        }
    }

    public void setValue(double value) {
        this.value = value;
    }
}
