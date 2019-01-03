package com.venus.esb.idl;

import com.alibaba.fastjson.annotation.JSONField;
import com.venus.esb.ESBSecurityLevel;
import com.venus.esb.annotation.ESBDesc;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by lingminjun on 17/8/11.
 * 用于ESB接口描述(IDL一部分)
 */
@ESBDesc("接口定义实体")
public final class ESBAPIDef implements Serializable {

    private static final long serialVersionUID = -2728823514971741755L;

    // 接口定义部分
    @ESBDesc("方法名")
    public String methodName;//方法名
    @ESBDesc("方法参数列表")
    public ESBAPIParam[] params; //参数列表
    @ESBDesc("方法返回值")
    public ESBReturnType returned;//返回值,所有返回值都将是复合类型【ESB规范】

    // 接口其他信息
    @ESBDesc("所属域")
    public String domain;//所属服务
    @ESBDesc("模块名")
    public String module;//所属服务模块 为空时,module.methodName
    @ESBDesc("负责人")
    public String owner;//接口负责人
    @ESBDesc("版本")
    public String version;//接口版本
    @ESBDesc("描述")
    public String desc;//方法描述/*512字*/
    @ESBDesc("详细描述")
    public String detail;//方法描述/*512字*/

    // 接口权限 0x8000,0000
    @ESBDesc("接口安全级别")
    public int security; //@See ESBSecurityLevel
//    public ESBSecurityLevel security;//安全级别,意味着验签和验权
    //兼容网关1.0 合作方调用级别 ESBSecurityLevel.Integrated是否让服务验签
    // false:验证由服务提供方完成
    // true:apigw负责签名验证
    @Deprecated
    public boolean needVerify = false;//

    // 所有对象struct定义描述 map<type,struct>
    @ESBDesc("接口包含所有的结构")
    public Map<String,ESBAPIStruct> structs;

    // 错误码 map<code,api_code>
    @ESBDesc("接口包含所有的错误码")
    public Map<String,ESBAPICode> codes;

    /**
     * 返回唯一API统一id
     * 采用.主要是方便后面url中非保留在不需要编译
     * @return
     */
    @JSONField(serialize = false, deserialize = false)
    public String getAPISelector() {
        if (module == null) {
            return "" + domain + "." + domain + "." + methodName;
        } else {
            return "" + domain + "." + module + "." + methodName;
        }
    }

    /**
     * 用于展示接口
     * @return
     */
    @JSONField(serialize = false, deserialize = false)
    public String getDisplayAPISelector() {
        if (module == null) {
            return domain + "." + methodName;
        } else {
            return module + "." + methodName;
        }
    }

    /**
     * 是否为open api接口
     * @return
     */
    public boolean isOpenAPI() {
        return ESBSecurityLevel.integrated.check(this.security);
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("\t * owner: " + owner + "\n");
        builder.append("\t * domain: " + domain + "\n");
        builder.append("\t * version: " + version + "\n");
        builder.append("\t * desc: " + desc + "\n");
        builder.append("\t * security: " + security + "\n");
        builder.append("\t * detail: " + detail + "\n");
        builder.append("\t */\n");
        builder.append("\tfunction " + (returned != null ? returned.getDisplayType() : "void") + " ");
        if (module == null) {
            builder.append(domain + "." + methodName + "(");
        } else {
            builder.append(module + "." + methodName + "(");
        }
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                ESBAPIParam param = params[i];
                if (i > 0) {
                    builder.append(", ");
                }
                if (param.required) {
                    builder.append("@ESBRequired ");
                }
                builder.append(param.getDisplayType() + " " + param.name);
                if (param.defaultValue != null && param.defaultValue.length() > 0) {
                    builder.append(" = " + param.defaultValue);
                }
            }
        }
        builder.append(");\n\r\n");

        //结构展示
        if (structs != null) {
            Iterator<Map.Entry<String, ESBAPIStruct>> entries = structs.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, ESBAPIStruct> entry = entries.next();
                ESBAPIStruct struct = entry.getValue();
                if (struct.desc != null) {
                    builder.append("\t/* " + struct.desc + " */\n");
                }
                builder.append("\tstruct " + entry.getKey() + " {\n");
                if (struct.fields != null) {
                    for (ESBAPIParam param : struct.fields) {
                        builder.append("\t\t" + param.getDisplayType() + " " + param.name + ";");
                        if (param.desc != null) {
                            builder.append(" /* " + param.desc + " */\n");
                        } else {
                            builder.append("\n");
                        }
                    }
                }
                builder.append("\t}\r\n\r\n");
            }
        }

        if (codes != null) {
            //codes { domain_code : {code,desc}}
            Iterator<Map.Entry<String, ESBAPICode>> entries = codes.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, ESBAPICode> entry = entries.next();
                builder.append("\t"+entry.getValue().toString()+"\n");//简单处理
            }
        }

        return builder.toString();
    }
}
