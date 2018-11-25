package com.venus.esb;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.venus.esb.annotation.ESBDesc;
import com.venus.esb.lang.*;
import com.venus.esb.sign.ESBTokenSign;
import com.venus.esb.sign.ESBUUID;

import java.util.Map;

/**
 * Created by lingminjun on 17/5/11.
 */
@ESBDesc("ESB API Context 定义")
public final class ESBAPIContext extends ESBContext {
    private static final long serialVersionUID = -5708176854310567042L;
    public static final String JSON_CONTENT = ESBConsts.JSON;
    public static final String XML_CONTENT  = ESBConsts.XML;
    public static final String TEXT_CONTENT = ESBConsts.TEXT;

    // 此可以主要用于透传时(transmit=true)设置的content type，由服务提供方自行配置，
    //  设置案例：content.putExt(CUSTOM_CONTENT_TYPE_KEY,"custom content type")
    public static final String CUSTOM_CONTENT_TYPE_KEY = TRANSMIT_CONTENT_TYPE_KEY;

    @JSONField(serialize = false)
    @ESBDesc(value = "API用户权限信息",ignore = true, inner = true)
    public String contentType = JSON_CONTENT;

//    @JSONField(serialize = false)
//    @ESBDesc(value = "国际化支持,语言参数的 ISO 国家/地区代码,参照HTTP Header Accept-Language:zh-CN,zh;q=0.8",ignore = true, inner = true)
//    public String localization;

    @JSONField(serialize = false)
    @ESBDesc(value = "method 请求的方法名: domain.module.methodName 或者 module.methodName",ignore = true, inner = true)
    public String selector;

    @JSONField(serialize = false)
    @ESBDesc(value = "signature 参数字符串签名",ignore = true, inner = true)
    public String signature;

    @JSONField(serialize = false)
    @ESBDesc(value = "CAPTCHA:人机是被参数,需要以此来界定是否问人操作,而非机器",ignore = true, inner = true)
    public String captcha;

    @JSONField(serialize = false)
    @ESBDesc(value = "signature method 签名算法 hmac,md5,sha1,rsa,ecc",ignore = true, inner = true)
    public String arithmetic;

    @JSONField(serialize = false)
    @ESBDesc(value = "jsonp callback名",ignore = true, inner = true)
    public String jsonpCallback;

    @JSONField(serialize = false)
    @ESBDesc(value = "API设备权限信息",ignore = true, inner = true)
    public String dtoken;

    @JSONField(serialize = false)
    @ESBDesc(value = "API调用安全信息",ignore = true, inner = true)
    public ESBSecur dsecur;

    @JSONField(serialize = false)
    @ESBDesc(value = "API用户权限信息",ignore = true, inner = true)
    public String utoken;

    @JSONField(serialize = false)
    @ESBDesc(value = "API调用安全信息",ignore = true, inner = true)
    public ESBSecur usecur;

    @JSONField(serialize = false)
    @ESBDesc(value = "user secret token 只存放于web/h5站点的secret cookie中，用于在不同domain间传递csrfToken",ignore = true, inner = true)
    public String stoken;//ssecur.securityLevel = SecurityType.SeceretUserToken

    @JSONField(serialize = false)
    @ESBDesc(value = "API调用安全信息",ignore = true, inner = true)
    public ESBSecur ssecur;

    @JSONField(serialize = false)
    @ESBDesc(value = "user refresh token ",ignore = true, inner = true)
    public String rtoken;//ssecur.securityLevel = SecurityType.SeceretUserToken

    @JSONField(serialize = false)
    @ESBDesc(value = "API调用安全信息",ignore = true, inner = true)
    public ESBSecur rsecur;

//    @JSONField(serialize = false)
//    @ESBDesc(value = "三方认证token,oauth隐藏在token中",ignore = true, inner = true)
//    public String otoken;
//
//    @JSONField(serialize = false)
//    @ESBDesc(value = "API调用安全信息",ignore = true, inner = true)
//    public ESBSecur osecur;

    @JSONField(serialize = false)
    @ESBDesc(value = "临时性token",ignore = true, inner = true)
    public String ttoken;

    @JSONField(serialize = false)
    @ESBDesc(value = "API调用安全信息",ignore = true, inner = true)
    public ESBSecur tsecur;

    @JSONField(serialize = false)
    @ESBDesc(value = "sso token",ignore = true, inner = true)
    public String ssoToken;

    @JSONField(serialize = false)
    @ESBDesc(value = "sso token",ignore = true, inner = true)
    public ESBSSOSecur ssoSecur;

