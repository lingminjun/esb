package com.venus.esb.helper;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.venus.esb.ESBAPIInfo;
import com.venus.esb.ESBInvocation;
import com.venus.esb.ESBSecurityLevel;
import com.venus.esb.annotation.*;
import com.venus.esb.dubbo.ESBDubboMethod;
import com.venus.esb.idl.ESBAPICode;
import com.venus.esb.idl.ESBAPIDef;
import com.venus.esb.idl.ESBAPIParam;
import com.venus.esb.idl.ESBAPIStruct;
import com.venus.esb.lang.ESBField;
import com.venus.esb.servlet.ESBHTTPMethod;
import com.venus.esb.utils.HTTP;
import com.venus.esb.utils.JavaCodeAssist;
import com.venus.esb.lang.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
import sun.reflect.generics.reflectiveObjects.TypeVariableImpl;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.sql.Date;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by lingminjun on 17/2/9.
 * 方便从java方法+注解定义来生产ESBAPIInfo
 */
public class ESBAPIHelper {

    static HashMap<String,ESBPOJOWrapper> pojos = new HashMap<String, ESBPOJOWrapper>();

    public static List<ESBAPIInfo> generate(Class<?> serverProvider, String baseUrl, boolean ignoreError) {
        if (serverProvider == null ) {
            throw new RuntimeException("请务必输入正确的serverProvider和methodName");
        }

        boolean isServlet = isServletController(serverProvider);
        if (isServlet && (baseUrl == null || baseUrl.length() == 0)) {
            throw new RuntimeException("当前是SpringMVC或者SpringBoot框架，请务必设置baseUrl");
        }

        if (isServlet) {
            RequestMapping mapping = serverProvider.getAnnotation(RequestMapping.class);
            baseUrl = togetherUrlPath(baseUrl,getPathFromRequestMapping(mapping,null,null,null,null));
        }

        List<ESBAPIInfo> list = new ArrayList<ESBAPIInfo>();
        Method[] methods = serverProvider.getMethods();
        for (Method method : methods) {
            //排出静态方法
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            ESBAPIInfo info = null;
            try {
                info = generate(serverProvider,method,null,null,null,null,null,null,isServlet,baseUrl);
            } catch (Throwable e) {
                e.printStackTrace();
                if (!ignoreError) {
                    throw new RuntimeException(e);
                }
            }

            if (info != null) {
                list.add(info);
            }
        }

        return list;
    }

    public static ESBAPIInfo generate(Class<?> serverProvider,
                                      String methodName,
                                      String apiDomain,
                                      String apiModule,
                                      String apiName,
                                      String apiDesc,
                                      String apiOwner,
                                      String apiDetail,
                                      String baseUrl) {
        if (serverProvider == null || StringUtils.isEmpty(methodName)) {
            throw new RuntimeException("请务必输入正确的serverProvider和methodName");
        }

        boolean isServlet = isServletController(serverProvider);
        if (isServlet && (baseUrl == null || baseUrl.length() <= 0)) {
            throw new RuntimeException("当前是SpringMVC或者SpringBoot框架，请务必设置baseUrl");
        }

        Method[] methods = serverProvider.getMethods();
        Method theMethod = null;
        for (Method method : methods) {
            //排出静态方法
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            if (method.getName().equals(methodName)) {
                theMethod = method;
                break;
            }
        }

        if (isServlet) {
            RequestMapping mapping = serverProvider.getAnnotation(RequestMapping.class);
            baseUrl = togetherUrlPath(baseUrl,getPathFromRequestMapping(mapping,null,null,null,null));
        }
        return generate(serverProvider,theMethod,apiDomain,apiModule,apiName,apiDesc,apiOwner,apiDetail,isServlet,baseUrl);
    }

    private static ESBAPIInfo generate(Class<?> serverProvider,
                                       Method method,
                                       String apiDomain,
                                       String apiModule,
                                       String apiName,
                                       String apiDesc,
                                       String apiOwner,
                                       String apiDetail,
                                       boolean isServlet,
                                       String baseUrl) {
        if (serverProvider == null || method == null) {
            throw new RuntimeException("请务必输入正确的serverProvider和methodName");
        }

        if (isServlet) {
            RequestMapping mapping = method.getAnnotation(RequestMapping.class);
            if (mapping == null) {//必须是servlet入口才行
                return null;
            }
        }

        if (!isServlet && !serverProvider.isInterface()) {
            throw new RuntimeException("请务必输入正确的serverProvider接口类");
        }

        String methodName = method.getName();

        ESBGroup group = serverProvider.getAnnotation(ESBGroup.class);
        ESBAPI esbapi = method.getAnnotation(ESBAPI.class);

        //必要参数整理
        apiDomain = tidyDomainString(serverProvider, apiDomain, group);
        apiModule = tidyAPIModule(apiModule,esbapi,apiDomain);
        apiName = tidyAPIName(methodName, apiName, esbapi);

        if (!verifyDomain(apiDomain) || !verifyDomain(apiModule) || !verifyAPIName(apiName)) {
            throw new RuntimeException("请务必输入正确的apiDomain、apiModule和apiName");
        }

        //api对象
        ESBAPIInfo esbapiInfo = new ESBAPIInfo();
        esbapiInfo.createAt = System.currentTimeMillis();
        esbapiInfo.modifyAt = esbapiInfo.createAt;

        //IDL部分
        esbapiInfo.api = new ESBAPIDef();
        esbapiInfo.api.domain = apiDomain;
        esbapiInfo.api.module = apiModule;
        esbapiInfo.api.methodName = apiName;
        esbapiInfo.api.desc = apiDesc;
        esbapiInfo.api.detail = apiDetail;
        esbapiInfo.api.owner = apiOwner;

        //填充其他属性
        fillAPIInfo(apiName, group, esbapi, esbapiInfo.api);

        //异常码部分【文档需要,不影响接口调用,所以未找到则忽略】
        parseCodes(apiDomain, group, method.getAnnotation(ESBError.class), esbapiInfo);

        //返回值处理
        parseReturnType(method,serverProvider,esbapiInfo);

        //自动生成
        if (isServlet) {// http 接口
            parseHttpInvocation(esbapiInfo, method, methodName, serverProvider, baseUrl);
        } else {// dubbo 接口
            parseDubboInvocation(esbapiInfo, method, methodName, serverProvider);
        }

        return esbapiInfo;
    }

