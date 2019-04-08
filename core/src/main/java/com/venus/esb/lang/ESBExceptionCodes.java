package com.venus.esb.lang;

import com.venus.esb.idl.ESBAPICode;
import com.venus.esb.l10n.ESBI18N;

/**
 * Created by lingminjun on 17/4/22.
 */
public final class ESBExceptionCodes {
    public final static String ESB_EXCEPTION_DOMAIN = "ESB";

    public final static int SUCCESS_CODE = 0;//正确code
    public static ESBException SUCCESS(String reason) {
        return new ESBException("成功",ESB_EXCEPTION_DOMAIN,SUCCESS_CODE,"");
    }

    public static ESBAPICode SUCCESS() {
        ESBAPICode code = new ESBAPICode();
        code.domain = ESB_EXCEPTION_DOMAIN;
        code.name = "SUCCESS";
        code.desc = ESBI18N.l10n("成功");
        return code;
    }

    public final static int UNKNOWN_ERROR_CODE = -100;//"服务端返回未知错误"
    public static ESBException UNKNOWN_ERROR(String reason) {
        return new ESBException("未知错误",ESB_EXCEPTION_DOMAIN,UNKNOWN_ERROR_CODE,reason);
    }

    public final static int INTERNAL_SERVER_ERROR_CODE = -101;
    public final static ESBException INTERNAL_SERVER_ERROR(String reason) {
        return new ESBException("服务器异常",ESB_EXCEPTION_DOMAIN,INTERNAL_SERVER_ERROR_CODE,reason);
    }

    public final static int DUBBO_SERVICE_NOTFOUND_CODE = -107;
    public static ESBException DUBBO_SERVICE_NOTFOUND(String reason) {
        return new ESBException("服务未找到",ESB_EXCEPTION_DOMAIN,DUBBO_SERVICE_NOTFOUND_CODE,reason);
    }

    public final static int DUBBO_NETWORK_EXCEPTION_CODE = -110;
    public static ESBException DUBBO_NETWORK_EXCEPTION(String reason) {
        return new ESBException("服务网络异常",ESB_EXCEPTION_DOMAIN,DUBBO_NETWORK_EXCEPTION_CODE,reason);
    }

    public final static int DUBBO_BIZ_EXCEPTION_CODE = -111;
    public static ESBException DUBBO_BIZ_EXCEPTION(String reason) {
        return new ESBException("服务内部异常",ESB_EXCEPTION_DOMAIN,DUBBO_BIZ_EXCEPTION_CODE,reason);
    }

    public final static int DUBBO_FORBIDDEN_EXCEPTION_CODE = -112;
    public static ESBException DUBBO_FORBIDDEN_EXCEPTION(String reason) {
        return new ESBException("服务请求被拒绝",ESB_EXCEPTION_DOMAIN,DUBBO_FORBIDDEN_EXCEPTION_CODE,reason);
    }

    public final static int DUBBO_SERVICE_TIMEOUT_CODE = -108;
    public static ESBException DUBBO_SERVICE_TIMEOUT(String reason) {
        return new ESBException("服务请求超时",ESB_EXCEPTION_DOMAIN,DUBBO_SERVICE_TIMEOUT_CODE,reason);
    }

    public final static int DUBBO_SERVICE_ERROR_CODE = -109;
    public static ESBException DUBBO_SERVICE_ERROR(String reason) {
        return new ESBException("未知错误",ESB_EXCEPTION_DOMAIN,DUBBO_SERVICE_ERROR_CODE,reason);
    }



    public final static int UNKNOWN_METHOD_CODE = -120;
    public static ESBException UNKNOWN_METHOD(String reason) {
        return new ESBException("服务未找到",ESB_EXCEPTION_DOMAIN,UNKNOWN_METHOD_CODE,reason);
    }

    public final static int PARSE_ERROR_CODE = -200;//"解析错误"
    public static ESBException PARSE_ERROR(String reason) {
        return new ESBException("请求解析错误",ESB_EXCEPTION_DOMAIN,PARSE_ERROR_CODE,reason);
    }

    public final static int API_UPGRADE_CODE = -220;
    public static ESBException API_UPGRADE(String reason) {
        return new ESBException("接口已升级",ESB_EXCEPTION_DOMAIN,API_UPGRADE_CODE,reason);
    }

    public final static int MOBILE_NOT_REGIST_CODE = -250;
    public static ESBException MOBILE_NOT_REGIST(String reason) {
        return new ESBException("手机号未绑定",ESB_EXCEPTION_DOMAIN,MOBILE_NOT_REGIST_CODE,reason);
    }

    public final static int DYNAMIC_CODE_ERROR_CODE = -260;
    public static ESBException DYNAMIC_CODE_ERROR(String reason) {
        return new ESBException("手机动态密码错误",ESB_EXCEPTION_DOMAIN,DYNAMIC_CODE_ERROR_CODE,reason);
    }

