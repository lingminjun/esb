package com.venus.esb.sign.utils;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Md5Helper implements Signable {
    public static final byte[] compute(byte[] content) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            return md5.digest(content);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static final String computeToHex(byte[] content) {
        return HexStringUtil.toHexString(compute(content));
    }

    public static final String computeToBase64(byte[] content) {
        return Base64Util.encodeToString(compute(content));
    }

    private final String suffix;
    public Md5Helper(String key) {
        this.suffix = key == null ? "" : key;
    }

    byte[] concatenateByteArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    @Override
    public String sign(byte[] content) {
        byte[] data = concatenateByteArrays(content,suffix.getBytes());
        return HexStringUtil.toHexString(compute(data));
    }

    @Override
    public boolean verify(String sign, byte[] content) {
        byte[] data = concatenateByteArrays(content,suffix.getBytes());
        return Arrays.equals(HexStringUtil.toByteArray(sign),compute(data));
    }
}