    //判断是否为servlet
    public static boolean isServletController(Class<?> serverProvider) {
        if (serverProvider == null) {
            throw new RuntimeException("请务必输入正确的serverProvider和methodName");
        }

        //支持rest
        Controller controller = serverProvider.getAnnotation(Controller.class);
        if (controller != null) {
            return true;
        }
        RestController restController = serverProvider.getAnnotation(RestController.class);
        if (restController != null) {
            return true;
        }

        RequestMapping mapping = serverProvider.getAnnotation(RequestMapping.class);
        if (mapping != null) {
            return true;
        }

        Method[] methods = serverProvider.getMethods();
        Method theMethod = null;
        for (Method method : methods) {
            //排出静态方法
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }

            RequestMapping methhodMapping = method.getAnnotation(RequestMapping.class);
            if (mapping != null) {
                return true;
            }

            GetMapping mapping1 = method.getAnnotation(GetMapping.class);
            if (mapping1 != null) {
                return true;
            }

            PostMapping mapping2 = method.getAnnotation(PostMapping.class);
            if (mapping2 != null) {
                return true;
            }

            PutMapping mapping3 = method.getAnnotation(PutMapping.class);
            if (mapping3 != null) {
                return true;
            }

            DeleteMapping mapping4 = method.getAnnotation(DeleteMapping.class);
            if (mapping4 != null) {
                return true;
            }
        }