    public final static int UPLINK_SMS_NOT_RECEIVED_CODE = -270;
    public static ESBException UPLINK_SMS_NOT_RECEIVED(String reason) {
        return new ESBException("上行短信尚未收到",ESB_EXCEPTION_DOMAIN,UPLINK_SMS_NOT_RECEIVED_CODE,reason);
    }

    public final static int APPID_NOT_EXIST_CODE = -280;
    public static ESBException APPID_NOT_EXIST(String reason) {
        return new ESBException("应用id不存在",ESB_EXCEPTION_DOMAIN,APPID_NOT_EXIST_CODE,reason);
    }

    public final static int PARAMETER_ERROR_CODE = -140;
    public static ESBException PARAMETER_ERROR(String reason) {
        return new ESBException("参数错误",ESB_EXCEPTION_DOMAIN,PARAMETER_ERROR_CODE,reason);
    }

    public final static int ACCESS_DENIED_CODE = -160;
    public static ESBException ACCESS_DENIED(String reason) {
        return new ESBException("访问被拒绝",ESB_EXCEPTION_DOMAIN,ACCESS_DENIED_CODE,reason);
    }

    public final static int NEED_CAPTCHA_CODE = -162;//人机验证,可以采用多个手段,图片或者滑块
    public static ESBException NEED_CAPTCHA(String reason) {
        return new ESBException("检查到访问异常",ESB_EXCEPTION_DOMAIN,NEED_CAPTCHA_CODE,reason);
    }

    public final static int CAPTCHA_INVALID_CODE = -164;//人机验证,可以采用多个手段,图片或者滑块
    public static ESBException CAPTCHA_INVALID(String reason) {
        return new ESBException("验证过期",ESB_EXCEPTION_DOMAIN,CAPTCHA_INVALID_CODE,reason);
    }

    public final static int CAPTCHA_ERROR_CODE = -166;//人机验证,可以采用多个手段,图片或者滑块
    public static ESBException CAPTCHA_ERROR(String reason) {
        return new ESBException("验证错误",ESB_EXCEPTION_DOMAIN,CAPTCHA_ERROR_CODE,reason);
    }


    public final static int SIGNATURE_ERROR_CODE = -180;
    public static ESBException SIGNATURE_ERROR(String reason) {
        return new ESBException("签名错误",ESB_EXCEPTION_DOMAIN,SIGNATURE_ERROR_CODE,reason);
    }

    public final static int NO_RIGHT_ACCESS_ERROR_CODE = -183;
    public static ESBException NO_RIGHT_ACCESS_ERROR(String reason) {
        return new ESBException("无权访问",ESB_EXCEPTION_DOMAIN,NO_RIGHT_ACCESS_ERROR_CODE,reason);
    }

    public final static int ILLEGAL_MULTIAPI_ASSEMBLY_CODE = -190;
    public static ESBException ILLEGAL_MULTIAPI_ASSEMBLY(String reason) {
        return new ESBException("非法的请求组合",ESB_EXCEPTION_DOMAIN,ILLEGAL_MULTIAPI_ASSEMBLY_CODE,reason);
    }

    public final static int SERIALIZE_FAILED_CODE = -102;
    public static ESBException SERIALIZE_FAILED(String reason) {
        return new ESBException("解析数据出错",ESB_EXCEPTION_DOMAIN,SERIALIZE_FAILED_CODE,reason);
    }

    public final static int TOKEN_EXPIRED_CODE = -300;
    public static ESBException TOKEN_EXPIRED(String reason) {
        return new ESBException("token已过期",ESB_EXCEPTION_DOMAIN,TOKEN_EXPIRED_CODE,reason);
    }

    public final static int TOKEN_INVALID_CODE = -310;
    public static ESBException TOKEN_INVALID(String reason) {
        return new ESBException("token已失效",ESB_EXCEPTION_DOMAIN,TOKEN_INVALID_CODE,reason);
    }

    public final static int TOKEN_ERROR_CODE = -360;
    public static ESBException TOKEN_ERROR(String reason) {
        return new ESBException("token验证错误",ESB_EXCEPTION_DOMAIN,TOKEN_ERROR_CODE,reason);
    }

    public final static int MOCKER_FAILED_CODE = -441;
    public static ESBException MOCKER_FAILED(String reason) {
        return new ESBException("MOCKER调用出错",ESB_EXCEPTION_DOMAIN,MOCKER_FAILED_CODE,reason);
    }

    public final static int SERVICE_RETURN_NULL_CODE = -142;
    public static ESBException SERVICE_RETURN_NULL(String reason) {
        return new ESBException("服务返回异常数据",ESB_EXCEPTION_DOMAIN,SERVICE_RETURN_NULL_CODE,reason);
    }
}
