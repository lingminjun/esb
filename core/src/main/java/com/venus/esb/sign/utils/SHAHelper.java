package com.venus.esb.sign.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class SHAHelper implements Signable {

    private final String suffix;
    public SHAHelper(String key) {
        this.suffix = key == null ? "" : key;
    }

    byte[] concatenateByteArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public static byte[] computeSHA1(byte[] content) {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA1");
            return sha1.digest(content);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static final String computeSHA1ToHex(byte[] content) {
        return HexStringUtil.toHexString(computeSHA1(content));
    }

    public static final String computeSHA1ToBase64(byte[] content) {
        return Base64Util.encodeToString(computeSHA1(content));
    }

    @Override
    public String sign(byte[] content) {
        byte[] data = concatenateByteArrays(content,suffix.getBytes());
        return computeSHA1ToHex(data);
    }

    @Override
    public boolean verify(String sign, byte[] content) {
        byte[] data = concatenateByteArrays(content,suffix.getBytes());
        return Arrays.equals(HexStringUtil.toByteArray(sign),computeSHA1(data));
    }
}