        return false;
    }

    private static void parseDubboInvocation(ESBAPIInfo esbapiInfo, Method method, String methodName, Class<?> serverProvider) {

        ESBDubboMethod dubboMethod = new ESBDubboMethod();
        dubboMethod.dubbo = serverProvider.getName();
        dubboMethod.method = methodName;

        //最后处理参数部分
        parseParameterTypes(esbapiInfo,dubboMethod.params,method,serverProvider,false);

        //最后转换出 dubbo的方法
        esbapiInfo.invocations = new HashMap<String, ESBInvocation>();
        ESBInvocation invocation = dubboMethod.getInvocation();
        esbapiInfo.invocations.put(invocation.getMD5(),invocation);

    }

    private static void parseHttpInvocation(ESBAPIInfo esbapiInfo, Method method, String methodName, Class<?> serverProvider, String baseUrl) {

        RequestMapping mapping = method.getAnnotation(RequestMapping.class);
        GetMapping mapping1 = method.getAnnotation(GetMapping.class);
        PostMapping mapping2 = method.getAnnotation(PostMapping.class);
        PutMapping mapping3 = method.getAnnotation(PutMapping.class);
        DeleteMapping mapping4 = method.getAnnotation(DeleteMapping.class);
        if (mapping == null
                && mapping1 == null
                && mapping2 == null
                && mapping3 == null
                && mapping4 == null) {
            throw new RuntimeException("servlet请求，必须有RequestMapping注解");
        }

        ESBHTTPMethod httpMethod = new ESBHTTPMethod();
        httpMethod.url = togetherUrlPath(baseUrl,getPathFromRequestMapping(mapping,mapping1,mapping2,mapping3,mapping4));
        httpMethod.version = "1.1";
        httpMethod.method = getMethodFromRequestMapping(mapping,mapping1,mapping2,mapping3,mapping4);

        //最后处理参数部分 SpringMVC会自动聚合ModelAttribute，所以参数必须分开处理
        parseParameterTypes(esbapiInfo,httpMethod.params,method,serverProvider,true);

        //最后转换出 http的方法
        esbapiInfo.invocations = new HashMap<String, ESBInvocation>();
        ESBInvocation invocation = httpMethod.getInvocation();
        esbapiInfo.invocations.put(invocation.getMD5(),invocation);

    }

    private static HTTP.Method getMethodFromRequestMapping(RequestMapping mapping, GetMapping getMapping, PostMapping postMapping, PutMapping putMapping, DeleteMapping deleteMapping) {
        if (mapping != null && mapping.method() != null && mapping.method().length > 0) {
            for (RequestMethod requestMethod : mapping.method()) {
                if (requestMethod == RequestMethod.GET) {
                    return HTTP.Method.GET;
                } else if (requestMethod == RequestMethod.POST) {
                    return HTTP.Method.POST;
                } else if (requestMethod == RequestMethod.PUT) {
                    return HTTP.Method.PUT;
                } else if (requestMethod == RequestMethod.DELETE) {
                    return HTTP.Method.DELETE;
                }
            }
            return HTTP.Method.GET;
        } else if (getMapping != null) {
            return HTTP.Method.GET;
        } else if (postMapping != null) {
            return HTTP.Method.POST;
        } else if (putMapping != null) {
            return HTTP.Method.PUT;
        } else if (deleteMapping != null) {
            return HTTP.Method.DELETE;
        }
        return HTTP.Method.GET;
    }

    private static String getPathFromRequestMapping(RequestMapping mapping, GetMapping getMapping, PostMapping postMapping, PutMapping putMapping, DeleteMapping deleteMapping) {
        if (mapping != null && mapping.path() != null && mapping.path().length > 0) {
            return mapping.path()[0];
        } else if (mapping != null && mapping.value() != null && mapping.value().length > 0) {
            return mapping.value()[0];
        } else if (getMapping != null && getMapping.path() != null && getMapping.path().length > 0) {
            return getMapping.path()[0];
        } else if (getMapping != null && getMapping.value() != null && getMapping.value().length > 0) {
            return getMapping.value()[0];
        } else if (postMapping != null && postMapping.path() != null && postMapping.path().length > 0) {
            return postMapping.path()[0];
        } else if (postMapping != null && postMapping.value() != null && postMapping.value().length > 0) {
            return postMapping.value()[0];
        } else if (putMapping != null && putMapping.path() != null && putMapping.path().length > 0) {
            return putMapping.path()[0];
        } else if (putMapping != null && putMapping.value() != null && putMapping.value().length > 0) {
            return putMapping.value()[0];
        } else if (deleteMapping != null && deleteMapping.path() != null && deleteMapping.path().length > 0) {
            return deleteMapping.path()[0];
        } else if (deleteMapping != null && deleteMapping.value() != null && deleteMapping.value().length > 0) {
            return deleteMapping.value()[0];
        }
        return "";
    }

    private static String togetherUrlPath(String left,String right) {
        if (right == null || right.length() == 0) {
            return left;
        }
        if (left.endsWith("/")) {
            if (right.startsWith("/")) {
                return left + right.substring(1);
            } else {
                return left + right;
            }
        } else {
            if (right.startsWith("/")) {
                return left + right;
            } else {
                return left + "/" + right;
            }
        }
    }

    /**
     * 加载codes
     * @param group
     */
    public static Map<Integer,ESBAPICode> loadCodes(String domain, ESBGroup group) {
//        String apiDomain = domain;
//        if (apiDomain == null || apiDomain.length() == 0) {
//            apiDomain = group.domain();
//        }

        Class<?> codeDefineClazz = group.codeDefine();
        if (codeDefineClazz == null) {
            return new HashMap<Integer, ESBAPICode>();
        }

        return loadCodes(codeDefineClazz,null);
    }
    /**
     * 加载codes
     * @param customDomain
     */
    public static Map<Integer,ESBAPICode> loadCodes(Class<?> codeDefineClazz, String customDomain) {
        HashMap<Integer,ESBAPICode> map = codesDefinedCache.get(codeDefineClazz.getName());
        if (map == null) {
            map = new HashMap<Integer, ESBAPICode>();
            //获取所有变量的值
            Field[] fields = codeDefineClazz.getFields();
            for (Field field : fields) {
                if (field.getType() != int.class) {
                    continue;
                }

                String name = field.getName();
                //错误码的定义不符合规范,建议检查
                if (!name.endsWith("_CODE")) {
                    System.out.println("错误码命名定义不符合规范,请检查" +
                            codeDefineClazz.getName() +".java文件中对" +
                            name + "的定义。必须以_CODE结尾。");
                    continue;
                }
                String methodName = name.substring(0,name.length() - "_CODE".length());

                ESBException exception = null;
                //命名
                try {
                    Method mtd = codeDefineClazz.getMethod(methodName,String.class);

                    exception = (ESBException)mtd.invoke(codeDefineClazz,"");
                } catch (NoSuchMethodException e) {
                    System.out.println("错误码build方法定义不符合规范,请检查" +
                            codeDefineClazz.getName() +".java文件,命名需与" +
                            name + "对应,应该定义为: public static ESBException " +
                            methodName + "(String reason);");
                    continue;
                } catch (InvocationTargetException e) {
                    System.out.println("错误码build方法定义不符合规范,请检查" +
                            codeDefineClazz.getName() +".java文件中对" +
                            methodName + "的定义。");
                    continue;
                } catch (IllegalAccessException e) {
                    System.out.println("错误码build方法定义不符合规范,请检查" +
                            codeDefineClazz.getName() +".java文件中对" +
                            methodName + "的定义。");
                    continue;
                }

                if (map.get(exception.getCode()) != null) {
                    System.out.println("错误码定义重复,请检查" +
                            codeDefineClazz.getName() +".java文件中对" +
                            name + "的定义,有重复的定义错误码:" + exception.getCode());
                }

                //名字命名: domain_codeName_code
//                exception.name = apiDomain + "_" + methodName + "_" + exception.getCode();
                ESBAPICode code = new ESBAPICode();
                if (name == null || name.length() == 0) {
                    if (exception.getCode() < 0) {
                        code.name = "_SYS_" + (-exception.getCode());
                    } else {
                        code.name = "_" + exception.getCode();
                    }
                } else {
                    if (exception.getCode() < 0) {
                        code.name = name + "_SYS_" + (-exception.getCode());
                    } else {
                        code.name = name + "_" + exception.getCode();
                    }
                }
                //code.name = (name == null || name.length() == 0) ? ("" + exception.getCode()) : (name + "_" + exception.getCode()); //仅仅描述需要
                code.code = exception.getCode();
                code.desc = exception.getMessage();
                code.domain = (customDomain == null || customDomain.length() == 0) ? exception.getDomain() : customDomain;
                map.put(exception.getCode(),code);
            }

            codesDefinedCache.put(codeDefineClazz.getName(),map);
        }

        return map;
    }

    /**
     * 此处小范围的缓存,以免不断的反射错误码定义类
     * @param group
     * @param error
     * @param esbapiInfo
     */
    private static void parseCodes(String apiDomain, ESBGroup group, ESBError error, ESBAPIInfo esbapiInfo) {
        if (error == null || error.value() == null || error.value().length == 0) {
            return;
        }

        if (group == null || group.codeDefine() == null) {
            return;
        }

        Class<?> codeDefineClazz = group.codeDefine();
        Map<Integer,ESBAPICode> map = loadCodes(error.value(),codeDefineClazz);
        if (map.size() > 0 && esbapiInfo.api.codes == null) {
            esbapiInfo.api.codes = new HashMap<String, ESBAPICode>();
        }

        for (ESBAPICode code : map.values()) {
            esbapiInfo.api.codes.put(code.getCodeId(),code);
        }

        //其他的错误码
        for (int i = 0; i < error.codes().length; i++ ) {
            ESBCode extCode = error.codes()[i];
            Map<Integer,ESBAPICode> extMap = loadCodes(extCode.value(),extCode.codeDefine());
            if (extMap.size() > 0 && esbapiInfo.api.codes == null) {
                esbapiInfo.api.codes = new HashMap<String, ESBAPICode>();
            }
            for (ESBAPICode code : extMap.values()) {
                esbapiInfo.api.codes.put(code.getCodeId(),code);
            }
        }
    }


    private static Map<Integer,ESBAPICode> loadCodes(int[] codes, Class<?> codeDefineClazz) {
        Map<Integer,ESBAPICode> cds = new HashMap<Integer, ESBAPICode>();
        Map<Integer,ESBAPICode> map = loadCodes(codeDefineClazz,null);
        //遍历
        for (int i = 0; i < codes.length; i++) {
            ESBAPICode code = map.get(codes[i]);
            if (code == null) {
                System.out.println("未在" +
                        codeDefineClazz.getName() +
                        ".java文件中找到错误码" + codes[i] +
                        "的定义");
            } else {
                cds.put(code.code,code);
            }
        }
        return cds;
    }


    //按文件存储错误码
    private static HashMap<String,HashMap<Integer,ESBAPICode>> codesDefinedCache = new HashMap<String, HashMap<Integer, ESBAPICode>>();

    private static void fillAPIInfo(String apiName, ESBGroup group, ESBAPI esbapi, ESBAPIDef apiDef) {
        //owner
        if (StringUtils.isEmpty(apiDef.owner) && esbapi != null) {
            apiDef.owner = esbapi.owner();
        }
        if (StringUtils.isEmpty(apiDef.owner) && group != null) {
            apiDef.owner = group.owner();
        }

        //desc
        if (StringUtils.isEmpty(apiDef.desc) && esbapi != null) {
            apiDef.desc = esbapi.desc();
        }

        //detail
        if (StringUtils.isEmpty(apiDef.detail) && esbapi != null) {
            apiDef.detail = esbapi.detail();
        }

        //只有注解形成的接口,才去配安全级别
        if (esbapi != null && apiName.equals(esbapi.name())) {
            ESBSecurityLevel securityLevel = esbapi.security();
            if (securityLevel != null) {
                apiDef.security = securityLevel.authorize(0);
            }
            apiDef.needVerify = esbapi.needVerify();
        }
    }

    private static String tidyAPIName(String methodName, String apiName, ESBAPI esbapi) {
        //参数优先
        if (!StringUtils.isEmpty(apiName)) {
            return apiName;
        }

        //注解
        if (esbapi != null) {
            return esbapi.name();
        }

        //方法名
        return methodName;
    }

    private static String tidyAPIModule(String apiModule, ESBAPI esbapi, String apiDomain) {
        //参数优先
        if (!StringUtils.isEmpty(apiModule)) {
            return apiModule;
        }

        //注解
        if (esbapi != null && esbapi.module() != null && esbapi.module().length() > 0) {
            return esbapi.module();
        }

        //方法名
        return apiDomain;
    }

    private static String tidyDomainString(Class<?> serverProvider, String apiDomain, ESBGroup group) {
        //首先使用输入参数作为最后module
        if (!StringUtils.isEmpty(apiDomain)) {
            return apiDomain;
        }

        //取配置
        if (group != null) {
            return group.domain();
        }

        //取包名中合适的服务名,最好是取root工程的artifactId
        //此处简单处理,一个原则,取第三位或者第四位:com.venus.xxxx.xxxxClass
        //com.venus.dubbo.demo.inferface.GenericService
        String[] strs = serverProvider.getName().split("\\.");
        if (strs.length > 4) {
            apiDomain = strs[2];
            //排出掉通用词
            if (apiDomain.contains("parent")
                    || apiDomain.contains("group")
                    || apiDomain.contains("dubbo")
                    || apiDomain.contains("module")
                    || apiDomain.contains("domain")
                    || apiDomain.contains("server")
                    || apiDomain.contains("service")
                    || apiDomain.contains("client")
                    ) {
                apiDomain = strs[3];
            }
        } else if (strs.length == 4) {
            apiDomain = strs[2];
        } else if (strs.length > 2){
            apiDomain = strs[strs.length - 2];
        } else {
            //报错,此处必须设置domain
            apiDomain = "server";
        }
        return apiDomain;
    }

    /**
     * 解析对象到struct中
     * @param clazz
     * @param structs 必传参数
     */
    public static void parseObjectType(Class<?> clazz, Map<String, ESBAPIStruct> structs) {
        //基础类型忽略,不要再放进来
        if (isBaseType(clazz)) {
            return;
        }

        ESBPOJOWrapper tempPojo = new ESBPOJOWrapper();
        ESBDesc desc = clazz.getAnnotation(ESBDesc.class);
        if (desc != null) {
            tempPojo.desc = desc.value();
        }

        tempPojo.setTypeClass(clazz);

        //将解析过的对象记录下来(必须提前放入,因为要防止属性递归依赖本身类)
        String coreType = tempPojo.getCoreType();
        ESBPOJOWrapper pojo = pojos.get(coreType);
        if (pojo == null) {
            pojo = tempPojo;
            pojos.put(tempPojo.getCoreType(),pojo);
            //从类型遍历所有属性
            parsePOJOFields(pojo, pojo.getTypeClass(), structs);
        }

        savePojoToAPI(pojo, structs);
    }


    private static void parseReturnType(Method method, Class dubboServiceClazz, ESBAPIInfo esbapiInfo) {
        ESBPOJOWrapper tempReturnedPOJO = new ESBPOJOWrapper();

        Class<?> returnType = method.getReturnType();

        //返回结果需要装箱处理(统一接口规范)
        if (ESBRawString.class == returnType) {//RawString
            tempReturnedPOJO.setTypeClass(ESBRawString.class);//取值的时候会自动解包
        } else if (String.class == returnType) {//
            tempReturnedPOJO.setTypeClass(ESBString.class);
        } else if (String[].class == returnType) {//不适用ObjectArrayResp，考虑到代码生成的时候会生成重复代码
            tempReturnedPOJO.setTypeClass(ESBStringArray.class);
        } else if (boolean.class == returnType || Boolean.class == returnType) {
            tempReturnedPOJO.setTypeClass(ESBBoolean.class);
        } else if (boolean[].class == returnType || Boolean[].class == returnType) {
            tempReturnedPOJO.setTypeClass(ESBBooleanArray.class);
        } else if (byte.class == returnType || Byte.class == returnType) {
            tempReturnedPOJO.setTypeClass(ESBNumber.class);
        } else if (short.class == returnType || Short.class == returnType) {
            tempReturnedPOJO.setTypeClass(ESBNumber.class);
        } else if (char.class == returnType || Character.class == returnType) {
            tempReturnedPOJO.setTypeClass(ESBNumber.class);
        } else if (int.class == returnType || Integer.class == returnType) {
            tempReturnedPOJO.setTypeClass(ESBNumber.class);
        } else if (byte[].class == returnType || Byte[].class == returnType) {
            tempReturnedPOJO.setTypeClass(ESBNumberArray.class);
        } else if (short[].class == returnType || Short[].class == returnType) {
            tempReturnedPOJO.setTypeClass(ESBNumberArray.class);
        } else if (char[].class == returnType || Character[].class == returnType) {
            tempReturnedPOJO.setTypeClass(ESBNumberArray.class);
        } else if (int[].class == returnType || Integer[].class == returnType) {
            tempReturnedPOJO.setTypeClass(ESBNumberArray.class);
        } else if (long.class == returnType || Long.class == returnType) {
            tempReturnedPOJO.setTypeClass(ESBLong.class);
        } else if (long[].class == returnType || Long[].class == returnType) {
            tempReturnedPOJO.setTypeClass(ESBLongArray.class);
        } else if (double.class == returnType || Double.class == returnType) {
            tempReturnedPOJO.setTypeClass(ESBDouble.class);
        } else if (float.class == returnType || Float.class == returnType) {
            tempReturnedPOJO.setTypeClass(ESBFloat.class);
        } else if (double[].class == returnType || Double[].class == returnType) {
            tempReturnedPOJO.setTypeClass(ESBDoubleArray.class);
        } else if (float[].class == returnType || Float[].class == returnType) {
            tempReturnedPOJO.setTypeClass(ESBFloatArray.class);
            /*
        } else if (JSONString.class == apiInfo.returned) {
            apiInfo.serializer = Serializer.jsonStringSerializer;
            apiInfo.wrapper = ResponseWrapper.objectWrapper;
        } else if (ESBRawString.class == returned) {
            tempReturnedPOJO.setTypeClass(ESBRawString.class);
        } else if (net.pocrd.responseEntity.RawString.class == apiInfo.returned) {
            //TODO remove,RawString不应对外暴露
            apiInfo.serializer = Serializer.deprecatedRawStringSerializer;
            apiInfo.wrapper = ResponseWrapper.objectWrapper;
            */
        } else if (Collection.class.isAssignableFrom(returnType)) {
            //增加对Collection自定义Object的支持+List<String>的支持
            Class<?> genericClazz;
            try {
                genericClazz = getListActuallyGenericType(returnType,method.getGenericReturnType(),null);
            } catch (Exception e) {
                throw new RuntimeException("generic type load failed:" + method.getGenericReturnType() + " in " + dubboServiceClazz.getName() + " method name:" + method.getName(), e);
            }

            //不支持的泛型对象
            if (genericClazz == null) {
                throw new RuntimeException("generic type load failed:" + method.getGenericReturnType() + " in " + dubboServiceClazz.getName() + " method name:" + method.getName());
            }

            if (String.class == genericClazz) {//如果要支持更多的jdk中已有类型的序列化
                tempReturnedPOJO.setTypeClass(ESBStringArray.class);
            }
            else {//需要被包装起来
                tempReturnedPOJO.setTypeClass(genericClazz);
                tempReturnedPOJO.isList = true;
            }
        } else if (returnType.isArray()) {//
            throw new RuntimeException("unsupported return type, method name:" + method.getName());
        } else {
            tempReturnedPOJO.setTypeClass(returnType);
        }

        ESBDesc desc = tempReturnedPOJO.getTypeClass().getAnnotation(ESBDesc.class);
        if (desc != null) {
            tempReturnedPOJO.desc = desc.value();
        }

        //
        Map<String,ESBAPIStruct> structs = new HashMap<>();
        if (esbapiInfo != null && esbapiInfo.api != null) {
            //防止重复装载
            if (esbapiInfo.api.structs != null) {
                structs = esbapiInfo.api.structs;
            } else {
                esbapiInfo.api.structs = structs;
            }
        }

        //将解析过的对象记录下来(必须提前放入,因为要防止属性递归依赖本身类)
        String finalType = tempReturnedPOJO.getFinalType();
        ESBPOJOWrapper returned = pojos.get(finalType);
        if (returned == null) {
            returned = tempReturnedPOJO;
            pojos.put(finalType,tempReturnedPOJO);

            //从类型遍历所有属性

            parsePOJOFields(tempReturnedPOJO, tempReturnedPOJO.getTypeClass(), structs);
        }

        //记录到esbapiInfo中
        esbapiInfo.api.returned = returned.getReturnType();

        //没有什么规则
//        esbapiInfo.rules = new HashMap<String, ESBRuleNode>();
//        esbapiInfo.rules.put("this",new ESBRuleNode());

        //将pojos转struct
        savePojoToAPI(returned,structs);
    }

    private static void parseParameterTypes(ESBAPIInfo esbapiInfo, List<ESBField> methodParams, Method method, Class service, boolean isServlet) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        //所有参数是否都被注解记录

        if (!isServlet && (parameterTypes.length != parameterAnnotations.length)) {
            throw new RuntimeException("存在未被标记的dubbo api参数" + service.getName());
//            fieldNames = JavassistMethodUtils.getMethodParameterNamesByJavassist(method);
        }

        if (parameterTypes.length == 0) {
            throw new RuntimeException("不支持没有参数的dubbo或者servlet方法" + service.getName());
        }

        //记录所有数据结构
        Map<String,ESBAPIStruct> structs = new HashMap<>();
        if (esbapiInfo != null && esbapiInfo.api != null) {
            //防止重复装载
            if (esbapiInfo.api.structs != null) {
                structs = esbapiInfo.api.structs;
            } else {
                esbapiInfo.api.structs = structs;
            }
        }

        //记录参数,主要是通过注解标示的参数名,方便从http接口中获取对应参数
