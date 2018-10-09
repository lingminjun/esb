package com.venus.esb.lang;

import com.venus.esb.annotation.ESBDesc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lingminjun on 17/4/29.
 */
@ESBDesc("扩展参数类型定义,因为框架不支持map")
public final class ESBCookieExts implements Serializable /*, JSONSerializable, ExtraProcessable*/ {
    private static final long serialVersionUID = -7840719301104982996L;
    @ESBDesc("所有cookie name")
    public List<String> keys = new ArrayList<String>();
    @ESBDesc("所有cookie")
    public List<ESBCookie> values = new ArrayList<ESBCookie>();

    public void put(String key, ESBCookie value) {
        int idx = keys.indexOf(key);
        if (idx >= 0 && idx < keys.size()) {
            values.remove(idx);
            values.add(idx,value);
        } else {
            keys.add(key);
            values.add(value);
        }
    }

    public ESBCookie get(String key) {
        int idx = keys.indexOf(key);
        if (idx >= 0 && idx < keys.size()) {
            return values.get(idx);
        } else {
            return null;
        }
    }

    public ESBCookie remove(String key) {
        int idx = keys.indexOf(key);
        if (idx >= 0 && idx < keys.size()) {
            keys.remove(idx);
            return values.remove(idx);
        }
        return null;
    }

    public int size() {
        return keys.size();
    }

    public Map<String,ESBCookie> map() {
        Map<String,ESBCookie> map = new HashMap<String, ESBCookie>();
        for (int idx = 0; idx < keys.size(); idx++) {
            map.put(keys.get(idx),values.get(idx));
        }
        return map;
    }
}
