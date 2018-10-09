package com.venus.esb.lang;

import com.venus.esb.annotation.ESBDesc;

import java.io.Serializable;

/**
 * Created by lingminjun on 17/4/22.
 */
@ESBDesc("原始字符串类型")
public class ESBRawString implements Serializable {
    private static final long serialVersionUID = 2224563307923723782L;

    @ESBDesc("值的编码类型，http/https contentType设置")
    public String contentType;

    @ESBDesc("值")
    public String value;

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