//        esbapiInfo.params = new ESBField[parameterTypes.length];
        List<ESBFieldDesc> invocationParams = new ArrayList<ESBFieldDesc>();
        List<ESBFieldDesc> params = new ArrayList<ESBFieldDesc>();

        String[] fieldNames = null;
        boolean useCompatibility = false;
        for (int i = 0; i < parameterTypes.length; i++) {
            ESBFieldDesc field = new ESBFieldDesc();
//            esbapiInfo.params[i] = field;

            //需要判断是否为泛型类型
            Class<?> type = parameterTypes[i];
            field.setTypeClass(type);

            Class<?> genericClazz = null;
            //入参的递归检查 对于泛型map,array不支持切记,仅仅支持list
            if (Collection.class.isAssignableFrom(type)) {//增加对List自定义Object的支持+List<String>的支持,此处可以支持set
                try {
                    genericClazz = getListActuallyGenericType(type,method.getGenericParameterTypes()[i],null);
                } catch (Exception e) {
                    throw new RuntimeException("generic type unsupported:" + method.getGenericParameterTypes()[i] + " in interface:" + service.getName() + " method:" + method.getName(), e);
                }

                if (genericClazz != null) {
                    field.setTypeClass(genericClazz);
                    field.isList = true;
                } else {
                    throw new RuntimeException("only list is support when using collection。in interface:" + service.getName() + " method:" + method.getName());
                }
            } else if (type.isArray()) {//不同类型的array需要支持
                if (isBaseType(type)) {
                    field.isArray = true;
                } else {
                    throw new RuntimeException("only base type array is support when using array。in interface:" + service.getName() + " method:" + method.getName());
                }
            } else {
                //如果参数是非List的容器类型,直接报错
                if (isContainerType(type)) {
                    throw new RuntimeException("only list is support when using collection。in interface:" + service.getName() + " method:" + method.getName());
                }
            }

            int j = 0;
            boolean ignore = false;
            Annotation[] a = parameterAnnotations[i];
            for (j = 0; a != null && j < a.length; j++) {
                Annotation n = a[j];
                if (n.annotationType() == PathVariable.class) {
                    ignore = false;
                    PathVariable p = (PathVariable) n;
                    field.desc = "PathVariable 参数";
                    field.required = p.required();
                    field.isQuiet = false;
                    field.name = p.name() != null && p.name().length() > 0 ? p.name() : p.value();
                    field.isPathVariable = true;
                    break;
                } else if (n.annotationType() == RequestParam.class) {
                    ignore = false;
                    RequestParam p = (RequestParam) n;
                    field.desc = "RequestParam 参数";
                    field.required = p.required();
                    field.isQuiet = false;
                    field.name = p.name() != null && p.name().length() > 0 ? p.name() : p.value();
                    field.defaultValue = p.defaultValue().trim();
                    break;
                } else if (n.annotationType() == RequestAttribute.class) {
                    ignore = true;
                    //忽略，此参数并不对外
                } else if (n.annotationType() == ModelAttribute.class) {
                    //此参数必须拆分
                    if (ESBT.isBaseType(type)) {
                        ignore = false;
                        ModelAttribute p = (ModelAttribute) n;
                        field.desc = "ModelAttribute 参数";
                        field.required = false;
                        field.isQuiet = false;
                        field.name = p.name() != null && p.name().length() > 0 ? p.name() : p.value();
                    } else {
                        ignore = true;
                        parseModelAttributeParameters(type, invocationParams, params);
                        break;
                    }
                } else if (n.annotationType() == RequestBody.class) {
                    ignore = false;
                    RequestBody p = (RequestBody) n;
                    field.desc = "RequestBody 参数";
                    field.required = p.required();
                    field.isQuiet = false;
                    field.name = "";
                    field.isRequestBody = true;
//                    if (ESBT.isBaseType(type)) {
//                        field.name = "body";//默认名称，只能有一个
//                    } else {//默认规则，取类型小写
//                        field.name = type.getSimpleName().toLowerCase();
//                    }
                    break;
                } else if (n.annotationType() == ESBParam.class) {//最后拿到注解
                    ignore = false;
                    ESBParam p = (ESBParam) n;
                    field.desc = p.desc();
                    field.required = p.required();
                    field.isQuiet = p.quiet();
                    field.name = p.name();
                    field.autoInjected = p.autoInjected();
                    field.defaultValue = p.defaultValue();
                    break;
                }
            }

            // 针对没有被 ModelAttribute 修饰的复杂对象参数，必须采用默认平铺方式加载
            if (isServlet && !ignore && !ESBT.isBaseType(type) && !field.isRequestBody) {
                ignore = true;
                parseModelAttributeParameters(type, invocationParams, params);
            }

            if (ignore) {
                continue;
            }

            //context类型特殊处理,一定要注入
            if (ESBContext.class == type) {
                if (field.name == null) {
                    field.name = "_context";
                }
                field.required = true;
                field.desc = "上下文参数";
                field.autoInjected = true;
            }

            if (StringUtils.isEmpty(field.name)) {

                //先用兼容方案修复
                if (!useCompatibility) {
                    useCompatibility = true;
                    fieldNames = JavaCodeAssist.methodParamNames(service,method.getName(),parameterTypes.length);
                    System.out.println("WARNING:务必采用注解的方式来修饰要暴露的接口参数,暂时采用兼容方案");
                }

                if (fieldNames != null) {
                    field.name = fieldNames[i];
                }
            }

            if (StringUtils.isEmpty(field.name)) {
                throw new RuntimeException("api参数未被标记" + method.getName() + " in " + service.getName());
            }

            //注入的说明IDL层不需要暴露，标准参数就是自动注入
            if (!field.autoInjected && !ESBSTDKeys.isSTDKey(field.name)) {
                params.add(field);
            }

            //转成field存储，注意插入位置
            invocationParams.add(field);

            //开始解析对象
            parseObjectType(field.getTypeClass(),structs);
        }

        //转
        methodParams.clear();
        methodParams.addAll(convertInvocationParams(invocationParams));

        //转参数记录
        esbapiInfo.api.params = convertAPIParams(params);
    }

    //平铺参数获取
    private static void parseModelAttributeParameters(Class<?> clazz, List<ESBFieldDesc> methodParams, List<ESBFieldDesc> params) {
        Field[] fields = ESBT.getClassDeclaredFields(clazz);
        for (Field field : fields) {
            //只记录基础属性
            if (!ESBT.isBaseType(field.getType())) {
                continue;
            }

            ESBDesc desc = field.getAnnotation(ESBDesc.class);

            //需要忽略的属性
            if (desc != null && desc.ignore()) {
                continue;
            }

            String fieldName = field.getName();
            ESBFieldDesc fd = new ESBFieldDesc();
            fd.setTypeClass(field.getType());
            fd.name = fieldName;
            fd.required = false;//默认都是非必选
            if (desc != null) {
                fd.desc = desc.value();
                fd.isInner = desc.inner();
                fd.canEntrust = desc.entrust();
            }

            if (!ESBSTDKeys.isSTDKey(fieldName)) {//用于idl参数
                params.add(fd);
            }
            methodParams.add(fd);//invocation参数
        }
    }

    private static List<ESBField> convertInvocationParams(List<ESBFieldDesc> list) {

        List<ESBField> paths = new ArrayList<>();
        List<ESBField> params = new ArrayList<>();
        List<ESBField> bodys = new ArrayList<>();

        Set<String> sets = new HashSet<>();
        for (int i = 0; i < list.size(); i++) {
            ESBFieldDesc desc = list.get(i);
            if (desc.isRequestBody) {
                bodys.add(desc.getField());
            } else if (desc.isPathVariable) {
                if (!sets.contains(desc.name)) {
                    sets.add(desc.name);
                    paths.add(desc.getField());
                }

            } else {
                params.add(desc.getField());
            }
        }

        for (ESBField field : params) {
            if (!sets.contains(field.name)) {
                sets.add(field.name);
                paths.add(field);
            }
        }

        for (ESBField field : bodys) {
            if (!sets.contains(field.name)) {
                sets.add(field.name);
                paths.add(field);
            }
        }

        return paths;
    }

    private static ESBAPIParam[] convertAPIParams(List<ESBFieldDesc> list) {

        List<ESBAPIParam> params = new ArrayList<>();
        Set<String> sets = new HashSet<>();
        ESBFieldDesc body = null;
        for (int i = 0; i < list.size(); i++) {
            ESBFieldDesc desc = list.get(i);
            if (desc.isRequestBody) {
                body = desc;
                continue;
            }

            if (!sets.contains(desc.name)) {
                sets.add(desc.name);
                params.add(desc.getFieldParam());
            }

        }

        //处理最后一个
        if (body != null) {
            params.add(body.getFieldParam());
        }

        return params.toArray(new ESBAPIParam[0]);
    }

    private static Class<?> getListActuallyGenericType(Class<?> clazz, Type genericType, Class<?> targetClazz) {
        Class<?> genericClazz = null;
        if (Collection.class.isAssignableFrom(clazz)) {//增加对List自定义Object的支持+List<String>的支持,此处可以支持set
            if (List.class.isAssignableFrom(clazz)) {

                Type genericArgument;
                try {
                    genericArgument = ((ParameterizedTypeImpl) genericType).getActualTypeArguments()[0];
                } catch (Throwable t) {
                    throw new RuntimeException("generic type unsupported:" + genericType, t);
                }

                // 取到泛型参数
                if (targetClazz != null && (genericArgument instanceof TypeVariableImpl)) {
                    //当类型泛型模板来自父类定义
                    if (((TypeVariableImpl) genericArgument).getGenericDeclaration() == targetClazz.getSuperclass()) {
                        ParameterizedType type = (ParameterizedType)targetClazz.getGenericSuperclass();
                        Type[] pts = type.getActualTypeArguments();
                        if (pts.length == 1) {//只有一个的时候才可以确定泛型参数来自类泛型，多个无法确认参数是第几个
                            genericArgument = pts[0];
                        }
                    }
                }

                try {
                    //当前class loader来加载类型
                    genericClazz = ESBT.classForName(((Class) genericArgument).getName());
                } catch (Exception e) {
                    throw new RuntimeException("generic type unsupported:" + genericType, e);
                }

                //容器内往下不支持容器类型
                if (isContainerType(genericClazz)) {
                    throw new RuntimeException("generic type unsupported:" + genericType);
                }

            } else {
                throw new RuntimeException("only list is support when using collection");
            }
        }
        return genericClazz;
    }

    /**
     * 是否为基本类型
     * @param clazz
     * @return
     */
    private static boolean isBaseType(Class clazz) {
        return ESBT.isBaseType(clazz);
    }

    /**
     * 是容器类型
     * @param clazz
     * @return
     */
    private static boolean isContainerType(Class clazz) {
        if (Collection.class.isAssignableFrom(clazz)) {
            return true;
        }

        if (Map.class.isAssignableFrom(clazz)) {
            return true;
        }

        if (clazz.isArray()) {
            return true;
        }

        return false;
    }

    /**
     * 是否合法
     * @param field
     * @param entityClazz
     * @return 0:不合法, 1合法类型, 2:合法array, 3:合法List
     */
    private static boolean typeDeal(ESBFieldDesc field, Class<?> clazz, Type getGenericType, Class<?> entityClazz, String fieldName) {

        //boolean、byte、short、char、int、long、float、double、String、RawString
        //基础类型全部支持
        if (isBaseType(clazz)) {
            if (field != null) {
                field.setTypeClass(clazz);
//                if (field.isArray) {
//                    return true;
//                }
            }
            return true;
        }

        //枚举不支持
        if (clazz.isEnum()) {
            System.out.println("对象" + entityClazz.getName() + "存在枚举类型属性" + clazz.getName() + " " + fieldName);
            return false;
        }

        //不支持
        if (Map.class.isAssignableFrom(clazz)) {
            System.out.println("对象" + entityClazz.getName() + "存在不合法类型属性" + clazz.getName() + " " + fieldName);
            return false;
        }

        //容器类型
        if (Collection.class.isAssignableFrom(clazz)) {

            Class<?> genericClazz = null;
            try {
                genericClazz = getListActuallyGenericType(clazz,getGenericType,entityClazz);
            } catch (Exception e) {
                System.out.println("对象" + entityClazz.getName() + "存在不合法泛型属性" + clazz.getName() + " " + fieldName);
                return false;
            }

            if (genericClazz == null) {
                System.out.println("对象" + entityClazz.getName() + "存在不合法泛型属性" + clazz.getName() + " " + fieldName);
                return false;
            }

            if (genericClazz == String.class) {
                if (field != null) {
                    field.setTypeClass(String.class);
                    field.isList = true;
                }
                return true;
            }

            //不在继续支持容器内套容器,这样的方式非常不友好
            if (genericClazz.isArray()
                    || Map.class.isAssignableFrom(genericClazz)
                    || Collection.class.isAssignableFrom(genericClazz)
                    || genericClazz.isEnum()) {
                System.out.println("对象" + entityClazz.getName() + "存在不合法泛型属性" + clazz.getName() + " " + fieldName);
                return false;
            }

            if (field != null) {
                field.setTypeClass(genericClazz);
                field.isList = true;
            }

            return true;
        }

        //不支持自定义泛型 getGenericType == clazz,表示没有泛型的参数
        if (getGenericType != null && getGenericType != clazz) {
            System.out.println("对象" + entityClazz.getName() + "存在不合法泛型属性" + clazz.getName() + " " + fieldName);
            return false;
        }

        if (field != null) {
            field.setTypeClass(clazz);
        }

        return true;
    }


    /**
     * 解析属性,放入pojo之中
     * @param pojo
     * @param clazz
     */
    private static void parsePOJOFields(ESBPOJOWrapper pojo, Class clazz, Map<String,ESBAPIStruct> structs) {
        Field[] fields = ESBT.getClassDeclaredFields(clazz);

        for (Field field : fields) {

            ESBDesc desc = field.getAnnotation(ESBDesc.class);

            //需要忽略的属性
            if (desc != null && desc.ignore()) {
                continue;
            }

            String fieldName = field.getName();
            ESBFieldDesc fd = new ESBFieldDesc();
            fd.name = fieldName;
            boolean status = typeDeal(fd,field.getType(),field.getGenericType(),clazz,fieldName);
            if (!status) {
                throw new RuntimeException("存在不支持的类型在:" + clazz.getSimpleName() + "." + fieldName);
//                continue;
            }

            if (desc != null) {
                fd.desc = desc.value();
                fd.isInner = desc.inner();
                fd.canEntrust = desc.entrust();
            }

            if (pojo.fields == null) {
                pojo.fields = new ArrayList<ESBField>();
            }

            pojo.fields.add(fd.getField());

            //基础类型就不用解析了
            if (isBaseType(fd.getTypeClass())) {
                continue;
            }

            //日期类型不想下寻找
            if (fd.getTypeClass().getName().startsWith("java.util.")) {
                continue;
            }

            //判断其类型是否为基础类型,若不是基础类型,则需要不断递归,将类型解析完
            String keyType = fd.getCoreType();

            //类型继续解析
            ESBPOJOWrapper pj = pojos.get(keyType);
            if (pj == null) {
                parseObjectType(fd.getTypeClass(), structs);//继续解析参数
            } else {
                savePojoToAPI(pj, structs);
            }
        }
    }

    private static void savePojoToAPI(ESBPOJOWrapper wrapper, Map<String, ESBAPIStruct> structs) {
        if (structs != null && wrapper != null) {
            if (!structs.containsKey(wrapper.getCoreType())) {
                structs.put(wrapper.getCoreType(),wrapper.convertStruct());

                //需要将其依赖的所有类型都依赖进来
                if (wrapper.fields != null) {
                    for (ESBField field : wrapper.fields) {
                        String coreType = field.getCoreType();
                        if (!ESBT.isBaseType(coreType)) {
                            ESBPOJOWrapper pj = pojos.get(coreType);
                            if (pj != null) {
                                savePojoToAPI(pj,structs);
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * 字母+数字
     * @param domain
     * @return
     */
    public static boolean verifyDomain(String domain) {
        if (StringUtils.isEmpty(domain)) {
            return false;
        }

        Pattern pattern = Pattern.compile("[0-9|a-z|A-Z]*");
        return pattern.matcher(domain).matches();
    }

    /**
     * 字母+数字+下划线,以字母和下划线开头
     * @param apiName
     * @return
     */
    public static boolean verifyAPIName(String apiName) {
        if (StringUtils.isEmpty(apiName)) {
            return false;
        }

        char c = apiName.charAt(0);
        if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_') {
            Pattern pattern = Pattern.compile("[0-9|a-z|A-Z|_]*");
            return pattern.matcher(apiName).matches();
        }

        return false;
    }

}
