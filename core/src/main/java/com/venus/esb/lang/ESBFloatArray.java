package com.venus.esb.lang;

import com.venus.esb.annotation.ESBDesc;

/**
 * Created by lingminjun on 17/4/22.
 */
@ESBDesc("float数组类型")
public class ESBFloatArray extends ESBResultWrapper {
    private static final long serialVersionUID = -8918859727433003387L;

    @ESBDesc("值")
    public float[] value;

    @Override
    public void setValue(Object value) throws ESBException {
        if (float[].class == value.getClass()) {
            this.value = (float[])value;
        } else if (Float[].class == value.getClass()) {
            Float[] bary = (Float[])value;
            this.value = new float[bary.length];
            for (int i = 0; i < bary.length; i++) {
                this.value[i] = ESBT.floatDecimal(bary[i]);
            }
        } else {
            throw ESBExceptionCodes.PARSE_ERROR("解析float[]类型不对,输入类型"+value.getClass());
        }
    }

    public void setValue(float[] value) {
        this.value = value;
    }
}
