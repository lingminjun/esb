package com.venus.esb;

import com.venus.esb.sign.ESBAPIAlgorithm;
import com.venus.esb.sign.ESBAPISignature;

import java.io.Serializable;

/**
 * Created by lingminjun on 17/4/13.
 */
public final class ESBAPISecurity implements Serializable {
    private static final long serialVersionUID = 8157910304359019421L;


    public long id;//数据库主键id
    //支持md5,crc32,是将key拼接到末尾然后执行
    public ESBAPIAlgorithm algorithm;//加密算法
    public transient String prikey;//私钥---不对外公开,非对称加密,秘钥主要存储在prikey上
    public String pubkey;//公钥---对外公开,做记录

    /**
     * crc32、md5、SHA1为hex编码字符串(小写),其他输出base64编码的字符串
     * @param data 原始数据流
     * @return
     */
    public String sign(byte[] data) {
        return ESBAPISignature.getSignable(algorithm.name(),pubkey,prikey).sign(data);
    }

    /**
     * 签名是否ok
     * @param sign 与sign(byte[] data)输出对应
     * @param data 原始数据流
     * @return
     */
    public boolean verify(String sign,byte[] data) {
        return ESBAPISignature.getSignable(algorithm.name(),pubkey,prikey).verify(sign,data);
    }
}
