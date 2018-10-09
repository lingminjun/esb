package com.venus.esb;

import com.venus.esb.ESBAPIInfo;
import com.venus.esb.ESBInvocation;
import com.venus.esb.lang.ESBCookie;
import com.venus.esb.lang.ESBException;
import com.venus.esb.lang.ESBExceptionCodes;

import java.util.Map;

/**
 * Created by lingminjun on 17/8/20.
 *
 * 暂时仅仅支持dubbo 泛型调用和http get、post、delete、put的调用
 */
public class ESBAPIInvoker implements ESB.APIInvoker {

    @Override
    public Object call(ESB esb, ESBAPIInfo info, ESBInvocation request, ESBAPIContext context, Map<String, String> params, Map<String, ESBCookie> cookies, int index) throws ESBException {
        try {
            if (request.protocol.toLowerCase().contains("dubbo")) {
                return dubboInvoke(info,request,context,params,cookies,index);
            } else if (request.protocol.toLowerCase().contains("http")) {
                return httpInvoke(info,request,context,params,cookies,index);
            } else {
                throw ESBExceptionCodes.SERIALIZE_FAILED("协议暂不支持");
            }
        } catch (ESBException e) {
            throw e;
        } catch (Throwable e) {
            throw ESBExceptionCodes.SERIALIZE_FAILED("请求出错").setCoreCause(e);
        }
    }

    private Object dubboInvoke(ESBAPIInfo info, ESBInvocation request, ESBAPIContext context, Map<String, String> params, Map<String, ESBCookie> cookies, int index) throws ESBException {
        return ESBDubboAPIInvoker.dubboInvoke(info,request,context,params,cookies,index);
    }

    private Object httpInvoke(ESBAPIInfo info, ESBInvocation request, ESBAPIContext context, Map<String, String> params, Map<String, ESBCookie> cookies, int index) throws ESBException {
        return ESBHTTPAPIInvoker.httpInvoke(info,request,context,params,cookies,index);
    }
}