package com.venus.esb.l10n;


import com.alibaba.dubbo.common.utils.StringUtils;
import com.venus.esb.lang.ESBSTDKeys;
import com.venus.esb.lang.ESBThreadLocal;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lingminjun on 17/5/26.
 * 国际化支持类,下面是支持的本地化类型[l10n],可以自定义
 * 中文简体:zh-CN,zh
 * 中文繁体:zh-TW,zh-HK
 * 英语:en
 *
 * 请在文案对照表的xml中以正确名字命名,如classpath:/i19n/zh-CH.xml
 * 内容定义
 */
public final class ESBI18N {

    private HashMap<String,HashMap<String,String>> i18n_res = new HashMap<String, HashMap<String, String>>();

    private static final String I18N_RES_PACKAGE = "classpath*:/i18n/*.xml";

    private ESBI18N() {

        //将以class path作为后加载 解决加载顺序问题
        String mainSourcePath = System.getProperty("java.class.path");//Thread.currentThread().getContextClassLoader().getResource("").getPath();

        List<Resource> lasts = new ArrayList<Resource>();

        //开始加载国际化资源
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] reses = resolver.getResources(I18N_RES_PACKAGE);
            if (reses == null) {
                return;
            }

//            System.out.println("========");
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            for (int i = 0; i < reses.length; i++) {
                Resource res = reses[i];
                String path = res.getURL().getPath();
                if (path.startsWith("file:")) {
                    path = path.substring("file:".length(), path.length());
                }
//                System.out.println("测试路径问题:"+path);
                InputStream inputStream = res.getInputStream();
                Document document = parse(inputStream,builderFactory);

                if (document == null) {
                    continue;
                }

                if (path.startsWith(mainSourcePath)) {
                    lasts.add(res);
                    continue;
                }

                String l10n = res.getFilename();
                try {
                    l10n = l10n.substring(0,l10n.length() - ".xml".length());
//                    System.out.println("start parse " + l10n + ".xml path:" + file.getAbsolutePath());
                } catch (Throwable e) {e.printStackTrace();continue;}

                //增加一种语言
                HashMap<String,String> map = i18n_res.get(l10n);
                if (map == null) {
                    map = new HashMap<String, String>();
                    i18n_res.put(l10n,map);
                }

                readNode(document,map);

//                System.out.println("end parse " + l10n + ".xml");
            }

            //最后读取目录的
            for (Resource res : lasts) {
                InputStream inputStream = res.getInputStream();
                Document document = parse(inputStream,builderFactory);
                if (document == null) {
                    continue;
                }

                String l10n = res.getFilename();
                try {
                    l10n = l10n.substring(0,l10n.length() - ".xml".length());
//                    System.out.println("start parse " + l10n + ".xml path:" + file.getAbsolutePath());
                } catch (Throwable e) {e.printStackTrace();continue;}

                //增加一种语言
                HashMap<String,String> map = i18n_res.get(l10n);
                if (map == null) {
                    map = new HashMap<String, String>();
                    i18n_res.put(l10n,map);
                }

                readNode(document,map);

//                System.out.println("end parse " + l10n + ".xml");
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static ESBI18N I18N() {
        return SingletonHolder.INSTANCE;
    }

    private String _l10n(String copywriting) {
        return _l10n(copywriting,null);
    }

    private String _l10n(String copywriting, String l10n) {
        if (copywriting == null || copywriting.length() == 0) {
            return copywriting;
        }

        if (l10n == null) {//直接取上下文存贮的语言类型
            l10n = ESBThreadLocal.get(ESBSTDKeys.L10N_KEY);
        }
        if (l10n == null) {
            l10n = "zh";//默认本地化语言
        }

        HashMap<String,String> rs = i18n_res.get(l10n);
        String copying = copywriting;
        if (rs != null) {
            copying = rs.get(copywriting);
        }

        if (copying == null) {
            copying = copywriting;
        }

        return copying;
    }

    public static String l10n(String copywriting) {
        return I18N()._l10n(copywriting);
    }

    public static String l10n(String copywriting, String l10n) {
        return I18N()._l10n(copywriting,l10n);
    }



    //Load and parse XML file into DOM
    private Document parse(InputStream in, DocumentBuilderFactory factory) {
        Document document = null;
        try {
            //DOM parser instance
            DocumentBuilder builder = factory.newDocumentBuilder();
            //parse an XML file into a DOM tree
            document = builder.parse(in);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return document;
    }

    private static final String COPY_WRITING = "copywriting";
    private static final String COPY_WRITING_KEY = "key";
    private static final String COPY_WRITING_VALUE = "value";
    private void readNode(Document document, HashMap<String,String> rs) {
        DOMParser parser = new DOMParser();

        //get root element
        Element rootElement = document.getDocumentElement();

        //traverse child elements
        NodeList nodes = rootElement.getChildNodes();
        for (int i=0; i < nodes.getLength(); i++)
        {
            Node node = nodes.item(i);
            if (node == null) {
                continue;
            }
//            System.out.println("name:"+node.getNodeName()
//                    +" value:"+node.getNodeValue());
            if (COPY_WRITING.equals(node.getNodeName())) {
                NodeList list = node.getChildNodes();
                if (list == null) {
                    continue;
                }

                String key = null;
                String value = null;
                for (int j=0; j < list.getLength(); j++) {
                    Node nd = list.item(j);
                    if (nd == null) {
                        continue;
                    }

                    if (COPY_WRITING_KEY.equals(nd.getNodeName())) {
                        key = nd.getTextContent();
                    } else if (COPY_WRITING_VALUE.equals(nd.getNodeName())) {
                        value = nd.getTextContent();

                        //开始写入
                        if (!StringUtils.isEmpty(key) && !StringUtils.isEmpty(value)) {
                            if (rs.containsKey(key)) {
//                                System.out.println("warning duplicate key:" + key + "=" + value);
                            }
                            rs.put(key, value);
//                            System.out.println("add key:" + key + "=" + value);
                        }

                        key = null;
                        value = null;
                    }
                }
            }
        }
    }

    private static class SingletonHolder {
        private static ESBI18N INSTANCE = new ESBI18N();
    }
}
