package com.venus.esb.lang;

import com.alibaba.fastjson.parser.ParserConfig;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: lingminjun
 * Date: 2019-04-22
 * Time: 12:04 PM
 */
public final class ESBFastJsonSupport {
    static {
        ParserConfig.getGlobalInstance().addAccept("com.venus.esb.lang.");
        ParserConfig.getGlobalInstance().addAccept("com.alibaba.dubbo.rpc.");
        ParserConfig.getGlobalInstance().addAccept("com.venus.esb.dubbo.filter.ESBGenericException");
    }

    public static void initialize() {}
}
