package com.venus.esb.lang;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.venus.esb.ESBSecurityLevel;
import com.venus.esb.annotation.ESBDesc;
import com.venus.esb.sign.ESBUUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * Created by lingminjun on 17/2/6.
 * 规定Dubbo服务调用之间必传参数,用于传递通用数据和跟踪数据
 * Dubbo Service接口定义,第一个参数必须是Context
 * 如:
 *   public long addCard(ESBContext context, CardInfo info) throws ESBException;
 */
@ESBDesc("dubbo 调用过程中的上下文参数 所有内容支持MDC")
public class ESBContext implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(ESBContext.class);

    // dubbo中使用前缀修饰放入ext和cookie
    public static final String EXTENSION_KEY = "com.venus.esb.lang.set_extension:";
    public static final String COOKIES_KEY = "com.venus.esb.lang.set_cookies:";

    // 此可以主要用于透传时(transmit=true)设置的content type，由服务提供方自行配置，
    //  设置案例：content.putExt(TRANSMIT_CONTENT_TYPE_KEY,"custom content type")
    public static final String TRANSMIT_CONTENT_TYPE_KEY = "transmit content type";

    private static final ThreadLocal<ESBContext> LOCAL = new ThreadLocal() {
        protected ESBContext initialValue() {
            return new ESBContext();
        }
    };

    private static final long serialVersionUID = 5129470651605387569L;

    @ESBDesc("application id")
    public String aid;//应用id
    @ESBDesc("call id MDC支持")
    public String cid;//用于日志追踪
    @ESBDesc("device id")
    public String did;
    @ESBDesc("partner id")
    public String pid;//合作方id
    @ESBDesc("user id")
    public String uid;//user id
    @ESBDesc("trace id MDC支持")
    public String tid;//trace id
    @ESBDesc("国际化支持,语言参数的 ISO 国家/地区代码,参照HTTP Header Accept-Language:zh-CN,zh;q=0.8")
    public String l10n;
    @ESBDesc("渠道")
    public String ch; //渠道
    @ESBDesc("来源")
    public String src;//来源
    @ESBDesc("搜索来源")
    public String spm;//spm 自媒体营销平台 引流
    @ESBDesc("访问路径,host链路")
    public String via;//网元(客户端或Proxy)的主机名或网络地址（包含端口号,暂且为客服端ip）
    @ESBDesc("设备指纹信息")
    public String dna;//客户端指纹信息(不超过32767长度)
    @ESBDesc("UserAgent")
    public String ua;
    @ESBDesc("客户端ip")
    public String cip;
    @ESBDesc("客户端版本")
    public String cvn;
    @ESBDesc("客户端版本号 version code")
    public int cvc;
