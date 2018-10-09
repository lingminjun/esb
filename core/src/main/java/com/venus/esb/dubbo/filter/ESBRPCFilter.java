package com.venus.esb.dubbo.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Activate;

import com.alibaba.dubbo.rpc.*;
import com.alibaba.dubbo.rpc.support.ProtocolUtils;
import com.github.kristofa.brave.Brave;
import com.venus.esb.brave.ESBBraveFactory;
import com.venus.esb.dubbo.brave.DubboClientRequestAdapter;
import com.venus.esb.dubbo.brave.DubboResponseAdapter;
import com.venus.esb.dubbo.brave.DubboServerRequestAdapter;
import com.venus.esb.lang.*;
import com.venus.esb.sign.ESBUUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;


/**
 * Created by lingminjun on 17/6/21.
 */
//genericFilter = provider=-20000 consumer=20000(调用端尽量再要调用出去时处理)
//ContextFilter order 是 provider和consumer都是-10000,只要比-10000大一点点就行,故填-9999
// RPCFilter回比EchoFilter=provider=-110000,-110001先调用
@Activate(
        group = {"provider", "consumer"}, order = -9999
)
public class ESBRPCFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(ESBRPCFilter.class);

    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {

        /**
         * Filter拦截provider 是响应端, 可以从RpcContext中获取tid,
         *       url为 dubbo://10.32.184.27:20880/com.venus.dubbo.demo.DemoService?anyhost=true&application=demo-provider&dubbo=2.5.3-notification-fix&interface=com.venus.dubbo.demo.DemoService&methods=standardCall,getList,sayHello&pid=5354&side=provider&threads=400&timestamp=1500638300458
         * Filter拦截consumer 是请求端, 请求端设置tid
         *       url为 dubbo://192.168.1.100:20880/com.venus.dubbo.demo.inferface.GenericService?anyhost=true&application=demo-consumer&check=false&dubbo=2.5.3-notification-fix&generic=true&interface=com.venus.dubbo.demo.inferface.GenericService&methods=callReturnPersons,callReturnString,callReturnPersonss,xxxCallback,callReturnDepartment,callReturnDepartments,callReturnPersonsV5,callReturnBoolean,callReturnList,callReturnIntergeArray,callReturnDepartmentPersons,callReturnFloat,callReturnInterge,callReturnPerson&pid=7402&side=consumer&timeout=3000&timestamp=1500903356483
         */
//        System.out.println("lmjtest:" + invoker.getUrl());
        //EchoTesting直接不做拦截在consumer中(provider根本就不会被调到,因为早已被处理)
        if (isEchoTesting(invocation)) {
            return invoker.invoke(invocation);
        }

        RpcContext context = RpcContext.getContext();
        ESBContext esbcxt = null;
        boolean consumer = false;

        String tid = null;
        String parent_cid = null;
        String cid = null;

        if("consumer".equals(invoker.getUrl().getParameter("side"))) {
            consumer = true;
            esbcxt = ESBContext.getContext();//只有consumer端,必须生产context
            //确保数据传递(自建cid\tid\parent_cid方案是为了将来自定义支持其他链路跟着，并不是brave需要)
            context.setAttachment(ESBSTDKeys.CID_KEY,esbcxt.getCid());//传递到provider端
            context.setAttachment(ESBSTDKeys.TID_KEY,esbcxt.getTid());//传递到provider端
            if (ESBThreadLocal.get(ESBSTDKeys.PARENT_CID_KEY) != null) {
                context.setAttachment(ESBSTDKeys.PARENT_CID_KEY, ESBThreadLocal.get(ESBSTDKeys.PARENT_CID_KEY));//传递到provider端
            }
            context.setAttachment(ESBSTDKeys.L10N_KEY,esbcxt.getL10n());//传递到provider端
            context.setAttachment(ESBSTDKeys.CLIENT_NAME_KEY,invoker.getUrl().getParameter("application"));//作为client写入

            //必要的业务数据
            if (esbcxt.getAid() != null) {
                context.setAttachment(ESBSTDKeys.AID_KEY, esbcxt.getAid());
            }
            if (esbcxt.getDid() != null) {
                context.setAttachment(ESBSTDKeys.DID_KEY, esbcxt.getDid());
            }
            if (esbcxt.getUid() != null) {
                context.setAttachment(ESBSTDKeys.UID_KEY, esbcxt.getUid());
            }
            if (esbcxt.getPid() != null) {
                context.setAttachment(ESBSTDKeys.PID_KEY, esbcxt.getPid());
            }
            if (ESBThreadLocal.get(ESBSTDKeys.MOCK_FLAG_KEY) != null) {
                context.setAttachment(ESBSTDKeys.MOCK_FLAG_KEY, ESBThreadLocal.get(ESBSTDKeys.MOCK_FLAG_KEY));
            }

            //用于日志
            tid = esbcxt.getTid();
            cid = esbcxt.getCid();
            parent_cid = ESBThreadLocal.get(ESBSTDKeys.PARENT_CID_KEY);

//            System.out.println("consumer:"+ ESBUUID.getProcessID()+"(tid:"+ESBUUID.convertProclaimedCID(tid)+";prev_cid:"+ESBUUID.convertProclaimedCID(parent_cid)+";cid:"+ESBUUID.convertProclaimedCID(cid)+")");
//            System.out.println("consume1:"+ ESBUUID.getProcessID()+"(tid:"+ESBUUID.convertSimplifyCID(esbcxt.getTid())+";cid:"+ESBUUID.convertSimplifyCID(esbcxt.getCid())+")");
//            System.out.println("consume2:"+ ESBUUID.getProcessID()+"(tid:"+ESBUUID.convertProclaimedCID(ESBUUID.convertSimplifyCID(esbcxt.getTid()))+";cid:"+ESBUUID.convertProclaimedCID(ESBUUID.convertSimplifyCID(esbcxt.getCid()))+")");
        } else {
//            ESBContext.removeContext();//防止脏数据,效率上不合理

            //确保本地数据预置
            ESBThreadLocal.put(ESBSTDKeys.TID_KEY,context.getAttachment(ESBSTDKeys.TID_KEY));//记录tid,继续透传
            ESBThreadLocal.put(ESBSTDKeys.PARENT_CID_KEY,context.getAttachment(ESBSTDKeys.CID_KEY));//从传过来的cid来做parent cid
            ESBThreadLocal.put(ESBSTDKeys.L10N_KEY,context.getAttachment(ESBSTDKeys.L10N_KEY));//保留上一次的
//            ESBThreadLocal.put(ESBSTDKeys.CLIENT_NAME_KEY,context.getAttachment(ESBSTDKeys.CLIENT_NAME_KEY));//作为client写入
            //确保日志能打印
            ESBMDC.put(ESBSTDKeys.CID_KEY,context.getAttachment(ESBSTDKeys.CID_KEY));//仅仅用于日志打印
            ESBMDC.put(ESBSTDKeys.TID_KEY,context.getAttachment(ESBSTDKeys.TID_KEY));

            //必要的业务数据传递
            ESBThreadLocal.put(ESBSTDKeys.AID_KEY,context.getAttachment(ESBSTDKeys.AID_KEY));
            ESBThreadLocal.put(ESBSTDKeys.DID_KEY,context.getAttachment(ESBSTDKeys.DID_KEY));
            ESBThreadLocal.put(ESBSTDKeys.UID_KEY,context.getAttachment(ESBSTDKeys.UID_KEY));
            ESBThreadLocal.put(ESBSTDKeys.PID_KEY,context.getAttachment(ESBSTDKeys.PID_KEY));
            ESBMDC.put(ESBSTDKeys.AID_KEY,context.getAttachment(ESBSTDKeys.AID_KEY));
            ESBMDC.put(ESBSTDKeys.DID_KEY,context.getAttachment(ESBSTDKeys.DID_KEY));
            ESBMDC.put(ESBSTDKeys.UID_KEY,context.getAttachment(ESBSTDKeys.UID_KEY));
            ESBMDC.put(ESBSTDKeys.PID_KEY,context.getAttachment(ESBSTDKeys.PID_KEY));

            ESBThreadLocal.put(ESBSTDKeys.MOCK_FLAG_KEY, context.getAttachment(ESBSTDKeys.MOCK_FLAG_KEY));

            //用于日志
            tid = context.getAttachment(ESBSTDKeys.TID_KEY);
            cid = context.getAttachment(ESBSTDKeys.CID_KEY);
            parent_cid = context.getAttachment(ESBSTDKeys.PARENT_CID_KEY);//仅仅从consumer传递过来,

//            System.out.println("provider:"+ ESBUUID.getProcessID()+"(tid:"+ESBUUID.convertProclaimedCID(tid)+";prev_cid:"+ESBUUID.convertProclaimedCID(parent_cid)+";cid:"+ESBUUID.convertProclaimedCID(cid)+")");
//            System.out.println("provide1:"+ ESBUUID.getProcessID()+"(tid:"+ESBUUID.convertSimplifyCID(context.getAttachment(ESBSTDKeys.TID_KEY))+";cid:"+ESBUUID.convertSimplifyCID(context.getAttachment(ESBSTDKeys.CID_KEY))+")");
//            System.out.println("provide2:"+ ESBUUID.getProcessID()+"(tid:"+ESBUUID.convertProclaimedCID(ESBUUID.convertSimplifyCID(context.getAttachment(ESBSTDKeys.TID_KEY)))+";cid:"+ESBUUID.convertProclaimedCID(ESBUUID.convertSimplifyCID(context.getAttachment(ESBSTDKeys.CID_KEY)))+")");
//            System.out.println("provider:"+ ESBUUID.getProcessID()+"(aid:"+context.getAttachment(ESBSTDKeys.AID_KEY)+";uid:"+context.getAttachment(ESBSTDKeys.UID_KEY)+")");
        }

        // 先清空Rpc中的Notice，作为媒介处理ext和cookie
//        RpcContext.getContext().clearAttachments();
//        RpcContext.getContext().clearNotifications();

        //日志处理
        try {
            braveInvoking(invoker,invocation,tid,cid,parent_cid,consumer);
        } catch (Throwable e) {e.printStackTrace();}

        Result var7 = null;
        String exceptoinMessage = null;
        boolean success = false;
        try {
            Result e = invoker.invoke(invocation);
            var7 = e;
            if(e.hasException()){
                exceptoinMessage = getExceptionStackTrace(e.getException());//不是很通用,关键的错误原因并没记录下来
            } else {
                if (esbcxt != null) {
                    if (consumer) {//RpcContext已经完成使命，清除数据
                        esbcxt.setDubboExts(RpcContext.getContext().getAttachments());
                    } else {//provider端填充数据带到consumer端
                        if (RpcResult.class.isInstance(e)) {
                            RpcResult rpcResult = (RpcResult) e;
                            addAttachments(rpcResult,esbcxt.getDubboExts());
//                            rpcResult.addAttachments(esbcxt.getDubboExts());
                        }
                    }
                }
                success = true;
            }
        } catch (ESBRuntimeException var11) {//将runtime exception转esb exception
            ESBException exception = var11.getException();
            exceptoinMessage = getExceptionStackTrace(exception);//不是很通用,关键的错误原因并没记录下来
            throw new RpcException(exception);
        } catch (RpcException var11) {
            exceptoinMessage = getExceptionStackTrace(var11);//不是很通用,关键的错误原因并没记录下来
            throw var11;
        } catch (Throwable e) {
            exceptoinMessage = getExceptionStackTrace(e);//不是很通用,关键的错误原因并没记录下来
            throw new RpcException(e);
        } finally {
            if(consumer) {
                //do nothing
            } else {
                if (esbcxt != null) {//只有consumer端
                    esbcxt.clear();
                }

                ESBMDC.remove(ESBSTDKeys.CID_KEY);
                ESBMDC.remove(ESBSTDKeys.TID_KEY);
                ESBMDC.remove(ESBSTDKeys.AID_KEY);
                ESBMDC.remove(ESBSTDKeys.DID_KEY);
                ESBMDC.remove(ESBSTDKeys.UID_KEY);
                ESBMDC.remove(ESBSTDKeys.PID_KEY);

                ESBThreadLocal.remove(ESBSTDKeys.TID_KEY);//记录tid,继续透传
                ESBThreadLocal.remove(ESBSTDKeys.AID_KEY);
                ESBThreadLocal.remove(ESBSTDKeys.DID_KEY);
                ESBThreadLocal.remove(ESBSTDKeys.UID_KEY);
                ESBThreadLocal.remove(ESBSTDKeys.PID_KEY);

                ESBThreadLocal.remove(ESBSTDKeys.MOCK_FLAG_KEY);

            }

            //日志处理
            try {
                braveInvoked(invoker,invocation,success,var7,exceptoinMessage,tid,cid,parent_cid,consumer);
            } catch (Throwable e) {e.printStackTrace();}

//            String server = "provider";
//            if (consumer) {
//                server = "consumer";
//            }
//            System.out.println(server+":"+ context.getUrl());
        }
        return var7;
    }

    private static void addAttachments(RpcResult result, Map<String, String> exts) {
        Set<Map.Entry<String,String>> entrySet = exts.entrySet();
        for (Map.Entry<String,String> entry : entrySet) {
            //兼容新老dubbo版本
            result.setAttachment(entry.getKey(),entry.getValue());
        }
//        result.addAttachments(esbcxt.getDubboExts());
    }

    private static String getExceptionStackTrace(Throwable e) {
        String msg = e.getMessage();
        StringBuilder builder = new StringBuilder(msg == null ? "" : msg);
        for(StackTraceElement elem : e.getStackTrace()) {
            builder.append("\t" + elem.toString() + "\n");
        }
        String var = builder.toString();
        e.printStackTrace();
        return var;
    }

    private static boolean isEchoTesting(Invocation inv) {
        String methodName = inv.getMethodName();
        //必须兼容invoke (provider此时排在)
        if (Constants.$ECHO.equals(methodName)) {
            return true;
        } else if (Constants.$INVOKE.equals(methodName)
                && inv.getArguments() != null
                && inv.getArguments().length == 3) {
            methodName = ((String) inv.getArguments()[0]).trim();
            if (Constants.$ECHO.equals(methodName)) {
                return true;
            }
        }
        return false;
    }

    private static String getSpanName(Invoker<?> invoker, Invocation inv) {
        RpcContext context = RpcContext.getContext();
        URL url = context.getUrl();
        String className = url.getPath();
//        String simpleName = className.substring(className.lastIndexOf(".")+1);

        String methodName = inv.getMethodName();
        //必须兼容invoke (provider此时排在)
        if (Constants.$INVOKE.equals(methodName)
                && inv.getArguments() != null
                && inv.getArguments().length == 3
                && ProtocolUtils.isGeneric(invoker.getUrl().getParameter(Constants.GENERIC_KEY)) //接口调用方式就是泛型
                ) {
            methodName = ((String) inv.getArguments()[0]).trim();
        }
        return className+"."+methodName;
    }

    private void braveInvoking(Invoker<?> invoker, Invocation invocation, String tid, String cid, String parent_cid, boolean consumer) {
        Brave brave = ESBBraveFactory.getBrave();
        if (brave == null) {
            return;
        }
        RpcContext context = RpcContext.getContext();
        String spanName = getSpanName(invoker,invocation);

        if (consumer) {
            brave.clientRequestInterceptor().handle(new DubboClientRequestAdapter(context,invocation,spanName));
        } else {
            brave.serverRequestInterceptor().handle(new DubboServerRequestAdapter(brave,context,spanName));
        }
    }

    private void braveInvoked(Invoker<?> invoker, Invocation invocation, boolean success ,Result result, String exceptoinMessage, String tid, String cid, String parent_cid, boolean consumer) {
        Brave brave = ESBBraveFactory.getBrave();
        if (brave == null) {
            return;
        }

        if (consumer) {
            brave.clientResponseInterceptor().handle(new DubboResponseAdapter(true,success,result,exceptoinMessage));
        } else {
            brave.serverResponseInterceptor().handle(new DubboResponseAdapter(false,success,result,exceptoinMessage));
        }
    }


}
