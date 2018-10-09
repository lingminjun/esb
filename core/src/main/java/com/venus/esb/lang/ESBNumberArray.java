package com.venus.esb.lang;


import com.venus.esb.annotation.ESBDesc;

/**
 * Created by lingminjun on 17/4/22.
 * 数值型返回值，包含byte, char, short, int
 */
@ESBDesc("int数组类型")
public class ESBNumberArray extends ESBResultWrapper {
    private static final long serialVersionUID = 7304912395341184154L;

    @ESBDesc("值")
    public int[] value;

    @Override
    public void setValue(Object value) throws ESBException {
        if (byte[].class == value.getClass()) {
            byte[] bs = (byte[])value;
            this.value = new int[bs.length];
            for (int i = 0; i < bs.length; i++) {
                this.value[i] = bs[i];
            }
        } else if (Byte[].class == value.getClass()) {
            Byte[] bary = (Byte[])value;
            this.value = new int[bary.length];
            for (int i = 0; i < bary.length; i++) {
                this.value[i] = ESBT.byteNumber(bary[i]);
            }
        } else if (char[].class == value.getClass()) {
            char[] bs = (char[])value;
            this.value = new int[bs.length];
            for (int i = 0; i < bs.length; i++) {
                this.value[i] = bs[i];
            }
        } else if (Character[].class == value.getClass()) {
            Character[] bary = (Character[])value;
            this.value = new int[bary.length];
            for (int i = 0; i < bary.length; i++) {
                this.value[i] = ESBT.character(bary[i]);
            }
        } else if (short[].class == value.getClass()) {
            short[] bs = (short[])value;
            this.value = new int[bs.length];
            for (int i = 0; i < bs.length; i++) {
                this.value[i] = bs[i];
            }
        } else if (Short[].class == value.getClass()) {
            Short[] bary = (Short[])value;
            this.value = new int[bary.length];
            for (int i = 0; i < bary.length; i++) {
                this.value[i] = ESBT.shortInteger(bary[i]);
            }
        } else if (int[].class == value.getClass()) {
            this.value = (int[])value;
        } else if (Integer[].class == value.getClass()) {
            Integer[] bary = (Integer[])value;
            this.value = new int[bary.length];
            for (int i = 0; i < bary.length; i++) {
                this.value[i] = ESBT.integer(bary[i]);
            }
        } else {
            throw ESBExceptionCodes.PARSE_ERROR("解析int[]类型不对,输入类型"+value.getClass());
        }
    }

    public void setValue(int[] value) {
        this.value = value;
    }
}
