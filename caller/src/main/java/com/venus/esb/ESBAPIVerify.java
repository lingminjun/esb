package com.venus.esb;

import com.venus.esb.ESBAPIInfo;
import com.venus.esb.ESBSecurityLevel;
import com.venus.esb.config.ESBConfigCenter;
import com.venus.esb.lang.*;
import com.venus.esb.sign.ESBAPISignature;
import com.venus.esb.sign.utils.Signable;

import java.util.*;

/**
 * Created by lingminjun on 17/5/9.
 * 默认需要校验token, ESB默认校验_tk、_stk、_dtk和_otk的值
 */
public class ESBAPIVerify implements ESB.APIVerify {
    @Override
    public final boolean verify(ESB esb, ESBAPIInfo info, ESBAPIContext context, Map<String, String> params, Map<String, ESBCookie> cookies) throws ESBException {
        //判断token是否过期

        //采用三方验证方式
        if (info.isOpenAPI()) {
            return checkIntegratedSignature(info,context,params);
        } else {
            return checkSignature(info,context,params);
        }

//        return true;
    }

    protected boolean checkIntegratedSignature(ESBAPIInfo info, ESBAPIContext context, Map<String, String> params) throws ESBException {
        //必须传入pid,否则不通过
        if (context.pid == null || context.pid.length() == 0) {
            throw ESBExceptionCodes.SIGNATURE_ERROR("第三方签名，缺省pid");
        }

        //兼容gw1.0不需要验证情况
        if (!info.api.needVerify) {//直接表明不需要认证签名,比较危险
            return true;
        }

        if (params == null) {
            return false;
        }

        // 拼装被签名参数列表
        StringBuilder sb = ESBAPISignature.getSortedParameters(params);
        // 验证签名
        String sig = params.get(ESBSTDKeys.SIGN_KEY);
        if (sig == null || sig.length() == 0) {
            return false;
        }

        //integrated级别接口只允许单接口调用,allowThirdPartyIds在接口注册时已进行校验
        ESBAPISecurity security = loadThirdPartySecurity(context.pid);
        if (security != null) {
            return security.verify(sig, sb.toString().getBytes(ESBConsts.UTF8));
        } else {
            return false;
        }

    }

    protected boolean checkSignature(ESBAPIInfo info, ESBAPIContext context, Map<String, String> params) throws ESBException {
        if (params == null) {
            return false;
        }

        // 拼装被签名参数列表
        StringBuilder sb = ESBAPISignature.getSortedParameters(params);

        // 验证签名
        String sig = params.get(ESBSTDKeys.SIGN_KEY);
        if (sig == null || sig.length() == 0) {
            throw ESBExceptionCodes.SIGNATURE_ERROR("签名失败，缺省sig");
        }

        // 安全级别为None的接口仅进行静态秘钥签名验证,sha1,md5
        String sm = context.arithmetic;

        //为none的情况
        if (ESBSecurityLevel.isNone(info.api.security)) {

            //生产环境，静态签名必须告知aid
            if (!ESBConsts.IS_DEBUG && ESBT.integer(context.aid) == 0) {
                throw ESBExceptionCodes.SIGNATURE_ERROR("静态签名，缺省aid");
            }

            Signable signature = ESBAPISignature.getSignable(sm, ESBConfigCenter.instance().getSignKey(),null);
            return signature.verify(sig,sb.toString().getBytes(ESBConsts.UTF8));
        } else if (context.usecur != null || context.dsecur != null || context.ttoken != null) {// 所有有安全验证需求的接口需要检测动态签名，
            ESBSecur secur = null;
            // 登录用户权限
            if (ESBSecurityLevel.userAuth.check(info.api.security)) {
                secur = context.usecur;
            }
            // 登录账号权限
            else if (ESBSecurityLevel.accountAuth.check(info.api.security)) {
                secur = context.usecur;
            }
            // 设备权限
            else if (ESBSecurityLevel.deviceAuth.check(info.api.security)) {
                secur = context.dsecur;
            }
            // 临时性的
            else {
                secur = context.tsecur;
            }

            //用户权限不对
            if (secur == null) {
                throw ESBExceptionCodes.SIGNATURE_ERROR("没有找到对应的验权token");
            }

            Signable signature = ESBAPISignature.getSignable(sm, secur.key,null);
            return signature.verify(sig,sb.toString().getBytes(ESBConsts.UTF8));
        } else {
            throw ESBExceptionCodes.SIGNATURE_ERROR("签名失败，没有有效token");
        }

    }

    protected ESBAPISecurity loadThirdPartySecurity(String tid) {
        return null;
    }
}
