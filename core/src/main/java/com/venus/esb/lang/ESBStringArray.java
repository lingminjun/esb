package com.venus.esb.lang;

import com.venus.esb.annotation.ESBDesc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lingminjun on 17/4/22.
 */
@ESBDesc("String列表类型")
public class ESBStringArray extends ESBResultWrapper {
    private static final long serialVersionUID = 8299930723569109319L;

    @ESBDesc("值")
    public List<String> value;

    @Override
    public void setValue(Object value) throws ESBException {
        if (value instanceof List) {
            List list = (List)value;
            for (Object obj : list) {
                if (String.class != obj.getClass()) {
                    throw ESBExceptionCodes.PARSE_ERROR("解析List<String>类型不对,输入类型List<"+obj.getClass()+">");
                }
            }
            this.value = list;
        } else if (value.getClass() == String[].class) {
            String[] strs = (String[])value;
            this.value = new ArrayList<String>();
            for (String str : strs) {
                if (str != null) {
                    this.value.add(str);
                }
            }
        } else {
            throw ESBExceptionCodes.PARSE_ERROR("解析String类型不对,输入类型"+value.getClass());
        }
    }

    public void setValue(List<String> value) {
        this.value = value;
    }
}
