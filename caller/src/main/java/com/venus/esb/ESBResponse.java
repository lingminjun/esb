package com.venus.esb;

import com.venus.esb.annotation.ESBDesc;
import com.venus.esb.lang.ESBException;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: lingminjun
 * Date: 2018-09-24
 * Time: 上午9:34
 */
@ESBDesc("响应")
public class ESBResponse {
    @ESBDesc("异常")
    public ESBException exception;

    @ESBDesc("实际返回值")
    public String result;
}