//    @ESBDesc("客户端请求scheme")
//    public String scheme;
    @ESBDesc("客户端请求host")
    public String host;
    @ESBDesc("客户端请求referer")
    public String referer;//Referer:https://www.google.com.hk/
    @ESBDesc("创建时间")
    public long at;   //调用开始发生时间点(参考值,Context生产时间点),毫秒

    @ESBDesc("额外参数")
    public ESBExts exts; //额外的key value计算，dubbo调用会携带到provider

    @ESBDesc("额外参数")
    public ESBCookieExts cookies; //额外的key value传递

    @JSONField(serialize = false)
    @ESBDesc(value = "当前请求是转发场景，配合ESBAPIInfo中特定转发服务一起使用（未来实现），切记转发内容contentType请参照CUSTOM_CONTENT_TYPE_KEY",ignore = true, inner = true)
    public boolean transmit = false;


    @JSONField(serialize = false)
    @ESBDesc(value = "额外临时参数，rpc不携带",ignore = true, inner = true)
    public ESBExts tExts; //

    /**
     * 不建议自行构造,请使用 ESBContext.getContext()方法获取
     */
    public ESBContext() {}

    protected void init() {
        this.at = System.currentTimeMillis();

        this.via = ESBUUID.getLocalIP();

        //tid是可以看到整个链路的,尽量参考来自上一个调用链路,cid是每次rpc调用流水id
        //当tid无法从ESBThreadLocal中取值时,会用第一次的cid
        String _cid = ESBThreadLocal.get(ESBSTDKeys.CID_KEY);//在ESB主机上,直接取ESBAPIContext的cid
        String _tid = ESBThreadLocal.get(ESBSTDKeys.TID_KEY);
        if (_cid != null) {
            this.cid = _cid;
        } else {
            this.cid = ESBUUID.genSimplifyCID();//老的API网关:_cid=a:e4b882|t:111|s:1500963029271) ip_md5前六位+线程id+时间
        }

        if (_tid != null) {
            this.tid = _tid;
        } else {
            this.tid = this.cid;
        }

        //多语言必须从ESBThreadLocal中透传,或者Rpc
        String ln = ESBThreadLocal.get(ESBSTDKeys.L10N_KEY);
        if (ln != null) {
            this.l10n = ln;
        }

        //业务字段透传
        String _aid = ESBThreadLocal.get(ESBSTDKeys.AID_KEY);
        if (_aid != null) {
            this.aid = _aid;
        }
        String _did = ESBThreadLocal.get(ESBSTDKeys.DID_KEY);
        if (_did != null) {
            this.did = _did;
        }
        String _uid = ESBThreadLocal.get(ESBSTDKeys.UID_KEY);
        if (_uid != null) {
            this.uid = _uid;
        }
        String _pid = ESBThreadLocal.get(ESBSTDKeys.PID_KEY);
        if (_pid != null) {
            this.pid = _pid;
        }
    }

    /**
     * 获取当前系统context
     */
    public static ESBContext getContext() {
        ESBContext ctx = LOCAL.get();
        if (ctx.cid == null) {//表示新的一次调用,填充
            ctx.init();
        }
        return ctx;
    }

    public static void removeContext() {
        LOCAL.remove();
    }

    public static void putContext(ESBContext context) {
        if (context != null) {
            LOCAL.set(context);
        }
    }

    public String getAid() {
        return aid;
    }

    public String getCid() {
        return cid;
    }

    public String getDid() {
        return did;
    }

    public String getPid() {
        return pid;
    }

    public String getUid() {
        return uid;
    }

    public String getTid() {
        return tid;
    }

    public String getL10n() {
        return l10n;
    }

    public String getCh() {
        return ch;
    }

    public String getSrc() {
        return src;
    }

    public String getSpm() {
        return spm;
    }

    public String getVia() {
        return via;
    }

    public String getDna() {
        return dna;
    }

    public String getUa() {
        return ua;
    }

    public String getCip() {
        return cip;
    }

    public String getCvn() {
        return cvn;
    }

    public int getCvc() {
        return cvc;
    }

    public String getHost() {
        return host;
    }

    public String getReferer() {
        return referer;
    }


