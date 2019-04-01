package com.venus.esb.sign;

import com.venus.esb.ESBSecurityLevel;
import com.venus.esb.config.ESBConfigCenter;
import com.venus.esb.lang.*;
import com.venus.esb.sign.utils.AesHelper;
import com.venus.esb.sign.utils.Base64Util;
import com.venus.esb.sign.utils.HexStringUtil;
import com.venus.esb.utils.MD5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * 处理使用AES秘钥加密用户信息而产生的token
 *
 * @author lingminjun
 */
public final class ESBTokenSign {

    private static final Logger logger                   = LoggerFactory.getLogger(ESBTokenSign.class);
    private static final short TOKEN_VERSION_1_0 = 10;//TOKEN的10版本
    private static final short SSO_TOKEN_VERSION_1_0 = 10;//TOKEN的10版本

    private AesHelper aes;

    public ESBTokenSign(String pwd) {
        aes = new AesHelper(Base64Util.decode(pwd), null);
    }

    public ESBTokenSign(AesHelper helper) {
        aes = helper;
    }

    public static ESBTokenSign defaultSign() {
        return new ESBTokenSign(ESBConfigCenter.instance().getAesKey());
    }


    /**
     * 从base64编码的字符串中解析调用者信息
     */
    public ESBSecur parseToken(String token) {
        try {
            return parseToken(Base64Util.decode(token));
        } catch (Exception e) {
            logger.error("token parse failed.", e);
        }
        return null;
    }

