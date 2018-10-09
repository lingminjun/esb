package com.venus.esb.sign.utils;

import sun.misc.CRC16;

/**
 * Created by lingminjun on 17/4/13.
 */
public class CRC16Helper implements Signable {


    private final String suffix;
    public CRC16Helper(String key) {
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
        CRC16 crc16 = new CRC16();
        for (int i = 0; i < data.length; i++) {
            crc16.update(data[i]);
        }
        return Integer.toHexString(crc16.value);
    }

    @Override
    public boolean verify(String sign, byte[] content) {
        byte[] data = concatenateByteArrays(content,suffix.getBytes());
        CRC16 crc16 = new CRC16();
        for (int i = 0; i < data.length; i++) {
            crc16.update(data[i]);
        }
        return Integer.parseInt(sign,16) == crc16.value;
    }

    /*
    public static void main(String var[]) {
        CRC16 crc16 = new CRC16();
        byte[] data ="suiyixiediansha".getBytes();
        for (int i = 0; i < data.length; i++) {
            crc16.update(data[i]);
        }
        int value = crc16.value;
        System.out.println(value);
        String sign = Integer.toHexString(value);
        System.out.println(sign);
        System.out.println(Integer.parseInt(sign,16));
    }*/

}
