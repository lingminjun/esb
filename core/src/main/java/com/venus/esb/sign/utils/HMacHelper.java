package com.venus.esb.sign.utils;

import com.venus.esb.lang.ESBConsts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

/**
 * Hmac
 */
public class HMacHelper implements Signable {
    private static final Logger logger = LoggerFactory.getLogger(HMacHelper.class);
    private Mac mac;

    /**
     * MAC算法可选以下多种算法
     * <pre>
     * HmacMD5
     * HmacSHA1
     * HmacSHA256
     * HmacSHA384
     * HmacSHA512
     * </pre>
     */
    private static final String KEY_MAC = "HmacMD5";

    public HMacHelper(String key) {
        try {
            SecretKey secretKey = new SecretKeySpec(key.getBytes(ESBConsts.UTF8_STR), KEY_MAC);
            mac = Mac.getInstance(secretKey.getAlgorithm());
            mac.init(secretKey);
        } catch (Exception e) {
            logger.error("create hmac helper failed.", e);
        }
    }

    @Override
    public String sign(byte[] content) {
        //        synchronized (this) {
        return Base64Util.encodeToString(mac.doFinal(content));
        //        }
    }

    @Override
    public boolean verify(String sig, byte[] content) {
        try {
            byte[] result = null;
            //            synchronized (this) {
            result = mac.doFinal(content);
            //            }
            return Arrays.equals(Base64Util.decode(sig), result);
        } catch (Exception e) {
            logger.error("varify signature failed.", e);
        }
        return false;
    }

}
