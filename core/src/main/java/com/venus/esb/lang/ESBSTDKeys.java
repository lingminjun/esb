package com.venus.esb.lang;

import com.venus.esb.annotation.ESBDesc;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by lingminjun on 17/5/9.
 * ESB Standard key defined.
 * ESB标准的参数定义
 * 给上下文注入参数
 * MDC(Mapped Diagnostic Contexts)注入日志
 *     除_tk、_stk、_rtk、_dtk、_ttk、_sso_tk、_sig和_cookie敏感数据不支持DMC,sso因为是临时性的,都不支持
 * _tk、_stk、_uinfo表明要认证时(ESBToken)将被写入到cookie,aid_tk、aid_stk、aid_uinfo的key设置
 */
@ESBDesc("ESB标准的参数定义,除_tk、_stk、_rtk、_dtk、_ttk、_sso_tk、_sig和_cookie敏感数据不支持DMC,其他都支持")
public final class ESBSTDKeys {

    private static Set<String> _keys = new HashSet<>(); //不需要客户端显示传递的接口（用于生成代码时声明）
    private static Set<String> _akeys = new HashSet<>(); //跟着应用id存放
    private static Set<String> _nkeys = new HashSet<>(); //不允许放到cookie中

    //esb上下文中通用的key,且以下key是支持auto write的参数
    @ESBDesc("application id 应用编号, 以XXX应用为例：pc端1, h5端2, iOS客户端3, android客户端4, 微信小程序5, 等等")
    public static final String AID_KEY = "_aid";
    static {
        _keys.add(AID_KEY);
        _nkeys.add(AID_KEY);
    }

    @ESBDesc("call id:客户端调用编号. ")
    public static final String CID_KEY = "_cid";
    static {
        _keys.add(CID_KEY);
        _nkeys.add(CID_KEY);
    }

    @ESBDesc("device id 设备标示符. 支持通过Cookie注入获取url中的值, cookie中存储负值")
    public static final String DID_KEY = "_did";
//    @ESBDesc("device id 设备标示符, 存储在cookie中的名字")
//    public static final String cookieDeviceId = "__da";
    static {
        _keys.add(DID_KEY);
    }

    @ESBDesc("第三方集成的身份标识(第三方集成情景下使用)")
    public static final String PID_KEY = "_pid";
//    @ESBDesc("第三方集成的身份标识(第三方集成情景下使用)")
//    public static final String thirdPartyId = "_tpid";
    static {
        _keys.add(PID_KEY);
    }

    @ESBDesc("日志追踪,parent id")
    public static final String PARENT_CID_KEY = "_prev_cid";
    static {
        _keys.add(PARENT_CID_KEY);
        _nkeys.add(PARENT_CID_KEY);
    }

    @ESBDesc("日志追踪,client id")
    public static final String CLIENT_NAME_KEY = "_clt_nm";
    static {
        _keys.add(CLIENT_NAME_KEY);
    }

    @ESBDesc("user id 用户标示符")
    public static final String UID_KEY = "_uid";
    static {
        _keys.add(UID_KEY);
    }

    @ESBDesc("account id 用户标示符")
    public static final String ACCT_KEY = "_acct";
    static {
        _keys.add(ACCT_KEY);
    }

    @ESBDesc("trace id 用于全局追踪")
    public static final String TID_KEY = "_tid";
    static {
        _keys.add(TID_KEY);
        _nkeys.add(TID_KEY);
    }

    @ESBDesc("客户端应用安装渠道, 支持通过Cookie注入获取url中的值")
    public static final String CH_KEY = "_ch";
    static {
        _keys.add(CH_KEY);
    }

    @ESBDesc("来源,用于追踪引流拉新路径")
    public static final String SRC_KEY = "_src";
    static {
        _keys.add(SRC_KEY);
    }

    @ESBDesc("自媒体营销平台 推广追踪")
    public static final String SMP_KEY = "_smp";
    static {
        _keys.add(SMP_KEY);
    }

    @ESBDesc("网元(客户端或Proxy)的主机名或网络地址（包含端口号,暂且为客服端ip）")
    public static final String VIA_KEY = "_via";
    static {
        _keys.add(VIA_KEY);
    }

    @ESBDesc("设备指纹信息,可用于风控")
    public static final String DNA_KEY = "_dna";
    static {
        _keys.add(DNA_KEY);
    }

    @ESBDesc("user agent注入 不支持在url中使用该参数")
    public static final String UA_KEY = "_ua";//userAgent
//    @ESBDesc("user agent注入 不支持在url中使用该参数")
//    public static final String userAgent = "_userAgent";
    static {
        _keys.add(UA_KEY);
    }

    @ESBDesc("client ip 用户ip. 支持通过Cookie注入获取url中的值")
    public static final String CIP_KEY = "_cip";//client ip
    static {
        _keys.add(CIP_KEY);
    }

