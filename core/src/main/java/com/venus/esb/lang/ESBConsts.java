package com.venus.esb.lang;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.venus.esb.annotation.ESBDesc;

import java.nio.charset.Charset;
import java.util.Comparator;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: lingminjun
 * Date: 2018-09-26
 * Time: 上午10:01
 */
public final class ESBConsts {

    public static final String UTF8_STR                 = "UTF-8";
    public static final String ASCII_STR                = "ASCII";

    public static final Charset UTF8                 = Charset.forName(UTF8_STR);
    public static final Charset ASCII                = Charset.forName(ASCII_STR);

    public static final String JSON = "JSON";
    public static final String XML = "XML";
    public static final String TEXT = "TEXT";

    public static final long TIME_2018_01_01 = 1483200000000l;

    // 设置调试模式
    public static boolean IS_DEBUG = false;

    // 两种特殊的请求ESB验权支持
    public static final String SSO_SPECIFIC_SELECTOR            = "esb.sso.ESBSpecial";
    public static final String REFRESH_TOKEN_SPECIFIC_SELECTOR  = "esb.auth.ESBSpecial";


    public static final SerializerFeature[] FASTJSON_SERIALIZER_FEATURES = new SerializerFeature[]{
            SerializerFeature.DisableCircularReferenceDetect,//disable循环引用
            //            SerializerFeature.WriteMapNullValue,//null属性，序列化为null, android sdk中 JSON.optString()将null convert成了"null",故关闭该特性
            SerializerFeature.NotWriteRootClassName,
            //            SerializerFeature.WriteEnumUsingToString, //
            // SerializerFeature.WriteNullNumberAsZero,
            // SerializerFeature.WriteNullBooleanAsFalse,
    };

    public static final Comparator<String> STR_COMPARATOR = new Comparator<String>() {

        @Override
        public int compare(String s1, String s2) {
            int n1 = s1 == null ? 0 : s1.length();
            int n2 = s2 == null ? 0 : s2.length();
            int mn = n1 < n2 ? n1 : n2;
            for (int i = 0; i < mn; i++) {
                int k = s1.charAt(i) - s2.charAt(i);
                if (k != 0) {
                    return k;
                }
            }
            return n1 - n2;
        }
    };

}
