package com.venus.esb.sign.utils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import sun.security.ec.ECPrivateKeyImpl;
//import sun.security.ec.ECPublicKeyImpl;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class EccHelper implements Signable {
    private static final Logger logger = LoggerFactory.getLogger(EccHelper.class);
    private static final int    SIZE   = 4096;
    private PublicKey publicKey;
    private PrivateKey privateKey;

    static {
        Provider provider = Security.getProvider("BC");
        if (provider == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
//        Security.addProvider(new com.sun.crypto.provider.SunJCE());//jdk 1.7以上 and also the JAVA_HOME/jre/lib/ext/ contains the sunec.jar. Also the US_export_policy.jar and local_policy.jar are in the JAVA_HOME/jre/lib/security folder.
    }

    public EccHelper(String publicKey, String privateKey) {
        this(Base64Util.decode(publicKey), privateKey == null ? null : Base64Util.decode(privateKey));
    }

    public EccHelper(byte[] publicKey, byte[] privateKey) {
        try {
            //使用sun JCE
//            if (publicKey != null && publicKey.length > 0) {
//                this.publicKey = new ECPublicKeyImpl(publicKey);
//            }
//
//            if (privateKey != null && privateKey.length > 0) {
//                this.privateKey = new ECPrivateKeyImpl(privateKey);
//            }

            //使用BC JCE
            KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
            if (publicKey != null && publicKey.length > 0) {
                this.publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKey));
            }
            if (privateKey != null && privateKey.length > 0) {
                this.privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKey));
            }
        } catch (ClassCastException e) {
            throw new RuntimeException("", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public EccHelper(String publicKey) {
        this(Base64Util.decode(publicKey));
    }

    public EccHelper(byte[] publicKey) {
        try {
            //使用sun JCE
//            if (publicKey != null && publicKey.length > 0) {
//                this.publicKey = new ECPublicKeyImpl(publicKey);
//            }

            //使用BC JCE
            KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
            if (publicKey != null && publicKey.length > 0) {
                this.publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKey));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] encrypt(byte[] content) {
        if (publicKey == null) {
            throw new RuntimeException("public key is null.");
        }
        try {
            Cipher cipher = Cipher.getInstance("ECIES", "BC");
//            Cipher cipher = Cipher.getInstance("ECIES");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            int size = SIZE;
            ByteArrayOutputStream baos = new ByteArrayOutputStream((content.length + size - 1) / size * (size + 45));
            int left = 0;
            for (int i = 0; i < content.length; ) {
                left = content.length - i;
                if (left > size) {
                    cipher.update(content, i, size);
                    i += size;
                } else {
                    cipher.update(content, i, left);
                    i += left;
                }
                baos.write(cipher.doFinal());
            }

            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] decrypt(byte[] secret) {
        if (privateKey == null) {
            throw new RuntimeException("private key is null.");
        }
        try {
            //使用sun JCE
//            Cipher cipher = Cipher.getInstance("ECIES");
            //使用BC JCE
            Cipher cipher = Cipher.getInstance("ECIES", "BC");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            int size = SIZE + 45;
            ByteArrayOutputStream baos = new ByteArrayOutputStream((secret.length + size + 44) / (size + 45) * size);
            int left = 0;
            for (int i = 0; i < secret.length; ) {
                left = secret.length - i;
                if (left > size) {
                    cipher.update(secret, i, size);
                    i += size;
                } else {
                    cipher.update(secret, i, left);
                    i += left;
                }

                baos.write(cipher.doFinal());
            }

            return baos.toByteArray();
        } catch (Exception e) {
            logger.error("ecc decrypt failed.", e);
        }
        return null;
    }

    @Override
    public String sign(byte[] content) {
        if (privateKey == null) {
            throw new RuntimeException("private key is null.");
        }
        try {
            //sun JEC
//            Signature signature = Signature.getInstance("SHA1withECDSA");
            //BC JEC
            Signature signature = Signature.getInstance("SHA1withECDSA", "BC");
            signature.initSign(privateKey);
            signature.update(content);
            return Base64Util.encodeToString(signature.sign());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean verify(String sign, byte[] content) {
        if (publicKey == null) {
            throw new RuntimeException("public key is null.");
        }
        try {
            //sun JEC
//            Signature signature = Signature.getInstance("SHA1withECDSA");
            //BC JEC
            Signature signature = Signature.getInstance("SHA1withECDSA", "BC");
            signature.initVerify(publicKey);
            signature.update(content);
            return signature.verify(Base64Util.decode(sign));
        } catch (Exception e) {
            logger.error("ecc verify failed.", e);
        }
        return false;
    }

    public static byte[] encrypt(byte[] content, byte[] publicKey) {
        return new EccHelper(publicKey, null).encrypt(content);
    }

    public static byte[] decrypt(byte[] secret, byte[] privateKey) {
        return new EccHelper(null, privateKey).decrypt(secret);
    }

    public static String sign(byte[] content, byte[] privateKey) {
        return new EccHelper(null, privateKey).sign(content);
    }

    public static boolean verify(String sign, byte[] content, byte[] publicKey) {
        return new EccHelper(publicKey, null).verify(sign, content);
    }
}
