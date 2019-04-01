package com.venus.esb;


import com.venus.esb.config.ESBConfigCenter;
import com.venus.esb.factory.ESBBeanFactory;
import com.venus.esb.idl.ESBAPIDef;
import com.venus.esb.idl.ESBReturnType;
import com.venus.esb.lang.*;
import com.venus.esb.sign.ESBTokenSign;
import com.venus.esb.sign.ESBUUID;
import com.venus.esb.utils.Injects;
import com.venus.esb.utils.MD5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by lingminjun on 17/4/25.
 * 所有接口调用入口
 */
public final class ESB {

//    private static Logger riskLogger  = LoggerFactory.getLogger("com.venus.esb.risky");
    private static final Logger logback = LoggerFactory.getLogger(ESB.class);

    public static interface APILoader {
        ESBAPIInfo load(ESB esb, String selector);
        void refresh(ESB esb, String selector);
    }

    public static interface APIMocker {
        String call(ESB esb, ESBAPIInfo info, ESBAPIContext context, Map<String,String> params, Map<String,ESBCookie> cookies, ESBAPISerializer serializer);
    }

    public static interface APIInvoker {
        Object call(ESB esb, ESBAPIInfo info, ESBInvocation request, ESBAPIContext context, Map<String,String> params, Map<String,ESBCookie> cookies, int idx) throws ESBException;
    }

    public static interface APIVerify {
        boolean verify(ESB esb, ESBAPIInfo info,ESBAPIContext context,Map<String,String> params, Map<String,ESBCookie> cookies) throws ESBException;
    }

    public static interface APIParser {
        ESBAPISerializer serializer(ESB esb, ESBAPIContext context);
    }

    public static interface APILogger {
        //针对整个调用
        void request(ESB esb, ESBAPIContext context, Map<String,String> params, Map<String,String> header, Map<String,ESBCookie> cookies, List<ESBResponse> result, ESBException e);
        //针对每一个api
        void access(ESB esb, ESBAPIInfo info, ESBAPIContext context, Map<String,String> params, Map<String,String> header, Map<String,ESBCookie> cookies, ESBInvocation invocation, Object result, ESBException e);
    }

    public static interface APIObserver {
        void before(ESB esb, ESBAPIContext context, Map<String,String> params, Map<String,String> header, Map<String,ESBCookie> cookies);
        void after(ESB esb, ESBAPIContext context, Map<String,String> params, Map<String,String> header, Map<String,ESBCookie> cookies, List<ESBResponse> result, ESBException e);
    }

    public static interface APIRisky {//风控嗅探
        // 返回值:
        // 0: 表示安全,
        // 10以内: 表示可以接受,
        // 100以内: 表示要验证【图片验证码】,
        // 100以上: 直接拒绝
        APIRiskyLevel sniffer(ESB esb, ESBAPIInfo info,ESBAPIContext context,Map<String,String> params, Map<String,ESBCookie> cookies) throws ESBException;
    }

    public static enum APIRiskyLevel {
        SAFETY,//通过
        DANGER,//危险(需要做人机识别仍正)
        DENIED,//拒绝
    }

    /**
     * 所有服务入口
     * @return
     */
    public static ESB bus() {return SingletonHolder.INSTANCE;}

    /**
     * 工程方法
     * @return
     */
    public static ESBBeanFactory beanFactory() {
        return ESBBeanFactory.shared();
    }