    @ESBDesc("version 客户端版本 : 1.0.0")
    public static final String CVN_KEY = "_cvn";//client version name
//    @ESBDesc("version code 客户端数字版本号. 支持通过Cookie注入获取url中的值")
//    public static final String versionCode = "_vc";
    static {
        _keys.add(CVN_KEY);
    }

    @ESBDesc("version 客户端版本号 : 10")
    public static final String CVC_KEY = "_cvc";//client version code
//    @ESBDesc("version 客户端版本名.")
//    public static final String versionName = "_vn";
    static {
        _keys.add(CVC_KEY);
    }

    @ESBDesc("当前站点host")
    public static final String HOST_KEY = "_host";//客户端请求host
    static {
        _keys.add(HOST_KEY);
        _nkeys.add(HOST_KEY);
    }

    @ESBDesc("当前站点scheme")
    public static final String SCHEME_KEY = "_scheme";//客户端请求scheme,注意从request中获取
    static {
        _keys.add(SCHEME_KEY);
        _nkeys.add(SCHEME_KEY);
    }

    @ESBDesc("当前站点referer")
    public static final String REFERER_KEY = "_referer";//客户端请求referer
    static {
        _keys.add(REFERER_KEY);
        _nkeys.add(REFERER_KEY);
    }

    @ESBDesc("调用时刻")
    public static final String MOENT_KEY = "_at";//客户端请求时间
    static {
        _keys.add(MOENT_KEY);
        _nkeys.add(MOENT_KEY);
    }


    @ESBDesc("format 返回值格式,取值为枚举SerializeType中的定义,取值范围JSON/XML")
    public static final String CONTENT_TYPE_KEY = "_ft";
    static {
        _keys.add(CONTENT_TYPE_KEY);
    }

    @ESBDesc("Localization[l10n] 用于返回信息国际化. 兼容HTTP Header 'Accept-Language'. 可支持通过Cookie注入获取url中的值")
    public static final String L10N_KEY = "_l10n";
//    public static final String L10N_KEY = "_lo";
    static {
        _keys.add(L10N_KEY);
    }

    @ESBDesc("user token 代表访问者身份,完成用户登入流程后获取")
    public static final String TOKEN_KEY = "_tk";
    static {
        _keys.add(TOKEN_KEY);
        _akeys.add(TOKEN_KEY);
    }

    @ESBDesc("user token 中的exts数据,将会被在必要的场景传输,oss时参数传递")
    public static final String TOKEN_EXTS_KEY = "_tk_exts";
    static {
        _keys.add(TOKEN_EXTS_KEY);
        _nkeys.add(TOKEN_EXTS_KEY);
    }

    @ESBDesc("user secret token 只存放于web/h5站点的secret cookie中，用于在不同domain间传递csrfToken")
    public static final String SECRET_TOKEN_KEY = "_stk";
    static {
//        _keys.add(SECRET_TOKEN_KEY);
        _akeys.add(SECRET_TOKEN_KEY);
    }

    @ESBDesc("user info")
    public static final String USER_INFO_KEY = "_uinfo";
    static {
        _keys.add(USER_INFO_KEY);
        _akeys.add(USER_INFO_KEY);
    }

    @ESBDesc("refresh token 刷新token需要，不能放入cookie")
    public static final String REFRESH_TOKEN_KEY = "_rtk";
    static {
//        _keys.add(REFRESH_TOKEN_KEY);
        _nkeys.add(REFRESH_TOKEN_KEY);
    }

    @ESBDesc("device token 代表访问设备的身份,完成设备注册流程后获取")
    public static final String DEVICE_TOKEN_KEY = "_dtk";
    static {
        _keys.add(DEVICE_TOKEN_KEY);
    }

//    @ESBDesc("oauth token 代表第三方平台给该用户的授权身份")
//    public static final String OAUTH_TOKEN_KEY = "_otk";

    @ESBDesc("temp token 临时验证权token")
    public static final String TEMP_TOKEN_KEY = "_ttk";
    static {
        _keys.add(TEMP_TOKEN_KEY);
        _nkeys.add(TEMP_TOKEN_KEY);
    }

//    @ESBDesc("selector请求的方法选表: domain.module.methodName 或者 module.methodName")
//    public static final String SELECTOR_KEY = "_sel";
    @ESBDesc("method 请求的方法名: domain.module.methodName 或者 module.methodName")
    public static final String SELECTOR_KEY = "_mt";
    static {
        _keys.add(SELECTOR_KEY);
        _nkeys.add(SELECTOR_KEY);
    }

    @ESBDesc("signature 参数字符串签名")
    public static final String SIGN_KEY = "_sig";
    static {
        _keys.add(SIGN_KEY);
        _nkeys.add(SIGN_KEY);
    }

    @ESBDesc("CAPTCHA:人机是被参数,需要以此来界定是否问人操作,而非机器")
    public static final String CAPTCHA_KEY = "_captcha";
    static {
        _keys.add(CAPTCHA_KEY);
        _nkeys.add(CAPTCHA_KEY);
    }

//    @ESBDesc("business id 业务流水号, 用于做幂等判断, 风控等. 支持通过Cookie注入获取url中的值")
//    public static final String businessId = "_bid";


