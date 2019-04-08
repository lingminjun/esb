package com.venus.esb.utils;


import com.venus.esb.lang.ESBT;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;

/**
 * url 的一些通用处理
 * 用于处理router固定规则
 */
public final class URLs {
    /**
     * 转uri
     * @param url
     * @return
     */
    public static URI toURI(String url) {
        if (ESBT.isEmpty(url)) {
            return null;
        }
        URI uri = null;
        try {
            uri = new URI(url);
            // 全部编码  %E6%90%9C%E7%B4%A2%E4%B8%AD%E6%96%87
            uri = new URI(uri.toASCIIString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return uri;
    }

    /**
     * 不返回query和fragment
     * @param url
     * @param scheme
     * @param host
     * @return
     */
    public static String tidyURI(String url, String scheme, String host) {
        return tidy(url,scheme,host,0,true,true,null,true,null);
    }

    /**
     * 简单整理url, 此处存在bug，暂时并没有修复，一律转小写
     * @param url
     * @param scheme
     * @param host
     * @param port
     * @param noQuery
     * @param query       必须encode
     * @param noFragment
     * @param fragment    必须encode
     * @return
     */
    public static String tidy(String url, String scheme, String host, int port, boolean ignoreCase, boolean noQuery, String query, boolean noFragment, String fragment) {
        URI uri = toURI(url);
        if (uri == null) {
            return url;
        }

        StringBuilder builder = new StringBuilder();

        // scheme
        String theScheme = null;
        if (ESBT.isEmpty(scheme)) {
            theScheme = uri.getScheme().toLowerCase();
        } else {
            theScheme = scheme.toLowerCase();
        }
        builder.append(theScheme);
        builder.append("://");

        // host
        if (ESBT.isEmpty(host)) {
            builder.append(uri.getHost().toLowerCase());
        } else {
            builder.append(host.toLowerCase());
        }

        // port
        int thePort = -1;
        if (port > 0) {
            thePort = port;
        } else {
            thePort = uri.getPort();
        }

        if (thePort > 0) {
            if ((thePort == 80 && theScheme.equals("http")) || (thePort == 443 && theScheme.equals("https"))) {
                //
            } else {
                builder.append(":");
                builder.append(thePort);
            }
        }

        // raw path 因为uri转以后是大写，只能忽略大小写
        if (ignoreCase) {
            builder.append(uri.getRawPath().toLowerCase());
        } else {
            builder.append(buildPath(pathSegments(uri.getRawPath(),false)));
        }

        if (!noQuery) {
            if (ESBT.isEmpty(query)) {
                query = uri.getRawQuery();
            }
            if (!ESBT.isEmpty(query)) {
                builder.append("?");
                // 暂时无法修复bug 只能忽略大小写
                builder.append(URLQueryString(URLParams(query,false),false));
            }
        }

        if (!noFragment) {
            if (ESBT.isEmpty(fragment)) {
                fragment = uri.getRawFragment();
            }
            if (!ESBT.isEmpty(fragment)) {
                builder.append("#");
                // 暂时无法修复bug 只能忽略大小写
                builder.append(URLQueryString(URLParams(fragment,false),false));
            }
        }

        return builder.toString();
    }

    /**********待调整url***********************************************/
    static final String URL_ALLOW_CHARS = ":/?#=&,!~;@-_";// $-_.+!*'(),  reserved
    public static final String URI_SPLIT = "/";

    /**
     * 将params key value编码，其中Object 只能是String，或者是List<String>以及基本数据类型
     * @param params
     * @return URL Query string , values url encode(UTF-8)
     */
    public static String URLQueryString(HashMap<String,Object> params) {
        return URLQueryString(params,true);
    }
    public static String URLQueryString(HashMap<String,Object> params, boolean encode) {

        if (params == null) {
            return "";
        }

        if (params.size() == 0) {
            return "";
        }

        String query = "";
        try {
            List<String> keys = new ArrayList<String>(params.keySet());
            Collections.sort(keys);

            Boolean first = true;
            for (String key : keys) {
                Object obj = params.get(key);
                if (obj instanceof CharSequence
                        || obj.getClass().isPrimitive()
                        || obj instanceof Number
                        || obj instanceof Boolean
                        || obj instanceof Character) {
                    String value = obj.toString();
                    if (ESBT.isEmpty(value)) {
                        if (keys.size() == 1) {//只有一个key的情况
                            return key;
                        }
                        continue;
                    }

                    if (first) {
                        first = false;
                        if (encode) {
//                            query = query + key + "=" + URI.encode(value);//RFC-2396
                        query = query + key + "=" + URLEncoder.encode((String)obj, "UTF-8");//RFC-1738
                        } else {
                            query = query + key + "=" + value;//
                        }
                    }
                    else  {
                        if (encode) {
//                            query = query + "&" + key + "=" + URI.encode(value);//RFC-2396
                        query = query + "&" + key + "=" + URLEncoder.encode((String)obj, "UTF-8");//RFC-1738
                        } else {
                            query = query + key + "=" + value;//
                        }
                    }
                } else if (obj instanceof List) {
                    List list = (List)obj;
                    ArrayList<String> values = new ArrayList<>();
                    for (Object o : list) {
                        if (o.getClass().isPrimitive()
                                || o instanceof Number
                                || o instanceof Boolean
                                || o instanceof Character
                                || o instanceof CharSequence) {
                            String s = o.toString();
                            if (!s.isEmpty()) {
                                values.add(s);
                            }
                        }
                    }
                    Collections.sort(values);
                    for (String value : values) {

                        if (first) {
                            first = false;
                            if (encode) {
//                                query = query + key + "=" + URI.encode(value);//RFC-2396
                            query = query + key + "=" + URLEncoder.encode(value, "UTF-8");//RFC-1738
                            } else {
                                query = query + key + "=" + value;//
                            }
                        }
                        else  {
                            if (encode) {
//                                query = query + "&" + key + "=" + URI.encode(value);//RFC-2396
                            query = query + "&" + key + "=" + URLEncoder.encode(value, "UTF-8");//RFC-1738
                            } else {
                                query = query + key + "=" + value;//
                            }
                        }
                    }
                } else if (obj == null && keys.size() == 1) {
                    return key;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            query = "";
        }

        return query;
    }

    /**
     * 取URL query 中的参数
     * @param queryString 只要是key=value&key1=value2方式
     * @return
     */
    public static HashMap<String,Object> URLParams(String queryString) {
        return URLParams(queryString,true);
    }
    public static HashMap<String,Object> URLParams(String queryString, boolean decode) {

        if (ESBT.isEmpty(queryString)) {
            return new HashMap<String, Object>();
        }

        HashMap<String, Object> params = new HashMap<String, Object>();

        String string = trim(queryString,"?#;!&");
        try {

            String[] comps = string.split("&");
            for (int i = 0; i < comps.length; i++) {
                String str = comps[i];
                if (ESBT.isEmpty(str)) {
                    continue;
                }

                Object obj = params.get(str);

                if (!str.contains("=")) {
                    if (obj == null) {//非空的情况才能加入
                        params.put(str,"");
                    }
                }
                else {
                    String[] innr = str.split("=",2);
                    String key = innr[0];
                    String value = innr[1];//必然有元素

                    if (ESBT.isEmpty(key)) {//key本身是非法的，直接不要了
                        continue;
                    }

                    if (decode) {
//                        value = URI.decode(value);//RFC-2396标准
                        value = URLDecoder.decode(value,"UTF-8");//RFC-1738标准
                    }

                    if (obj == null) {
                        params.put(key,value);
                    }
                    else {
                        if (obj instanceof List) {
                            ((List<String>)obj).add(value);
                        }
                        else {
                            List<String> vls = new ArrayList<String>();
                            vls.add((String)obj);
                            vls.add(value);
                            params.put(key,vls);
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
//            APPLog.error(e.toString());
        }

        return params;
    }


    /**
     * 解析url中的query 参数 value url decode
     * @return
     */
    public static HashMap<String,Object> URLQuery(String url) {
        URI uri = toURI(url);
        if (uri == null) {
            return new HashMap<String,Object>();
        }
        String queryString = uri.getRawQuery();
        return URLParams(queryString,true);
    }

    /**
     * 解析url中的fragment 参数 value url decode
     * @param url，注意此方法url必须是已经encode方法，否则可能出现解析参数出错
     * @return
     */
    public static HashMap<String,Object> URLFragment(String url) {
        URI uri = toURI(url);
        if (uri == null) {
            return new HashMap<String,Object>();
        }
        String queryString = uri.getRawFragment();
        return URLParams(queryString,true);
    }

    /**
     * url host
     * @return
     */
    public static String URLHost(String url) {
        URI uri = toURI(url);
        if (uri == null) {
            return url;
        }

        return uri.getHost().toUpperCase();
    }

    /**
     * 返回整理过的path，所有字母都小写
     * @param url
     * @return
     */
    public static String tidyURLPath(String url, boolean ignoreCase) {
        if (ESBT.isEmpty(url)) {
            return "";
        }

        List<String> paths = tidyURLPathSegments(url, ignoreCase);
        return buildPath(paths);
    }

    /**
     * 返回所有path，去掉"/", ".", "..";等path
     * @param url
     * @return
     */
    public static List<String> tidyURLPathSegments(String url, boolean ignoreCase) {
        URI uri = toURI(url);

        if (uri == null) {
            return new ArrayList<>();
        }

        return pathSegments(uri.getRawPath(),ignoreCase);
    }

    /**
     * 单页应用支持，如:AngularJS、ExtJS、jQuery、Backbone，Knockout，AngularJS，Avalon等。
     * 返回所有fragment path结构，去掉"/", ".", "..";等path
     * @param url
     * @return
     */
    public static List<String> tidyURLFragmentPathSegments(String url, boolean ignoreCase) {
        URI uri = toURI(url);

        if (uri == null) {
            return new ArrayList<>();
        }

        String fragment = uri.getRawFragment();
        if (fragment == null || fragment.length() == 0 || !fragment.startsWith("/")) {
            return new ArrayList<>();
        }

        //去掉非标准的 单页应用存在非标准的query参数 如：#/product/3918744?from=singlemessage
        int idx = fragment.indexOf("?");
        if (idx > 0 && idx < fragment.length()) {
            fragment = fragment.substring(0,idx);
        }

        return pathSegments(fragment,ignoreCase);
    }

    public static List<String> pathSegments(String path, boolean ignoreCase) {
        String[] paths = path.split("/",-1);
        List<String> list = new ArrayList<>();
        for (String p : paths) {
            if (ESBT.isEmpty(p)) {continue;}
            if (p.equals("/")) {continue;}
            if (p.equals(".")) {continue;}
            if (p.equals("..")) {continue;}
            String pp = null;
            if (ignoreCase) {
                pp = p.toLowerCase();
            } else {
                try {
                    pp = URLEncoder.encode(URLDecoder.decode(p,"UTF-8"),"UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    pp = p;
                }
            }
            list.add(pp);
        }
        return list;
    }

    public static String buildPath(List<String> paths) {
        StringBuilder builder = new StringBuilder();
        boolean isFirst = true;
        for (String p : paths) {
            if (!ESBT.isEmpty(p)) {
                if (isFirst) {
                    isFirst = false;
                }
                else {
                    builder.append("/");
                }
                builder.append(p);
            }
        }
        return builder.toString();
    }

    public static String trim(String stream, String characters) {//

        // null或者空字符串的时候不处理
        if (stream == null || stream.length() == 0 || characters == null || characters.length() == 0) {
            return stream;
        }

        int begin = 0;
        int end = stream.length();

        while (begin < end) {
            if (characters.contains(stream.substring(begin,begin+1))) {
                begin++;
            }
            else {
                break;
            }
        }

        String target = stream.substring(begin);
        if (target.length() <= 1) {
            return target;
        }

        end = target.length() - 1;
        while (end > 0) {
            if (characters.contains(target.substring(end,end+1))) {
                end--;
            }
            else {
                break;
            }
        }

        return target.substring(0,end + 1);
    }
}