//    public String getScheme() {
//        return scheme;
//    }

    public long getAt() {
        return at;
    }

    public boolean isTransmit() {
        return transmit;
    }

    public final void putExt(String key, String value) {
        if (key != null && key.length() > 0 && value != null) {
            if (exts == null) {
                exts = new ESBExts();
            }
            exts.put(key,value);
        }
    }

    public final String getExt(String key) {
        if (exts != null && key != null && key.length() > 0) {
            return exts.get(key);
        }
        return null;
    }

    public final void removeExt(String key) {
        if (exts != null && key != null && key.length() > 0) {
            exts.remove(key);
        }
    }

    public final void putCookie(String key, ESBCookie value) {
        if (key != null && key.length() > 0 && value != null) {
            if (cookies == null) {
                cookies = new ESBCookieExts();
            }
            cookies.put(key,value);
        }
    }

    public final ESBCookie getCookie(String key) {
        if (cookies != null && key != null && key.length() > 0) {
            return cookies.get(key);
        }
        return null;
    }

    public final void removeCookie(String key) {
        if (cookies != null && key != null && key.length() > 0) {
            cookies.remove(key);
        }
    }

    public final void putTempExt(String key, String value) {
        if (key != null && key.length() > 0 && value != null) {
            if (tExts == null) {
                tExts = new ESBExts();
            }
            tExts.put(key,value);
        }
    }

    public final String getTempExt(String key) {
        if (tExts != null && key != null && key.length() > 0) {
            return tExts.get(key);
        }
        return null;
    }

    public final void removeTempExt(String key) {
        if (tExts != null && key != null && key.length() > 0) {
            tExts.remove(key);
        }
    }

    public ESBSecur generateClient() {
        ESBSecur client = new ESBSecur();
        client.aid = ESBT.integer(getAid());
        client.did = ESBT.longInteger(getDid());
        client.uid = ESBT.longInteger(getUid());
        if (client.uid > 0) {
            client.securityLevel = ESBSecurityLevel.deviceAuth.authorize(ESBSecurityLevel.userAuth.authorize(0));
        } else {
            client.securityLevel = ESBSecurityLevel.deviceAuth.authorize(0);
        }
        client.dna = getDna();
        return client;
    }

//    //遍历建议使用此方法,效率高,java5以上
//    public Set<Map.Entry<String, String>> extEntrySet() {
//        if (exts != null) {
//            return exts.entrySet();
//        }
//        return new HashSet<Map.Entry<String, String>>();//返回空set
//    }
//
//    //如果你需要获取keys,
//    public Set<String> extKeySet() {
//        if (exts != null) {
//            return exts.keySet();
//        }
//        return new HashSet<String>();//返回空的keys
//    }

    //
    public Map<String, String> getAllExts() {
        if (exts != null) {
            return exts.map();
        }
        return null;
    }

    @Override
    public String toString() {
        return toJson();//采用json方式展示
    }

    public String toJson() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        int flag = builder.length();
        addElementInBuilder("aid",aid,builder,flag);
        addElementInBuilder("cid",cid,builder,flag);
        addElementInBuilder("did",did,builder,flag);
        addElementInBuilder("pid",pid,builder,flag);
        addElementInBuilder("uid",uid,builder,flag);
        addElementInBuilder("tid",tid,builder,flag);
        addElementInBuilder("l10n",l10n,builder,flag);
        addElementInBuilder("ch",ch,builder,flag);
        addElementInBuilder("src",src,builder,flag);
        addElementInBuilder("spm",spm,builder,flag);
        addElementInBuilder("via",via,builder,flag);
        addElementInBuilder("dna",dna,builder,flag);
        addElementInBuilder("ua",ua,builder,flag);
        addElementInBuilder("cip",cip,builder,flag);
        addElementInBuilder("cvn",cvn,builder,flag);
        addElementInBuilder("cvc",""+cvc,builder,flag);
        addElementInBuilder("host",host,builder,flag);
