package com.venus.esb.servlet.brave;

import com.github.kristofa.brave.ClientResponseAdapter;
import com.github.kristofa.brave.KeyValueAnnotation;
import com.github.kristofa.brave.ServerResponseAdapter;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by lmj on 17/9/1.
 * 客户端/服务端响应
 */
public class HTTPResponseAdapter implements ClientResponseAdapter,ServerResponseAdapter {

    private final boolean client;
    private final boolean success;
    private final String result;
    private final String errorMessage;

    public HTTPResponseAdapter(boolean client, boolean success, String var7, String errorMessage) {
        this.client = client;
        this.success = success;
        this.result = var7;
        this.errorMessage = errorMessage;
    }

    private String getPrefix() {
        if (client) {
            return "Client ";
        } else {
            return "Server ";
        }
    }

    @Override
    public Collection<KeyValueAnnotation> responseAnnotations() {
        List<KeyValueAnnotation> annotations = new ArrayList<KeyValueAnnotation>();
        if(!success){
            /*//上报统一压缩,不在参数处理
            if (errorMessage.length() >= com.venus.esb.brave.Constants.BRAVE_CONTENT_MAX_LENGTH) {//超过则做压缩存储
                String msg = GZIP.compressToBase64String(errorMessage,ESBConsts.UTF8_STR);
                annotations.add(KeyValueAnnotation.create(getPrefix() + "exception", msg));
                annotations.add(KeyValueAnnotation.create(getPrefix() + "exception_zip", "true"));
            } else {*/
                annotations.add(KeyValueAnnotation.create(getPrefix() + "exception", errorMessage));
            /*}*/
        } else {
            KeyValueAnnotation keyValueAnnotation=  KeyValueAnnotation.create(getPrefix() + "status","success");
            annotations.add(keyValueAnnotation);

            //参数处理
            if (client) {//客服端更好处理结果，故客户端处理一次即可
                if (result != null) {
                    String json = this.result;
                    /*//上报统一压缩,不在参数处理
                    if (json.length() >= com.venus.esb.brave.Constants.BRAVE_CONTENT_MAX_LENGTH) {//超过则做压缩存储
                        json = GZIP.compressToBase64String(json,ESBConsts.UTF8_STR);
                        annotations.add(KeyValueAnnotation.create("result", json));
                        annotations.add(KeyValueAnnotation.create("result_zip", "true"));
                    } else {*/
                        annotations.add(KeyValueAnnotation.create("result", json));
                    /*}*/
                } else {
                    annotations.add(KeyValueAnnotation.create("result", "null"));
                }
            }
        }
        return annotations;
    }

}
