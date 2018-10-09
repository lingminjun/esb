package com.venus.esb.lang;


import com.venus.esb.annotation.ESBDesc;

/**
 * Created by lingminjun on 17/4/22.
 */
@ESBDesc("double数组类型")
public class ESBDoubleArray extends ESBResultWrapper {
    private static final long serialVersionUID = -8918859727433003387L;

    @ESBDesc("值")
    public double[] value;

    @Override
    public void setValue(Object value) throws ESBException {
        if (double[].class == value.getClass()) {
            this.value = (double[])value;
        } else if (Double[].class == value.getClass()) {
            Double[] bary = (Double[])value;
            this.value = new double[bary.length];
            for (int i = 0; i < bary.length; i++) {
                this.value[i] = ESBT.doubleDecimal(bary[i]);
            }
        } else if (float[].class == value.getClass()) {
            float[] bary = (float[])value;
            this.value = new double[bary.length];
            for (int i = 0; i < bary.length; i++) {
                this.value[i] = ESBT.doubleDecimal(bary[i]);
            }
        } else if (Float[].class == value.getClass()) {
            Float[] bary = (Float[])value;
            this.value = new double[bary.length];
            for (int i = 0; i < bary.length; i++) {
                this.value[i] = ESBT.doubleDecimal(bary[i]);
            }
        } else {
            throw ESBExceptionCodes.PARSE_ERROR("解析double[]类型不对,输入类型"+value.getClass());
        }
    }

    public void setValue(double[] value) {
        this.value = value;
    }
}
