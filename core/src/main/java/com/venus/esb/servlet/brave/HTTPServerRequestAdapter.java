package com.venus.esb.servlet.brave;

import com.github.kristofa.brave.*;
import com.venus.esb.brave.IPV4Conversion;
import com.venus.esb.lang.ESBT;
import com.venus.esb.sign.ESBUUID;
import com.venus.esb.utils.Hex;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by lmj on 17/9/1.
 * 服务端收到请求(收到就汇报)
 */
public class HTTPServerRequestAdapter implements ServerRequestAdapter {

    private final Brave brave;
    private final String method;
    private final String url;
    private final String path;
    private final String spanId;
    private final String clientName;
    private final String clientIp;
    private final int clientPort;
    private final String serverIp;

    public HTTPServerRequestAdapter(Brave brave, String method, String url, String path, String spanId, String sip, String client, String cip, int cport) {
        this.brave = brave;
        this.method = method;
        this.url = url;
        this.path = path;
        this.spanId = spanId;
        this.clientName = client;
        this.clientIp = cip == null ? "127.0.0.1" : cip;
        this.clientPort = cport;
        this.serverIp = sip == null ? "127.0.0.1" : sip;
    }

    @Override
    public TraceData getTraceData() {
        String span = this.spanId;
        if (span != null) {
            SpanId spanId = SpanId.fromBytes(Hex.decodeHexString(span));
            return TraceData.builder().sample(true).spanId(spanId).build();
        }
        return TraceData.builder().build();

    }

    @Override
    public String getSpanName() {
        if (path != null && path.length() > 0) {
            return method + " " + path;
        }
        return method + " " + url;
    }

    @Override
    public Collection<KeyValueAnnotation> requestAnnotations() {

        int port = this.clientPort;

        brave.serverTracer().setServerReceived(IPV4Conversion.convertToInt(clientIp),port,clientName);

        Collection<KeyValueAnnotation> annotations = new ArrayList<KeyValueAnnotation>();
        //Server Address
        String ipAddr = serverIp;
        String server = ESBT.getServiceName();

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
