package com.venus.esb.sign;

/**
 * Created by lingminjun on 17/4/13.
 */
public enum ESBAPIAlgorithm {

    CRC16("crc16"),
    CRC32("crc32"),
    MD5("md5"),
    SHA1("sha1"),
    HMAC("hmac"),
    RSA("rsa"),
    ECC("ecc");

    private final String name;

    ESBAPIAlgorithm(String name) {this.name=name;}

    //命中
    public boolean hit(String algorithm) {
        return name.equalsIgnoreCase(algorithm);
    }
}
