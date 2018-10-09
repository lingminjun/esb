package com.venus.esb.utils;

import com.venus.esb.lang.ESBConsts;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by lingminjun on 17/8/10.
 */
public final class MD5 {
    public static String md5(String string) {
        if (string == null) {string = "";}
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes(ESBConsts.UTF8_STR));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xff) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xff));
        }
        return hex.toString();
    }
}
