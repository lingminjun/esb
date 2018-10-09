package com.venus.esb.sign.utils;

import java.security.Signature;

/**
 * Created by lingminjun on 17/4/13.
 */
public interface Signable {
    String sign(byte[] content);
    boolean verify(String sign, byte[] content);
}
