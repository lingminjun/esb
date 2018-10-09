package com.venus.esb;

import com.venus.esb.ESBAPIInfo;

/**
 * Created with IntelliJ IDEA.
 * Description: 裁剪返回数据 (此功能暂时未实现)
 * User: lingminjun
 * Date: 2018-09-27
 * Time: 下午4:04
 */
public final class ESBTailor {
    public static Object tailor(Object obj, ESBAPIInfo api, ESBAPIContext context) {
        return obj;
//
//        if (obj == null) {
//            return obj;
//        }
//
//        if (obj instanceof Map) {
//            if (((Map) obj).size() == 0) {
//                return obj;
//            }
//        }
//
//        // 检验返回值
//
//
//
//        return obj;
    }
}
