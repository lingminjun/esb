package com.venus.esb.config;

import com.venus.esb.lang.ESBT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lingminjun on 17/4/30.
 */
public final class ESBConfigCenter {

    private static final Logger logger = LoggerFactory.getLogger(ESBConfigCenter.class);

    private String appName;//
    private String registry;//

    private String esbLoader;//api加载器
    private String esbInvoker;//api调用器
    private String esbParser;//api序列器工程
    private String esbVerify;//接口验证器
    private String esbMocker;//mock返回值
    private String esbLogger;//日志
    private String esbObserver;//观察者
    private String esbRisky; //风控检查防止dos攻击

    private int apiCapacitySize;//api容量配置

    private String apisDir;//api缓存目录
    private String apisJSONListPath;//输入所有配置

    private String pubRSAKey;//
    private String priRSAKey;//
    private String aesKey;//
    private String signKey;//crc16,crc32,md5,sha1安全级别为none时,给予固定的验签名方式,安全性非常低,因为此key客户端暴露

    private int dubboTimeout;//超时,默认2000
    private int dubboRetries;//重试,默认0
    private String dubboVersion;
    private String genericFilter;//泛型重载

    private int httpConnectTimeout;//超时,默认2000
    private int httpReadTimeout;//超时,默认2000

    private String zipkinHost;//zipkin的url http://zipkin-server-ip:9411/
    private String braveDir;//zipkin brave日志目录
    private boolean brave;//开启监控

    private boolean esbCloseFilter;//关闭ESBRequestFilter过滤

    private ESBProperties properties;

    private ESBConfigCenter() {

        properties = new ESBProperties(new String[]{"application.properties","xconfig.properties","config.properties","esb.properties"});

        appName = ESBT.getServiceName();//直接取服务名
        if (appName == null || appName.length() == 0) {
            appName = "esb-consumer";
        }

        if (properties.getString("registry.address") != null) {
            registry = properties.getString("registry.address");
        } else {
            registry = properties.getString("dubbo.registry.url");
        }

        esbLoader = properties.getString("com.venus.esb.loader");
        if (ESBT.isEmpty(esbLoader)) {
            esbLoader = "com.venus.esb.ESBAPILoader";
        }
        esbInvoker = properties.getString("com.venus.esb.invoker");
        if (ESBT.isEmpty(esbInvoker)) {
            esbInvoker = "com.venus.esb.ESBAPIInvoker";
        }

        esbParser = properties.getString("com.venus.esb.parser");
        if (ESBT.isEmpty(esbParser)) {
            esbParser = "com.venus.esb.ESBAPIParser";
        }

        esbVerify = properties.getString("com.venus.esb.verify");
        if (ESBT.isEmpty(esbVerify)) {
            esbVerify = "com.venus.esb.ESBAPIVerify";
        }

        esbMocker = properties.getString("com.venus.esb.mocker");
        esbObserver = properties.getString("com.venus.esb.observer");
        esbLogger = properties.getString("com.venus.esb.logger");

        esbRisky = properties.getString("com.venus.esb.risky");
//        if (ESBT.isEmpty(esbRisky)) {
//            esbRisky = "com.venus.esb.ESBAPIRisky";
//        }

        if (properties.getString("com.venus.esb.api.capacity.size") != null) {
            apiCapacitySize = ESBT.integer(properties.getString("com.venus.esb.api.capacity.size"));
        }
        if (apiCapacitySize <= 0) {
            apiCapacitySize = 20000;//默认值两万
        }

        //配置loader相关配置
        apisDir = properties.getString("com.venus.esb.apis.dir");
        apisJSONListPath = properties.getString("com.venus.esb.apis.json.list.path");

        //认证相关
        pubRSAKey = properties.getString("com.venus.esb.rsa.pub.key");
        priRSAKey = properties.getString("com.venus.esb.rsa.pri.key");
        aesKey = properties.getString("com.venus.esb.aes.key");//
        signKey = properties.getString("com.venus.esb.static.sign.key");

        //dubbo调用
        dubboTimeout = properties.getInt("dubbo.reference.timeout",2000);
        dubboRetries = properties.getInt("dubbo.reference.retries",0);
        dubboVersion = properties.getString("dubbo.reference.version");

        //http调用
        httpConnectTimeout = properties.getInt("dubbo.reference.timeout",2000);
        httpReadTimeout = properties.getInt("dubbo.reference.retries",2000);

        //这里配置dubbo的注册中心信息，因此demo没有额外的dubbo.xml配置文件
        if (properties.getString("dubbo.generic.filter") != null) {
            genericFilter = properties.getString("dubbo.generic.filter");
        }

        //这里配置dubbo的注册中心信息，因此demo没有额外的dubbo.xml配置文件
        if (properties.getString("com.venus.esb.zipkin.host") != null) {
            zipkinHost = properties.getString("com.venus.esb.zipkin.host");
        }
        if (properties.getString("com.venus.esb.zipkin.brave.log.dir") != null) {
            braveDir = properties.getString("com.venus.esb.zipkin.brave.log.dir");
        }
        brave = properties.getBoolean("com.venus.esb.open.brave");

        esbCloseFilter = properties.getBoolean("com.venus.esb.exclude.servlet.filter");
    }

    public static ESBConfigCenter instance() {
        return SingletonHolder.INSTANCE;
    }

    public String getAppName() {
        return appName;
    }

    public String getRegistry() {
        return registry;
    }

    public String getEsbLoader() {
        return esbLoader;
    }

    public String getEsbInvoker() {
        return esbInvoker;
    }

    public String getEsbParser() {
        return esbParser;
    }

    public String getEsbVerify() {
        return esbVerify;
    }

    public String getEsbMocker() {
        return esbMocker;
    }

    public String getEsbObserver() {
        return esbObserver;
    }

    public String getEsbLogger() {
        return esbLogger;
    }

    public String getEsbRisky() {
        return esbRisky;
    }

    public int getApiCapacitySize() {
        return apiCapacitySize;
    }

    public String getApisDir() {
        return apisDir;
    }

    public String getApisJSONListPath() {
        return apisJSONListPath;
    }

    public String getPubRSAKey() {
        return pubRSAKey;
    }

    public String getPriRSAKey() {
        return priRSAKey;
    }

    public String getAesKey() {
        return aesKey;
    }

    public String getSignKey() {
        return signKey;
    }

    public int getDubboTimeout() {
        return dubboTimeout;
    }

    public int getDubboRetries() {
        return dubboRetries;
    }

    public String getDubboVersion() {
        return dubboVersion;
    }

    public String getGenericFilter() {
        return genericFilter;
    }

    public int getHttpConnectTimeout() {
        return httpConnectTimeout;
    }

    public int getHttpReadTimeout() {
        return httpReadTimeout;
    }

    public String getZipkinHost() {
        return zipkinHost;
    }

    public String getBraveDir() {
        return braveDir;
    }

    public boolean openBrave() {
        return brave;
    }

    public boolean isEsbCloseFilter() { return esbCloseFilter; }

    private static class SingletonHolder {
        private static ESBConfigCenter INSTANCE = new ESBConfigCenter();
    }

    public ESBProperties getProperties() {
        return properties;
    }
}
