package com.venus.esb.sign;

import com.venus.esb.lang.ESBConsts;
import com.venus.esb.lang.ESBSTDKeys;
import com.venus.esb.sign.utils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by lingminjun on 17/4/13.
 */
public final class ESBAPISignature {

    /**
     * 注意，参数一律为明文（esb内部采用明文签名）
     * @param params
     * @return
     */
    public static StringBuilder getSortedParameters(Map<String, String> params) {
        // 拼装被签名参数列表
        StringBuilder sb = new StringBuilder(200);
        if (params != null) {
            List<String> list = new ArrayList<String>(10);
            list.addAll(params.keySet());
            // 参数排序
            String[] array = list.toArray(new String[list.size()]);
            if (array.length > 0) {
                Arrays.sort(array, ESBConsts.STR_COMPARATOR);
                for (String key : array) {
                    if (ESBSTDKeys.SIGN_KEY.equals(key)) {
                        continue;
                    }
                    sb.append(key);
                    sb.append("=");
                    sb.append(params.get(key));
                }
            }
        }
        return sb;
    }

    public static Signable getSignable(String algorithm, String pubKey, String priKey) {
        if (ESBAPIAlgorithm.CRC16.hit(algorithm)) {
            return new CRC16Helper(pubKey);
        } else if (ESBAPIAlgorithm.CRC32.hit(algorithm)) {
            return new CRC32Helper(pubKey);
        } else if (ESBAPIAlgorithm.MD5.hit(algorithm)) {
            return new Md5Helper(pubKey);
        } else if (ESBAPIAlgorithm.SHA1.hit(algorithm)) {
            return new SHAHelper(pubKey);
        } else if (ESBAPIAlgorithm.HMAC.hit(algorithm)) {
            return new HMacHelper(pubKey);
        } else if (ESBAPIAlgorithm.RSA.hit(algorithm)) {
            return new RsaHelper(pubKey,priKey);
        } else if (ESBAPIAlgorithm.ECC.hit(algorithm)) {
            return new EccHelper(pubKey,priKey);
        }
        //默认sha1
        return new SHAHelper(pubKey);
    }
}
