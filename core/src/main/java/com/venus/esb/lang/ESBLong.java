package com.venus.esb.lang;


import com.venus.esb.annotation.ESBDesc;

/**
 * Created by lingminjun on 17/4/22.
 */
@ESBDesc("long类型")
public class ESBLong extends ESBResultWrapper {
    private static final long serialVersionUID = -1651450767602263495L;

    @ESBDesc("值")
    public long value;

    @Override
    public void setValue(Object value) throws ESBException {
        if (Long.class == value.getClass()) {
            this.value = ((Long)value).longValue();
        } else if (Integer.class == value.getClass()) {
            this.value = ((Integer)value).longValue();
        } else if (Short.class == value.getClass()) {
            this.value = ((Short)value).longValue();
        } else {
            throw ESBExceptionCodes.PARSE_ERROR("解析long类型不对,输入类型"+value.getClass());
        }
    }

    public void setValue(long value) {
        this.value = value;
    }
}
