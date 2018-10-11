package com.venus.esb.servlet.brave;

import com.alibaba.dubbo.rpc.RpcContext;
import com.github.kristofa.brave.*;
import com.venus.esb.brave.IPV4Conversion;
import com.venus.esb.lang.ESBSTDKeys;
import com.venus.esb.lang.ESBT;
import com.venus.esb.sign.ESBUUID;
import com.venus.esb.utils.Hex;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by lmj on 17/9/1.
 * 服务端收到请求(收到就汇报)
 */
public class HTTPServerRequestAdapter implements ServerRequestAdapter {

    private final RpcContext context;
    private final String spanName;
    private final Brave brave;
//    private final String spanId;
//    private final String traceId;
//    private final String parentId;
//    private Invocation invocation;
//    private ServerTracer serverTracer;
//    private final static  DubboSpanNameProvider spanNameProvider = new DefaultSpanNameProvider();
//    private final static  DubboClientNameProvider clientNameProvider = new DefaultClientNameProvider();



    public HTTPServerRequestAdapter(Brave brave, RpcContext context, String spanName) {
        this.brave = brave;
        this.context = context;
        this.spanName = spanName;
//        this.spanId = cid;
//        this.traceId = tid;
//        this.parentId = pid;
    }

    @Override
    public TraceData getTraceData() {
        String span = context.getAttachment(ESBSTDKeys.ZIPKIN_BRAVE_SPAN_ID_KEY);
        if (span != null) {
            SpanId spanId = SpanId.fromBytes(Hex.decodeHexString(span));
            return TraceData.builder().sample(true).spanId(spanId).build();
        }
//      String sampled =   invocation.getAttachment("sampled");
//      if(sampled != null && sampled.equals("0")){
//          return TraceData.builder().sample(false).build();
//      }else {
//          final String parentId = invocation.getAttachment("parentId");
//          final String spanId = invocation.getAttachment("spanId");
//          final String traceId = invocation.getAttachment("traceId");
//          if (traceId != null && spanId != null) {
//              SpanId span = getSpanId(traceId, spanId, parentId);
//              return TraceData.builder().sample(true).spanId(span).build();
//          }
//      }
//
//        if (traceId != null && spanId != null) {
//            SpanId span = getSpanId(traceId, spanId, parentId);
//            return TraceData.builder().sample(true).spanId(span).build();
//        }
        return TraceData.builder().build();

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
    public Collection<KeyValueAnnotation> requestAnnotations() {


        int port = context.getUrl().getPort();
        InetSocketAddress client_ip = context.getRemoteAddress();
//        ESBThreadLocal.put(ESBSTDKeys.CLIENT_NAME_KEY,context.getAttachment(ESBSTDKeys.CLIENT_NAME_KEY));//作为client写入
        final String clientName = context.getAttachment(ESBSTDKeys.CLIENT_NAME_KEY);

        //Client Address remote_ip + 端口
        brave.serverTracer().setServerReceived(IPV4Conversion.convertToInt(client_ip.getAddress().getHostAddress()),port,clientName);

        Collection<KeyValueAnnotation> annotations = new ArrayList<KeyValueAnnotation>();
        //Server Address
        String ipAddr = context.getUrl().getIp();
        String server = context.getUrl().getParameter("application");
        if (server == null) {
            server = ESBT.getServiceName();
        }
        annotations.add(KeyValueAnnotation.create("Server Address", ipAddr+":"+port+"("+server+")"));
        annotations.add(KeyValueAnnotation.create("Server PId", ESBUUID.getProcessID()));
        return annotations;
    }

    static SpanId getSpanId(String traceId, String spanId, String parentSpanId) {
        return SpanId.builder()
                .traceId(IdConversion.convertToLong(traceId))
                .spanId(IdConversion.convertToLong(spanId))
                .parentId(parentSpanId == null ? null : IdConversion.convertToLong(parentSpanId)).build();
    }


}
