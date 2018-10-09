package com.venus.esb;

import com.venus.esb.lang.ESBException;

import java.io.IOException;

/**
 * Created by lingminjun on 17/4/13.
 */
public abstract class ESBAPISerializer {
    public abstract String serialized(Object pojo) throws ESBException;
    public abstract Object deserialized(String data, String type, boolean isList);
}