//        addElementInBuilder("scheme",scheme,builder,flag);
        addElementInBuilder("referer",referer,builder,flag);

        //时间点
        if (at > 0) {
            if (builder.length() > flag) {//说明前面有内容
                builder.append(",");
            }
            builder.append("\"at\":" + at);
        }

        //额外信息加入
        if (exts != null && exts.size() > 0) {
            if (builder.length() > flag) {//说明前面有内容
                builder.append(",");
            }
            builder.append("\"exts\":{");
            flag = builder.length();
            for (int idx = 0; idx < exts.size(); idx++) {
                addElementInBuilder(exts.keys.get(idx), exts.values.get(idx),builder,flag);
            }
            builder.append("}");
        }

        //额外信息加入
        if (cookies != null && cookies.size() > 0) {
            if (builder.length() > flag) {//说明前面有内容
                builder.append(",");
            }
            builder.append("\"exts\":{");
            flag = builder.length();
            for (int idx = 0; idx < cookies.size(); idx++) {
                addElementInBuilder(cookies.keys.get(idx), JSON.toJSONString(cookies.values.get(idx),ESBConsts.FASTJSON_SERIALIZER_FEATURES),builder,flag);
            }
            builder.append("}");
        }

        builder.append("}");

        return builder.toString();
    }

    public HashMap<String,Object> toGenericPOJO() {

        HashMap<String,Object> map = new HashMap<String, Object>();
        addElementInHashMap("class",ESBContext.class.getName(),map);
        addElementInHashMap("aid",aid,map);
        addElementInHashMap("cid",cid,map);
        addElementInHashMap("did",did,map);
        addElementInHashMap("pid",pid,map);
        addElementInHashMap("uid",uid,map);
        addElementInHashMap("tid",tid,map);
        addElementInHashMap("l10n",l10n,map);
        addElementInHashMap("ch",ch,map);
        addElementInHashMap("src",src,map);
        addElementInHashMap("spm",spm,map);
        addElementInHashMap("via",via,map);
        addElementInHashMap("dna",dna,map);
        addElementInHashMap("ua",ua,map);
        addElementInHashMap("cip",cip,map);
        addElementInHashMap("cvn",cvn,map);
        addElementInHashMap("cvc",cvc,map);
        addElementInHashMap("host",host,map);
//        addElementInHashMap("scheme",scheme,map);
        addElementInHashMap("referer",referer,map);

        if (exts != null) {
            HashMap<String,Object> e = new HashMap<String, Object>();
            e.put("keys",exts.keys);
            e.put("values",exts.values);
            e.put("class", exts.getClass().getName());
            map.put("exts",e);
        }

        if (cookies != null) {
            HashMap<String,Object> e = new HashMap<String, Object>();
            e.put("keys",cookies.keys);

            ArrayList<HashMap<String,Object>> list = new ArrayList<>();
            for (ESBCookie ck : cookies.values) {
                HashMap<String,Object> eck = new HashMap<String, Object>();
                addElementInHashMap("name",ck.name, eck);
                addElementInHashMap("value",ck.value,eck);
                addElementInHashMap("domain",ck.domain,eck);
                addElementInHashMap("maxAge",ck.maxAge,eck);
                addElementInHashMap("secure",ck.secure,eck);
                addElementInHashMap("path",ck.path,eck);
                addElementInHashMap("version",ck.version,eck);
                addElementInHashMap("httpOnly",ck.httpOnly,eck);
                list.add(eck);
            }
            e.put("values",list);
            e.put("class", cookies.getClass().getName());
            map.put("exts",e);
        }

        return map;
    }

    public String getValue(String key) {
        if (ESBSTDKeys.AID_KEY.equals(key)) {
            return this.getAid();
        } else if (ESBSTDKeys.CID_KEY.equals(key)) {
            return this.getCid();
        } else if (ESBSTDKeys.DID_KEY.equals(key)) {
            return this.getDid();
        } else if (ESBSTDKeys.PID_KEY.equals(key)) {
            return this.getPid();
        } else if (ESBSTDKeys.UID_KEY.equals(key)) {
            return this.getUid();
        } else if (ESBSTDKeys.TID_KEY.equals(key)) {
            return this.getTid();
        } else if (ESBSTDKeys.L10N_KEY.equals(key)) {
            return this.getL10n();
        } else if (ESBSTDKeys.CH_KEY.equals(key)) {
            return this.getCh();
        } else if (ESBSTDKeys.SRC_KEY.equals(key)) {
            return this.getSrc();
        } else if (ESBSTDKeys.SMP_KEY.equals(key)) {
            return this.getSpm();
        } else if (ESBSTDKeys.VIA_KEY.equals(key)) {
            return this.getVia();
        } else if (ESBSTDKeys.DNA_KEY.equals(key)) {
            return this.getDna();
        } else if (ESBSTDKeys.UA_KEY.equals(key)) {
            return this.getUa();
        } else if (ESBSTDKeys.CIP_KEY.equals(key)) {
            return this.getCip();
        } else if (ESBSTDKeys.CVN_KEY.equals(key)) {
            return this.getCvn();
        } else if (ESBSTDKeys.HOST_KEY.equals(key)) {
            return this.getHost();
        } else if (ESBSTDKeys.REFERER_KEY.equals(key)) {
            return this.getReferer();
        } else if (ESBSTDKeys.MOENT_KEY.equals(key)) {
            return ""+this.getAt();
        }
        return null;
    }

    //添加元素到json中
    private static void addElementInBuilder(String key,String value,StringBuilder builder, int flag) {
        if (key != null && key.length() > 0 && value != null && value.length() > 0) {
            if (builder.length() > flag) {//说明前面有内容
                builder.append(",");
            }
            builder.append("\"" + key + "\":\"" + value + "\"");
        }
    }

    //添加元素到map中
    private static void addElementInHashMap(String key,Object value,HashMap<String,Object> map) {
        if (key != null && key.length() > 0 && value != null) {
            if (value instanceof String && ((String)value).length() > 0) {
                map.put(key, value);
            } else {
                map.put(key, value);
            }
        }
    }

    public void clear() {
        this.aid = null;
        this.cid = null;
        this.did = null;
        this.pid = null;
        this.uid = null;
        this.tid = null;
        this.l10n = null;
        this.ch = null;
        this.src = null;
        this.spm = null;
        this.via = null;
        this.dna = null;
        this.ua = null;
        this.cip = null;
        this.cvn = null;
        this.cvc = 0;
        this.host = null;
        this.referer = null;
        this.at = 0;
        this.exts = null;
        this.cookies = null;
        this.transmit = false;
    }

    /**
     * 获取一个Context实例
     */
    public static ESBContext obtain(String aid,
                                    String cid,
                                    String did,
                                    String pid,
                                    String uid,
                                    String tid,
                                    String ch,
                                    String src,
                                    String spm,
                                    String via,
                                    String dna,
                                    String ua,
                                    String cip,
                                    String cvn,
                                    int cvc,
                                    String host) {
        long at = System.currentTimeMillis();//优先取时间

        ESBContext c = new ESBContext();
        c.aid = aid;
        c.cid = cid;
        c.did = did;
        c.pid = pid;
        c.uid = uid;
        c.tid = tid;
        c.ch = ch;
        c.src = src;
        c.spm = spm;
        c.via = via;
        c.dna = dna;
        c.ua = ua;
        c.cip = cip;
        c.cvn = cvn;
        c.cvc = cvc;
        c.host = host;
        c.at = at;
        return c;
    }


    /**
     * 优先从params中获取,因为params中参与签名,然后是header,最后是cookie
     * @param key
     * @param params
     * @param header
     * @param cookies
     * @return
     */
    public static String parseValue(String key, Map<String,String> params, Map<String,String> header, Map<String,ESBCookie> cookies) {
        return parseValue(key,null,params,header,cookies);
    }
    public static String parseValue(String key, String aid, Map<String,String> params, Map<String,String> header, Map<String,ESBCookie> cookies) {
        String value = null;
        if (params != null) {value = params.get(key);}
        if (value != null) {
            return value;
        }

        if (header != null) {value = header.get(key);}
        if (value != null) {
            return value;
        }

        if (cookies != null) {

            // 特殊cookie处理
            ESBCookie c = null;
            if (ESBT.integer(aid) > 0 && (ESBSTDKeys.isApplicationCookieKey(key))) {
                c = cookies.get(aid + key);
            } else if (!ESBSTDKeys.notCookieKey(key)) {
                c = cookies.get(key);
            }
            if (c != null) {
                value = c.value;
            }
        }
        return value;
    }

    /**
     * 从前端api中获取值
     * @param key
     * @param params
     * @param cookies
     * @param index
     * @return
     */
    public final String getRightValue(String key, Map<String,String> params, Map<String,ESBCookie> cookies, int index) {
        return getRightValue(key,this,params,cookies,index);
    }

    /**
     * 从前端api中获取值
     * @param key
     * @param context
     * @param params
     * @param cookies
     * @param index
     * @return
     */
    public static String getRightValue(String key, ESBContext context, Map<String,String> params, Map<String,ESBCookie> cookies, int index) {
        String value = context.getValue(key);

        if (value != null) {
            return value;
        }

        //开始取参数
        if (params != null) {
            String p_key = key;
            if (index >= 0) {//组合请求,按照小标取值
                p_key = index + "_" + key;
            }
            value = params.get(p_key);
            if (value != null) {
                return value;
            }
        }

        if (cookies != null) {
            // 特殊cookie处理
            ESBCookie c = null;
            if (ESBT.integer(context.aid) > 0 && (ESBSTDKeys.isApplicationCookieKey(key))) {
                c = cookies.get(context.aid + key);
            } else if (!ESBSTDKeys.notCookieKey(key)) {
                c = cookies.get(key);
            }

            if (c != null) {
                return c.value;
            }
        }

        //仅仅cookie处理下
        if (ESBSTDKeys.COOKIE_KEY.equals(key) && cookies != null) {
            return JSON.toJSONString(cookies, ESBConsts.FASTJSON_SERIALIZER_FEATURES);
        }

        return null;
    }



    //do value copy 仅仅记录一些相关的数据
    public final void setDubboExts(Map<String, String> rpcMap) {
        if (rpcMap != null && !rpcMap.isEmpty()) {
            for (Map.Entry<String, String> entry : rpcMap.entrySet()) {
                if (entry.getKey().startsWith(EXTENSION_KEY)) {//ext
                    String key = entry.getKey().substring(EXTENSION_KEY.length());
                    putExt(key,entry.getValue());
                } else if (entry.getKey().startsWith(COOKIES_KEY)) {//cookie
                    String tmp = entry.getValue();
                    if (tmp != null) {
                        try {
                            ESBCookie cookie = JSON.parseObject(tmp, ESBCookie.class);
                            String key = entry.getKey().substring(COOKIES_KEY.length());
                            putCookie(key,cookie);
                        } catch (Throwable e) {e.printStackTrace();}
                    }
                }
            }
        }
    }

    public final Map<String, String> getDubboExts() {
        Map<String, String> rpc = new HashMap<>();
        if (exts != null && exts.size() > 0) {
            Map<String,String> map = exts.map();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                rpc.put(EXTENSION_KEY + entry.getKey(),entry.getValue());
            }
        }

        if (cookies != null && cookies.size() > 0) {
            Map<String,ESBCookie> map = cookies.map();
            for (Map.Entry<String, ESBCookie> entry : map.entrySet()) {
                rpc.put(COOKIES_KEY + entry.getKey(),JSON.toJSONString(entry.getValue(),ESBConsts.FASTJSON_SERIALIZER_FEATURES));
            }
        }
        return rpc;
    }

    // spring mvc 数据注入必须

    public void setAid(String aid) {
        this.aid = aid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public void setL10n(String l10n) {
        this.l10n = l10n;
    }

    public void setCh(String ch) {
        this.ch = ch;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public void setSpm(String spm) {
        this.spm = spm;
    }

    public void setVia(String via) {
        this.via = via;
    }

    public void setDna(String dna) {
        this.dna = dna;
    }

    public void setUa(String ua) {
        this.ua = ua;
    }

    public void setCip(String cip) {
        this.cip = cip;
    }

    public void setCvn(String cvn) {
        this.cvn = cvn;
    }

    public void setCvc(int cvc) {
        this.cvc = cvc;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public void setAt(long at) {
        this.at = at;
    }
}