    @JSONField(serialize = false)
    @ESBDesc(value = "post body",ignore = true, inner = true)
    public String body;

    @JSONField(serialize = false)
    @ESBDesc(value = "sso to domain【仅仅用于sso过程--起始站请求:必传参数】",ignore = true, inner = true)
    public String ssoToDomain;

    @JSONField(serialize = false)
    @ESBDesc(value = "sso to did【仅仅用于sso过程--起始站请求:必传参数】",ignore = true, inner = true)
    public long ssoToDid;

//    @JSONField(serialize = false)
//    @ESBDesc(value = "sso from aid；由域名+aid方可确认一个端【仅仅用于sso过程--起始站请求:必传参数】",ignore = true, inner = true)
//    public int ssoFromAid;

    @JSONField(serialize = false)
    @ESBDesc(value = "sso to aid【仅仅用于sso过程--起始站请求:必传参数】",ignore = true, inner = true)
    public int ssoToAid;

//    @JSONField(serialize = false)
//    @ESBDesc(value = "sso to scheme【仅仅用于sso过程--起始站请求:非必传参数】",ignore = true, inner = true)
//    public String ssoToScheme;

    @JSONField(serialize = false)
    @ESBDesc(value = "token exts,json串",ignore = true, inner = true)
    public String extsString;

//    @JSONField(serialize = false)
//    @ESBDesc(value = "sso from exts,json串【仅仅用于sso过程--起始站请求:非必传参数】",ignore = true, inner = true)
//    public ESBExts ssoExts;

    public void seed() {
        //传递给context
        ESBThreadLocal.put(ESBSTDKeys.CID_KEY,cid);
        ESBThreadLocal.put(ESBSTDKeys.TID_KEY,tid);
        ESBThreadLocal.put(ESBSTDKeys.L10N_KEY,l10n);

        ESBThreadLocal.put(ESBSTDKeys.AID_KEY,aid);
        ESBThreadLocal.put(ESBSTDKeys.DID_KEY,did);
        ESBThreadLocal.put(ESBSTDKeys.UID_KEY,uid);
        ESBThreadLocal.put(ESBSTDKeys.ACCT_KEY,acct);
        ESBThreadLocal.put(ESBSTDKeys.PID_KEY,pid);

        //ESB机器可以记录一些客户端参数
        ESBThreadLocal.put(ESBSTDKeys.CH_KEY,ch);
        ESBThreadLocal.put(ESBSTDKeys.SRC_KEY,src);
        ESBThreadLocal.put(ESBSTDKeys.SMP_KEY,spm);
        ESBThreadLocal.put(ESBSTDKeys.VIA_KEY,via);
        ESBThreadLocal.put(ESBSTDKeys.DNA_KEY,dna);
        ESBThreadLocal.put(ESBSTDKeys.UA_KEY,ua);
        ESBThreadLocal.put(ESBSTDKeys.CIP_KEY,cip);
        ESBThreadLocal.put(ESBSTDKeys.CVC_KEY,""+cvc);
        ESBThreadLocal.put(ESBSTDKeys.CVN_KEY,cvn);
        ESBThreadLocal.put(ESBSTDKeys.HOST_KEY,host);
        ESBThreadLocal.put(ESBSTDKeys.REFERER_KEY,referer);
        ESBThreadLocal.put(ESBSTDKeys.SELECTOR_KEY,selector);
        ESBThreadLocal.put(ESBSTDKeys.SIGN_KEY,signature);
        ESBThreadLocal.put(ESBSTDKeys.CAPTCHA_KEY,captcha);

        ESBMDC.put(ESBSTDKeys.AID_KEY,aid);
        ESBMDC.put(ESBSTDKeys.CID_KEY,cid);
        ESBMDC.put(ESBSTDKeys.DID_KEY,did);
        ESBMDC.put(ESBSTDKeys.PID_KEY,pid);
        ESBMDC.put(ESBSTDKeys.UID_KEY,uid);
        ESBMDC.put(ESBSTDKeys.ACCT_KEY,acct);
        ESBMDC.put(ESBSTDKeys.TID_KEY,tid);
        ESBMDC.put(ESBSTDKeys.CH_KEY,ch);
        ESBMDC.put(ESBSTDKeys.SRC_KEY,src);
        ESBMDC.put(ESBSTDKeys.SMP_KEY,spm);
        ESBMDC.put(ESBSTDKeys.VIA_KEY,via);
        ESBMDC.put(ESBSTDKeys.DNA_KEY,dna);
        ESBMDC.put(ESBSTDKeys.UA_KEY,ua);
        ESBMDC.put(ESBSTDKeys.CIP_KEY,cip);
        ESBMDC.put(ESBSTDKeys.CVC_KEY,""+cvc);
        ESBMDC.put(ESBSTDKeys.CVN_KEY,cvn);
        ESBMDC.put(ESBSTDKeys.HOST_KEY,host);
//        ESBMDC.put(ESBSTDKeys.SCHEME_KEY,scheme);
        ESBMDC.put(ESBSTDKeys.REFERER_KEY,referer);
        ESBMDC.put(ESBSTDKeys.MOENT_KEY,""+at);
        ESBMDC.put(ESBSTDKeys.CONTENT_TYPE_KEY, contentType);
        ESBMDC.put(ESBSTDKeys.L10N_KEY,l10n);
        ESBMDC.put(ESBSTDKeys.SELECTOR_KEY,selector);
        ESBMDC.put(ESBSTDKeys.SIGN_KEY,signature);
        ESBMDC.put(ESBSTDKeys.CAPTCHA_KEY,captcha);
        ESBMDC.put(ESBSTDKeys.SIGNATURE_METHOD_KEY,arithmetic);
        ESBMDC.put(ESBSTDKeys.JSONP_CALLBACK_KEY,jsonpCallback);
//        ESBMDC.put(ESBSTDKeys.JSONP_CALLBACK_KEY,jsonpCallback);
    }

