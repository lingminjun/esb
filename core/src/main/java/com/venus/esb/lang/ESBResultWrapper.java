package com.venus.esb.lang;

import java.io.Serializable;
import java.util.List;

/**
 * Created by lingminjun on 17/4/22.
 * ESB dubbo接口返回值基础类型的装箱类型
 */
public abstract class ESBResultWrapper implements Serializable {
    private static final long serialVersionUID = -3717802294621068566L;

    public abstract void setValue(Object value) throws ESBException;

    public static boolean needWrap(Object obj) {
        if (ESBT.isBaseType(obj.getClass())) {
            return true;
        }

        //List<String>
        if (obj instanceof List) {
            List list = (List)obj;
            for (Object o : list) {
                if (String.class != o.getClass()) {
                    return false;
                }
            }
            return true;
        }
        
        return false;
    }
}
