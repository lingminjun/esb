package com.venus.esb.config;

import com.venus.esb.lang.ESBT;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * Description: merge所有配置到一个文件，然后再取其值，优先取当前工程
 * User: lingminjun
 * Date: 2019-01-03
 * Time: 10:36 PM
 */
public class ESBProperties {

    private Map<String, String> _props = new HashMap<>(); //线程安全

    public String getString(String key) {
        return _props.get(key);
    }

    public boolean getBoolean(String key) {
        return ESBT.bool(_props.get(key));
    }

    public int getInt(String key) {
        return ESBT.integer(_props.get(key));
    }

    public long getLong(String key) {
        return ESBT.longInteger(_props.get(key));
    }

    public float getFloat(String key) {
        return ESBT.floatDecimal(_props.get(key));
    }

    public double getDouble(String key) {
        return ESBT.doubleDecimal(_props.get(key));
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return ESBT.bool(_props.get(key), defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        return ESBT.integer(_props.get(key), defaultValue);
    }

    public long getLong(String key, long defaultValue) {
        return ESBT.longInteger(_props.get(key), defaultValue);
    }

    public float getFloat(String key, float defaultValue) {
        return ESBT.floatDecimal(_props.get(key), defaultValue);
    }

    public double getDouble(String key, double defaultValue) {
        return ESBT.doubleDecimal(_props.get(key), defaultValue);
    }

    public boolean hasKey(String key) {
        return _props.containsKey(key);
    }

    public ESBProperties(String[] configs) {
        if (configs == null) {
            return;
        }

        for (String config : configs) {
            if (ESBT.isEmpty(config)) {
                continue;
            }
            //遍历config配置文件，记住每一个config
            Enumeration<URL> urls = null;
            try {
                //获取所有的配置文件
                urls = Thread.currentThread().getContextClassLoader().getResources(config);
            } catch (Throwable e) {
                continue;
            }

            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    Properties prop = new Properties();
                    try {
                        InputStream input = url.openStream();
                        prop.load(input);
                        input.close();
                    } catch (Throwable e) {
                        continue;
                    }

                    for (Object key : prop.keySet()) {
                        String strKey = key.toString();
                        if (!_props.containsKey(strKey)) {
                            _props.put(strKey, prop.getProperty(strKey));
                        }
                    }
                }
            }
        }
    }
}