package com.venus.esb.lang;


import com.venus.esb.annotation.ESBDesc;

/**
 * Created by lingminjun on 17/4/22.
 */
@ESBDesc("boolean数组类型")
public class ESBBooleanArray extends ESBResultWrapper {
    private static final long serialVersionUID = 3789506240877925157L;

    @ESBDesc("值")
    public boolean[] value;

    @Override
    public void setValue(Object value) throws ESBException {
        if (boolean[].class == value.getClass()) {
            this.value = (boolean[])value;
        } else if (Boolean[].class == value.getClass()) {
            Boolean[] bary = (Boolean[])value;
            this.value = new boolean[bary.length];
            for (int i = 0; i < bary.length; i++) {
                this.value[i] = ESBT.bool(bary[i]);
            }
        } else {
            throw ESBExceptionCodes.PARSE_ERROR("解析boolean[]类型不对,输入类型"+value.getClass());
        }
    }

    // spring mvc 数据注入必须
    public void setValue(boolean[] value) {
        this.value = value;
    }
}
