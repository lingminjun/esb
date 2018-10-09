package com.venus.esb.sign.utils;

import java.util.zip.CRC32;

/**
 * Created by lingminjun on 17/4/13.
 */
public class CRC32Helper implements Signable {


    private final String suffix;
    public CRC32Helper(String key) {
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
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        return Long.toHexString(crc32.getValue());
    }

    @Override
    public boolean verify(String sign, byte[] content) {
        byte[] data = concatenateByteArrays(content,suffix.getBytes());
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        return Long.parseLong(sign,16) == crc32.getValue();
    }

    /*
    public static void main(String var[]) {
        CRC32 crc32 = new CRC32();
        crc32.update("suiyixiediansha".getBytes());
        long value = crc32.parseValue();
        System.out.println(value);
        String sign = Long.toHexString(value);
        System.out.println(sign);
        System.out.println(Long.parseLong(sign,16));
    }
    */
}
