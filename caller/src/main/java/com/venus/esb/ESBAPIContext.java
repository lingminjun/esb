package com.venus.esb;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.venus.esb.annotation.ESBDesc;
import com.venus.esb.config.ESBConfigCenter;
import com.venus.esb.lang.*;
import com.venus.esb.sign.ESBTokenSign;
import com.venus.esb.sign.ESBUUID;
import com.venus.esb.utils.MD5;

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

        //记录会话标识
        ESBThreadLocal.put(ESBSTDKeys.GUID_KEY,guid);

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
        ESBMDC.put(ESBSTDKeys.GUID_KEY,guid); // 会话标识
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
        context.tml = "" + (int)((0xff000000 & ESBT.integer(context.aid)) >>> 24);
        context.app = "" + (int)(0x00ffffff & ESBT.integer(context.aid));
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
        // 必须从dtoken中获取
//        context.dna = parseValue(ESBSTDKeys.DNA_KEY,context.aid,params,header,cookies);
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
        if (!ESBT.isEmpty(context.extsString)) {//直接覆盖不是很好，最好是merge
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

    public final void injectToken(ESBToken token) {
        //登录失败,将数据清除
        if (!token.success) {
            token.token = null;
            token.stoken = null;
            token.refresh = null;
            token.key = null;
            token.expire = 0;

            //反向清除
            this.utoken = null;
            this.stoken = null;
            this.rtoken = null;

            this.usecur = null;
            this.ssecur = null;
            this.rsecur = null;

            //清除cookie token
            pushTokenCookie("","", "");

        } else {
            //自动注入token
            ESBTokenSign.injectDefaultToken(token,this);

            //反向注入
            this.utoken = token.token;
            this.stoken = token.stoken;
            this.rtoken = token.refresh;

            //注入token到cookie
            pushTokenCookie(token.token,token.stoken, token.user);
        }
    }

    public final void autoInjectDeviceToken() {
        //自动注入did
        if (!ESBT.isEmpty(this.did)) {
            return;
        }

        this.did = "" + (-ESBUUID.genDID());
        ESBDeviceToken token = new ESBDeviceToken();
        token.scope = "device";
        token.success = true;
        injectDeviceToken(token);

        //只有自动注入时添加dsecur
        if (this.dtoken != null) {
            this.dsecur = ESBTokenSign.parseDefaultToken(this.dtoken, this);
        }
    }

    public final void injectDeviceToken(ESBDeviceToken token) {
        //登录失败,将数据清除
        if (!token.success) {
            token.token = null;
            token.key = null;
            //注册设备失败，不清除原有token
//            pushDeviceTokenCookie(context,"");
        } else {
            //替换后端返回的设备号
            if (token.did != null && !token.did.equals(this.did)) {
                this.did = token.did;
            }

            //自动注入token
            ESBTokenSign.injectDeviceToken(token,this);

            this.dtoken = token.token;//新的token

            pushDeviceTokenCookie(token.token);
        }
    }

    public final void pushTokenCookie(String utoken, String stoken, String user) {

        String domain = this.getCookieDomain();
        {
            ESBCookie cookie = new ESBCookie();
            cookie.domain = domain;
            cookie.name = this.aid + ESBSTDKeys.TOKEN_KEY;
            cookie.value = (utoken == null) ? ESBT.string(this.utoken,"") : utoken;
            cookie.httpOnly = false; //js可读写，主要是方便客户端主动清除。每次请求带上来，验证有效性
            cookie.secure = false;
            this.putCookie(cookie.name, cookie);
        }

        {
            ESBCookie cookie = new ESBCookie();
            cookie.domain = domain;
            cookie.name = this.aid + ESBSTDKeys.SECRET_TOKEN_KEY;
            cookie.value = (stoken == null) ? ESBT.string(this.stoken,"") : stoken;
            cookie.httpOnly = true; //js不可访问
            cookie.secure = true;     //只在https下访问,sso必须走https
            this.putCookie(cookie.name, cookie);
        }

        if (user != null && user.length() != 0) {
            ESBCookie cookie = new ESBCookie();
            cookie.domain = domain;
            cookie.name = this.aid + ESBSTDKeys.USER_INFO_KEY;
            cookie.value = user;
            cookie.httpOnly = false; //
            cookie.secure = false;     //
            this.putCookie(cookie.name, cookie);
        }
    }

    public final void pushDeviceTokenCookie(String dtoken) {
        String domain = this.getCookieDomain();

        if (this.did != null && this.did.length() > 0){
            ESBCookie cookie = new ESBCookie();
            cookie.domain = domain;
            cookie.name = ESBSTDKeys.DID_KEY;
            cookie.value = this.did;
            cookie.httpOnly = false; //前段可读（sso需要读取），如果随意修改将会导致dtoken验证通不过
            cookie.secure = false;
            this.putCookie(cookie.name, cookie);
        }

        {
            ESBCookie cookie = new ESBCookie();
            cookie.domain = domain;
            cookie.name = ESBSTDKeys.DEVICE_TOKEN_KEY;
            cookie.value = (dtoken == null) ? ESBT.string(this.dtoken,"") : dtoken;
            cookie.httpOnly = true; //客户端不对属性
            cookie.secure = false;
            this.putCookie(cookie.name, cookie);
        }

    }

    public final void clearAllTokenCookie() {
        String domain = this.getCookieDomain();

        // did
        {
            ESBCookie cookie = new ESBCookie();
            cookie.domain = domain;
            cookie.name = ESBSTDKeys.DID_KEY;
            cookie.value = "";
            cookie.maxAge = 0;
            cookie.httpOnly = false; //前段可读（sso需要读取），如果随意修改将会导致dtoken验证通不过
            cookie.secure = false;
            this.putCookie(cookie.name, cookie);
        }

        // dtoken
        {
            ESBCookie cookie = new ESBCookie();
            cookie.domain = domain;
            cookie.name = ESBSTDKeys.DEVICE_TOKEN_KEY;
            cookie.value = "";
            cookie.maxAge = 0;
            cookie.httpOnly = true; //客户端不对属性
            cookie.secure = false;
            this.putCookie(cookie.name, cookie);
        }

        // utoken
        {
            ESBCookie cookie = new ESBCookie();
            cookie.domain = domain;
            cookie.name = this.aid + ESBSTDKeys.TOKEN_KEY;
            cookie.value = "";
            cookie.maxAge = 0;
            cookie.httpOnly = false; //js可读写，主要是方便客户端主动清除。每次请求带上来，验证有效性
            cookie.secure = false;
            this.putCookie(cookie.name, cookie);
        }

        // stoken
        {
            ESBCookie cookie = new ESBCookie();
            cookie.domain = domain;
            cookie.name = this.aid + ESBSTDKeys.SECRET_TOKEN_KEY;
            cookie.value = "";
            cookie.maxAge = 0;
            cookie.httpOnly = true; //js不可访问
            cookie.secure = true;     //只在https下访问,sso必须走https
            this.putCookie(cookie.name, cookie);
        }
    }

    private static final String DEFAULT_SALT    = "m.captcha.123";
    private static final long CODE_VALIDITY     = 15 * 60 * 1000; //15分钟有效
    /**
     * 验证验证码
     *
     * @param salt    加盐
     * @param code    用户输入的验证码
     * @param session tupiansessuib
     * @return 是否正确
     */
    public static boolean verifyCaptcha(String salt, String code, String session) throws ESBException {
        if (ESBT.isEmpty(salt)) {
            salt = DEFAULT_SALT;
        }

        // 验证验证码有效性
        String ssn = session.trim();
        if (!ESBT.isEmpty(code) && !ESBT.isEmpty(ssn) && ssn.length() > 32) {
            String coreSession = ssn.substring(0,32);
            String timeString = ssn.substring(32);
            long time = ESBT.longInteger(timeString) + ESBConsts.TIME_2018_01_01;
            // 简单过期判断，防止反复使用同一个session和code，
            // 没有加签，是否存在安全隐患？？？实际无所谓，主要防止已经验证通过的code和session
            if (time > System.currentTimeMillis()) {
                String sign = MD5.md5(salt + code.trim() + ESBConfigCenter.instance().getAesKey());
                return coreSession.toLowerCase().equals(sign);
            } else {
                throw ESBExceptionCodes.CAPTCHA_INVALID("验证过期");
            }
        }

        return false;
    }

    public static String captchaSession(String salt, String code) {
        String session = MD5.md5(salt + code + ESBConfigCenter.instance().getAesKey());
        session += (System.currentTimeMillis() - ESBConsts.TIME_2018_01_01 + CODE_VALIDITY);
        return session;
    }
}
