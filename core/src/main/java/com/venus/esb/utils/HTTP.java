package com.venus.esb.utils;

import com.alibaba.fastjson.JSON;
import com.venus.esb.lang.ESBConsts;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Description: 用于简单的访问http各种情况
 * User: lingminjun
 * Date: 2018-08-11
 * Time: 下午2:59
 */
public final class HTTP {
    private static Logger logger = LogManager.getLogger(HTTP.class);

    private static final String DEFAULT_BOUNDARY = "----WebKitFormBoundaryeeebaozhengweiyixing";

    public enum Method {
        GET,POST,PUT,DELETE
    }

    public enum ContentType {
        //原始表单方式：Content-Type: application/x-www-form-urlencoded;charset=utf-8
        form,
        //数据块方式：Content-Type:multipart/form-data; boundary=----WebKitFormBoundaryrGKCBY7qhFd3TrwA
        /*//示例
        POST http://www.example.com HTTP/1.1
		Content-Type:multipart/form-data; boundary=----WebKitFormBoundaryrGKCBY7qhFd3TrwA

		------WebKitFormBoundaryrGKCBY7qhFd3TrwA
		Content-Disposition: form-data; name="text"

		title
		------WebKitFormBoundaryrGKCBY7qhFd3TrwA
		Content-Disposition: form-data; name="file"; filename="chrome.png"
		Content-Type: image/png

		PNG ... content of chrome.png ...
		------WebKitFormBoundaryrGKCBY7qhFd3TrwA--
        */
        data,
        //json格式： Content-Type: application/json;charset=utf-8
        json,
        //xml方式：Content-Type: text/xml
        xml,
        //文本模式： Content-Type: text/plain
        text;

        public String type() {
            return type(null);
        }

        public String type(String charsetOrBoundary) {
            String charset = ESBConsts.UTF8_STR;
            String boundary = DEFAULT_BOUNDARY;
            if (charsetOrBoundary != null && charsetOrBoundary.length() > 0) {
                charset = charsetOrBoundary;
                boundary = charsetOrBoundary;
            }
            switch (this) {
                case form:{ return "application/x-www-form-urlencoded; charset=" + charset; }
                case data:{ return "multipart/form-data; boundary=" + boundary; }
                case json:{ return "application/json; charset=" + charset; }
                case xml:{ return "text/xml" + charset; }
                case text:{ return "text/plain" + charset; }
                default: { return "application/x-www-form-urlencoded; charset=" + charset; }
            }
        }
    }

