package com.venus.esb;

import com.alibaba.dubbo.common.utils.LRUCache;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.venus.esb.ESBAPIInfo;
import com.venus.esb.config.ESBConfigCenter;
import com.venus.esb.lang.ESBConsts;
import com.venus.esb.utils.FileUtils;
import com.venus.esb.utils.MD5;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by lingminjun on 17/4/30.
 * 主要是实现api接口文件缓存,内存缓存采用LruCache实现,直接在ESB中
 */
public class ESBAPILoader implements ESB.APILoader {

    private String dir = "/home/admin/esb/apis/";//存储api配置地址

    public ESBAPILoader() {
        if (!StringUtils.isEmpty(ESBConfigCenter.instance().getApisDir())) {
            dir = ESBConfigCenter.instance().getApisDir();
            if (!dir.endsWith(File.separator)) {
                dir += File.separator;
            }
        }
    }

    private Map<String,ESBAPIInfo>  cache = new LRUCache<String, ESBAPIInfo>(ESBConfigCenter.instance().getApiCapacitySize() < 1000 ? 1000 : ESBConfigCenter.instance().getApiCapacitySize());

    @Override
    public ESBAPIInfo load(ESB esb, String selector) {

        //内存加载
        ESBAPIInfo info = getMemoryCacheAPI(selector);
        if (info != null) {
            return info;
        }

        return info;
    }

    @Override
    public void refresh(ESB esb, String selector) {
        //必须让其从远程加载
        removeFileCacheAPI(selector);
        removeMemoryCacheAPI(selector);
    }


    protected final ESBAPIInfo getMemoryCacheAPI(String selector) {
        return cache.get(selector);
    }

    protected final void saveMemoryCacheAPI(String selector, ESBAPIInfo info) {
        cache.put(selector,info);
    }

    protected final void removeMemoryCacheAPI(String selector) {
        cache.remove(selector);
    }

    protected final ESBAPIInfo getFileCacheAPI(String selector) {
        ESBAPIInfo api = null;

        String path = path(selector);
        if (StringUtils.isEmpty(path)) {
            return api;
        }

        try {
            String json = FileUtils.readFile(path, ESBConsts.UTF8);
            if (!StringUtils.isEmpty(json)) {
                api = JSON.parseObject(json,ESBAPIInfo.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return api;
    }

    protected final void saveFileCacheAPI(String selector, ESBAPIInfo info) {
        String path = path(selector);
        if (StringUtils.isEmpty(path)) {
            return;
        }
        if (info != null && info.api != null && info.api.getAPISelector() != null && info.api.getAPISelector().equals(selector)) {
            try {
                String json = JSON.toJSONString(info, ESBConsts.FASTJSON_SERIALIZER_FEATURES);
                if (!StringUtils.isEmpty(json)) {
                    FileUtils.writeFile(path,json,ESBConsts.UTF8);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected final void removeFileCacheAPI(String selector) {
        String path = path(selector);
        if (StringUtils.isEmpty(path)) {
            return;
        }
        try {
            FileUtils.deleteFile(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String path(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }

        String md5 = MD5.md5(key);
        if (StringUtils.isEmpty(md5)) {
            return null;
        }

        //二级目录,有利于存储更多
        String sub0 = md5.substring(0, 2);
        String sub1 = md5.substring(2, 4);

        String finder = this.dir + sub0 + File.separator + sub1;

        File file = new File(finder);
        if (!file.exists()) {//判断文件夹是否存在,如果不存在则创建文件夹
            file.mkdirs();
        }

        return finder + File.separator + md5;
    }
}
