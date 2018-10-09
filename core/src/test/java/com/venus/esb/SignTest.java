package com.venus.esb;

import com.alibaba.fastjson.JSON;
import com.venus.esb.lang.ESBConsts;
import com.venus.esb.lang.ESBField;
import com.venus.esb.sign.utils.AesHelper;
import com.venus.esb.sign.utils.Base64Util;
import com.venus.esb.sign.utils.EccHelper;
import com.venus.esb.sign.utils.RsaHelper;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.security.*;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by lingminjun on 17/10/20.
 */
public class SignTest {
    @Test
    public void testAesGen() {
        byte[] key = AesHelper.randomKey(256);
        AesHelper aes = new AesHelper(key, true);
        System.out.println(Base64Util.encodeToString(key));
    }

    @Test
    public void testRsaGen() {
        KeyPairGenerator keygen;
        try {
            keygen = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = new SecureRandom();
            try {
                random.setSeed("mm.static.key!".getBytes(ESBConsts.UTF8_STR));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            keygen.initialize(1024, random);
            KeyPair kp = keygen.generateKeyPair();
            byte[] pub = kp.getPublic().getEncoded();
            byte[] pri = kp.getPrivate().getEncoded();
            System.out.println("pub:  " + Base64Util.encodeToString(pub));
            System.out.println("pri:  " + Base64Util.encodeToString(pri));
            RsaHelper rsa = new RsaHelper(pub, pri);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 10000; i++) {
                sb.append(String.valueOf(i));
            }
            byte[] data = sb.toString().getBytes(ESBConsts.UTF8);
            assertTrue(rsa.verify(rsa.sign(data), data));
            assertTrue(RsaHelper.verify(rsa.sign(data), data, pub));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testEccSignAndVerify() {
        KeyPairGenerator keygen;
        try {
            Security.addProvider(new BouncyCastleProvider());

            keygen = KeyPairGenerator.getInstance("EC", "BC");
            keygen.initialize(192, SecureRandom.getInstance("SHA1PRNG"));
            KeyPair kp = keygen.generateKeyPair();
            byte[] pub = kp.getPublic().getEncoded();
            byte[] pri = kp.getPrivate().getEncoded();
            System.out.println("pub:  " + Base64Util.encodeToString(pub));
            System.out.println("pri:  " + Base64Util.encodeToString(pri));
            EccHelper ecc = new EccHelper(pub, pri);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 10000; i++) {
                sb.append(String.valueOf(i));
            }
            byte[] data = sb.toString().getBytes(ESBConsts.UTF8);
            assertTrue(ecc.verify(ecc.sign(data), data));
            assertTrue(EccHelper.verify(ecc.sign(data), data, pub));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