    /**
     * 发送信息到服务端
     * @param content
     * @return success response
     * @throws Exception
     */
    public static String post(String url, String content, Map<String,String> header) throws Exception {
        try {
            String charset = ESBConsts.UTF8_STR;
            HttpURLConnection httpURLConnection = postConnection(new URL(url),0,0, ContentType.text, charset,null);
            if (null == httpURLConnection) {
                throw new Exception("Create httpURLConnection Failure");
            }
            if (header != null) {
                writeRequestHeader(httpURLConnection,header);
            }
            if (content != null && content.length() > 0) {
                writeContent(httpURLConnection, content, charset);
            }
            String result = response(httpURLConnection, charset);
            logger.info("Response message:[" + result + "]");
            if (httpURLConnection.getResponseCode() == 200) {
                return result;
            } else {
                return null;
            }
        } catch (Exception e) {
//            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 发送信息到服务端
     * @param content
     * @return success response
     * @throws Exception
     */
    public static String post(String url, String content, String authorization) throws Exception {
        HashMap<String,String> map = new HashMap<String,String>();
        if (authorization != null) {
            map.put("Authorization",authorization);
        }
        return post(url,content,map);
    }

    /**
     * 发送信息到服务端
     * @param content
     * @return success response
     * @throws Exception
     */
    public static String post(String url, String content) throws Exception {
        return post(url,content,(Map<String, String>) null);
    }

    /**
     * 发送信息到服务端
     * @param params
     * @return success response
     * @throws Exception
     */
    public static String post(String url, Map<String, String> params, Map<String,String> header) throws Exception {
        try {
            String charset = ESBConsts.UTF8_STR;
            HttpURLConnection httpURLConnection = postConnection(new URL(url),0,0, ContentType.form, charset,null);
            if (null == httpURLConnection) {
                throw new Exception("Create httpURLConnection Failure");
            }
            if (header != null) {
                writeRequestHeader(httpURLConnection,header);
            }
            writeFormParams(httpURLConnection,params,charset);
            String result = response(httpURLConnection, charset);
            logger.info("Response message:[" + result + "]");
            if (httpURLConnection.getResponseCode() == 200) {
                return result;
            } else {
                return null;
            }
        } catch (Exception e) {
//            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 发送信息到服务端
     * @param params
     * @return success response
     * @throws Exception
     */
    public static String post(String url, Map<String, String> params, String authorization) throws Exception {
        HashMap<String,String> map = new HashMap<String,String>();
        if (authorization != null) {
            map.put("Authorization",authorization);
        }
        return post(url,params,map);
    }

    /**
     * 发送信息到服务端
     * @param params
     * @return success response
     * @throws Exception
     */
    public static String post(String url, Map<String, String> params) throws Exception {
        return post(url,params,(Map<String, String>) null);
    }

    /**
     * 发送信息到服务端
     * @param object
     * @return success response
     * @throws Exception
     */
    public static String postJSON(String url, Object object, Map<String,String> header) throws Exception {
        try {
            String charset = ESBConsts.UTF8_STR;
            HttpURLConnection httpURLConnection = postConnection(new URL(url),0,0, ContentType.json, charset,null);
            if (null == httpURLConnection) {
                throw new Exception("Create httpURLConnection Failure");
            }
            if (header != null) {
                writeRequestHeader(httpURLConnection,header);
            }
            if (object != null) {
                if (object instanceof String) {
                    writeContent(httpURLConnection, (String) object, charset);
                } else {
                    writeContent(httpURLConnection, JSON.toJSONString(object, ESBConsts.FASTJSON_SERIALIZER_FEATURES), charset);
                }
            }
            String result = response(httpURLConnection, charset);
            logger.info("Response message:[" + result + "]");
            if (httpURLConnection.getResponseCode() == 200) {
                return result;
            } else {
                return null;
            }
        } catch (Exception e) {
//            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 发送信息到服务端
     * @param object
     * @return success response
     * @throws Exception
     */
    public static String postJSON(String url, Object object, String authorization) throws Exception {
        HashMap<String,String> map = new HashMap<String,String>();
        if (authorization != null) {
            map.put("Authorization",authorization);
        }
        return postJSON(url,object,map);
    }

    /**
     * 发送信息到服务端
     * @param object
     * @return success response
     * @throws Exception
     */
    public static String postJSON(String url, Object object) throws Exception {
        return postJSON(url,object,(Map<String, String>) null);
    }

    /**
     * 发送信息到服务端
     * @param object
     * @return success response
     * @throws Exception
     */
    public static String putJSON(String url, Object object, Map<String,String> header) throws Exception {
        try {
            String charset = ESBConsts.UTF8_STR;
            HttpURLConnection httpURLConnection = postConnection(Method.PUT, new URL(url),0,0, ContentType.json, charset,null);
            if (null == httpURLConnection) {
                throw new Exception("Create httpURLConnection Failure");
            }
            if (header != null) {
                writeRequestHeader(httpURLConnection,header);
            }
            if (object != null) {
                if (object instanceof String) {
                    writeContent(httpURLConnection, (String) object, charset);
                } else {
                    writeContent(httpURLConnection, JSON.toJSONString(object, ESBConsts.FASTJSON_SERIALIZER_FEATURES), charset);
                }
            }
            String result = response(httpURLConnection, charset);
            logger.info("Response message:[" + result + "]");
            if (httpURLConnection.getResponseCode() == 200) {
                return result;
            } else {
                return null;
            }
        } catch (Exception e) {
//            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 发送信息到服务端
     * @param object
     * @return success response
     * @throws Exception
     */
    public static String putJSON(String url, Object object, String authorization) throws Exception {
        HashMap<String,String> map = new HashMap<String,String>();
        if (authorization != null) {
            map.put("Authorization",authorization);
        }
        return putJSON(url,object,map);
    }

    /**
     * 发送信息到服务端
     * @param object
     * @return success response
     * @throws Exception
     */
    public static String putJSON(String url, Object object) throws Exception {
        return putJSON(url,object,(Map<String, String>) null);
    }

    /**
     * 发送信息到服务端
     * @param data
     * @return success response
     * @throws Exception
     */
    public static String postData(String url, byte[] data, String fileName, Map<String,String> header) throws Exception {
        try {
            String boundary = "------WebKitFormBoundary" + randomString();
            HttpURLConnection httpURLConnection = postConnection(new URL(url),0,0, ContentType.data,null,boundary);
            if (null == httpURLConnection) {
                throw new Exception("Create httpURLConnection Failure");
            }

            if (header != null) {
                writeRequestHeader(httpURLConnection,header);
            }
            writeData(httpURLConnection,data,"file",null,boundary,fileName);
            String result = response(httpURLConnection, ESBConsts.UTF8_STR);
            logger.info("Response message:[" + result + "]");
            if (httpURLConnection.getResponseCode() == 200) {
                return result;
            } else {
                return null;
            }
        } catch (Exception e) {
//            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 发送信息到服务端
     * @param data
     * @return success response
     * @throws Exception
     */
    public static String postData(String url, byte[] data, String fileName, String authorization) throws Exception {
        HashMap<String,String> map = new HashMap<String,String>();
        if (authorization != null) {
            map.put("Authorization",authorization);
        }
        return postData(url,data,fileName,map);
    }

    /**
     * 发送信息到服务端
     * @param data
     * @return success response
     * @throws Exception
     */
    public static String postData(String url, byte[] data, String fileName) throws Exception {
        return postData(url,data,fileName,(Map<String, String>) null);
    }


    /**
     * 发送信息到服务端
     * @param content
     * @return response code
     * @throws Exception
     */
    public static int send(String url, String content, Map<String,String> header) throws Exception {
        try {
            String charset = ESBConsts.UTF8_STR;
            HttpURLConnection httpURLConnection = postConnection(new URL(url),0,0, ContentType.text, charset,null);
            if (null == httpURLConnection) {
                throw new Exception("Create httpURLConnection Failure");
            }
            if (header != null) {
                writeRequestHeader(httpURLConnection,header);
            }
            writeContent(httpURLConnection,content,charset);
            String result = response(httpURLConnection, charset);
            logger.info("Response message:[" + result + "]");
            return httpURLConnection.getResponseCode();
        } catch (Exception e) {
//            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 发送信息到服务端
     * @param content
     * @return response code
     * @throws Exception
     */
    public static int send(String url, String content, String authorization) throws Exception {
        HashMap<String,String> map = new HashMap<String,String>();
        if (authorization != null) {
            map.put("Authorization",authorization);
        }
        return send(url,content,map);
    }

    /**
     * 发送信息到服务端
     * @param content
     * @return response code
     * @throws Exception
     */
    public static int send(String url, String content) throws Exception {
        return send(url,content,(Map<String, String>) null);
    }

    /**
     * 发送信息到服务端
     * @param params
     * @return response code
     * @throws Exception
     */
    public static int send(String url, Map<String, String> params, Map<String,String> header) throws Exception {
        try {
            String charset = ESBConsts.UTF8_STR;
            HttpURLConnection httpURLConnection = postConnection(new URL(url),0,0, ContentType.form, charset,null);
            if (null == httpURLConnection) {
                throw new Exception("Create httpURLConnection Failure");
            }
            if (header != null) {
                writeRequestHeader(httpURLConnection,header);
            }
            writeFormParams(httpURLConnection,params,charset);
            String result = response(httpURLConnection, charset);
            logger.info("Response message:[" + result + "]");
            return httpURLConnection.getResponseCode();
        } catch (Exception e) {
//            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 发送信息到服务端
     * @param params
     * @return response code
     * @throws Exception
     */
    public static int send(String url, Map<String, String> params, String authorization) throws Exception {
        HashMap<String,String> map = new HashMap<String,String>();
        if (authorization != null) {
            map.put("Authorization",authorization);
        }
        return send(url,params,map);
    }

    /**
     * 发送信息到服务端
     * @param params
     * @return response code
     * @throws Exception
     */
    public static int send(String url, Map<String, String> params) throws Exception {
        return send(url,params,(Map<String, String>)null);
    }

    /**
     * 发送信息到服务端
     * @param object
     * @return response code
     * @throws Exception
     */
    public static int sendJSON(String url, Object object, Map<String,String> header) throws Exception {
        try {
            String charset = ESBConsts.UTF8_STR;
            HttpURLConnection httpURLConnection = postConnection(new URL(url), 0, 0, ContentType.json, charset, null);
            if (null == httpURLConnection) {
                throw new Exception("Create httpURLConnection Failure");
            }
            if (header != null) {
                writeRequestHeader(httpURLConnection, header);
            }
            if (object != null) {
                if (object instanceof String) {
                    writeContent(httpURLConnection, (String) object, charset);
                } else {
                    writeContent(httpURLConnection, JSON.toJSONString(object, ESBConsts.FASTJSON_SERIALIZER_FEATURES), charset);
                }
            }
            String result = response(httpURLConnection, charset);
            logger.info("Response message:[" + result + "]");
            return httpURLConnection.getResponseCode();
        } catch (Exception e) {
//            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 发送信息到服务端
     * @param object
     * @return response code
     * @throws Exception
     */
    public static int sendJSON(String url, Object object, String authorization) throws Exception {
        HashMap<String,String> map = new HashMap<String,String>();
        if (authorization != null) {
            map.put("Authorization",authorization);
        }
        return sendJSON(url,object,map);
    }

    /**
     * 发送信息到服务端
     * @param object
     * @return response code
     * @throws Exception
     */
    public static int sendJSON(String url, Object object) throws Exception {
        return sendJSON(url,object,(Map<String, String>) null);
    }

    /**
     * 发送信息到服务端
     * @param data
     * @return response code
     * @throws Exception
     */
    public static int sendData(String url, byte[] data, String fileName, Map<String,String> header) throws Exception {
        try {
            String boundary = "------WebKitFormBoundary" + randomString();
            HttpURLConnection httpURLConnection = postConnection(new URL(url),0,0, ContentType.data,null,boundary);
            if (null == httpURLConnection) {
                throw new Exception("Create httpURLConnection Failure");
            }
            if (header != null) {
                writeRequestHeader(httpURLConnection,header);
            }
            writeData(httpURLConnection,data,"file",null,boundary,fileName);
            String result = response(httpURLConnection, ESBConsts.UTF8_STR);
            logger.info("Response message:[" + result + "]");
            return httpURLConnection.getResponseCode();
        } catch (Exception e) {
//            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 发送信息到服务端
     * @param data
     * @return response code
     * @throws Exception
     */
    public static int sendData(String url, byte[] data, String fileName, String authorization) throws Exception {
        HashMap<String,String> map = new HashMap<String,String>();
        if (authorization != null) {
            map.put("Authorization",authorization);
        }
        return sendData(url,data,fileName,map);
    }

    /**
     * 发送信息到服务端
     * @param data
     * @return response code
     * @throws Exception
     */
    public static int sendData(String url, byte[] data, String fileName) throws Exception {
        return sendData(url,data,fileName,(Map<String, String>) null);
    }

    /**
     * 发送信息到服务端 GET方式
     * @return success response
     * @throws Exception
     */
    public static String get(String url, Map<String, String> params, Map<String,String> header, Map<String,String> cookie) throws Exception {
        try {
            String charset = ESBConsts.UTF8_STR;
            URL u = new URL(url);
            String q = u.getQuery();
            if (q != null && q.length() > 0) {
                q = q + "&" + getFormParamsString(params,charset);
            } else {
                q = getFormParamsString(params,charset);
            }
            u = new URL(u.getProtocol()+ "://" + u.getHost() + (u.getPort() > 0 ? ":" + u.getPort() : "") + u.getPath() + "?" + q);

            HttpURLConnection httpURLConnection = getConnection(u,0,60, ContentType.form, charset);
            if(null == httpURLConnection){
                throw new Exception("创建联接失败");
            }
            if (header != null) {
                writeRequestHeader(httpURLConnection,header);
            }
            if (cookie != null) {
                writeCookie(httpURLConnection,cookie);
            }
            String result = response(httpURLConnection, charset);
            logger.info("Response message:[" + result + "]");
            if (httpURLConnection.getResponseCode() == 200) {
                return result;
            } else {
                return null;
            }
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 发送信息到服务端 GET方式
     * @return success response
     * @throws Exception
     */
    public static String get(String url, Map<String, String> params, Map<String,String> header) throws Exception {
        return get(url,params,header,null);
    }

    /**
     * 发送信息到服务端 GET方式
     * @return success response
     * @throws Exception
     */
    public static String get(String url, Map<String, String> params, String authorization) throws Exception {
        HashMap<String,String> map = new HashMap<String,String>();
        if (authorization != null) {
            map.put("Authorization",authorization);
        }
        return get(url,params,map,null);
    }

    /**
     * 发送信息到服务端 GET方式
     * @return success response
     * @throws Exception
     */
    public static String get(String url, Map<String, String> params) throws Exception {
        return get(url,params,null,null);
    }

    /**
     * 发送信息到服务端 DELETE方式
     * @return success response
     * @throws Exception
     */
    public static String delete(String url, Map<String, String> params, Map<String,String> header, Map<String,String> cookie) throws Exception {
        try {
            String charset = ESBConsts.UTF8_STR;
            URL u = new URL(url);
            String q = u.getQuery();
            if (q != null && q.length() > 0) {
                q = q + "&" + getFormParamsString(params,charset);
            } else {
                q = getFormParamsString(params,charset);
            }
            u = new URL(u.getProtocol()+ "://" + u.getHost() + (u.getPort() > 0 ? ":" + u.getPort() : "") + u.getPath() + "?" + q);

            HttpURLConnection httpURLConnection = getConnection(Method.DELETE, u,0,60, ContentType.form, charset);
            if(null == httpURLConnection){
                throw new Exception("创建联接失败");
            }
            if (header != null) {
                writeRequestHeader(httpURLConnection,header);
            }
            if (cookie != null) {
                writeCookie(httpURLConnection,cookie);
            }
            String result = response(httpURLConnection, charset);
            logger.info("Response message:[" + result + "]");
            if (httpURLConnection.getResponseCode() == 200) {
                return result;
            } else {
                return null;
            }
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 发送信息到服务端 DELETE方式
     * @return success response
     * @throws Exception
     */
    public static String delete(String url, Map<String, String> params, Map<String,String> header) throws Exception {
        return delete(url,params,header,null);
    }

    /**
     * 发送信息到服务端 DELETE方式
     * @return success response
     * @throws Exception
     */
    public static String delete(String url, Map<String, String> params, String authorization) throws Exception {
        HashMap<String,String> map = new HashMap<String,String>();
        if (authorization != null) {
            map.put("Authorization",authorization);
        }
        return delete(url,params,map,null);
    }

    /**
     * 发送信息到服务端 DELETE方式
     * @return success response
     * @throws Exception
     */
    public static String delete(String url, Map<String, String> params) throws Exception {
        return delete(url,params,null,null);
    }

    /**
     * post 创建连接：四种常见的 POST 提交数据方式
     * @param url
     * @param connectTimeout 秒
     * @param readTimeout 秒
     * @param type
     * @param charset
     * @param boundary
     * @return
     * @throws ProtocolException
     */
    private static HttpURLConnection postConnection(URL url, int connectTimeout, int readTimeout, ContentType type, String charset, String boundary) throws ProtocolException {
        return postConnection(Method.POST,url,connectTimeout,readTimeout,type,charset,boundary);
    }
    private static HttpURLConnection postConnection(Method method, URL url, int connectTimeout, int readTimeout, ContentType type, String charset, String boundary) throws ProtocolException {
        if (url == null) {
            return null;
        }
        if (connectTimeout < 0 ) {
            connectTimeout = 30;
        }
        if (readTimeout < 0) {
            readTimeout = 30;
        }

        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            logger.error("创建 post HttpURLConnection 失败！", e);
            e.printStackTrace();
            return null;
        }
        httpURLConnection.setConnectTimeout(connectTimeout * 1000);// 毫秒 连接超时时间
        httpURLConnection.setReadTimeout(readTimeout * 1000);//  毫秒 读取结果超时时间
        httpURLConnection.setDoInput(true); // 可读
        httpURLConnection.setDoOutput(true); // 可写
        httpURLConnection.setUseCaches(false);// 取消缓存
//        httpURLConnection.setRequestProperty("User-Agent", "Mozilla/32");
        if (type != null && type == ContentType.data) {
            httpURLConnection.setRequestProperty("Content-type", type.type(boundary));
        } else {
            httpURLConnection.setRequestProperty("Content-type", type != null ? type.type(charset) : ContentType.form.type());
        }

        if (Method.PUT == method) {
            httpURLConnection.setRequestMethod("PUT");
        } else {
            httpURLConnection.setRequestMethod("POST");
        }

        if ("https".equalsIgnoreCase(url.getProtocol())) {
            HttpsURLConnection husn = (HttpsURLConnection) httpURLConnection;
            //是否验证https证书，测试环境请设置false，生产环境建议优先尝试true，不行再false
            husn.setSSLSocketFactory(new BaseHttpSSLSocketFactory());
            husn.setHostnameVerifier(new TrustAnyHostnameVerifier());//解决由于服务器证书问题导致HTTPS无法访问的情况
            return husn;
        }
        return httpURLConnection;
    }

    /**
     * 创建get连接
     * @param url
     * @param connectTimeout
     * @param readTimeout
     * @return
     * @throws ProtocolException
     */
    private static HttpURLConnection getConnection(URL url, int connectTimeout, int readTimeout, ContentType type, String charset) throws ProtocolException {
        return getConnection(Method.GET,url,connectTimeout,readTimeout,type,charset);
    }
    private static HttpURLConnection getConnection(Method method, URL url, int connectTimeout, int readTimeout, ContentType type, String charset) throws ProtocolException {
        if (url == null) {
            return null;
        }
        if (connectTimeout < 0 ) {
            connectTimeout = 30;
        }
        if (readTimeout < 0) {
            readTimeout = 30;
        }

        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            logger.error("创建 get HttpURLConnection 失败！", e);
            e.printStackTrace();
            return null;
        }
        httpURLConnection.setConnectTimeout(connectTimeout * 1000);// 毫秒 连接超时时间
        httpURLConnection.setReadTimeout(readTimeout * 1000);// 读取结果超时时间
        httpURLConnection.setUseCaches(false);// 取消缓存
        //这个不重要，get请求
        if (type != null && type == ContentType.data) {
            httpURLConnection.setRequestProperty("Content-type", type.type("------WebKitFormBoundary" + randomString()));
        } else {
            httpURLConnection.setRequestProperty("Content-type", type != null ? type.type(charset) : ContentType.form.type());
        }
//        httpURLConnection.setRequestProperty("Content-type","application/x-www-form-urlencoded; charset=utf-8");
        httpURLConnection.setRequestMethod("GET");
        if ("https".equalsIgnoreCase(url.getProtocol())) {
            HttpsURLConnection husn = (HttpsURLConnection) httpURLConnection;
            //是否验证https证书，测试环境请设置false，生产环境建议优先尝试true，不行再false
            husn.setSSLSocketFactory(new BaseHttpSSLSocketFactory());
            husn.setHostnameVerifier(new TrustAnyHostnameVerifier());//解决由于服务器证书问题导致HTTPS无法访问的情况
            return husn;
        }
        return httpURLConnection;
    }

    /**
     * 显示Response消息
     *
     * @param connection
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    private static String response(final HttpURLConnection connection, String charset)
            throws URISyntaxException, IOException, Exception {
        InputStream in = null;
        StringBuilder sb = new StringBuilder(1024);
        BufferedReader br = null;
        try {
            if (200 == connection.getResponseCode()) {
                in = connection.getInputStream();
                sb.append(new String(read(in), charset));
            } else {//
                in = connection.getErrorStream();
                sb.append(new String(read(in), charset));
            }
            logger.info("Request URL:[" + connection.getURL() + "] Return Status-Code:[" + connection.getResponseCode() + "]");
            return sb.toString();
        } catch (Exception e) {
            throw e;
        } finally {
            if (null != br) {
                br.close();
            }
            if (null != in) {
                in.close();
            }
            if (null != connection) {
                connection.disconnect();
            }
        }
    }

    /**
     * 将Map存储的对象，转换为key=value&key=value的字符
     *
     * @param requestParam
     * @param charset
     * @return
     */
    public static String getFormParamsString(Map<String, String> requestParam, String charset) {
        if (null == charset || "".equals(charset)) {
            charset = ESBConsts.UTF8_STR;
        }
        StringBuffer sf = new StringBuffer("");
        String reqstr = "";
        if (null != requestParam && 0 != requestParam.size()) {
            for (Map.Entry<String, String> en : requestParam.entrySet()) {
                String key = en.getKey();
                String value = en.getValue();
                try {
                    sf.append(key
                            + "="
                            + (null == value || value.length() == 0 ? "" : URLEncoder.encode(value, charset)) + "&");
                } catch (UnsupportedEncodingException e) {
                    logger.error("参数编码失败！", e);
                    e.printStackTrace();
                    return "";
                }
            }
            reqstr = sf.substring(0, sf.length() - 1);
        }
        //LogUtil.writeLog("Request Message:[" + reqstr + "]");
        return reqstr;
    }

    /**
     * HTTP Post发送消息
     *
     * @param connection
     * @param params
     * @throws IOException
     */
    private static void writeFormParams(final URLConnection connection, Map<String,String> params, String charset)
            throws Exception {
        writeContent(connection,getFormParamsString(params,charset),charset);
    }

    /**
     * HTTP Post发送消息
     *
     * @param connection
     * @param content
     * @throws IOException
     */
    private static void writeContent(final URLConnection connection, String content, String charset)
            throws Exception {
        if (content == null || content.length() == 0) {
            return;
        }
        PrintStream out = null;
        try {
            connection.connect();
            out = new PrintStream(connection.getOutputStream(), false, charset);
            out.print(content);
            out.flush();
        } catch (Exception e) {
            throw e;
        } finally {
            if (null != out) {
                out.close();
            }
        }
    }

    private static void writeRequestHeader(final URLConnection connection, Map<String,String> header)
            throws Exception {
        if (header == null || header.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> en : header.entrySet()) {
            String key = en.getKey();
            String value = en.getValue();
            if (key == null || key.length() == 0 || value == null || value.length() == 0) {
                continue;
            }
            connection.setRequestProperty(key, value);
        }
    }

    //cookie: __da=-847366894926686; 301_uinfo=15673886363%2C%E6%9C%AA%E7%A1%AE%E8%AE%A4%2Ccms%2C1%2C0
    private static void writeCookie(final URLConnection connection, Map<String,String> cookie)
            throws Exception {
        if (cookie == null || cookie.isEmpty()) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> en : cookie.entrySet()) {
            String key = en.getKey();
            String value = en.getValue();
            if (key == null || key.length() == 0 || value == null || value.length() == 0) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append("; ");
            }
            builder.append(key
                    + "="
                    + URLEncoder.encode(value,ESBConsts.UTF8_STR));
        }
        if (builder.length() > 0) {
            HashMap<String,String> ck = new HashMap<String, String>();
            ck.put("Cookie",builder.toString());
            writeRequestHeader(connection, ck);
        }
    }

    /**
     * HTTP Post发送消息
     * @param connection
     * @param data
     * @param name
     * @param type
     * @param fileName
     * @throws Exception
     */
    private static void writeData(final URLConnection connection, byte[] data, String name, String type, String boundary, String fileName)
            throws Exception {

        if (data == null || data.length == 0) {
            return;
        }

        OutputStream out = null;
        try {
            connection.connect();
            out = new DataOutputStream(connection.getOutputStream());
            byte[] bdy = ("--" + (boundary != null ? boundary : DEFAULT_BOUNDARY) + "--\r\n").getBytes();// 定义最后数据分隔线
            out.write(bdy);
            //写Content-Disposition:
            out.write(("Content-Disposition: form-data;name=\""+name+"\";").getBytes());
            if (fileName != null && fileName.length() > 0) {
                out.write(("filename=\""+ fileName + "\"").getBytes());
            }
            out.write(("\r\n").getBytes());
            if (type == null) {
                if (fileName != null && fileName.length() > 0) {
                    type = FileContentType.getContentTypeForFileName(fileName);
                } else {
                    type = FileContentType.getContentTypeForFileName(name);
                }
            }
            out.write(("Content-Type: "+type+"\r\n\r\n").getBytes());
            out.write(data);
            out.write("\r\n".getBytes());
            out.write(bdy);
            out.flush();
        } catch (Exception e) {
            throw e;
        } finally {
            if (null != out) {
                out.close();
            }
        }
    }

    private static byte[] read(InputStream in) throws IOException {
        byte[] buf = new byte[1024];
        int length = 0;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        while ((length = in.read(buf, 0, buf.length)) > 0) {
            bout.write(buf, 0, length);
        }
        bout.flush();
        return bout.toByteArray();
    }

    private static final String LETTER_MASK = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";//52
    private static String randomString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 25; i++) {
            int idx = ((int) (Math.random() * 100)) / 52;//取小数位，具有不可预测性
            builder.append(LETTER_MASK.charAt(idx));
        }
        return builder.toString();
    }

    /**
     * 解决由于服务器证书问题导致HTTPS无法访问的情况 PS:HTTPS hostname wrong: should be <localhost>
     */
    public static class TrustAnyHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            //直接返回true
            return true;
        }
    }

    public static class BaseHttpSSLSocketFactory extends SSLSocketFactory {
        private SSLContext getSSLContext() {
            return createEasySSLContext();
        }

        @Override
        public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2,
                                   int arg3) throws IOException {
            return getSSLContext().getSocketFactory().createSocket(arg0, arg1,
                    arg2, arg3);
        }

        @Override
        public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3)
                throws IOException, UnknownHostException {
            return getSSLContext().getSocketFactory().createSocket(arg0, arg1,
                    arg2, arg3);
        }

        @Override
        public Socket createSocket(InetAddress arg0, int arg1) throws IOException {
            return getSSLContext().getSocketFactory().createSocket(arg0, arg1);
        }

        @Override
        public Socket createSocket(String arg0, int arg1) throws IOException,
                UnknownHostException {
            return getSSLContext().getSocketFactory().createSocket(arg0, arg1);
        }

        @Override
        public String[] getSupportedCipherSuites() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String[] getDefaultCipherSuites() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Socket createSocket(Socket arg0, String arg1, int arg2, boolean arg3)
                throws IOException {
            return getSSLContext().getSocketFactory().createSocket(arg0, arg1,
                    arg2, arg3);
        }

        private SSLContext createEasySSLContext() {
            try {
                SSLContext context = SSLContext.getInstance("SSL");
                context.init(null,
                        new TrustManager[]{MyX509TrustManager.manger}, null);
                return context;
            } catch (Exception e) {
                logger.error("createEasySSLContext获取失败！", e);
                e.printStackTrace();
                return null;
            }
        }
    }

    public static class MyX509TrustManager implements X509TrustManager {

        static MyX509TrustManager manger = new MyX509TrustManager();

        public MyX509TrustManager() {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }
    }

    public static final class FileContentType {
        static HashMap<String,String> cts = new HashMap<String, String>();
        static {
            cts.put(".load","text/html");
            cts.put(".123","application/vnd.lotus-1-2-3");
            cts.put(".3ds","image/x-3ds");
            cts.put(".3g2","video/3gpp");
            cts.put(".3ga","video/3gpp");
            cts.put(".3gp","video/3gpp");
            cts.put(".3gpp","video/3gpp");
            cts.put(".602","application/x-t602");
            cts.put(".669","audio/x-mod");
            cts.put(".7z","application/x-7z-compressed");
            cts.put(".a","application/x-archive");
            cts.put(".aac","audio/mp4");
            cts.put(".abw","application/x-abiword");
            cts.put(".abw.crashed","application/x-abiword");
            cts.put(".abw.gz","application/x-abiword");
            cts.put(".ac3","audio/ac3");
            cts.put(".ace","application/x-ace");
            cts.put(".adb","text/x-adasrc");
            cts.put(".ads","text/x-adasrc");
            cts.put(".afm","application/x-font-afm");
            cts.put(".ag","image/x-applix-graphics");
            cts.put(".ai","application/illustrator");
            cts.put(".aif","audio/x-aiff");
            cts.put(".aifc","audio/x-aiff");
            cts.put(".aiff","audio/x-aiff");
            cts.put(".al","application/x-perl");
            cts.put(".alz","application/x-alz");
            cts.put(".amr","audio/amr");
            cts.put(".ani","application/x-navi-animation");
            cts.put(".anim[1-9j]","video/x-anim");
            cts.put(".anx","application/annodex");
            cts.put(".ape","audio/x-ape");
            cts.put(".arj","application/x-arj");
            cts.put(".arw","image/x-sony-arw");
            cts.put(".as","application/x-applix-spreadsheet");
            cts.put(".asc","text/plain");
            cts.put(".asf","video/x-ms-asf");
            cts.put(".asp","application/x-asp");
            cts.put(".ass","text/x-ssa");
            cts.put(".asx","audio/x-ms-asx");
            cts.put(".atom","application/atom+xml");
            cts.put(".au","audio/basic");
            cts.put(".avi","video/x-msvideo");
            cts.put(".aw","application/x-applix-word");
            cts.put(".awb","audio/amr-wb");
            cts.put(".awk","application/x-awk");
            cts.put(".axa","audio/annodex");
            cts.put(".axv","video/annodex");
            cts.put(".bak","application/x-trash");
            cts.put(".bcpio","application/x-bcpio");
            cts.put(".bdf","application/x-font-bdf");
            cts.put(".bib","text/x-bibtex");
            cts.put(".bin","application/octet-stream");
            cts.put(".blend","application/x-blender");
            cts.put(".blender","application/x-blender");
            cts.put(".bmp","image/bmp");
            cts.put(".bz","application/x-bzip");
            cts.put(".bz2","application/x-bzip");
            cts.put(".c","text/x-csrc");
            cts.put(".c++","text/x-c++src");
            cts.put(".cab","application/vnd.ms-cab-compressed");
            cts.put(".cb7","application/x-cb7");
            cts.put(".cbr","application/x-cbr");
            cts.put(".cbt","application/x-cbt");
            cts.put(".cbz","application/x-cbz");
            cts.put(".cc","text/x-c++src");
            cts.put(".cdf","application/x-netcdf");
            cts.put(".cdr","application/vnd.corel-draw");
            cts.put(".cer","application/x-x509-ca-cert");
            cts.put(".cert","application/x-x509-ca-cert");
            cts.put(".cgm","image/cgm");
            cts.put(".chm","application/x-chm");
            cts.put(".chrt","application/x-kchart");
            cts.put(".class","application/x-java");
            cts.put(".cls","text/x-tex");
            cts.put(".cmake","text/x-cmake");
            cts.put(".cpio","application/x-cpio");
            cts.put(".cpio.gz","application/x-cpio-compressed");
            cts.put(".cpp","text/x-c++src");
            cts.put(".cr2","image/x-canon-cr2");
            cts.put(".crt","application/x-x509-ca-cert");
            cts.put(".crw","image/x-canon-crw");
            cts.put(".cs","text/x-csharp");
            cts.put(".csh","application/x-csh");
            cts.put(".css","text/css");
            cts.put(".cssl","text/css");
            cts.put(".csv","text/csv");
            cts.put(".cue","application/x-cue");
            cts.put(".cur","image/x-win-bitmap");
            cts.put(".cxx","text/x-c++src");
            cts.put(".d","text/x-dsrc");
            cts.put(".dar","application/x-dar");
            cts.put(".dbf","application/x-dbf");
            cts.put(".dc","application/x-dc-rom");
            cts.put(".dcl","text/x-dcl");
            cts.put(".dcm","application/dicom");
            cts.put(".dcr","image/x-kodak-dcr");
            cts.put(".dds","image/x-dds");
            cts.put(".deb","application/x-deb");
            cts.put(".der","application/x-x509-ca-cert");
            cts.put(".desktop","application/x-desktop");
            cts.put(".dia","application/x-dia-diagram");
            cts.put(".diff","text/x-patch");
            cts.put(".divx","video/x-msvideo");
            cts.put(".djv","image/vnd.djvu");
            cts.put(".djvu","image/vnd.djvu");
            cts.put(".dng","image/x-adobe-dng");
            cts.put(".doc","application/msword");
            cts.put(".docbook","application/docbook+xml");
            cts.put(".docm","application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            cts.put(".docx","application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            cts.put(".dot","text/vnd.graphviz");
            cts.put(".dsl","text/x-dsl");
            cts.put(".dtd","application/xml-dtd");
            cts.put(".dtx","text/x-tex");
            cts.put(".dv","video/dv");
            cts.put(".dvi","application/x-dvi");
            cts.put(".dvi.bz2","application/x-bzdvi");
            cts.put(".dvi.gz","application/x-gzdvi");
            cts.put(".dwg","image/vnd.dwg");
            cts.put(".dxf","image/vnd.dxf");
            cts.put(".e","text/x-eiffel");
            cts.put(".egon","application/x-egon");
            cts.put(".eif","text/x-eiffel");
            cts.put(".el","text/x-emacs-lisp");
            cts.put(".emf","image/x-emf");
            cts.put(".emp","application/vnd.emusic-emusic_package");
            cts.put(".ent","application/xml-external-parsed-entity");
            cts.put(".eps","image/x-eps");
            cts.put(".eps.bz2","image/x-bzeps");
            cts.put(".eps.gz","image/x-gzeps");
            cts.put(".epsf","image/x-eps");
            cts.put(".epsf.bz2","image/x-bzeps");
            cts.put(".epsf.gz","image/x-gzeps");
            cts.put(".epsi","image/x-eps");
            cts.put(".epsi.bz2","image/x-bzeps");
            cts.put(".epsi.gz","image/x-gzeps");
            cts.put(".epub","application/epub+zip");
            cts.put(".erl","text/x-erlang");
            cts.put(".es","application/ecmascript");
            cts.put(".etheme","application/x-e-theme");
            cts.put(".etx","text/x-setext");
            cts.put(".exe","application/x-ms-dos-executable");
            cts.put(".exr","image/x-exr");
            cts.put(".ez","application/andrew-inset");
            cts.put(".f","text/x-fortran");
            cts.put(".f90","text/x-fortran");
            cts.put(".f95","text/x-fortran");
            cts.put(".fb2","application/x-fictionbook+xml");
            cts.put(".fig","image/x-xfig");
            cts.put(".fits","image/fits");
            cts.put(".fl","application/x-fluid");
            cts.put(".flac","audio/x-flac");
            cts.put(".flc","video/x-flic");
            cts.put(".fli","video/x-flic");
            cts.put(".flv","video/x-flv");
            cts.put(".flw","application/x-kivio");
            cts.put(".fo","text/x-xslfo");
            cts.put(".for","text/x-fortran");
            cts.put(".g3","image/fax-g3");
            cts.put(".gb","application/x-gameboy-rom");
            cts.put(".gba","application/x-gba-rom");
            cts.put(".gcrd","text/directory");
            cts.put(".ged","application/x-gedcom");
            cts.put(".gedcom","application/x-gedcom");
            cts.put(".gen","application/x-genesis-rom");
            cts.put(".gf","application/x-tex-gf");
            cts.put(".gg","application/x-sms-rom");
            cts.put(".gif","image/gif");
            cts.put(".glade","application/x-glade");
            cts.put(".gmo","application/x-gettext-translation");
            cts.put(".gnc","application/x-gnucash");
            cts.put(".gnd","application/gnunet-directory");
            cts.put(".gnucash","application/x-gnucash");
            cts.put(".gnumeric","application/x-gnumeric");
            cts.put(".gnuplot","application/x-gnuplot");
            cts.put(".gp","application/x-gnuplot");
            cts.put(".gpg","application/pgp-encrypted");
            cts.put(".gplt","application/x-gnuplot");
            cts.put(".gra","application/x-graphite");
            cts.put(".gsf","application/x-font-type1");
            cts.put(".gsm","audio/x-gsm");
            cts.put(".gtar","application/x-tar");
            cts.put(".gv","text/vnd.graphviz");
            cts.put(".gvp","text/x-google-video-pointer");
            cts.put(".gz","application/x-gzip");
            cts.put(".h","text/x-chdr");
            cts.put(".h++","text/x-c++hdr");
            cts.put(".hdf","application/x-hdf");
            cts.put(".hh","text/x-c++hdr");
            cts.put(".hp","text/x-c++hdr");
            cts.put(".hpgl","application/vnd.hp-hpgl");
            cts.put(".hpp","text/x-c++hdr");
            cts.put(".hs","text/x-haskell");
            cts.put(".htm","text/html");
            cts.put(".html","text/html");
            cts.put(".hwp","application/x-hwp");
            cts.put(".hwt","application/x-hwt");
            cts.put(".hxx","text/x-c++hdr");
            cts.put(".ica","application/x-ica");
            cts.put(".icb","image/x-tga");
            cts.put(".icns","image/x-icns");
            cts.put(".ico","image/vnd.microsoft.icon");
            cts.put(".ics","text/calendar");
            cts.put(".idl","text/x-idl");
            cts.put(".ief","image/ief");
            cts.put(".iff","image/x-iff");
            cts.put(".ilbm","image/x-ilbm");
            cts.put(".ime","text/x-imelody");
            cts.put(".imy","text/x-imelody");
            cts.put(".ins","text/x-tex");
            cts.put(".iptables","text/x-iptables");
            cts.put(".iso","application/x-cd-image");
            cts.put(".iso9660","application/x-cd-image");
            cts.put(".it","audio/x-it");
            cts.put(".j2k","image/jp2");
            cts.put(".jad","text/vnd.sun.j2me.app-descriptor");
            cts.put(".jar","application/x-java-archive");
            cts.put(".java","text/x-java");
            cts.put(".jng","image/x-jng");
            cts.put(".jnlp","application/x-java-jnlp-file");
            cts.put(".jp2","image/jp2");
            cts.put(".jpc","image/jp2");
            cts.put(".jpe","image/jpeg");
            cts.put(".jpeg","image/jpeg");
            cts.put(".jpf","image/jp2");
            cts.put(".jpg","image/jpeg");
            cts.put(".jpr","application/x-jbuilder-project");
            cts.put(".jpx","image/jp2");
            cts.put(".js","application/javascript");
            cts.put(".json","application/json");
            cts.put(".jsonp","application/jsonp");
            cts.put(".k25","image/x-kodak-k25");
            cts.put(".kar","audio/midi");
            cts.put(".karbon","application/x-karbon");
            cts.put(".kdc","image/x-kodak-kdc");
            cts.put(".kdelnk","application/x-desktop");
            cts.put(".kexi","application/x-kexiproject-sqlite3");
            cts.put(".kexic","application/x-kexi-connectiondata");
            cts.put(".kexis","application/x-kexiproject-shortcut");
            cts.put(".kfo","application/x-kformula");
            cts.put(".kil","application/x-killustrator");
            cts.put(".kino","application/smil");
            cts.put(".kml","application/vnd.google-earth.kml+xml");
            cts.put(".kmz","application/vnd.google-earth.kmz");
            cts.put(".kon","application/x-kontour");
            cts.put(".kpm","application/x-kpovmodeler");
            cts.put(".kpr","application/x-kpresenter");
            cts.put(".kpt","application/x-kpresenter");
            cts.put(".kra","application/x-krita");
            cts.put(".ksp","application/x-kspread");
            cts.put(".kud","application/x-kugar");
            cts.put(".kwd","application/x-kword");
            cts.put(".kwt","application/x-kword");
            cts.put(".la","application/x-shared-library-la");
            cts.put(".latex","text/x-tex");
            cts.put(".ldif","text/x-ldif");
            cts.put(".lha","application/x-lha");
            cts.put(".lhs","text/x-literate-haskell");
            cts.put(".lhz","application/x-lhz");
            cts.put(".log","text/x-log");
            cts.put(".ltx","text/x-tex");
            cts.put(".lua","text/x-lua");
            cts.put(".lwo","image/x-lwo");
            cts.put(".lwob","image/x-lwo");
            cts.put(".lws","image/x-lws");
            cts.put(".ly","text/x-lilypond");
            cts.put(".lyx","application/x-lyx");
            cts.put(".lz","application/x-lzip");
            cts.put(".lzh","application/x-lha");
            cts.put(".lzma","application/x-lzma");
            cts.put(".lzo","application/x-lzop");
            cts.put(".m","text/x-matlab");
            cts.put(".m15","audio/x-mod");
            cts.put(".m2t","video/mpeg");
            cts.put(".m3u","audio/x-mpegurl");
            cts.put(".m3u8","audio/x-mpegurl");
            cts.put(".m4","application/x-m4");
            cts.put(".m4a","audio/mp4");
            cts.put(".m4b","audio/x-m4b");
            cts.put(".m4v","video/mp4");
            cts.put(".mab","application/x-markaby");
            cts.put(".man","application/x-troff-man");
            cts.put(".mbox","application/mbox");
            cts.put(".md","application/x-genesis-rom");
            cts.put(".mdb","application/vnd.ms-access");
            cts.put(".mdi","image/vnd.ms-modi");
            cts.put(".me","text/x-troff-me");
            cts.put(".med","audio/x-mod");
            cts.put(".metalink","application/metalink+xml");
            cts.put(".mgp","application/x-magicpoint");
            cts.put(".mid","audio/midi");
            cts.put(".midi","audio/midi");
            cts.put(".mif","application/x-mif");
            cts.put(".minipsf","audio/x-minipsf");
            cts.put(".mka","audio/x-matroska");
            cts.put(".mkv","video/x-matroska");
            cts.put(".ml","text/x-ocaml");
            cts.put(".mli","text/x-ocaml");
            cts.put(".mm","text/x-troff-mm");
            cts.put(".mmf","application/x-smaf");
            cts.put(".mml","text/mathml");
            cts.put(".mng","video/x-mng");
            cts.put(".mo","application/x-gettext-translation");
            cts.put(".mo3","audio/x-mo3");
            cts.put(".moc","text/x-moc");
            cts.put(".mod","audio/x-mod");
            cts.put(".mof","text/x-mof");
            cts.put(".moov","video/quicktime");
            cts.put(".mov","video/quicktime");
            cts.put(".movie","video/x-sgi-movie");
            cts.put(".mp+","audio/x-musepack");
            cts.put(".mp2","video/mpeg");
            cts.put(".mp3","audio/mpeg");
            cts.put(".mp4","video/mp4");
            cts.put(".mpc","audio/x-musepack");
            cts.put(".mpe","video/mpeg");
            cts.put(".mpeg","video/mpeg");
            cts.put(".mpg","video/mpeg");
            cts.put(".mpga","audio/mpeg");
            cts.put(".mpp","audio/x-musepack");
            cts.put(".mrl","text/x-mrml");
            cts.put(".mrml","text/x-mrml");
            cts.put(".mrw","image/x-minolta-mrw");
            cts.put(".ms","text/x-troff-ms");
            cts.put(".msi","application/x-msi");
            cts.put(".msod","image/x-msod");
            cts.put(".msx","application/x-msx-rom");
            cts.put(".mtm","audio/x-mod");
            cts.put(".mup","text/x-mup");
            cts.put(".mxf","application/mxf");
            cts.put(".n64","application/x-n64-rom");
            cts.put(".nb","application/mathematica");
            cts.put(".nc","application/x-netcdf");
            cts.put(".nds","application/x-nintendo-ds-rom");
            cts.put(".nef","image/x-nikon-nef");
            cts.put(".nes","application/x-nes-rom");
            cts.put(".nfo","text/x-nfo");
            cts.put(".not","text/x-mup");
            cts.put(".nsc","application/x-netshow-channel");
            cts.put(".nsv","video/x-nsv");
            cts.put(".o","application/x-object");
            cts.put(".obj","application/x-tgif");
            cts.put(".ocl","text/x-ocl");
            cts.put(".oda","application/oda");
            cts.put(".odb","application/vnd.oasis.opendocument.database");
            cts.put(".odc","application/vnd.oasis.opendocument.chart");
            cts.put(".odf","application/vnd.oasis.opendocument.formula");
            cts.put(".odg","application/vnd.oasis.opendocument.graphics");
            cts.put(".odi","application/vnd.oasis.opendocument.image");
            cts.put(".odm","application/vnd.oasis.opendocument.text-master");
            cts.put(".odp","application/vnd.oasis.opendocument.presentation");
            cts.put(".ods","application/vnd.oasis.opendocument.spreadsheet");
            cts.put(".odt","application/vnd.oasis.opendocument.text");
            cts.put(".oga","audio/ogg");
            cts.put(".ogg","video/x-theora+ogg");
            cts.put(".ogm","video/x-ogm+ogg");
            cts.put(".ogv","video/ogg");
            cts.put(".ogx","application/ogg");
            cts.put(".old","application/x-trash");
            cts.put(".oleo","application/x-oleo");
            cts.put(".opml","text/x-opml+xml");
            cts.put(".ora","image/openraster");
            cts.put(".orf","image/x-olympus-orf");
            cts.put(".otc","application/vnd.oasis.opendocument.chart-template");
            cts.put(".otf","application/x-font-otf");
            cts.put(".otg","application/vnd.oasis.opendocument.graphics-template");
            cts.put(".oth","application/vnd.oasis.opendocument.text-web");
            cts.put(".otp","application/vnd.oasis.opendocument.presentation-template");
            cts.put(".ots","application/vnd.oasis.opendocument.spreadsheet-template");
            cts.put(".ott","application/vnd.oasis.opendocument.text-template");
            cts.put(".owl","application/rdf+xml");
            cts.put(".oxt","application/vnd.openofficeorg.extension");
            cts.put(".p","text/x-pascal");
            cts.put(".p10","application/pkcs10");
            cts.put(".p12","application/x-pkcs12");
            cts.put(".p7b","application/x-pkcs7-certificates");
            cts.put(".p7s","application/pkcs7-signature");
            cts.put(".pack","application/x-java-pack200");
            cts.put(".pak","application/x-pak");
            cts.put(".par2","application/x-par2");
            cts.put(".pas","text/x-pascal");
            cts.put(".patch","text/x-patch");
            cts.put(".pbm","image/x-portable-bitmap");
            cts.put(".pcd","image/x-photo-cd");
            cts.put(".pcf","application/x-cisco-vpn-settings");
            cts.put(".pcf.gz","application/x-font-pcf");
            cts.put(".pcf.z","application/x-font-pcf");
            cts.put(".pcl","application/vnd.hp-pcl");
            cts.put(".pcx","image/x-pcx");
            cts.put(".pdb","chemical/x-pdb");
            cts.put(".pdc","application/x-aportisdoc");
            cts.put(".pdf","application/pdf");
            cts.put(".pdf.bz2","application/x-bzpdf");
            cts.put(".pdf.gz","application/x-gzpdf");
            cts.put(".pef","image/x-pentax-pef");
            cts.put(".pem","application/x-x509-ca-cert");
            cts.put(".perl","application/x-perl");
            cts.put(".pfa","application/x-font-type1");
            cts.put(".pfb","application/x-font-type1");
            cts.put(".pfx","application/x-pkcs12");
            cts.put(".pgm","image/x-portable-graymap");
            cts.put(".pgn","application/x-chess-pgn");
            cts.put(".pgp","application/pgp-encrypted");
            cts.put(".php","application/x-php");
            cts.put(".php3","application/x-php");
            cts.put(".php4","application/x-php");
            cts.put(".pict","image/x-pict");
            cts.put(".pict1","image/x-pict");
            cts.put(".pict2","image/x-pict");
            cts.put(".pickle","application/python-pickle");
            cts.put(".pk","application/x-tex-pk");
            cts.put(".pkipath","application/pkix-pkipath");
            cts.put(".pkr","application/pgp-keys");
            cts.put(".pl","application/x-perl");
            cts.put(".pla","audio/x-iriver-pla");
            cts.put(".pln","application/x-planperfect");
            cts.put(".pls","audio/x-scpls");
            cts.put(".pm","application/x-perl");
            cts.put(".png","image/png");
            cts.put(".pnm","image/x-portable-anymap");
            cts.put(".pntg","image/x-macpaint");
            cts.put(".po","text/x-gettext-translation");
            cts.put(".por","application/x-spss-por");
            cts.put(".pot","text/x-gettext-translation-template");
            cts.put(".ppm","image/x-portable-pixmap");
            cts.put(".pps","application/vnd.ms-powerpoint");
            cts.put(".ppt","application/vnd.ms-powerpoint");
            cts.put(".pptm","application/vnd.openxmlformats-officedocument.presentationml.presentation");
            cts.put(".pptx","application/vnd.openxmlformats-officedocument.presentationml.presentation");
            cts.put(".ppz","application/vnd.ms-powerpoint");
            cts.put(".prc","application/x-palm-database");
            cts.put(".ps","application/postscript");
            cts.put(".ps.bz2","application/x-bzpostscript");
            cts.put(".ps.gz","application/x-gzpostscript");
            cts.put(".psd","image/vnd.adobe.photoshop");
            cts.put(".psf","audio/x-psf");
            cts.put(".psf.gz","application/x-gz-font-linux-psf");
            cts.put(".psflib","audio/x-psflib");
            cts.put(".psid","audio/prs.sid");
            cts.put(".psw","application/x-pocket-word");
            cts.put(".pw","application/x-pw");
            cts.put(".py","text/x-python");
            cts.put(".pyc","application/x-python-bytecode");
            cts.put(".pyo","application/x-python-bytecode");
            cts.put(".qif","image/x-quicktime");
            cts.put(".qt","video/quicktime");
            cts.put(".qtif","image/x-quicktime");
            cts.put(".qtl","application/x-quicktime-media-link");
            cts.put(".qtvr","video/quicktime");
            cts.put(".ra","audio/vnd.rn-realaudio");
            cts.put(".raf","image/x-fuji-raf");
            cts.put(".ram","application/ram");
            cts.put(".rar","application/x-rar");
            cts.put(".ras","image/x-cmu-raster");
            cts.put(".raw","image/x-panasonic-raw");
            cts.put(".rax","audio/vnd.rn-realaudio");
            cts.put(".rb","application/x-ruby");
            cts.put(".rdf","application/rdf+xml");
            cts.put(".rdfs","application/rdf+xml");
            cts.put(".reg","text/x-ms-regedit");
            cts.put(".rej","application/x-reject");
            cts.put(".rgb","image/x-rgb");
            cts.put(".rle","image/rle");
            cts.put(".rm","application/vnd.rn-realmedia");
            cts.put(".rmj","application/vnd.rn-realmedia");
            cts.put(".rmm","application/vnd.rn-realmedia");
            cts.put(".rms","application/vnd.rn-realmedia");
            cts.put(".rmvb","application/vnd.rn-realmedia");
            cts.put(".rmx","application/vnd.rn-realmedia");
            cts.put(".roff","text/troff");
            cts.put(".rp","image/vnd.rn-realpix");
            cts.put(".rpm","application/x-rpm");
            cts.put(".rss","application/rss+xml");
            cts.put(".rt","text/vnd.rn-realtext");
            cts.put(".rtf","application/rtf");
            cts.put(".rtx","text/richtext");
            cts.put(".rv","video/vnd.rn-realvideo");
            cts.put(".rvx","video/vnd.rn-realvideo");
            cts.put(".s3m","audio/x-s3m");
            cts.put(".sam","application/x-amipro");
            cts.put(".sami","application/x-sami");
            cts.put(".sav","application/x-spss-sav");
            cts.put(".scm","text/x-scheme");
            cts.put(".sda","application/vnd.stardivision.draw");
            cts.put(".sdc","application/vnd.stardivision.calc");
            cts.put(".sdd","application/vnd.stardivision.impress");
            cts.put(".sdp","application/sdp");
            cts.put(".sds","application/vnd.stardivision.chart");
            cts.put(".sdw","application/vnd.stardivision.writer");
            cts.put(".sgf","application/x-go-sgf");
            cts.put(".sgi","image/x-sgi");
            cts.put(".sgl","application/vnd.stardivision.writer");
            cts.put(".sgm","text/sgml");
            cts.put(".sgml","text/sgml");
            cts.put(".sh","application/x-shellscript");
            cts.put(".shar","application/x-shar");
            cts.put(".shn","application/x-shorten");
            cts.put(".siag","application/x-siag");
            cts.put(".sid","audio/prs.sid");
            cts.put(".sik","application/x-trash");
            cts.put(".sis","application/vnd.symbian.install");
            cts.put(".sisx","x-epoc/x-sisx-app");
            cts.put(".sit","application/x-stuffit");
            cts.put(".siv","application/sieve");
            cts.put(".sk","image/x-skencil");
            cts.put(".sk1","image/x-skencil");
            cts.put(".skr","application/pgp-keys");
            cts.put(".slk","text/spreadsheet");
            cts.put(".smaf","application/x-smaf");
            cts.put(".smc","application/x-snes-rom");
            cts.put(".smd","application/vnd.stardivision.mail");
            cts.put(".smf","application/vnd.stardivision.math");
            cts.put(".smi","application/x-sami");
            cts.put(".smil","application/smil");
            cts.put(".sml","application/smil");
            cts.put(".sms","application/x-sms-rom");
            cts.put(".snd","audio/basic");
            cts.put(".so","application/x-sharedlib");
            cts.put(".spc","application/x-pkcs7-certificates");
            cts.put(".spd","application/x-font-speedo");
            cts.put(".spec","text/x-rpm-spec");
            cts.put(".spl","application/x-shockwave-flash");
            cts.put(".spx","audio/x-speex");
            cts.put(".sql","text/x-sql");
            cts.put(".sr2","image/x-sony-sr2");
            cts.put(".src","application/x-wais-source");
            cts.put(".srf","image/x-sony-srf");
            cts.put(".srt","application/x-subrip");
            cts.put(".ssa","text/x-ssa");
            cts.put(".stc","application/vnd.sun.xml.calc.template");
            cts.put(".std","application/vnd.sun.xml.draw.template");
            cts.put(".sti","application/vnd.sun.xml.impress.template");
            cts.put(".stm","audio/x-stm");
            cts.put(".stw","application/vnd.sun.xml.writer.template");
            cts.put(".sty","text/x-tex");
            cts.put(".sub","text/x-subviewer");
            cts.put(".sun","image/x-sun-raster");
            cts.put(".sv4cpio","application/x-sv4cpio");
            cts.put(".sv4crc","application/x-sv4crc");
            cts.put(".svg","image/svg+xml");
            cts.put(".svgz","image/svg+xml-compressed");
            cts.put(".swf","application/x-shockwave-flash");
            cts.put(".sxc","application/vnd.sun.xml.calc");
            cts.put(".sxd","application/vnd.sun.xml.draw");
            cts.put(".sxg","application/vnd.sun.xml.writer.global");
            cts.put(".sxi","application/vnd.sun.xml.impress");
            cts.put(".sxm","application/vnd.sun.xml.math");
            cts.put(".sxw","application/vnd.sun.xml.writer");
            cts.put(".sylk","text/spreadsheet");
            cts.put(".t","text/troff");
            cts.put(".t2t","text/x-txt2tags");
            cts.put(".tar","application/x-tar");
            cts.put(".tar.bz","application/x-bzip-compressed-tar");
            cts.put(".tar.bz2","application/x-bzip-compressed-tar");
            cts.put(".tar.gz","application/x-compressed-tar");
            cts.put(".tar.lzma","application/x-lzma-compressed-tar");
            cts.put(".tar.lzo","application/x-tzo");
            cts.put(".tar.xz","application/x-xz-compressed-tar");
            cts.put(".tar.z","application/x-tarz");
            cts.put(".tbz","application/x-bzip-compressed-tar");
            cts.put(".tbz2","application/x-bzip-compressed-tar");
            cts.put(".tcl","text/x-tcl");
            cts.put(".tex","text/x-tex");
            cts.put(".texi","text/x-texinfo");
            cts.put(".texinfo","text/x-texinfo");
            cts.put(".tga","image/x-tga");
            cts.put(".tgz","application/x-compressed-tar");
            cts.put(".theme","application/x-theme");
            cts.put(".themepack","application/x-windows-themepack");
            cts.put(".tif","image/tiff");
            cts.put(".tiff","image/tiff");
            cts.put(".tk","text/x-tcl");
            cts.put(".tlz","application/x-lzma-compressed-tar");
            cts.put(".tnef","application/vnd.ms-tnef");
            cts.put(".tnf","application/vnd.ms-tnef");
            cts.put(".toc","application/x-cdrdao-toc");
            cts.put(".torrent","application/x-bittorrent");
            cts.put(".tpic","image/x-tga");
            cts.put(".tr","text/troff");
            cts.put(".ts","application/x-linguist");
            cts.put(".tsv","text/tab-separated-values");
            cts.put(".tta","audio/x-tta");
            cts.put(".ttc","application/x-font-ttf");
            cts.put(".ttf","application/x-font-ttf");
            cts.put(".ttx","application/x-font-ttx");
            cts.put(".txt","text/plain");
            cts.put(".txz","application/x-xz-compressed-tar");
            cts.put(".tzo","application/x-tzo");
            cts.put(".ufraw","application/x-ufraw");
            cts.put(".ui","application/x-designer");
            cts.put(".uil","text/x-uil");
            cts.put(".ult","audio/x-mod");
            cts.put(".uni","audio/x-mod");
            cts.put(".uri","text/x-uri");
            cts.put(".url","text/x-uri");
            cts.put(".ustar","application/x-ustar");
            cts.put(".vala","text/x-vala");
            cts.put(".vapi","text/x-vala");
            cts.put(".vcf","text/directory");
            cts.put(".vcs","text/calendar");
            cts.put(".vct","text/directory");
            cts.put(".vda","image/x-tga");
            cts.put(".vhd","text/x-vhdl");
            cts.put(".vhdl","text/x-vhdl");
            cts.put(".viv","video/vivo");
            cts.put(".vivo","video/vivo");
            cts.put(".vlc","audio/x-mpegurl");
            cts.put(".vob","video/mpeg");
            cts.put(".voc","audio/x-voc");
            cts.put(".vor","application/vnd.stardivision.writer");
            cts.put(".vst","image/x-tga");
            cts.put(".wav","audio/x-wav");
            cts.put(".wax","audio/x-ms-asx");
            cts.put(".wb1","application/x-quattropro");
            cts.put(".wb2","application/x-quattropro");
            cts.put(".wb3","application/x-quattropro");
            cts.put(".wbmp","image/vnd.wap.wbmp");
            cts.put(".wcm","application/vnd.ms-works");
            cts.put(".wdb","application/vnd.ms-works");
            cts.put(".webm","video/webm");
            cts.put(".wk1","application/vnd.lotus-1-2-3");
            cts.put(".wk3","application/vnd.lotus-1-2-3");
            cts.put(".wk4","application/vnd.lotus-1-2-3");
            cts.put(".wks","application/vnd.ms-works");
            cts.put(".wma","audio/x-ms-wma");
            cts.put(".wmf","image/x-wmf");
            cts.put(".wml","text/vnd.wap.wml");
            cts.put(".wmls","text/vnd.wap.wmlscript");
            cts.put(".wmv","video/x-ms-wmv");
            cts.put(".wmx","audio/x-ms-asx");
            cts.put(".wp","application/vnd.wordperfect");
            cts.put(".wp4","application/vnd.wordperfect");
            cts.put(".wp5","application/vnd.wordperfect");
            cts.put(".wp6","application/vnd.wordperfect");
            cts.put(".wpd","application/vnd.wordperfect");
            cts.put(".wpg","application/x-wpg");
            cts.put(".wpl","application/vnd.ms-wpl");
            cts.put(".wpp","application/vnd.wordperfect");
            cts.put(".wps","application/vnd.ms-works");
            cts.put(".wri","application/x-mswrite");
            cts.put(".wrl","model/vrml");
            cts.put(".wv","audio/x-wavpack");
            cts.put(".wvc","audio/x-wavpack-correction");
            cts.put(".wvp","audio/x-wavpack");
            cts.put(".wvx","audio/x-ms-asx");
            cts.put(".x3f","image/x-sigma-x3f");
            cts.put(".xac","application/x-gnucash");
            cts.put(".xbel","application/x-xbel");
            cts.put(".xbl","application/xml");
            cts.put(".xbm","image/x-xbitmap");
            cts.put(".xcf","image/x-xcf");
            cts.put(".xcf.bz2","image/x-compressed-xcf");
            cts.put(".xcf.gz","image/x-compressed-xcf");
            cts.put(".xhtml","application/xhtml+xml");
            cts.put(".xi","audio/x-xi");
            cts.put(".xla","application/vnd.ms-excel");
            cts.put(".xlc","application/vnd.ms-excel");
            cts.put(".xld","application/vnd.ms-excel");
            cts.put(".xlf","application/x-xliff");
            cts.put(".xliff","application/x-xliff");
            cts.put(".xll","application/vnd.ms-excel");
            cts.put(".xlm","application/vnd.ms-excel");
            cts.put(".xls","application/vnd.ms-excel");
            cts.put(".xlsm","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            cts.put(".xlsx","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            cts.put(".xlt","application/vnd.ms-excel");
            cts.put(".xlw","application/vnd.ms-excel");
            cts.put(".xm","audio/x-xm");
            cts.put(".xmf","audio/x-xmf");
            cts.put(".xmi","text/x-xmi");
            cts.put(".xml","application/xml");
            cts.put(".xpm","image/x-xpixmap");
            cts.put(".xps","application/vnd.ms-xpsdocument");
            cts.put(".xsl","application/xml");
            cts.put(".xslfo","text/x-xslfo");
            cts.put(".xslt","application/xml");
            cts.put(".xspf","application/xspf+xml");
            cts.put(".xul","application/vnd.mozilla.xul+xml");
            cts.put(".xwd","image/x-xwindowdump");
            cts.put(".xyz","chemical/x-pdb");
            cts.put(".xz","application/x-xz");
            cts.put(".w2p","application/w2p");
            cts.put(".z","application/x-compress");
            cts.put(".zabw","application/x-abiword");
            cts.put(".zip","application/zip");
            cts.put(".zoo","application/x-zoo");
            cts.put(".apk","application/vnd.android.package-archive");
            cts.put(".ipa","application/vnd.iphone");
            cts.put(".xap","application/x-silverlight-app");
        }

        //通用的二进制方式
        public static final String FILE_OCTET = "application/octet-stream";

        /**
         * 从后准名直接取contentType
         * @param fileExt
         * @return
         */
        public static String getContentTypeForFileExtensions(String fileExt) {
            if (fileExt == null || fileExt.length() == 0) {//默认值
                return FILE_OCTET;//文件下载
            }
            if (fileExt.charAt(0) != '.') {
                fileExt = '.' + fileExt;
            }
            String ct = cts.get(fileExt);
            if (ct == null) {//未取到时都返回默认
                ct = FILE_OCTET;
            }
            return ct;
        }

        public static String getContentTypeForFileName(String name) {
            if (name == null || name.length() == 0) {
                return FILE_OCTET;
            }
            int idx = name.lastIndexOf(".");
            if (idx < 0 || idx >= name.length()) {
                return FILE_OCTET;
            }
            return getContentTypeForFileExtensions(name.substring(idx,name.length()));
        }
    }

}
