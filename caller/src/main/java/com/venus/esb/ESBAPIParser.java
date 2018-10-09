package com.venus.esb;

/**
 * Created by lingminjun on 17/5/14.
 */
public class ESBAPIParser implements ESB.APIParser {
    @Override
    public ESBAPISerializer serializer(ESB esb, ESBAPIContext context) {
        //暂时仅仅支持json
        return new ESBAPIJsonSerializer();
    }
}
