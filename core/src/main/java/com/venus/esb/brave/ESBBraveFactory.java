package com.venus.esb.brave;

import com.github.kristofa.brave.*;
import com.venus.esb.config.ESBConfigCenter;
import com.venus.esb.lang.ESBT;
import com.venus.esb.utils.Hex;
import com.twitter.zipkin.gen.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Created by jack-cooper on 2017/2/20.
 */
public class ESBBraveFactory {

    private static final Logger logger = LoggerFactory.getLogger(ESBBraveFactory.class);
    private static final float rate = 1.0f;//采样率1.0

    public static Brave getBrave() {
        return getBrave(true);
    }

    public static Brave getBrave(boolean forcedLoad) {
        if (_hasBrave || !forcedLoad) {
            return _brave;
        }
        synchronized (ESBBraveFactory.class) {
            if (_hasBrave) {
                return _brave;
            }

            //开启上报
            if (ESBConfigCenter.instance().openBrave()) {
                String zipkinHost = ESBConfigCenter.instance().getZipkinHost();
                String braveLogger = ESBConfigCenter.instance().getBraveDir();
                Brave.Builder builder = new Brave.Builder(ESBT.getServiceName());
                /*if (zipkinHost != null && zipkinHost.length() > 0) {
                    builder.spanCollector(KafkaSpanCollector.create(zipkinHost, new EmptySpanCollectorMetricsHandler())).traceSampler(Sampler.create(rate)).build();
                    logger.info("brave config collect whith kafkaSpanColler , rate is " + rate);
                } else*/ if (zipkinHost != null && zipkinHost.length() > 0) {
//                    builder.spanCollector(HttpSpanCollector.create(zipkinHost, new EmptySpanCollectorMetricsHandler())).traceSampler(Sampler.create(rate)).build();
                    builder.spanCollector(OfflineSpanCollector.create(zipkinHost, braveLogger)).traceSampler(Sampler.create(rate)).build();
                    logger.info("brave config collect whith httpSpanColler , rate is " + rate);
                } else {
                    builder.spanCollector(new LoggingSpanCollector()).traceSampler(Sampler.create(rate)).build();
                    logger.info("brave config collect whith loggingSpanColletor , rate is " + rate);
                }
                _brave = builder.build();
            }

            _hasBrave = true;
        }
        return _brave;
    }

    /**
     * 自行设置span衔接 spanId+@+spanName
     * @param span
     */
    public static void setCurrentSpan(String span) {
        Brave brave = getBrave();
        if (brave == null) {
            return;
        }

        if (span != null && span.length() > 0) {
            String[] strs = span.split("@");
            SpanId spanId = SpanId.fromBytes(Hex.decodeHexString(strs[0]));
            if (spanId == null) {
                return;
            }

            try {
                Class<?> threadClazz = Class.forName(ServerSpan.class.getName());
                Method method = threadClazz.getDeclaredMethod("create", Span.class, Boolean.class);
                if (method != null) {
                    method.setAccessible(true);
                    Span sp = spanId.toSpan();
                    if (strs.length > 1) {
                        sp.setName(strs[1]);
                    } else {
                        sp.setName("service.offline.invoke");
                    }

                    sp.setTimestamp(System.currentTimeMillis());
                    Boolean bb = spanId.sampled();
                    if (bb == null) {
                        bb = Boolean.TRUE;
                    }
                    ServerSpan serverSpan = (ServerSpan)method.invoke(null, sp, bb);
                    brave.serverSpanThreadBinder().setCurrentSpan(serverSpan);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else {
            brave.serverSpanThreadBinder().setCurrentSpan(null);
        }
    }

    public static String getCurrentSpan() {
        Brave brave = getBrave();
        if (brave == null) {
            return null;
        }

        ServerSpan serverSpan = brave.serverSpanThreadBinder().getCurrentServerSpan();
        if (serverSpan == null) {
            return null;
        }
        Span span = serverSpan.getSpan();
        if (span == null) {
            return null;
        }
        SpanId.Builder builder = SpanId.builder();
        builder.spanId(span.getId());
        builder.traceId(span.getTrace_id());
        builder.parentId(span.getParent_id());
        Boolean b = span.isDebug();
        if (b != null) {
            builder.debug(b);
        }
        SpanId spanId = builder.build();
        return Hex.encodeHexString(spanId.bytes()) + "@" + span.getName();
    }

    /**单例模式*/
    private static Brave _brave;
    private static volatile boolean _hasBrave = false;

}
