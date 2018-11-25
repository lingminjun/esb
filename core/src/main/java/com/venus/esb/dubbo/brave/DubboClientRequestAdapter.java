package com.venus.esb.dubbo.brave;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.json.JSON;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.RpcContext;
import com.github.kristofa.brave.ClientRequestAdapter;
import com.github.kristofa.brave.KeyValueAnnotation;
import com.github.kristofa.brave.SpanId;
import com.github.kristofa.brave.internal.Nullable;
import com.venus.esb.lang.ESBConsts;
import com.venus.esb.lang.ESBSTDKeys;
import com.venus.esb.lang.ESBThreadLocal;
import com.venus.esb.sign.ESBUUID;
import com.venus.esb.utils.Hex;
import com.twitter.zipkin.gen.Endpoint;

import java.io.IOException;
import java.util.*;

/**
 * Created by lmj on 17/9/1.
 * 客户端发起请求
 */
public class DubboClientRequestAdapter implements ClientRequestAdapter {
//    private Invoker<?> invoker;
//    private Invocation invocation;
//    private final static DubboSpanNameProvider spanNameProvider = new DefaultSpanNameProvider();
//    private final static DubboServerNameProvider serverNameProvider = new DefaultServerNameProvider();

    private final String spanName;
    private final RpcContext context;
    private final Invocation invocation;

    public DubboClientRequestAdapter(RpcContext context, Invocation invocation, String spanName) {
        this.context = context;
        this.invocation = invocation;
        this.spanName = spanName;
    }

    @Override
    public String getSpanName() {
        if (spanName != null) {
            return spanName;
        }
        String className = context.getUrl().getPath();
//        String simpleName = className.substring(className.lastIndexOf(".")+1);
        return className+"."+context.getMethodName();
    }

    @Override
    public void addSpanIdToRequest(@Nullable SpanId spanId) {
//        System.out.println("client spanId:" + spanId);
        if (spanId != null) {//不监控
            context.setAttachment(ESBConsts.ZIPKIN_BRAVE_SPAN_ID_KEY, Hex.encodeHexString(spanId.bytes()));
        }
//        String application = RpcContext.getContext().getUrl().getParameter("application");
//        RpcContext.getContext().setAttachment("clientName", application);
//        if (spanId == null) {
//            RpcContext.getContext().setAttachment("sampled", "0");
//        }else{
//            RpcContext.getContext().setAttachment("traceId", IdConversion.convertToString(spanId.traceId));
//            RpcContext.getContext().setAttachment("spanId", IdConversion.convertToString(spanId.spanId));
//            if (spanId.nullableParentId() != null) {
//                RpcContext.getContext().setAttachment("parentId", IdConversion.convertToString(spanId.parentId));
//            }
//        }
    }


    private static final Set<String> excepts = new HashSet<String>();//需要被排出的参数
    private static final Set<String> alone = new HashSet<String>();//需要单独记录参数
    static {
        excepts.add("protocol");
        excepts.add("username");
        excepts.add("password");
        excepts.add("host");
        excepts.add("port");
        excepts.add("path");
        excepts.add("anyhost");//dubbo参数,说明非指定某个服务调用,基本没多少用
        excepts.add("application");//dubbo服务名称,没多少用
        excepts.add("interface");//dubbo接口服务名,没用意义,因为前面我们已经定义在spanName中
        excepts.add("methods");//dubbo服务多有接口名,没啥意义
        excepts.add("side");//dubbo provider和consumer标示,没啥意义
        excepts.add("check");//dubbo 是否异步加载,我们必须异步,所以意义不大
        excepts.add("async");//dubbo 是否异步调用,意义不大
        excepts.add("pid");//dubbo server端进程,后面单独设置client进程和服务端进程
//        excepts.add("retries");//dubbo 重试次数,关键不知道是第几次重试

//        excepts.add("generic");//dubbo 是否泛型,意义不是很大
        excepts.add(ESBSTDKeys.AID_KEY);//会在record中单独记录
        excepts.add(ESBSTDKeys.DID_KEY);//会在record中单独记录
        excepts.add(ESBSTDKeys.UID_KEY);//会在record中单独记录
        excepts.add(ESBSTDKeys.ACCT_KEY);//会在record中单独记录
        excepts.add(ESBSTDKeys.PID_KEY);//会在record中单独记录
        excepts.add(ESBSTDKeys.L10N_KEY);//会在record中单独记录
        excepts.add(ESBSTDKeys.TOKEN_KEY);//敏感信息不应该记录
        excepts.add(ESBSTDKeys.SECRET_TOKEN_KEY);//敏感信息不应该记录
        excepts.add(ESBSTDKeys.DEVICE_TOKEN_KEY);//敏感信息不应该记录
//        excepts.add("_context");//上下文信息
        excepts.add(ESBSTDKeys.CID_KEY);
        excepts.add(ESBSTDKeys.TID_KEY);
        excepts.add(ESBSTDKeys.CH_KEY);
        excepts.add(ESBSTDKeys.SRC_KEY);
        excepts.add(ESBSTDKeys.SMP_KEY);
        excepts.add(ESBSTDKeys.VIA_KEY);
        excepts.add(ESBSTDKeys.DNA_KEY);
        excepts.add(ESBSTDKeys.UA_KEY);
        excepts.add(ESBSTDKeys.CIP_KEY);
        excepts.add(ESBSTDKeys.CVC_KEY);
        excepts.add(ESBSTDKeys.CVN_KEY);
        excepts.add(ESBSTDKeys.HOST_KEY);
        excepts.add(ESBSTDKeys.REFERER_KEY);
        excepts.add(ESBSTDKeys.MOENT_KEY);
        excepts.add(ESBSTDKeys.CONTENT_TYPE_KEY);
        excepts.add(ESBSTDKeys.SELECTOR_KEY);
        excepts.add(ESBSTDKeys.SIGN_KEY);
        excepts.add(ESBSTDKeys.CAPTCHA_KEY);
        excepts.add(ESBSTDKeys.SIGNATURE_METHOD_KEY);
        excepts.add(ESBSTDKeys.JSONP_CALLBACK_KEY);
        excepts.add(ESBSTDKeys.POST_BODY_KEY);
        
        //需要单独记录的
//        alone.add("revision");//接受方版本
//        alone.add("version");//发起方版本
        alone.add("timestamp");//调用时间戳,单独记录
    }