    @Override
    public void clear() {
        super.clear();
        contentType = JSON_CONTENT;
        selector = null;
        signature = null;
        arithmetic = null;
        jsonpCallback = null;
        dtoken = null;
        dsecur = null;
        utoken = null;
        usecur = null;
        stoken = null;
        ssecur = null;
        rtoken = null;
        rsecur = null;
        ttoken = null;
        tsecur = null;
        body = null;
        ssoToken = null;
        ssoSecur = null;
        ssoToDid = 0;
        ssoToAid = 0;
        ssoToDomain = null;
//        ssoToScheme = null;
        extsString = null;
//        ssoExts = null;
        ESBMDC.clear();
        ESBThreadLocal.clear();
    }

    /**
     * 当前线程Context
     */
    private static final ThreadLocal<ESBAPIContext> ctxs = new ThreadLocal() {
        protected ESBAPIContext initialValue() {
            return new ESBAPIContext();
        }
    };

    private ESBAPIContext() {
    }

    /**
     * 获取当前ESB上下文
     */
    public static ESBAPIContext context() {
        ESBAPIContext ctx = ctxs.get();
        if (ctx.cid == null) {//表示新的一次调用,填充
            ctx.init();
        }
        ESBContext.putContext(ctx);//防止出现多分context
        return ctx;
    }

    public static void remove() {
        ctxs.get().clear();
        ESBContext.removeContext();
    }

