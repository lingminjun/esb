package com.venus.esb.servlet;

import com.venus.esb.ESBInvocation;
import com.venus.esb.lang.ESBConsts;
import com.venus.esb.lang.ESBField;
import com.venus.esb.utils.HTTP;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: lingminjun
 * Date: 2018-09-30
 * Time: 下午7:55
 */
public final class ESBHTTPMethod implements Serializable {

    private static final long serialVersionUID = 888087124025783857L;

    public String url;//服务名
    public String version;//接口版本
    public int timeout = 30000;//默认30秒
    public HTTP.Method method;

    public List<ESBField> params = new ArrayList<ESBField>();//参数列表,顺序一致

    public ESBInvocation getInvocation() {
        URL u = null;
        try {
            u = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        ESBInvocation invocation = new ESBInvocation();
        invocation.protocol = "HTTP 1.1";
        invocation.scheme = u.getProtocol();
        invocation.serverName = u.getHost();
        if (u.getPort() < 0) {
            if (invocation.scheme.equalsIgnoreCase("https")) {
                invocation.serverPort = 443;
            } else {
                invocation.serverPort = 80;
        }
        } else {
            invocation.serverPort = u.getPort();
        }

        if (u.getQuery() != null && u.getQuery().length() > 0) {
            invocation.methodName = u.getPath() + "?" + u.getQuery();
        } else {
            invocation.methodName = u.getPath();
        }
        invocation.timeout = this.timeout;
        invocation.version = this.version;
        invocation.encoding = ESBConsts.UTF8_STR;
        invocation.serialization = ESBConsts.JSON;
        invocation.method = this.method.name();

        //参数部分
        invocation.paramTypes = this.params.toArray(new ESBField[0]);

        return invocation;
    }


}