    /**
     * 解析调用者信息
     */
    public ESBSecur parseToken(byte[] token) {
        DataInputStream dis = null;
        ESBSecur client = null;
        try {
            dis = new DataInputStream(new ByteArrayInputStream(aes.decrypt(token)));
            short tokenVersion = dis.readShort(); // token version for backward compliance
            if (tokenVersion != TOKEN_VERSION_1_0) {
                logger.error("token version mismatch!");
                return null;
            }
            client = new ESBSecur();
            client.expire = dis.readLong();
            client.securityLevel = dis.readInt();
            client.aid = dis.readInt();
            client.did = dis.readLong();
            client.uid = dis.readLong();
            client.acct = dis.readLong();
            short len = dis.readShort();
            if (len > 0) {
                byte[] bys = new byte[len];
                if (len != dis.read(bys)) {
                    return null;
                }
                client.key = HexStringUtil.toHexString(bys);
            }

            if (dis.available() > 0) {//非必填
                len = dis.readShort();
                if (len > 0) {
                    byte[] bs = new byte[len];
                    if (len != dis.read(bs)) {
                        return null;
                    }
                    client.dna = new String(bs, ESBConsts.UTF8);
                }
            }

            //发现还有数据,退出
            if (dis.available() > 0) {
                return null;
            }

        } catch (Exception e) {
            logger.error("token parse failed.", e);
        } finally {
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    logger.error("token parse failed.close input stream failed!", e);
                }
            }
        }
        return client;
    }

    /**
     * 生成token
     */
    public byte[] generateToken(ESBSecur client) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(8);
        DataOutputStream dos = new DataOutputStream(baos);
        byte[] token = null;
        try {
            //设置版本
            dos.writeShort(TOKEN_VERSION_1_0);

            dos.writeLong(client.expire);
            dos.writeInt(client.securityLevel);
            dos.writeInt(client.aid);
            dos.writeLong(client.did);
            dos.writeLong(client.uid);
            dos.writeLong(client.acct);
            short len = 0;
            byte[] bys = null;
            if (client.key != null && client.key.length() > 0) {
                bys = HexStringUtil.toByteArray(client.key);
            } else {
                bys = new byte[0];
            }
            dos.writeShort(bys.length);
            dos.write(bys);

            // 写入设备指纹
            if (!ESBT.isEmpty(client.dna)) {
                byte[] dna = client.dna.getBytes(ESBConsts.UTF8);
                dos.writeShort(dna.length);
                dos.write(dna);
            }

            byte[] bs = baos.toByteArray();
            token = aes.encrypt(bs);
        } catch (IOException e) {
            throw new RuntimeException("generator token failed.", e);
        } finally {
            try {
                dos.close();
            } catch (IOException e) {
                logger.error("generator token failed.close output stream failed!", e);
            }
        }
        return token;
    }

    /**
     * 生成设备token
     *
     * @param client
     */
    public byte[] generateDeviceToken(ESBSecur client) {
        client.uid = 0;
        client.acct = 0;
        client.securityLevel = ESBSecurityLevel.deviceAuth.authorize(0);
        return generateToken(client);
    }

    /**
     * 生成用户token
     *
     * @param client
     */
    public String generateStringToken(ESBSecur client) {
        return Base64Util.encodeToString(this.generateToken(client));
    }

    /**
     * 生成设备token
     *
     * @param client
     */
    public String generateStringDeviceToken(ESBSecur client) {
        return Base64Util.encodeToString(this.generateDeviceToken(client));
    }

    /**
     * 生产 sso token
     * @param client
     * @return
     */
    public String generateStringSSOToken(ESBSSOSecur client) {
        return Base64Util.encodeToString(this.generateSSOToken(client));
    }

    /**
     * 注入默认token
     * @param token
     * @param context
     * @return
     */
    public static ESBDeviceToken injectDeviceToken(ESBDeviceToken token, ESBContext context) {
        if (token == null) {
            token = new ESBDeviceToken();
            token.scope = "device";
        }

        token.success = true;

        byte[] key = AesHelper.randomKey(256);
        String csrfToken = HexStringUtil.toHexString(key);

        ESBSecur client = context.generateClient();
        client.uid = 0;
        client.acct = 0;
        client.securityLevel =  ESBSecurityLevel.deviceAuth.authorize(0);
        client.expire = 0;//永不过期
        client.key = csrfToken;
        if (context.ua != null) {
            client.dna = MD5.md5(context.ua);//线束ua
        }

        token.token = ESBTokenSign.defaultSign().generateStringDeviceToken(client);
        token.key = csrfToken;

        return token;
    }

    /**
     * 注入默认token
     * @param token
     * @param context
     * @return
     */
    public static ESBToken injectDefaultToken(ESBToken token, ESBContext context) {

        if (token == null) {
            token = new ESBToken();
            if (context.uid != null && context.uid.length() > 0) {
                token.scope = "user";
            } else if (context.acct != null && context.acct.length() > 0) {
                token.scope = "account";
            } else {
                token.scope = "user";
            }
        } else {//反向注入uid和acct--(重要)
            if (token.uid != null && context.uid == null) {
                context.uid = token.uid;
            }
            if (token.acct != null && context.acct == null) {
                context.acct = token.acct;
            }
        }

        token.success = true;

        //默认一个月
        long expire = token.expire > 0 ? token.expire : (context.at/1000l + 30 * 24 * 3600);
        if (expire <= context.at/1000l) {//修正并兼容expire设置错误
            expire = context.at/1000l + expire;
        }
        byte[] key = AesHelper.randomKey(256);
        String csrfToken = HexStringUtil.toHexString(key);

        ESBSecur client = context.generateClient();

        client.expire = expire;
        token.expire = expire;

        client.key = csrfToken;
        //产生最新的会话id, 刷新token，会话id保持不变
        if (ESBT.isEmpty(context.guid)) {
            client.dna = ESBUUID.genSimplifyCID();
            context.guid = client.dna;
        }
        token.token = ESBTokenSign.defaultSign().generateStringToken(client);

        // cookie保存或者客户端保存，用sso
        // 针对web,保护token，防止伪造，假装csrf_token，写入cookie，并设置httpOnly=true，secure=true
        // 默认过期时间比token多一个月
        client.expire = expire + 30 * 24 * 3600;
        client.securityLevel = ESBSecurityLevel.secretAuth.authorize(0);
        client.dna = MD5.md5(token.token);//包含token签名
        token.stoken = ESBTokenSign.defaultSign().generateStringToken(client);

        // 刷新token
        client.securityLevel = ESBSecurityLevel.extend.authorize(0);
        token.refresh = ESBTokenSign.defaultSign().generateStringToken(client);;//客户端保存，不存cookie

        // 颁发公钥
        token.key = csrfToken;//可以代替csrf_token做签名，校验安全性

        return token;
    }

    /**
     * 注入默认token
     * @param token
     * @param context
     * @return
     */
    public static ESBSSOToken injectSSOToken(ESBSSOToken token, ESBContext context, int ssoAid, long ssoDid, String ssoDomain) {

        if (token == null) {
            token = new ESBSSOToken();
        }

        token.success = true;

        //默认1分钟
        long expire = token.expire > 0 ? token.expire : (context.at/1000l + 60);
        if (expire <= context.at/1000l) {//修正并兼容expire设置错误
            expire = context.at/1000l + expire;
        }
//        byte[] key = AesHelper.randomKey(256);
//        String csrfToken = HexStringUtil.toHexString(key);
        ESBSSOSecur client = new ESBSSOSecur();

        client.expire = expire;
        token.expire = expire;

//        client.fdid = ESBT.longInteger(context.did);
//        client.fscope = "sso";
        client.uid = ESBT.longInteger(context.uid);
        client.tdid = ssoDid;
        client.taid = ssoAid;
//        client.faid = ESBT.integer(context.aid); //前面已经校验了aid与faid一致
        client.tdomain = ssoDomain;
//        client.tscheme = ssoScheme;

//        client.key = csrfToken;
//        client.oauthid = token.oauth;//从后端传入
        token.ssoToken = ESBTokenSign.defaultSign().generateStringSSOToken(client);

        return token;
    }

    /**
     * 解析默认token
     * @param token
     * @param context
     * @return
     */
    public static ESBSecur parseDefaultToken(String token, ESBContext context) {
        return parseDefaultToken(token,context,null);
    }
    public static ESBSecur parseDefaultToken(String token, ESBContext context, String utoken) {
        if (ESBT.isEmpty(token)) {return null;}
        ESBSecur client = ESBTokenSign.defaultSign().parseToken(token);
        if (client == null) {
            return client;
        }

        //aid一定要对应上
        if (context != null && !ESBT.isEmpty(context.aid)) {
            //校验合法性,不允许有变化,跨域采用跨域手段完成
            if (!context.aid.equals(""+client.aid)) {
                return null;
            }
        } else if (context != null) {
            context.aid = "" + client.aid;
            context.tml = "" + (int)((0xff000000 & client.aid) >>> 24);
            context.app = "" + (int)(0x00ffffff & client.aid);
        }

        //did也需要对应上
        if (context != null && !ESBT.isEmpty(context.did)) {
            //校验合法性,不允许有变化
            if (!context.did.equals(""+client.did)) {
                return null;
            }
        } else if (context != null) {
            context.did = "" + client.did;
        }

        //uid的校验
        if (client.uid != 0) {
            //校验合法性
            if (context != null && !ESBT.isEmpty(context.uid) && !context.uid.equals(""+client.uid)) {
                return null;
            }
            if (context != null) {
                context.uid = "" + client.uid;//补全数据
            }
        }

        //acct的校验
        if (client.acct != 0) {
            //校验合法性
            if (context != null && !ESBT.isEmpty(context.acct) && !context.acct.equals(""+client.acct)) {
                return null;
            }
            if (context != null) {
                context.acct = "" + client.acct;//补全数据
            }
        }

        //DNA界定,是否做强校验，参考 injectDeviceToken 和 injectDefaultToken 方法
        // 有三类情况，
        //      1、device token, 签署user agent，将于context.dna一致
        //      2、user and account token, 签署did 故不需要做dna比较
        //      3、security token和refresh token，签署 user token
        //      4、其他token，不做校验

        if (!ESBT.isEmpty(client.dna)) {
            // 第1种情况，injectDeviceToken
            if (client.securityLevel == ESBSecurityLevel.deviceAuth.getCode()) {
                // 说明此处user agent被串改
                if (context != null &&
                        !ESBT.isEmpty(context.ua)
                        && !client.dna.equals(MD5.md5(context.ua))) {

                    // 暂时不校验 context.ua 发现android小程序 ua每次变化
//                    return null;
                }
                //覆盖dna
                if (context != null) {
                    context.dna = client.dna;
                }
            }
            // 第2种情况 不需要验证dna，此处只需记录会话id
            else if (client.securityLevel == ESBSecurityLevel.userAuth.getCode()
                    || client.securityLevel == ESBSecurityLevel.accountAuth.getCode()) {
                // 依靠did签署，顾只要验证dtoken是合法，此处utoken肯定合法
                /*
                if (dtoken != null && dtoken.length() > 0) {

                }*/
                context.guid = client.dna;
            } else if (client.securityLevel == ESBSecurityLevel.secretAuth.getCode()
                    || client.securityLevel == ESBSecurityLevel.extend.getCode()) {
                // 业务ESB同样会验证
                if (!ESBT.isEmpty(utoken) && !MD5.md5(utoken).equals(client.dna)) {
                    return null;
                }
            }
        }

        return client;
    }

    public static ESBSSOSecur parseSSOToken(String token, ESBContext context) {
        if (ESBT.isEmpty(token)) {return null;}

        ESBSSOSecur client = ESBTokenSign.defaultSign().parseSSOToken(token);
        if (client == null) {
            return client;
        }

        //uid的校验
        if (client.uid != 0) {
            //校验合法性
            if (context != null && !ESBT.isEmpty(context.uid) && !context.uid.equals(""+client.uid)) {
                return null;
            }
            if (context != null) {
                context.uid = "" + client.uid;//补全数据
            }
        }

        return client;
    }

    private static byte[] readSerialByte(DataInputStream dis) throws IOException {
        short len = dis.readShort();
        if (len > 0) {
            byte[] tmp = new byte[len];
            if (len != dis.read(tmp)) {
                return null;
            }
            return tmp;
        }
        return null;
    }

    private static String readString(DataInputStream dis) throws IOException {
        short len = dis.readShort();
        if (len > 0) {
            byte[] tmp = new byte[len];
            if (len != dis.read(tmp)) {
                return null;
            }
            return new String(tmp, ESBConsts.UTF8);//ESBConsts.UTF8
        }
        return null;
    }

    private static void writeString(DataOutputStream dos,String value) throws IOException {
        if (value != null) {
            byte[] bytes = value.getBytes(ESBConsts.UTF8);
            dos.writeShort(bytes.length);
            dos.write(bytes);
        } else {
            dos.writeShort(0);
        }
    }

    public ESBSSOSecur parseSSOToken(String token) {
        try {
            return parseSSOToken(Base64Util.decode(token));
        } catch (Exception e) {
            logger.error("sso token parse failed.", e);
        }
        return null;
    }

    /**
     * 解析调用者信息
     */
    public ESBSSOSecur parseSSOToken(byte[] token) {
        DataInputStream dis = null;
        ESBSSOSecur client = null;
        try {
            dis = new DataInputStream(new ByteArrayInputStream(aes.decrypt(token)));
            short tokenVersion = dis.readShort(); // token version for backward compliance
            if (tokenVersion != SSO_TOKEN_VERSION_1_0) {
                logger.error("token version mismatch!");
                return null;
            }
            client = new ESBSSOSecur();
            client.expire = dis.readLong();
//            client.faid = dis.readInt();
            client.taid = dis.readInt();
            client.tdid = dis.readLong();
            client.uid = dis.readLong();
            client.tdomain = readString(dis);//必填字段

//            if (dis.available() > 0) {//非必填
//                client.tscheme = readString(dis);
//            }



            //发现还有数据,退出
            if (dis.available() > 0) {
                return null;
            }

        } catch (Exception e) {
            logger.error("token parse failed.", e);
        } finally {
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    logger.error("token parse failed.close input stream failed!", e);
                }
            }
        }
        return client;
    }

    /**
     * 生成sso token
     */
    public byte[] generateSSOToken(ESBSSOSecur client) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(8);
        DataOutputStream dos = new DataOutputStream(baos);
        byte[] token = null;
        try {

            dos.writeShort(SSO_TOKEN_VERSION_1_0);
            dos.writeLong(client.expire);
//            dos.writeInt(client.faid);
            dos.writeInt(client.taid);
            dos.writeLong(client.tdid);
            dos.writeLong(client.uid);
            writeString(dos,client.tdomain);
//            writeString(dos,client.fscope);
//            writeString(dos,client.tscheme);
//            if (client.fexts != null && client.fexts.size() > 0) {
//                try {
//                    String ext = JSON.toJSONString(client.fexts, SerializerFeature.DisableCircularReferenceDetect);
//                    writeString(dos,ext);
//                } catch (Throwable e) {}
//            }

            byte[] bs = baos.toByteArray();
            token = aes.encrypt(bs);
        } catch (IOException e) {
            throw new RuntimeException("generator token failed.", e);
        } finally {
            try {
                dos.close();
            } catch (IOException e) {
                logger.error("generator token failed.close output stream failed!", e);
            }
        }
        return token;
    }

}