    public static void fill(ESBAPIContext context, Map<String,String> params, Map<String,String> header, Map<String,ESBCookie> cookies, String body) {
        context.aid = parseValue(ESBSTDKeys.AID_KEY,params,header,cookies);
        context.did = parseValue(ESBSTDKeys.DID_KEY,context.aid,params,header,cookies);
//        if (context.did == null) {//需要兼容获取gw1.0中did信息
//            context.did = ESBCompatibility.getCookieDeviceId(cookies);
//        }
        context.pid = parseValue(ESBSTDKeys.PID_KEY,params,null,null);
//        if (context.pid == null) {
//            context.pid = ESBCompatibility.getParamsThirdPartyId(params);
//        }
        context.ch = parseValue(ESBSTDKeys.CH_KEY,context.aid,params,header,cookies);
        context.src = parseValue(ESBSTDKeys.SRC_KEY,context.aid,params,header,cookies);
        context.spm = parseValue(ESBSTDKeys.SMP_KEY,context.aid,params,header,cookies);
        context.dna = parseValue(ESBSTDKeys.DNA_KEY,context.aid,params,header,cookies);
        context.ua = parseValue(ESBSTDKeys.UA_KEY,context.aid,params,header,cookies);
//        if (context.ua == null) {
//            context.ua = ESBCompatibility.getParamsUserAgent(params);
//        }
        if (context.ua == null && header != null) {
            context.ua = header.get("User-Agent");
            if (context.ua == null) {
                context.ua = header.get("user-agent");
            }
        }
        context.cvn = parseValue(ESBSTDKeys.CVN_KEY,params,null,null);
//        if (context.cvn == null) {
//            context.cvn = ESBCompatibility.getParamsClientVersion(params);
//        }
        context.cvc = ESBT.integer(parseValue(ESBSTDKeys.CVC_KEY,params,null,null));
//        if (context.cvc == 0) {
//            context.cvc = ESBCompatibility.getParamsClientVersionCode(params);
//        }
        if (header != null) {
            context.host = header.get("Host");
            if (context.host == null) {
                context.host = header.get("host");
            }

            context.referer = header.get("Referer");
            if (context.referer == null) {
                context.referer = header.get("referer");
            }
        }
//        context.scheme = parseValue(ESBSTDKeys.SCHEME_KEY,params,header,null);

        context.contentType = parseValue(ESBSTDKeys.CONTENT_TYPE_KEY,params,null,null);
        if (context.contentType == null || context.contentType.length() == 0) {
            context.contentType = JSON_CONTENT;
        }
        context.l10n = parseValue(ESBSTDKeys.L10N_KEY,params,null,null);
//        if (context.l10n == null) {
//            context.l10n = ESBCompatibility.getParamsOrCookieLocalization(params,cookies);
//        }
        if (header != null && context.l10n == null) {
            context.l10n = header.get("Accept-Language");
            if (context.l10n == null) {
                context.l10n = header.get("accept-language");
            }
        }
        context.selector = parseValue(ESBSTDKeys.SELECTOR_KEY,params,null,null);
        context.signature = parseValue(ESBSTDKeys.SIGN_KEY,params,null,null);
        context.captcha = parseValue(ESBSTDKeys.CAPTCHA_KEY,params,null,null);
        context.arithmetic = parseValue(ESBSTDKeys.SIGNATURE_METHOD_KEY,params,null,null);
        context.jsonpCallback = parseValue(ESBSTDKeys.JSONP_CALLBACK_KEY,params,null,null);

        context.dtoken = parseValue(ESBSTDKeys.DEVICE_TOKEN_KEY,context.aid,params,header,cookies);
        context.dsecur = ESBTokenSign.parseDefaultToken(context.dtoken,context);

        context.utoken = parseValue(ESBSTDKeys.TOKEN_KEY,context.aid,params,header,cookies);
        if (context.utoken == null && header != null) {//支持Authorization
            //Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==
            String hd = header.get("Authorization");//
            if (hd == null) {
                hd = header.get("authorization");//
            }
            if (hd != null) {
                String[] hds = hd.split(" ");
                if (hds.length == 2) {
                    String scope = hds[0];//Basic
                    context.utoken = hds[1];//QWxhZGRpbjpvcGVuIHNlc2FtZQ==
                } else if (hds.length == 1) {
                    context.utoken = hds[0];
                }
            }

        }
        context.usecur = ESBTokenSign.parseDefaultToken(context.utoken,context);
        context.stoken = parseValue(ESBSTDKeys.SECRET_TOKEN_KEY,context.aid,params,header,cookies);
        context.ssecur = ESBTokenSign.parseDefaultToken(context.stoken,context);

        context.rtoken = parseValue(ESBSTDKeys.REFRESH_TOKEN_KEY,context.aid,params,header,cookies);
        context.rsecur = ESBTokenSign.parseDefaultToken(context.rtoken,context);

//        context.otoken = null;
//        context.osecur = null;
        context.ttoken = parseValue(ESBSTDKeys.TEMP_TOKEN_KEY,context.aid,params,header,cookies);
        context.tsecur = ESBTokenSign.parseDefaultToken(context.ttoken,null);

        //sso部分解析
        context.ssoToDid = ESBT.longInteger(parseValue(ESBSTDKeys.SSO_TO_DID_KEY,context.aid,params,header,cookies));
//        context.ssoFromAid = ESBT.integer(parseValue(ESBSTDKeys.SSO_FROM_AID_KEY,context.aid,params,header,cookies));
        context.ssoToAid = ESBT.integer(parseValue(ESBSTDKeys.SSO_TO_AID_KEY,context.aid,params,header,cookies));
        context.ssoToDomain = parseValue(ESBSTDKeys.SSO_TO_DOMAIN_KEY,context.aid,params,header,cookies);
        context.extsString = parseValue(ESBSTDKeys.TOKEN_EXTS_KEY,context.aid,params,header,cookies);
        if (!StringUtils.isEmpty(context.extsString)) {//直接覆盖不是很好，最好是merge
            try {
                context.exts = JSON.parseObject(context.extsString, ESBExts.class);
            } catch (Throwable e) {}
        }
        context.ssoToken = parseValue(ESBSTDKeys.SSO_TOKEN_KEY,context.aid,params,header,cookies);
        context.ssoSecur = ESBTokenSign.parseSSOToken(context.ssoToken,context);
        if (context.ssoSecur != null) {
            context.ssoToDid = context.ssoSecur.tdid;
//            context.ssoFromAid = context.ssoSecur.faid;
            context.ssoToAid = context.ssoSecur.taid;
            context.ssoToDomain = context.ssoSecur.tdomain;
        }

        context.body = body;

        //获取id的
        fillContextClientIP(context,header);
    }

