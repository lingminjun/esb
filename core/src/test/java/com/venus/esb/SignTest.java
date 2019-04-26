package com.venus.esb;

import com.alibaba.dubbo.common.utils.PojoUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.venus.esb.annotation.ESBDesc;
import com.venus.esb.lang.ESBConsts;
import com.venus.esb.lang.ESBException;
import com.venus.esb.lang.ESBField;
import com.venus.esb.lang.ESBT;
import com.venus.esb.sign.utils.AesHelper;
import com.venus.esb.sign.utils.Base64Util;
import com.venus.esb.sign.utils.EccHelper;
import com.venus.esb.sign.utils.RsaHelper;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by lingminjun on 17/10/20.
 */
public class SignTest {

    @Test
    public void fastJSONTest() {
        ParserConfig.getGlobalInstance().addAccept("com.venus.esb.lang.");

        ESBException exception = new ESBException("随便","test",100,"哈哈哈哈");
        String json = JSON.toJSONString(exception);

        Exception exp = (Exception) JSON.parse(json);

        System.out.println(exp.getMessage());
    }

    private static class CmsPagePOJO implements Serializable {
        private static final long serialVersionUID = 1L;
        @ESBDesc("自增id")
        private long    pageId;
        @ESBDesc("页面名称")
        private String  title;
        @ESBDesc("页面icon,80x80，便于分享")
        private String  icon;
        @ESBDesc("页面简洁，便于分享")
        private String  brief;
        @ESBDesc("页面关键词，便于seo")
        private String  keyword;
        @ESBDesc("状态 1-激活 0-未激活")
        private int     status;
        @ESBDesc("背景颜色，RGB定义方式，如#ffffff")
        private String  backgourd;
        @ESBDesc("作用于应用的appid，0或者null标识未分配")
        private int     app;
        @ESBDesc("页面所属type，如app，merchant")
        private String  type;
        @ESBDesc("页面所属id，如appid，merchantId")
        private String  group;
        @ESBDesc("创建时间")
        private long    createAt;
        @ESBDesc("修改时间")
        private long    modifyAt;

        public long getPageId() {
            return this.pageId;
        }
        public void setPageId(long value) {
            this.pageId = value;
        }
        public String getTitle() {
            return this.title;
        }
        public void setTitle(String value) {
            this.title = value;
        }
        public String getIcon() {
            return this.icon;
        }
        public void setIcon(String value) {
            this.icon = value;
        }
        public String getBrief() {
            return this.brief;
        }
        public void setBrief(String value) {
            this.brief = value;
        }
        public String getKeyword() {
            return this.keyword;
        }
        public void setKeyword(String value) {
            this.keyword = value;
        }
        public int getStatus() {
            return this.status;
        }
        public void setStatus(int value) {
            this.status = value;
        }
        public String getBackgourd() {
            return this.backgourd;
        }
        public void setBackgourd(String value) {
            this.backgourd = value;
        }
        public int getApp() {
            return this.app;
        }
        public void setApp(int value) {
            this.app = value;
        }
        public String getType() {
            return this.type;
        }
        public void setType(String value) {
            this.type = value;
        }
        public String getGroup() {
            return this.group;
        }
        public void setGroup(String value) {
            this.group = value;
        }
        public long getCreateAt() {
            return this.createAt;
        }
        public void setCreateAt(long value) {
            this.createAt = value;
        }
        public long getModifyAt() {
            return this.modifyAt;
        }
        public void setModifyAt(long value) {
            this.modifyAt = value;
        }
    }

    public static class TestPojo {
        public String name;
        public boolean value;
        public int[] array;
        public char ch;
    }

    @Test
    public void testDubboGereCall() {

        String json = "{\"class\":\"com.venus.cms.entities.CmsPagePOJO\",\"brief\":\"中文多好的咋试试\",\"app\":1,\"title\":\"在嘎嘎嘎\",\"pageId\":5,\"type\":\"app\",\"createAt\":1555405033000,\"modifyAt\":1555422188000,\"group\":\"1\",\"status\":1,\"brief_origin\":\"中文多好的\",\"is_brief_editing\":true}";
        Object object = com.alibaba.fastjson.JSON.parse(json);


        Date date1 = new Date();
        Date date2 = new Date();

        List list = new ArrayList();
        list.add(date1);
        list.add(date2);
        String lj = JSON.toJSONString(list);
        System.out.println(lj);

        List list1 = JSON.parseArray(lj,String.class);
        System.out.println(list1);
//        {
//            long time = System.currentTimeMillis();
//            for (int i = 0; i < 10000; i++) {
//                CmsPagePOJO pojo1 = (CmsPagePOJO) ESBT.realize(object, CmsPagePOJO.class, null);
//            }
//            System.out.println(System.currentTimeMillis() - time);
//        }
//        {
//            long time = System.currentTimeMillis();
//            for (int i = 0; i < 10000; i++) {
//                CmsPagePOJO pojo1 = (CmsPagePOJO) PojoUtils.realize(object, CmsPagePOJO.class, null);
//            }
//            System.out.println(System.currentTimeMillis() - time);
//        }

    }


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