    /**
     * 启动设置,一旦设置,不可再修改
     */
    private ESB() {

        {
            String name = ESBConfigCenter.instance().getEsbLoader();
            Class<?> clazz = ESBT.classForName(name);
            try {
                this.loader = (APILoader) clazz.newInstance();
            } catch (Throwable e) {
                throw new RuntimeException("没有找到com.venus.esb.loader配置", e);
            }
        }

        {
            String name = ESBConfigCenter.instance().getEsbInvoker();
            Class<?> clazz = ESBT.classForName(name);
            try {
                this.invoker = (APIInvoker) clazz.newInstance();
            } catch (Throwable e) {
                throw new RuntimeException("没有找到com.venus.esb.invoker配置", e);
            }
        }

        {
            String name = ESBConfigCenter.instance().getEsbParser();
            Class<?> clazz = ESBT.classForName(name);
            try {
                this.parser = (APIParser) clazz.newInstance();
            } catch (Throwable e) {
                throw new RuntimeException("没有找到com.venus.esb.parser配置", e);
            }
        }

        if (ESBConfigCenter.instance().getEsbVerify() != null) {
            String name = ESBConfigCenter.instance().getEsbVerify();
            Class<?> clazz = ESBT.classForName(name);
            try {
                this.verify = (APIVerify) clazz.newInstance();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        if (ESBConfigCenter.instance().getEsbMocker() != null) {
            String name = ESBConfigCenter.instance().getEsbMocker();
            Class<?> clazz = ESBT.classForName(name);
            try {
                this.mocker = (APIMocker) clazz.newInstance();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        if (ESBConfigCenter.instance().getEsbObserver() != null) {
            String name = ESBConfigCenter.instance().getEsbObserver();
            Class<?> clazz = ESBT.classForName(name);
            try {
                this.observer = (APIObserver) clazz.newInstance();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        if (ESBConfigCenter.instance().getEsbLogger() != null) {
            String name = ESBConfigCenter.instance().getEsbLogger();
            Class<?> clazz = ESBT.classForName(name);
            try {
                this.logger = (APILogger) clazz.newInstance();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }


        if (ESBConfigCenter.instance().getEsbRisky() != null) {
            String name = ESBConfigCenter.instance().getEsbRisky();
            Class<?> clazz = ESBT.classForName(name);
            try {
                this.risky = (APIRisky) clazz.newInstance();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 刷洗接口,接口将需要重新重loader中获取
     */
    public void refresh(String selector) {
        if (!ESBT.isEmpty(selector)) {

            if (loader != null) {
                try {
                    loader.refresh(this, selector);
                } catch (Throwable e) { e.printStackTrace(); }
            }

        }
    }

    /**
     * ESB(服务总线管理),切面试调用各个服务,不直接依赖服务本身,将服务可视化管理起来
     * @param params 前端请求参数,HTTP header中的authorization请转换成_tk放入params
     * @param header 请求头中的字段,content-type authorization 等等
     * @param params 前端请求cookies,针对HTTP/HTTPS请求支持
     * @return
     */
    public List<ESBResponse> call(Map<String,String> params, Map<String,String> header, Map<String,ESBCookie> cookies) throws ESBException {
        return call(params,header,cookies,null);
    }

    /**
     * ESB(服务总线管理),切面试调用各个服务,不直接依赖服务本身,将服务可视化管理起来
     * @param params
     * @param header
     * @param cookies
     * @param body binary内容自行编码存入
     * @return
     */
    public List<ESBResponse> call(Map<String,String> params, Map<String,String> header, Map<String,ESBCookie> cookies, String body) throws ESBException {
        List<ESBResponse> result = null;
        ESBException exception = null;

        try {
            result = _call(params,header,cookies,body);
        } catch (ESBException e) {
            // 此时需要清除token
            if (ESBExceptionCodes.ESB_EXCEPTION_DOMAIN.equals(e.getDomain())
                  &&  e.getCode() == ESBExceptionCodes.TOKEN_INVALID_CODE) {
                // 清除token
                ESBAPIContext.context().clearAllTokenCookie();
            }
            logback.error("ESB call error!",e);
            exception = e;
            throw e;
        } catch (Throwable e) {
            logback.error("ESB call error!",e);
            exception = ESBExceptionCodes.PARAMETER_ERROR("参数无法正确解析").setCoreCause(e);
            throw exception;
        } finally {

            //记录 access 日志
            if (logger != null) {
                try {
                    logger.request(this, ESBAPIContext.context(), params, header, cookies, result, exception);
                } catch (Throwable e) {/*日志错误不影响业务*/e.printStackTrace();}
            }

            //结束上报
            if (observer != null) {
                try {
                    observer.after(this,ESBAPIContext.context(),params, header, cookies, result, exception);
                } catch (Throwable e) {/*日志错误不影响业务*/e.printStackTrace();}
            }

//            ESBAPIContext.context().clear();
        }

        return result;
    }

    private List<ESBResponse> _call(Map<String,String> params, Map<String,String> header, Map<String,ESBCookie> cookies, String body) throws ESBException {

        ESBAPIContext context = ESBAPIContext.context();//

        //将数据填充到context中
        ESBAPIContext.fill(context,params,header,cookies,body);

        //自动注入did
        context.autoInjectDeviceToken();

        //将数据设置日志预置信息
        context.seed();

        //开始上报brave
        if (observer != null) {
            try {
                observer.before(this, context, params, header, cookies);
            } catch (Throwable e) {/*日志错误不影响业务*/e.printStackTrace();}
        }

        //方法
        String selectors = context.selector;
        if (ESBT.isEmpty(selectors)) {
            throw ESBExceptionCodes.UNKNOWN_METHOD("没有传入正确的方法选标器");
        }

        ESBAPISerializer serializer = serializer(context);
        if (serializer == null) {
            throw ESBExceptionCodes.SERIALIZE_FAILED("没有正确的解析器");
        }

        List<ESBAPIInfo> max = new ArrayList<ESBAPIInfo>();
        List<ESBAPIInfo> apis = getAPI(selectors,max,context);
        if (apis == null || apis.size() == 0) {
            throw ESBExceptionCodes.UNKNOWN_METHOD("没有传入正确的方法选标器");
        }

        //检查是否联合请求
        if (illegalAssemble(apis)) {
            throw ESBExceptionCodes.ILLEGAL_MULTIAPI_ASSEMBLY("非法的请求组合");
        }

        // token有效性判断（内部验证token），不验证时效性
        if (isTokenInvalid(context,header,cookies)) {
            throw ESBExceptionCodes.TOKEN_INVALID("token解析错误，存在非法token");
        }

        // token的时效性判断
        if (isTokenExpired(context,ESBConsts.REFRESH_TOKEN_SPECIFIC_SELECTOR.equals(selectors))) {
//            System.out.println(selectors);
            throw ESBExceptionCodes.TOKEN_EXPIRED("token过期! sel:" + selectors);
        }

        // 接口验证权限 (需要验证最高权限的)
        if (!verifyAPI(max.get(0),context,params,cookies)) {
            throw ESBExceptionCodes.SIGNATURE_ERROR("验签失败");
        }

        List<ESBResponse> ress = new ArrayList<>();

        // 针对sso特殊处理 [不支持联合]
        if (ESBConsts.SSO_SPECIFIC_SELECTOR.equals(selectors)) {
            ress.add(esbSSO(context,cookies,serializer));
            return ress;
        } else if (ESBConsts.REFRESH_TOKEN_SPECIFIC_SELECTOR.equals(selectors)) {//刷新token [不支持联合]
            ress.add(esbAuthRefresh(context, serializer));
            return ress;
        }

        //风控控制,组合取最高权限控制,因为前面验签会使用最高权限验证
        if (sniffer(max.get(0),context,params,cookies) != APIRiskyLevel.SAFETY) {
            throw ESBExceptionCodes.ACCESS_DENIED("被风控拦截,直接拒绝访问");
        }

        //填充返回值
        fillResponse(ress,apis,context,params,header,cookies,serializer);

        return ress;
    }

    private APIRiskyLevel sniffer(ESBAPIInfo info,ESBAPIContext context,Map<String,String> params, Map<String,ESBCookie> cookies) throws ESBException {
        //记录ua
//        riskLogger.info("{}",context.getUa());

        if (risky == null) {return APIRiskyLevel.SAFETY;}

        //风控可能直接抛出需要图片验证码
        return risky.sniffer(this, info,context,params,cookies);
    }

    private static class TaskResponse {
        boolean success;
        boolean mock;
        Object result;
        String path;

        //收集数据
        ESBExts exts;
        ESBCookieExts cookies;

        ESBException exception;//将异常栈保留
    }

    private static class MockFuture implements Future<TaskResponse> {

        public MockFuture(String result) {
            this.result = result;
        }

        String result;

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return false;
        }

        @Override
        public TaskResponse get() throws InterruptedException, ExecutionException {
            TaskResponse response = new TaskResponse();
            response.mock = true;
            response.success = true;
            response.result = result;
            return response;
        }

        @Override
        public TaskResponse get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return get();
        }
    }

    // 预处理返回结果
    private String preprocessResult(Object obj, ESBAPIInfo api, ESBAPIContext context, ESBAPISerializer serializer) throws ESBException {
        //特殊类型支持,将数据注入
        if (!context.isTransmit() && ESBT.isBaseType(obj.getClass())) {
            obj = wrapperResult(api, obj);
        } else if (obj instanceof ESBToken) {
            context.injectToken((ESBToken) obj);
        } else if (obj instanceof ESBDeviceToken) {
            context.injectDeviceToken((ESBDeviceToken) obj);
        } //坑，dubbo泛型调用，不知道类型是 ESBToken
        else if (ESBToken.class.getName().equals(api.api.returned.type)) {
            obj = injectGenericTokenResult(obj, context);
        } else if (ESBDeviceToken.class.getName().equals(api.api.returned.type)) {
            obj = injectGenericDeviceTokenResult(obj, context);
        } else {// 精简返回
            obj = tailor(obj, api, context);
        }

        if (obj == null) {
            throw ESBExceptionCodes.SERIALIZE_FAILED("组合请求异常");
        }

        //最后转文本
        String rst = serializer.serialized(obj);
        if (rst != null) {//过滤掉dubbo解析数据的class
            //"class":"com.venus.scm.entities.ScmSkuImagePOJO",
            rst = rst.replaceAll("\\{\"class\":\"[a-zA-Z_\\$]+[\\w\\$\\.]*\"\\}", "{}");
            rst = rst.replaceAll("\\{\"class\":\"[a-zA-_\\$]+[\\w\\$\\.]*\",", "{");
            rst = rst.replaceAll(",\"class\":\"[a-zA-_\\$]+[\\w\\$\\.]*\"", "");
        }
        return rst;
    }

    //需要优化,若非组合请求,且没有其他多个请求组装时,切换到单一调用
    private void concurrentFillResponse(List<ESBResponse> ress,List<ESBAPIInfo> apis, ESBAPIContext context, Map<String,String> params, Map<String,String> header, Map<String,ESBCookie> cookies, ESBAPISerializer serializer) throws ESBException {
        HashMap<Integer,List<Future<TaskResponse>>> task = new HashMap<Integer, List<Future<TaskResponse>>>();
        for (int i = 0; i < apis.size(); i++) {
            ESBAPIInfo api = apis.get(i);

            List<Future<TaskResponse>> list = new ArrayList<Future<TaskResponse>>();

            //mocker
            String result = mocker(api, context, params, cookies, serializer);
            if (result != null) {
                list.add(new MockFuture(result));
            } else {
                //获取单个请求【内部多个请求,多线程同时调用,并发】
                list.addAll(asyncInvoke(api, context, params, header, cookies, serializer, (apis.size() == 1 ? -1 : i)));
            }

            task.put(api.hashCode(),list);
        }

        //统一获取结果,单个请求，忽略ignore参数
        boolean ignoreError = apis.size() <= 1 ? false : ESBT.bool(params.get(ESBSTDKeys.MULT_APIS_IGNORE_MASTER_ERROR_KEY));
        for (int i = 0; i < apis.size(); i++) {
            ESBAPIInfo api = apis.get(i);

            ESBResponse res = new ESBResponse();

            List<Future<TaskResponse>> list = task.get(api.hashCode());
            //组合的obj
            Object obj = new HashMap<String,Object>();
            for (int fidx = 0; fidx < list.size(); fidx++) {
                Future<TaskResponse> future = list.get(fidx);
                try {
                    TaskResponse response = future.get();//不设置超时好么
                    if (response.mock) {
                        obj = ESBRule.assembleResult(obj, response.path, response.result);
                    } else {
                        if (response.success) {
                            obj = ESBRule.assembleResult(obj, response.path, response.result);

                            //合并ext和cookie
                            mergeExts(context,response);

                        } else if (!ignoreError && i == 0) {//主请求若异常，直接抛出错误
                            throw response.exception;
                        } else {
                            if (fidx == 0) {//记录首个请求异常
                                res.exception = response.exception;
                                break;//忽略错误继续？？？
                            }
                            logback.error("组合请求异常"+api.api.getAPISelector(), response.exception);
                        }
                    }
                } catch (ESBException e) {
                    if (!ignoreError && i == 0) {//主请求将错误抛出去
                        throw e;
                    } else {
                        if (fidx == 0) {//记录首个请求异常
                            res.exception = e;
                            break;//忽略错误继续？？？
                        }
                        logback.error("组合请求异常"+api.api.getAPISelector(), e);
                    }
                } catch (Throwable e) {
                    if (!ignoreError && i == 0) {//主请求将错误抛出去
                        throw ESBExceptionCodes.INTERNAL_SERVER_ERROR("请求异常").setCoreCause(e);
                    } else {
                        if (fidx == 0) {//记录首个请求异常
                            res.exception = ESBExceptionCodes.INTERNAL_SERVER_ERROR("请求异常").setCoreCause(e);
                            break;//忽略错误继续？？？
                        }
                        logback.error("组合请求异常"+api.api.getAPISelector(), e);
                    }
                }
            }

            //兼容null被返回
            if (obj != null) {
                try {
                    //预处理结果
                    res.result = preprocessResult(obj,api,context,serializer);
                } catch (ESBException e) {
                    if (!ignoreError && i == 0) {//主请求将错误抛出去
                        throw e;
                    } else {
                        res.exception = e;
                        logback.error("组合请求包装结果" + api.api.getAPISelector(), e);
                    }
                } catch (Throwable e) {
                    if (!ignoreError && i == 0) {//主请求将错误抛出去
                        throw ESBExceptionCodes.SERIALIZE_FAILED("请求异常").setCoreCause(e);
                    } else {
                        res.exception = ESBExceptionCodes.SERIALIZE_FAILED("请求异常").setCoreCause(e);
                        logback.error("组合请求包装结果" + api.api.getAPISelector(), e);
                    }
                }
            } else if (res.exception == null) { //没有异常的情况，自动补充异常
                if (!ignoreError && i == 0) {//主请求将错误抛出去
                    throw ESBExceptionCodes.SERVICE_RETURN_NULL("无法返回了非法的null");
                } else {
                    res.exception = ESBExceptionCodes.SERVICE_RETURN_NULL("无法返回了非法的null");
                    logback.error("组合请求异常" + api.api.getAPISelector());
                }
            }

            ress.add(res);
        }
    }

    //获取异步请求
    private List<Future<TaskResponse>> asyncInvoke(final ESBAPIInfo api, final ESBAPIContext context, final Map<String,String> params,final Map<String,String> header, final Map<String,ESBCookie> cookies, ESBAPISerializer serializer, final int index) throws ESBException {

        //dubbo接口
        Map<String,ESBInvocation> invocations = getInvocations(api, context, params, cookies, index);
        if (invocations == null || invocations.size() == 0) {
            throw ESBExceptionCodes.UNKNOWN_METHOD("服务配置缺失");
        }

        List<Future<TaskResponse>> list = new ArrayList<Future<TaskResponse>>();

        Iterator<Map.Entry<String,ESBInvocation>> entries = invocations.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, ESBInvocation> entry = entries.next();
            final String keyPath = entry.getKey();
            final ESBInvocation invocation = entry.getValue();
            //并发请求
            list.add(queue.submit(new Callable<TaskResponse>() {
                @Override
                public TaskResponse call() throws Exception {
                    TaskResponse response = new TaskResponse();
                    response.path = keyPath;
                    try {
                        response.success = true;
                        response.result = invoker.call(ESB.this, api, invocation, context, params, cookies, index);
                        response.cookies = ESBAPIContext.context().cookies;
                        response.exts = ESBAPIContext.context().exts;
                    } catch (ESBException e) {
                        response.success = false;
                        response.exception = e;
                    } catch (Throwable e) {
                        response.success = false;
                        response.exception = ESBExceptionCodes.INTERNAL_SERVER_ERROR("请求异常").setCoreCause(e);
                    } finally {
                        //日志,错误一律忽略
                        if (logger != null) {
                            try {
                                logger.access(ESB.this, api, context, params, header, cookies, invocation, response.result, response.exception);
                            } catch (Throwable e) {/*日志错误不影响业务*/e.printStackTrace();}
                        }
                    }
                    return response;
                }
            }));
        }

        return list;
    }

    private void mergeExts(final ESBAPIContext context, TaskResponse response) {
        if (response.exts != null && response.exts.size() > 0) {
            for (int idx = 0; idx < response.exts.size(); idx++) {
                context.putExt(response.exts.keys.get(idx),response.exts.values.get(idx));
            }
        }

        if (response.cookies != null && response.cookies.size() > 0) {
            for (int idx = 0; idx < response.cookies.size(); idx++) {
                context.putCookie(response.cookies.keys.get(idx),response.cookies.values.get(idx));
            }
        }
    }

    //判断是否有开窗需求，将在后面的版本中实现
    private boolean isSerialRequest(List<ESBAPIInfo> apis, Map<String,String> params) {

        //以下是对单个请求的优化，没必要开启线程组
        if (apis == null || apis.size() == 0) {
            return true;
        }

        // 只有一个api，没有多个子请求
        if (apis.size() == 1 && (apis.get(0).rules == null || apis.get(0).rules.size() <= 1)) {
            return true;
        }

        boolean serial = ESBT.bool(params.get(ESBSTDKeys.MULT_APIS_SERIAL_CALL_KEY));
        if (serial) {
            return true;
        }

        return false;

    }

    private void fillResponse(List<ESBResponse> ress,List<ESBAPIInfo> apis, ESBAPIContext context, Map<String,String> params, Map<String,String> header, Map<String,ESBCookie> cookies, ESBAPISerializer serializer) throws ESBException {
        if (isSerialRequest(apis,params)) {
            serialFillResponse(ress,apis,context,params,header,cookies,serializer);
        } else {
            concurrentFillResponse(ress,apis,context,params,header,cookies,serializer);
        }
    }

    private void serialFillResponse(List<ESBResponse> ress, List<ESBAPIInfo> apis, ESBAPIContext context, Map<String,String> params, Map<String,String> header, Map<String,ESBCookie> cookies, ESBAPISerializer serializer) throws ESBException {
        // 默认,单个请求，忽略ignore参数
        boolean ignoreError = apis.size() <= 1 ? false : ESBT.bool(params.get(ESBSTDKeys.MULT_APIS_IGNORE_MASTER_ERROR_KEY));

        for (int i = 0; i < apis.size(); i++) {
            ESBAPIInfo api = apis.get(i);
            ESBResponse res = new ESBResponse();

            //mocker
            res.result = mocker(api, context, params, cookies, serializer);
            if (res.result == null) {
                //获取单个请求【内部多个请求,多线程同时调用,并发】
                try {
                    res.result = syncInvoke(api, context, params, header, cookies, serializer, (apis.size() == 1 ? -1 : i));
                } catch (ESBException e) {
                    if (!ignoreError && i == 0) {//主请求直接抛，忽略错误，继续
                        throw e;
                    } else {
                        res.exception = e;
                        logback.error("组合请求异常" + api.api.getAPISelector(), e);
                    }
                } catch (Throwable e) {
                    if (!ignoreError && i == 0) {//主请求直接抛，忽略错误，继续
                        throw ESBExceptionCodes.INTERNAL_SERVER_ERROR("请求异常").setCoreCause(e);
                    } else {
                        res.exception = ESBExceptionCodes.INTERNAL_SERVER_ERROR("请求异常").setCoreCause(e);
                        logback.error("组合请求异常" + api.api.getAPISelector(), e);
                    }
                }
            }

            ress.add(res);
        }
    }

    //同步一个一个调用，为了支持开创需求
    private String syncInvoke(ESBAPIInfo api, ESBAPIContext context, Map<String,String> params, Map<String,String> header, Map<String,ESBCookie> cookies, ESBAPISerializer serializer, int index) throws ESBException {

        //dubbo接口
        Map<String,ESBInvocation> invocations = getInvocations(api, context, params, cookies, index);
        if (invocations == null || invocations.size() == 0) {
            throw ESBExceptionCodes.UNKNOWN_METHOD("服务配置缺失");
        }

        Object obj = new HashMap<String,Object>();
        Iterator<Map.Entry<String,ESBInvocation>> entries = invocations.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String,ESBInvocation> entry = entries.next();

            Object child = null;
            ESBException exception = null;

            try {
                child = invoker.call(ESB.this, api,entry.getValue(),context,params,cookies,index);
            } catch (ESBException e) {
                exception = e;
                throw e;
            } catch (Throwable e) {
                exception = ESBExceptionCodes.INTERNAL_SERVER_ERROR("请求异常").setCoreCause(e);
                throw exception;
            } finally {
                //日志,错误一律忽略
                if (logger != null) {
                    try {
                        logger.access(ESB.this, api, context, params, header, cookies, entry.getValue(), child, exception);
                    } catch (Throwable e) {/*日志错误不影响业务*/e.printStackTrace();}
                }
            }

            //组合请求
            obj = ESBRule.assembleResult(obj,entry.getKey(),child);
        }

        //兼容null被返回
        if (obj == null) {
            throw ESBExceptionCodes.SERVICE_RETURN_NULL("无法返回了非法的null");
        }

        //基础类型包装一下
        return preprocessResult(obj,api,context,serializer);
    }

    private static Object tailor(Object obj, ESBAPIInfo api, ESBAPIContext context) {
        try {
            return ESBTailor.tailor(obj,api,context);
        } catch (Throwable e) {e.printStackTrace();}
        return obj;
    }

    private static HashSet<String> singleAPIs = new HashSet<>();
    static {
        //凡是返回值为token的必须单一请求
        singleAPIs.add(ESBToken.class.getName());
        singleAPIs.add(ESBSSOToken.class.getName());
        singleAPIs.add(ESBDeviceToken.class.getName());
    }
    private boolean illegalAssemble(List<ESBAPIInfo> apis) {
        int count = apis.size();
        if (count <= 1) {
            return false;
        }
        for (ESBAPIInfo api : apis) {
            String coreType = api.api.returned.getCoreType();
            //组合认证请求不被允许
            if (singleAPIs.contains(coreType) && count > 1) {
                return true;
            }
        }
        return false;
    }

    //判断所有token的有效性
    private boolean isTokenInvalid(ESBAPIContext context, Map<String,String> header, Map<String,ESBCookie> cookies) throws ESBException {
        if (!ESBT.isEmpty(context.dtoken) && context.dsecur == null) {
            throw ESBExceptionCodes.TOKEN_INVALID("device token无法解析");
        }

        // 要求有设备验证
        if (!ESBT.isEmpty(context.utoken) && (context.usecur == null || context.dsecur == null)) {
            throw ESBExceptionCodes.TOKEN_INVALID("user token无法解析");
        }

        // 要求有user token
        if (!ESBT.isEmpty(context.stoken) && (context.ssecur == null || context.usecur == null)) {
            throw ESBExceptionCodes.TOKEN_INVALID("secret token无法解析");
        }

        // 要求有user token
        if (!ESBT.isEmpty(context.rtoken) && (context.rsecur == null || context.usecur == null)) {
            throw ESBExceptionCodes.TOKEN_INVALID("refresh token无法解析");
        }

        if (!ESBT.isEmpty(context.ttoken) && context.tsecur == null) {
            throw ESBExceptionCodes.TOKEN_INVALID("temporary token无法解析");
        }

        if (!ESBT.isEmpty(context.ssoToken) && context.ssoSecur == null) {
            throw ESBExceptionCodes.TOKEN_INVALID("sso token无法解析");
        }

        // 以下几个特殊场景的教研
        // dtoken与cookie中的不相符合(说明有人恶意修改cookie的did)
        ESBCookie didCookie = cookies.get(ESBSTDKeys.DID_KEY);
        if (context.dsecur != null && didCookie != null && context.dsecur.did != ESBT.longInteger(didCookie.value)) {
            throw ESBExceptionCodes.TOKEN_INVALID("检查到cookie中的did与device token不一致");
        }

        // stoken验证utoken
        if (context.ssecur != null && !MD5.md5(context.utoken).equals(context.ssecur.dna)) {
            throw ESBExceptionCodes.TOKEN_INVALID("user token与secret token不匹配");
        }

        // rtoken验证utoken
        if (context.rsecur != null && !MD5.md5(context.utoken).equals(context.rsecur.dna)) {
            throw ESBExceptionCodes.TOKEN_INVALID("refresh token与secret token不匹配");
        }

        // ssoToken中的参数验证，已经在后面验证了
//        if (context.ssoSecur != null &&
//                (context.ssoSecur.taid != ESBT.integer(context.aid)
//                || context.ssoSecur.tdid != EsSBT.longInteger(context.did))) {
//            return true;
//        }

        return false;
    }

    private boolean isTokenExpired(ESBAPIContext context, boolean refresh) throws ESBException {

        // 刷新token过期，只能踢出用户
        if (context.rsecur != null && context.rsecur.expire > 0 && context.rsecur.expire * 1000l < context.at) {
            throw ESBExceptionCodes.TOKEN_INVALID("refresh token已经失效了，必须重新登录");
        }

        // 设备token过期后，必须重新登录
        if (context.dsecur != null && context.dsecur.expire > 0 && context.dsecur.expire * 1000l < context.at) {
            throw ESBExceptionCodes.TOKEN_INVALID("device token已经失效了，必须重新登录");
        }

        // refresh不care user token 过期
        if (context.usecur != null && context.usecur.expire > 0 && context.usecur.expire * 1000l < context.at) {
//            logback.info("user token expired; refresh:" + refresh);
            if (!refresh) {
                throw ESBExceptionCodes.TOKEN_EXPIRED("user token过期; refresh:" + refresh);
            }
        }

        // secret token 过期，尽量本次操作被打回
        if (context.ssecur != null && context.ssecur.expire > 0 && context.ssecur.expire * 1000l < context.at) {
            throw ESBExceptionCodes.TOKEN_INVALID("secret token已经失效了，必须重新登录");
        }

        if (context.tsecur != null && context.tsecur.expire > 0 && context.tsecur.expire * 1000l< context.at) {
//            logback.info("temporary token expired; refresh:" + refresh);
            throw ESBExceptionCodes.TOKEN_EXPIRED("temporary token过期; refresh:" + refresh);
        }

        if (context.ssoSecur != null && context.ssoSecur.expire > 0 && context.ssoSecur.expire * 1000l < context.at) {
//            logback.info("sso token expired; refresh:" + refresh);
            throw ESBExceptionCodes.TOKEN_EXPIRED("sso token过期; refresh:" + refresh);
        }

        return false;
    }

    private Object injectGenericTokenResult(Object token, ESBAPIContext context) {
        if (token instanceof Map) {
            ESBToken tk = new ESBToken();
            Injects.fill(token,tk);
            context.injectToken(tk);
            return tk;
        }
        return token;
    }

    private Object injectGenericDeviceTokenResult(Object token, ESBAPIContext context) {
        if (token instanceof Map) {
            ESBDeviceToken tk = new ESBDeviceToken();
            Injects.fill(token,tk);
            context.injectDeviceToken(tk);
            return tk;
        }
        return token;
    }

    private Object wrapperResult(ESBAPIInfo api, Object value) throws ESBException {
        Class<?> clazz = ESBT.classForName(api.api.returned.type);
        Object o = value;
        if (clazz != null) {
            try {
                o = clazz.newInstance();
            } catch (Throwable e) {
                throw ESBExceptionCodes.SERIALIZE_FAILED("结果无法正确解析").setCoreCause(e);
            }
            if (o != null && o instanceof ESBResultWrapper) {
                ((ESBResultWrapper) o).setValue(value);
            }
        }
        return o;
    }

    /**
     * 获取key ==> 对应的 ESBInvocation
     * @param api
     * @param context
     * @param params
     * @param cookies
     * @param index
     * @return
     */
    private Map<String,ESBInvocation> getInvocations(ESBAPIInfo api,ESBAPIContext context, Map<String,String> params, Map<String,ESBCookie> cookies, int index) throws ESBException {
        Map<String,ESBInvocation> map = new HashMap<String, ESBInvocation>();
        try {
            //没有规则的前提下,仅仅取当前的请求
            if (api.rules == null || api.rules.size() == 0) {
                if (api.invocations == null || api.invocations.size() == 0) {
                    throw ESBExceptionCodes.UNKNOWN_METHOD("没有配置接口实现");
                }
                ESBInvocation invocation = api.invocations.values().iterator().next();
                map.put(ESBRule.THIS_PATH,invocation);
                return map;
            }

            //有规则,多个keypath,每个keypath需要采用规则来决策出一个invocation
            Iterator<Map.Entry<String,List<ESBRuleNode>>> entries = api.rules.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, List<ESBRuleNode>> entry = entries.next();
                String path = entry.getKey();
                List<ESBRuleNode> list = entry.getValue();
                //是否需要继续排序
//                Collections.sort(list, ESBRule.RuleComparator);//此处比应该在此排序
                ESBInvocation invocation = null;
                for (ESBRuleNode node : list) {
                    if (ESBRule.conformToRule(node.rule,context,params,cookies,index)) {
                        invocation = api.getInvocation(node.invocation);
                        break;
                    }
                }

                //某一个path没有配置实现,不允许通过
                if (invocation == null) {
                    throw ESBExceptionCodes.UNKNOWN_METHOD(path + "没有配置接口实现");
                }

                map.put(path,invocation);
            }

        } catch (Throwable e) {
            throw ESBExceptionCodes.UNKNOWN_METHOD("服务未找到").setCoreCause(e);
        }

        return map;
    }

    private ESBAPISerializer serializer(ESBAPIContext context) throws ESBException {
        try {
            return parser.serializer(this, context);
        } catch (Throwable e) {
            throw ESBExceptionCodes.SERIALIZE_FAILED("序列化没找到").setCoreCause(e);
        }
    }

    private String mocker(ESBAPIInfo info, ESBAPIContext context, Map<String,String> params, Map<String,ESBCookie> cookies, ESBAPISerializer serializer) throws ESBException {
        if (!info.mock) {return null;}
        if (!ESBConsts.IS_DEBUG) {return null;}
        if (mocker == null) {return null;}
        try {
            return mocker.call(this, info,context,params,cookies,serializer);
        } catch (Throwable e) {
            throw ESBExceptionCodes.MOCKER_FAILED("MOCKER调用出错").setCoreCause(e);
        }
    }

    private boolean verifyAPI(ESBAPIInfo info,ESBAPIContext context,Map<String,String> params, Map<String,ESBCookie> cookies) throws ESBException {
        //Integrate None Internal级别接口不具备用户身份
        //api是需要验证权限的
        if (ESBSecurityLevel.requireToken(info.api.security)) {

            //设备权限
            if (ESBSecurityLevel.deviceAuth.check(info.api.security)) {
                if (context.dsecur == null) {
                    throw ESBExceptionCodes.TOKEN_ERROR("设备token异常");
                }
            }

            //登录权限
            if (ESBSecurityLevel.accountAuth.check(info.api.security)) {
                if (!ESBConsts.IS_DEBUG) {
                    if (context.dsecur == null) {
                        throw ESBExceptionCodes.TOKEN_ERROR("验证AccountToken时，设备token异常，不考虑不受信任的设备请求");
                    }
                }

                if (context.usecur == null) {
                    throw ESBExceptionCodes.TOKEN_ERROR("用户token解析错误或者不存在");
                }

                //需要验证stoken与referer的一致性，防止csrf攻击
//                if (context.ssecur != null && ) {
//
//                }
            }

            //登录权限
            if (ESBSecurityLevel.userAuth.check(info.api.security)) {
                //暂时 不强制依赖需要设备token，否则无法调试---------实际上是需要的
                if (!ESBConsts.IS_DEBUG) {
                    if (context.dsecur == null) {
                        throw ESBExceptionCodes.TOKEN_ERROR("验证UserToken时，设备token异常，不考虑不受信任的设备请求");
                    }
                }

                if (context.usecur == null) {
                    throw ESBExceptionCodes.TOKEN_ERROR("用户token解析错误或者不存在");
                }

                //需要验证stoken与referer的一致性，防止csrf攻击
//                if (context.ssecur != null && ) {
//
//                }
            }
        }


        if (verify == null) {//不验证权限
            return true;
        }

        //验证权限
        boolean result = false;
        try {
            result = verify.verify(this, info,context,params,cookies);
        } catch (Throwable e) {
            throw ESBExceptionCodes.SIGNATURE_ERROR("验签异常").setCoreCause(e);
        }

        if (!result) {
            throw ESBExceptionCodes.SIGNATURE_ERROR("验签失败");
        }

        return result;
    }

    /**
     * 获取所有方法
     * @param selectors
     * @param out 输出需要最高验证权限的接口
     * @return
     */
    private List<ESBAPIInfo> getAPI(String selectors, List<ESBAPIInfo> out, ESBAPIContext context) throws ESBException {

        // 解析多个由','拼接的api名
        String[] names = selectors.split(",");

        // 检测当前安全级别是否允许调用请求中的所有api
        List<ESBAPIInfo> list = new ArrayList<ESBAPIInfo>();

        ESBAPIInfo maxSecurity = null;
        for (int m = 0; m < names.length; m++) {
            String selector = names[m];

            if (ESBT.isEmpty(selector)) {
                throw ESBExceptionCodes.UNKNOWN_METHOD(selector + "服务未找到配置");
            }

            //自动补全domain名称
            String[] ss = selector.split("\\.");
            if (ss.length == 2) {
                selector = ss[0] + "." + ss[0] + "." + ss[1];
            } else if (ss.length == 3) {

            } else {
                throw ESBExceptionCodes.UNKNOWN_METHOD(selector + "服务未找到配置,传入名字歧义");
            }

            //加载服务
            ESBAPIInfo api = loadAPIInfo(selector,context);

            if (api != null) {
                list.add(api);

                //比较 权限 更高的保留
                if (maxSecurity == null
                        || maxSecurity.api.security < api.api.security) {
                    maxSecurity = api;
                }
            } else {
                throw ESBExceptionCodes.UNKNOWN_METHOD(selector + "服务未找到配置");
            }
        }

        if (out != null && maxSecurity != null) {
            out.add(maxSecurity);
        }

        return list;
    }

    private static ESBAPIInfo sso1 = new ESBAPIInfo();
    private static ESBAPIInfo sso2 = new ESBAPIInfo();
    private static ESBAPIInfo rfrsh = new ESBAPIInfo();
    static {
        //请求sso token
        sso1.api = new ESBAPIDef();
        sso1.api.methodName = "ESBSpecial";
        sso1.api.module = "sso";
        sso1.api.domain = "esb";
        sso1.api.security = ESBSecurityLevel.userAuth.getCode();
        sso1.api.returned = new ESBReturnType();
        sso1.api.returned.type = ESBSSOToken.class.getName();

        //sso token换user token
        sso2.api = new ESBAPIDef();
        sso2.api.methodName = "ESBSpecial";
        sso2.api.module = "sso";
        sso2.api.domain = "esb";
        sso2.api.security = ESBSecurityLevel.once.getCode();
        sso2.api.returned = new ESBReturnType();
        sso2.api.returned.type = ESBToken.class.getName();

        //refresh token
        rfrsh.api = new ESBAPIDef();
        rfrsh.api.methodName = "ESBSpecial";
        rfrsh.api.module = "auth";
        rfrsh.api.domain = "esb";
        rfrsh.api.security = ESBSecurityLevel.userAuth.getCode();
        rfrsh.api.returned = new ESBReturnType();
        rfrsh.api.returned.type = ESBToken.class.getName();

    }
    private ESBAPIInfo loadAPIInfo(String selector, ESBAPIContext context) {

        if (ESBConsts.SSO_SPECIFIC_SELECTOR.equals(selector)) {
            if (context.ssoSecur != null) {
                return sso2;
            } else {
                return sso1;
            }
        } else if (ESBConsts.REFRESH_TOKEN_SPECIFIC_SELECTOR.equals(selector)) {
            return rfrsh;
        }

        if (loader != null) {
            try {
                return loader.load(this, selector);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    //更换新的user token，必须携带refresh token
    private ESBResponse esbAuthRefresh(ESBAPIContext context, ESBAPISerializer serializer) throws ESBException {
        // 使用usecur + ssecur + refresh ==> ESBToken
        if (context.usecur != null
                && context.ssecur != null && context.ssecur.expire < context.at
                && context.rsecur != null && context.rsecur.expire < context.at) {
            ESBToken token = ESBTokenSign.injectDefaultToken(null,context);

            //反向注入
            context.utoken = token.token;
            context.stoken = token.stoken;
            context.rtoken = token.refresh;

            //注入token到cookie
            context.pushTokenCookie(token.token,token.stoken, token.user);

            ESBResponse res = new ESBResponse();
            res.result = serializer.serialized(token);
            return res;
        }

        //反向清除
        context.utoken = null;
        context.guid = null;
        context.stoken = null;
        context.rtoken = null;

        context.usecur = null;
        context.ssecur = null;
        context.rsecur = null;

        //清除cookie token
        context.pushTokenCookie("","", "");

        ESBResponse res = new ESBResponse();
        res.result = "{\"success\":false}";
        return res;
    }

    private ESBResponse esbSSO(ESBAPIContext context, Map<String,ESBCookie> cookies, ESBAPISerializer serializer) throws ESBException {
        //两种结果返回,第一种,如果是首次请求,返回关系如下
        //1、usecur(用户ok) + ssecur(域名ok) ==> ESBSSOToken (一次性token)
        if (context.usecur != null && context.ssecur != null) {//返回ESBSSOToken
            if (!ESBT.isEmpty(context.ssoToDomain)
                    && context.ssoToDid != 0
                    && context.ssoToAid != 0) {
                ESBSSOToken token = ESBTokenSign.injectSSOToken(null,context,context.ssoToAid,context.ssoToDid,context.ssoToDomain);

                // 将用户反向传递过去，与下面的取值对应
                token.putExt(ESBSTDKeys.USER_INFO_KEY,context.getRightValue(ESBSTDKeys.USER_INFO_KEY,null,cookies,-1));

                //反向注入
                context.ssoToken = token.ssoToken;

                ESBResponse res = new ESBResponse();
                res.result = serializer.serialized(token);
                return res;
            }

            // 反向清除
            context.ssoToken = null;
            context.ssoSecur = null;

            //其他参数没有验证过
            ESBResponse res = new ESBResponse();
            res.result = "{\"success\":false}";
            return res;
        }

        //2、ssoSecur ==> 新域名下的ESBToken
        if (context.ssoSecur != null) {//如何确保请求是走https，而不是http：在https下换得sso，再用http下使用sso来拿token

            //发现已经是登录场景，必须停止授权认证（无论登录是否有效），不能替换原有认证
            if (context.usecur != null) {
                ESBToken token = new ESBToken();
                token.success = true;
                if (context.uid != null && context.uid.length() > 0) {
                    token.scope = "user";
                } else if (context.acct != null && context.acct.length() > 0) {
                    token.scope = "account";
                } else {
                    token.scope = "user";
                }
                token.token = context.utoken;
                token.key = context.usecur.key;
                token.user = context.getExt(ESBSTDKeys.USER_INFO_KEY);
                token.did = context.did;
                token.uid = context.uid;
                token.acct = context.acct;
                token.expire = context.usecur.expire;

                //不返回原stoken和refresh
                //token.stoken = "";
                //token.refresh = "";

                ESBResponse res = new ESBResponse();
                res.result = serializer.serialized(token);
                return res;
            }

            //需要再次验证是否符合当前
            if (ESBT.longInteger(context.uid) == context.ssoSecur.uid
                    && ESBT.longInteger(context.did) == context.ssoSecur.tdid
                    && ESBT.integer(context.aid) == context.ssoSecur.taid
                    && context.host != null
                    && context.host.toLowerCase().contains(context.ssoSecur.tdomain.toLowerCase())) {

                // 刷新会话id，标识一个新的会话开始
                context.guid = ESBUUID.genSimplifyCID();

                //只有可能是user的token(sso只允许在user权限下进行，account下不行)
                ESBToken token = ESBTokenSign.injectDefaultToken(null,context);

                // 从token exts中获取
//                token.exts = context.exts;

                // 与上面的设置对应
                token.user = context.getExt(ESBSTDKeys.USER_INFO_KEY);

                //反向注入
                context.utoken = token.token;
                context.stoken = token.stoken;
                context.rtoken = token.refresh;

                //注入token到cookie
                context.pushTokenCookie(token.token,token.stoken,token.user);

                ESBResponse res = new ESBResponse();
                res.result = serializer.serialized(token);
                return res;
            }

            //反向清除
            context.utoken = null;
            context.guid = null;
            context.stoken = null;
            context.rtoken = null;

            context.usecur = null;
            context.ssecur = null;
            context.rsecur = null;

            //清除cookie token
            context.pushTokenCookie("","", "");
        }

        ESBResponse res = new ESBResponse();
        res.result =  "{\"success\":false}";
        return res;
    }

    private static class SingletonHolder {
        private static ESB INSTANCE = new ESB();
    }


    APILoader loader;
    APIInvoker invoker;
    APIParser parser;
    APIMocker mocker;
    APIVerify verify;
    APIObserver observer;
    APILogger logger;
    APIRisky  risky;

    ESBDispatchQueue queue = new ESBDispatchQueue(1000,"esb");

    //所有的apis
//    LRUCache<String,ESBAPIInfo> apis = new LRUCache<String,ESBAPIInfo>(ESBConfigCenter.instance().getApiCapacitySize());
//    Map<String,ESBAPIInfo> apis = new ConcurrentHashMap<String,ESBAPIInfo>();
}