    private static final String HTTP_HEADER_SEPARATE = ", ";
    public static void fillContextClientIP(ESBContext context,Map<String,String> header) {
        String ip = null;
        if (header != null) {
            ip = header.get("x-forwarded-for");
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = header.get("Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = header.get("http-x-forwarded-for");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = header.get("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = header.get("remote-addr");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = header.get(ESBSTDKeys.CIP_KEY);//request.getRemoteAddr();
            }
            //
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = header.get("Remote Address");//request.getRemoteAddr();
            }
        }

        //X-Forwarded-For：192.168.1.110, 192.168.1.120, 192.168.1.130, 192.168.1.100
        //设置cip和via
        if (ip  ==   null   ||  ip.length()  ==   0   ||  "unknown" .equalsIgnoreCase(ip)) {
            context.via = ESBUUID.getLocalIP();
//            context.cip = "127.0.0.1";
        } else {
            String[] ips = ip.split(",");
            if (ips != null && ips.length > 0) {
                context.cip = ips[0];
            } else {
                context.cip = ip;
            }
            context.via = ip + HTTP_HEADER_SEPARATE + ESBUUID.getLocalIP();
        }
    }

    @Override
    public String getValue(String key) {
        String value = super.getValue(key);
        if (value != null) {
            return value;
        }

        //扩展部分
        if (ESBSTDKeys.CONTENT_TYPE_KEY.equals(key)) {
            return this.contentType;
        } else if (ESBSTDKeys.SELECTOR_KEY.equals(key)) {
            return this.selector;
        } else if (ESBSTDKeys.SIGN_KEY.equals(key)) {
            return this.signature;
        } else if (ESBSTDKeys.CAPTCHA_KEY.equals(key)) {
            return this.captcha;
        } else if (ESBSTDKeys.SIGNATURE_METHOD_KEY.equals(key)) {
            return this.arithmetic;
        } else if (ESBSTDKeys.JSONP_CALLBACK_KEY.equals(key)) {
            return this.jsonpCallback;
        } else if (ESBSTDKeys.DEVICE_TOKEN_KEY.equals(key)) {
            return this.dtoken;
        } else if (ESBSTDKeys.TOKEN_KEY.equals(key)) {
            return this.utoken;
        } else if (ESBSTDKeys.SECRET_TOKEN_KEY.equals(key)) {
            return this.stoken;
        } else if (ESBSTDKeys.REFRESH_TOKEN_KEY.equals(key)) {
            return this.rtoken;
        } else if (ESBSTDKeys.TEMP_TOKEN_KEY.equals(key)) {
            return this.ttoken;
        } else if (ESBSTDKeys.SSO_TOKEN_KEY.equals(key)) {
            return this.ssoToken;
        } else if (ESBSTDKeys.POST_BODY_KEY.equals(key)) {
            return this.body;
        } else if (ESBSTDKeys.SSO_TO_DOMAIN_KEY.equals(key)) {
            return this.ssoToDomain;
        } else if (ESBSTDKeys.TOKEN_EXTS_KEY.equals(key)) {
            return this.extsString;
        }

        return null;//ESBCompatibility.getContextValue(this,key);
    }

    public final String getCookieDomain() {
        if (this.host == null || this.host.length() == 0) { return ""; }

        //只取主域名
        String h = this.host;
        int idx = h.lastIndexOf(":");
        if (idx >= 0 && idx < h.length()) {
            h = h.substring(0,idx);
        }
        String[] strs = h.split("\\.");
        if (strs.length > 2) {
            return "." + strs[strs.length - 2] + "." + strs[strs.length - 1];
        } else if (strs.length == 2) {
            return strs[strs.length - 2] + "." + strs[strs.length - 1];
        } else {
            return h;
        }
    }
}