    @Override
    public Collection<KeyValueAnnotation> requestAnnotations() {
        //参数,直接把url记下来太过于粗暴【可以修改,仅仅记录有用的参数】
        //dubbo://10.32.184.27:20880/com.venus.dubbo.demo.DemoService?anyhost=true&application=demo-provider&dubbo=2.5.3-notification-fix&interface=com.venus.dubbo.demo.DemoService&methods=standardCall,getList,sayHello&pid=5354&side=provider&threads=400&timestamp=1500638300458

        //应该被记录下来的参数 [参数记录]
        //pid=27410&retries=0&revision=DEV1&side=consumer&timeout=2000&timestamp=1504277024944&version=DEV1
        URL url = context.getUrl();

        Collection<KeyValueAnnotation> annotations = new ArrayList<KeyValueAnnotation>();

        //基本参数拉取
        addESBParameters(annotations);

        //拉取参数
        Map<String,String> map = url.toMap();
        Iterator<Map.Entry<String,String>> entries = map.entrySet().iterator();
        StringBuilder builder = new StringBuilder();
        boolean isFirst = true;
        while (entries.hasNext()) {
            Map.Entry<String,String> entry = entries.next();

            //去掉一些没必要的参数
            String key = entry.getKey();
            if(excepts.contains(key)) {//直接舍弃好了,没有意义
                continue;
            } else if (alone.contains(key)) {
                annotations.add(KeyValueAnnotation.create(key, entry.getValue()));
            } else {
                if (isFirst) {
                    isFirst = false;
                } else {
                    builder.append("&");
                }
                builder.append(key+"="+entry.getValue());
            }
//            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.parseValue());
        }

        //取参数
        addDubboParameters(annotations,invocation);

        String parameters = builder.toString();
        if (parameters != null && parameters.length() > 0) {//dubbo的其他参数
            annotations.add(KeyValueAnnotation.create("dubbo", parameters));
        }

        //设置client进程
        annotations.add(KeyValueAnnotation.create("Client PId", ESBUUID.getProcessID()));

        return annotations;
    }