    @ESBDesc("signature method 签名算法 hmac,md5,sha1,rsa,ecc")
    public static final String SIGNATURE_METHOD_KEY = "_sm";
    static {
        _keys.add(SIGNATURE_METHOD_KEY);
        _nkeys.add(SIGNATURE_METHOD_KEY);
    }

//    @ESBDesc("动态密码验证对应的手机号")
//    public static final String phoneNumber = "_pn";
//
//    @ESBDesc("动态密码验证对应的动态码")
//    public static final String dynamic = "_dyn";

    @ESBDesc("jsonp callback名 名字字母开头任意一个字母或数字或下划线,5到64位:^[A-Za-z]\\w{5,64} 【用于sso过程:必传参数】")
    public static final String JSONP_CALLBACK_KEY = "_cb";
    static {
        _keys.add(JSONP_CALLBACK_KEY);
        _nkeys.add(JSONP_CALLBACK_KEY);
    }

//    @ESBDesc("接受的语种类")
//    public static final String ACCEPT_LANGUAGE_KEY = "_al";

    @ESBDesc("cookie注入 不支持在url中使用该参数")
    public static final String COOKIE_KEY = "_cookie";
    static {
        _keys.add(COOKIE_KEY);
        _nkeys.add(COOKIE_KEY);
    }

    @ESBDesc("sso token【仅仅用于sso过程--目标站请求:必传参数】")
    public static final String SSO_TOKEN_KEY = "_sso_tk";
    static {
//        _keys.add(SSO_TOKEN_KEY);
        _nkeys.add(SSO_TOKEN_KEY);
    }

    @ESBDesc("sso to domain【仅仅用于sso过程--起始站请求:必传参数】")
    public static final String SSO_TO_DOMAIN_KEY = "_sso_domain";
    static {
//        _keys.add(SSO_TO_DOMAIN_KEY);
        _nkeys.add(SSO_TO_DOMAIN_KEY);
    }

    @ESBDesc("sso to did【仅仅用于sso过程--起始站请求:必传参数】")
    public static final String SSO_TO_DID_KEY = "_sso_did";
    static {
//        _keys.add(SSO_TO_DID_KEY);
        _nkeys.add(SSO_TO_DID_KEY);
    }

//    @ESBDesc("sso from aid【仅仅用于sso过程--起始站请求:必传参数】")
//    public static final String SSO_FROM_AID_KEY = "_sso_f_aid";
//    static {
//        _keys.add(SSO_FROM_AID_KEY);
//        _nkeys.add(SSO_FROM_AID_KEY);
//    }

    @ESBDesc("sso to aid【仅仅用于sso过程--起始站请求:必传参数】")
    public static final String SSO_TO_AID_KEY = "_sso_aid";
    static {
//        _keys.add(SSO_TO_AID_KEY);
        _nkeys.add(SSO_TO_AID_KEY);
    }

//    @ESBDesc("sso to scheme【仅仅用于sso过程--起始站请求:非必传参数】")
//    public static final String SSO_TO_SCHEME_KEY = "_sso_scheme";
//    static {
//        _keys.add(SSO_TO_SCHEME_KEY);
//        _nkeys.add(SSO_TO_SCHEME_KEY);
//    }

//    @ESBDesc("传入参数字符集(参数需要被urlencode). 支持通过Cookie注入获取url中的值")
//    public static final String inputCharset = "_input_charset";

    /**
     * 用于内部参数注入的标识符，用于指示在第三方集成的场景下网关向后台传递整个post表单
     */
    @ESBDesc("用于内部参数注入的标识符，用于指示在第三方集成的场景下网关向后台传递整个post表单")
    public static final String POST_BODY_KEY = "_pb";
    static {
        _keys.add(POST_BODY_KEY);
        _nkeys.add(POST_BODY_KEY);
    }

//    @ESBDesc("mock flag 专门用于录制回放测试")
//    public static final String MOCK_FLAG_KEY = "_mock_flag";
//    static {
//        _keys.add(MOCK_FLAG_KEY);
//        _nkeys.add(MOCK_FLAG_KEY);
//    }

    @ESBDesc("合并请求专用参数，链式请求标记")
    public static final String MULT_APIS_SERIAL_CALL_KEY = "_mult_serial";
    static {
        _nkeys.add(MULT_APIS_SERIAL_CALL_KEY);
    }

    @ESBDesc("合并请求专用参数，忽略主错误（不因第一个请求异常终止）。注意：默认为false")
    public static final String MULT_APIS_IGNORE_MASTER_ERROR_KEY = "_mult_ignore";
    static {
        _nkeys.add(MULT_APIS_IGNORE_MASTER_ERROR_KEY);
    }


    public static boolean isSTDKey(String key) {
        return _keys.contains(key);
    }

    public static boolean isApplicationCookieKey(String key) {
        return _akeys.contains(key);
    }

    public static boolean notCookieKey(String key) {
        return _nkeys.contains(key);
    }
}
