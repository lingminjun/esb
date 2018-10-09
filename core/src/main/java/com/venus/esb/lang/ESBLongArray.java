package com.venus.esb.lang;


import com.venus.esb.annotation.ESBDesc;

/**
 * Created by lingminjun on 17/4/22.
 */
@ESBDesc("long数组类型")
public class ESBLongArray extends ESBResultWrapper {
    private static final long serialVersionUID = 6007922982960225023L;

    @ESBDesc("值")
    public long[] value;

    @Override
    public void setValue(Object value) throws ESBException {
        if (long[].class == value.getClass()) {
            this.value = (long[])value;
        } else if (Long[].class == value.getClass()) {
            Long[] bary = (Long[])value;
            this.value = new long[bary.length];
            for (int i = 0; i < bary.length; i++) {
                this.value[i] = ESBT.longInteger(bary[i]);
            }
        } else if (int[].class == value.getClass()) {
            int[] bary = (int[])value;
            this.value = new long[bary.length];
            for (int i = 0; i < bary.length; i++) {
                this.value[i] = ESBT.integer(bary[i]);
            }
        } else if (Integer[].class == value.getClass()) {
            Integer[] bary = (Integer[])value;
            this.value = new long[bary.length];
            for (int i = 0; i < bary.length; i++) {
                this.value[i] = ESBT.integer(bary[i]);
            }
        }  else {
            throw ESBExceptionCodes.PARSE_ERROR("解析long[]类型不对,输入类型"+value.getClass());
        }
    }

    public void setValue(long[] value) {
        this.value = value;
    }
}