    private static void addDubboParameters(Collection<KeyValueAnnotation> annotations, Invocation inv) {
        Object[] args = inv.getArguments();
        if(Constants.$INVOKE.equals(inv.getMethodName())
                && args != null
                && args.length == 3) {
            args = (Object[])args[2];
        }

        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                Object obj = args[i];
                if (obj != null) {//采用dubbo的方式
                    String json = null;
                    try {
                        json = JSON.json(obj);
                    } catch (IOException e) {
                        json = obj.toString();
                    }
                    /*//因为上报会做压缩,所以不考虑压缩问题
                    if (json.length() >= com.venus.esb.brave.Constants.BRAVE_CONTENT_MAX_LENGTH) {//超过则做压缩存储
                        json = GZIP.compressToBase64String(json,ESBConsts.UTF8_STR);
                        annotations.add(KeyValueAnnotation.create("_var" + i, json));
                        annotations.add(KeyValueAnnotation.create("_var_zip" + i, "true"));
                    } else {
                    */
                        annotations.add(KeyValueAnnotation.create("_var" + i, json));
                    /*}*/
                } else {
                    annotations.add(KeyValueAnnotation.create("_var" + i, "null"));
                }
            }
        }
    }

    private static void addESBParameters(Collection<KeyValueAnnotation> annotations) {
        annotations.add(KeyValueAnnotation.create("protocol", "dubbo"));

        String v = ESBThreadLocal.get(ESBSTDKeys.TID_KEY);//主要是方便与日志对应起来
        if (v != null) {
            annotations.add(KeyValueAnnotation.create(ESBSTDKeys.TID_KEY, v));
        }
        v = ESBThreadLocal.get(ESBSTDKeys.AID_KEY);
        if (v != null) {
            annotations.add(KeyValueAnnotation.create(ESBSTDKeys.AID_KEY, v));
        }
        v = ESBThreadLocal.get(ESBSTDKeys.DID_KEY);
        if (v != null) {
            annotations.add(KeyValueAnnotation.create(ESBSTDKeys.DID_KEY, v));
        }
        v = ESBThreadLocal.get(ESBSTDKeys.UID_KEY);
        if (v != null) {
            annotations.add(KeyValueAnnotation.create(ESBSTDKeys.UID_KEY, v));
        }
        v = ESBThreadLocal.get(ESBSTDKeys.PID_KEY);
        if (v != null) {
            annotations.add(KeyValueAnnotation.create(ESBSTDKeys.PID_KEY, v));
        }
        v = ESBThreadLocal.get(ESBSTDKeys.L10N_KEY);
        if (v != null) {
            annotations.add(KeyValueAnnotation.create(ESBSTDKeys.L10N_KEY, v));
        }
        v = ESBThreadLocal.get(ESBSTDKeys.CH_KEY);
        if (v != null) {
            annotations.add(KeyValueAnnotation.create(ESBSTDKeys.CH_KEY, v));
        }
        v = ESBThreadLocal.get(ESBSTDKeys.SRC_KEY);
        if (v != null) {
            annotations.add(KeyValueAnnotation.create(ESBSTDKeys.SRC_KEY, v));
        }
        v = ESBThreadLocal.get(ESBSTDKeys.SMP_KEY);
        if (v != null) {
            annotations.add(KeyValueAnnotation.create(ESBSTDKeys.SMP_KEY, v));
        }
        v = ESBThreadLocal.get(ESBSTDKeys.DNA_KEY);
        if (v != null) {
            annotations.add(KeyValueAnnotation.create(ESBSTDKeys.DNA_KEY, v));
        }
        v = ESBThreadLocal.get(ESBSTDKeys.UA_KEY);
        if (v != null) {
            annotations.add(KeyValueAnnotation.create(ESBSTDKeys.UA_KEY, v));
        }
        v = ESBThreadLocal.get(ESBSTDKeys.CIP_KEY);
        if (v != null) {
            annotations.add(KeyValueAnnotation.create(ESBSTDKeys.CIP_KEY, v));
        }
        v = ESBThreadLocal.get(ESBSTDKeys.CVC_KEY);
        if (v != null) {
            annotations.add(KeyValueAnnotation.create(ESBSTDKeys.CVC_KEY, v));
        }
        v = ESBThreadLocal.get(ESBSTDKeys.CVN_KEY);
        if (v != null) {
            annotations.add(KeyValueAnnotation.create(ESBSTDKeys.CVN_KEY, v));
        }
        v = ESBThreadLocal.get(ESBSTDKeys.HOST_KEY);
        if (v != null) {
            annotations.add(KeyValueAnnotation.create(ESBSTDKeys.HOST_KEY, v));
        }
        v = ESBThreadLocal.get(ESBSTDKeys.REFERER_KEY);
        if (v != null) {
            annotations.add(KeyValueAnnotation.create(ESBSTDKeys.REFERER_KEY, v));
        }
        v = ESBThreadLocal.get(ESBSTDKeys.SELECTOR_KEY);
        if (v != null) {
            annotations.add(KeyValueAnnotation.create(ESBSTDKeys.SELECTOR_KEY, v));
        }
        v = ESBThreadLocal.get(ESBSTDKeys.SIGN_KEY);
        if (v != null) {
            annotations.add(KeyValueAnnotation.create(ESBSTDKeys.SIGN_KEY, v));
        }
        v = ESBThreadLocal.get(ESBSTDKeys.CAPTCHA_KEY);
        if (v != null) {
            annotations.add(KeyValueAnnotation.create(ESBSTDKeys.CAPTCHA_KEY, v));
        }
    }

    @Override
    public Endpoint serverAddress() {
//        InetSocketAddress inetSocketAddress = context.getRemoteAddress();
        //dubbo://10.32.184.27:20880/com.venus.dubbo.demo.inferface.GenericService?anyhost=true&application=esb-consumer&check=false&dubbo=2.5.3-notification-fix&generic=true&interface=com.venus.dubbo.demo.inferface.GenericService&methods=callReturnPersons,callReturnString,callReturnPersonss,xxxCallback,callReturnDepartment,callReturnDepartments,callReturnPersonsV5,callReturnBoolean,callReturnList,callReturnIntergeArray,callReturnDepartmentPersons,callReturnFloat,callReturnInterge,callReturnPerson&pid=28712&retries=0&revision=DEV1&side=consumer&timeout=2000&timestamp=1504323723653&version=DEV1
//        System.out.println("server:"+context.getUrl());
        return null;
//        String ipAddr = context.getUrl().getIp();
//        int port = context.getUrl().getPort();
//        String serverName = "EServer";//拿不到serverName context.getUrl().getParameter("application");
//        return Endpoint.create(serverName, IPV4Conversion.convertToInt(ipAddr),port);
    }



}
