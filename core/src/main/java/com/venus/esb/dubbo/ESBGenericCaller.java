package com.venus.esb.dubbo;


import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.utils.ReferenceConfigCache;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.venus.esb.config.ESBConfigCenter;
import com.venus.esb.dubbo.filter.ESBEchoFilter;
import com.venus.esb.lang.ESBException;
import com.venus.esb.lang.ESBExceptionCodes;
import com.venus.esb.lang.ESBT;

import java.util.*;

/**
 * Created by lingminjun on 17/2/8.
 */
public final class ESBGenericCaller {

//    private static final Integer RPC_TIME_OUT = 3000;

    public abstract class Filter {
        Object invoke(GenericService service, String methodName, MethodParams params) {
            return service.$invoke(methodName, params.getTyps(), params.getObjs());
        }
    }

    public ApplicationConfig application;
    public RegistryConfig registry;
    public Filter filter;

    private static class SingletonHolder {
        private static ESBGenericCaller INSTANCE = new ESBGenericCaller();
    }

    private ESBGenericCaller(){

        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName(ESBConfigCenter.instance().getAppName());

        //这里配置了dubbo的application信息*(demo只配置了name)*，因此demo没有额外的dubbo.xml配置文件
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress(ESBConfigCenter.instance().getRegistry());

        //这里配置dubbo的注册中心信息，因此demo没有额外的dubbo.xml配置文件
        if (ESBConfigCenter.instance().getGenericFilter() != null) {
            try {
                Class<?> clazz = ESBT.classForName(ESBConfigCenter.instance().getGenericFilter());
                if (Filter.class.isAssignableFrom(clazz)) {
                    filter = (Filter)clazz.newInstance();
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        this.application = applicationConfig;
        this.registry = registryConfig;

    }

    public static ESBGenericCaller getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 请求服务方法
     * @param interfaceClass
     * @param methodName
     * @param params
     * @return
     */
    public Object genericInvoke(String interfaceClass, String methodName, MethodParams params) throws ESBException {
        return genericInvoke(interfaceClass,methodName,params,null,null,null);
    }
    /**
     * 请求服务方法
     * @param interfaceClass
     * @param methodName
     * @param params
     * @param version
     * @param timeout
     * @param retries
     * @return
     */
    public Object genericInvoke(String interfaceClass, String methodName, MethodParams params, String version, Integer timeout, Integer retries) throws ESBException {

        if (StringUtils.isEmpty(interfaceClass)
                || StringUtils.isEmpty(methodName)) {
            throw new RuntimeException("dubbo接口调用必须指定接口类和对应的方法");
        }

        //对于无参传入,参数个数要被记录下来
        if (params == null) {
            params = new MethodParams(0);
        }

        //需要解决序列化性能问题,
        //https://my.oschina.net/zhaojy/blog/646662
        //
        ReferenceConfig<GenericService> reference = buildReferenceConfig(interfaceClass,version,timeout,retries);

        /*ReferenceConfig实例很重，封装了与注册中心的连接以及与提供者的连接，
        需要缓存，否则重复生成ReferenceConfig可能造成性能问题并且会有内存和连接泄漏。
        API方式编程时，容易忽略此问题。
        这里使用dubbo内置的简单缓存工具类进行缓存*/
        ReferenceConfigCache cache = ReferenceConfigCache.getCache();
        GenericService genericService = cache.get(reference);
//        cache.destroy(reference);
//        GenericService genericService = reference.get();
        // 用com.venus.dubbo.rpc.service.GenericService可以替代所有接口引用

        //最后的调用
        try {
            if (filter != null) {
                return filter.invoke(genericService,methodName,params);
            } else {
                return genericService.$invoke(methodName, params.getTyps(), params.getObjs());
            }
        } catch (RpcException e) {
            switch (e.getCode()) {
                case RpcException.NETWORK_EXCEPTION:{
                    throw ESBExceptionCodes.DUBBO_NETWORK_EXCEPTION("Dubbo请求网络错误,可能方法找不到").setCoreCause(e);
                }
                case RpcException.TIMEOUT_EXCEPTION:{
                    throw ESBExceptionCodes.DUBBO_SERVICE_TIMEOUT("Dubbo请求超时").setCoreCause(e);
                }
                case RpcException.BIZ_EXCEPTION:{
                    throw ESBExceptionCodes.DUBBO_SERVICE_TIMEOUT("Dubbo请求业务异常").setCoreCause(e);
                }
                case RpcException.FORBIDDEN_EXCEPTION:{
                    throw ESBExceptionCodes.DUBBO_FORBIDDEN_EXCEPTION("Dubbo请求被拒绝,可能服务找不到").setCoreCause(e);
                }
                case RpcException.SERIALIZATION_EXCEPTION:{
                    throw ESBExceptionCodes.SERIALIZE_FAILED("Dubbo请求序列化出错").setCoreCause(e);
                }
                default:{
                    throw ESBExceptionCodes.DUBBO_SERVICE_ERROR("Dubbo请求出错").setCoreCause(e);
                }
            }
        } catch (Throwable e) {
            throw ESBExceptionCodes.DUBBO_SERVICE_ERROR("Dubbo请求出错").setCoreCause(e);
        }
    }

    /**
     * 是否一个服务,注意此方法仅在明确需要下掉服务时使用
     * @param interfaceClass
     */
    public void releaseService(String interfaceClass) {
        releaseService(interfaceClass,null);
    }
    public void releaseService(String interfaceClass, String version) {
        ReferenceConfig<GenericService> reference = buildReferenceConfig(interfaceClass,version,null,null);
        ReferenceConfigCache cache = ReferenceConfigCache.getCache();
        cache.destroy(reference);
    }

    /**
     * 同步回声测试
     * @param interfaceClass
     * @return
     */
    public boolean testingServiceAvailable(String interfaceClass) {
        return testingServiceAvailable(interfaceClass,null);
    }
    public boolean testingServiceAvailable(String interfaceClass, String version) {
        ReferenceConfig<GenericService> reference = buildReferenceConfig(interfaceClass,version,null,null);
        ReferenceConfigCache cache = ReferenceConfigCache.getCache();
        GenericService genericService = cache.get(reference);
        Object status = "OK";
        boolean rt = false;
        try {
            MethodParams params = new MethodParams(1);
            params.add(Object.class.getName(),status);
            Object obj = genericService.$invoke(Constants.$ECHO,params.getTyps(),params.getObjs());
            if (obj != null && obj.toString().equals(status)) {
                rt = true;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return rt;
    }

    /**
     * 同步回声测试方法存在
     * @param interfaceClass
     * @param methodName
     * @param paramsTypes
     * @return
     */
    public boolean testingServiceMethodExist(String interfaceClass, String methodName, String[] paramsTypes) {
        return testingServiceMethodExist(interfaceClass,methodName,paramsTypes,null);
    }
    public boolean testingServiceMethodExist(String interfaceClass, String methodName, String[] paramsTypes, String version) {
        ReferenceConfig<GenericService> reference = buildReferenceConfig(interfaceClass,version,null,null);
        ReferenceConfigCache cache = ReferenceConfigCache.getCache();
        GenericService genericService = cache.get(reference);
        Object status = ESBEchoFilter.$EXIST;
        boolean rt = false;
        try {
            StringBuilder builder = new StringBuilder();
            if (paramsTypes != null) {
                for (int i = 0; i < paramsTypes.length; i++) {
                    if (i > 0) {builder.append(",");}
                    String type = paramsTypes[i];
                    int ii = type.indexOf("<");
                    if (ii >= 0 && ii < type.length()) {//回声测试无法
                        type = type.substring(0,ii);
                    }
                    builder.append(type);
                }
            }
            MethodParams params = new MethodParams(1);
            params.add(Object.class.getName(), ESBEchoFilter.$EXIST_METHOD+methodName+"|"+builder.toString());
            Object obj = genericService.$invoke(Constants.$ECHO,params.getTyps(),params.getObjs());
            if (obj != null && obj.toString().equals(status)) {
                rt = true;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return rt;
    }

    public static class MethodParams {
        private final String[] typs;
        private final Object[] objs;

        private int _last = -1;//用于友好支持add方式

        public MethodParams(int size) {
            typs = new String[size];
            objs = new Object[size];
        }

        public String[] getTyps() {
            return typs;
        }

        public Object[] getObjs() {
            return objs;
        }

        public MethodParams set(int index, String clazz, Object value) {
            if (index < 0 || index >= typs.length) {
                throw new RuntimeException("请指定正确的下标设置参数类型和值");
            }

            if (StringUtils.isEmpty(clazz)) {
                throw new RuntimeException("请设置正确的参数类型");
            }

            $set(index,clazz,value);

            _last = index;

            return this;
        }

        public MethodParams add(String clazz, Object value) {

            if (StringUtils.isEmpty(clazz)) {
                throw new RuntimeException("请设置正确的参数类型");
            }
            _last = (_last + 1) % typs.length;
            $set(_last,clazz,value);
            return this;
        }

        private void $set(int index, String clazz, Object value) {
            //对泛型接口的支持
            int idx = clazz.indexOf("<");
            if (idx >= 0 && idx < clazz.length()) {
                typs[_last] = clazz.substring(0,idx);
                String t = clazz.substring(idx+1,clazz.length() - 1);
                if (value instanceof Map) {
                    if (!((Map) value).containsKey("class")) {
                        ((Map) value).put("class",t);
                    }
                }
            } else {
                typs[_last] = clazz;
            }
            objs[_last] = value;
        }
    }

    private ReferenceConfig<GenericService> buildReferenceConfig(String service, String version, Integer timeout, Integer retries) {
        ReferenceConfig<GenericService> reference = new ReferenceConfig<GenericService>();
        reference.setApplication(application);
        reference.setRegistry(registry);
        reference.setInterface(service); // 接口名
//        reference.setTimeout(RPC_TIME_OUT);//默认超时时间
        reference.setCheck(false);//必须是check的
        if (retries != null) {
            reference.setRetries(retries);
        } else {
            reference.setRetries(ESBConfigCenter.instance().getDubboRetries());
        }
        if (timeout != null) {
            reference.setTimeout(timeout);
        } else {
            reference.setTimeout(ESBConfigCenter.instance().getDubboTimeout());
        }
        if (version != null) {
            reference.setVersion(version);
        } else if (ESBConfigCenter.instance().getDubboVersion() != null) {
            reference.setVersion(ESBConfigCenter.instance().getDubboVersion());
        }
        reference.setGeneric(true); // 声明为泛化接口
        return reference;
    }

    /**
     * 泛型pojo对象 非线程安全
     * 一些属性是不支持的,如自定容器类型,以及Map
     */
//    public final static class GenericPOJOBuilder {
//        private Map<String,Object> obj = new HashMap<String, Object>();
//        private Object basal;
//        private final boolean ignoreError;
//
//        public GenericPOJOBuilder() {
//            this(false);
//        }
//
//        public GenericPOJOBuilder(boolean ignoreError) {
//            this.ignoreError = ignoreError;
//        }
//
//        /**
//         * 当前是否防止基本类型,此处基本类型定义如:
//         * static boolean isPrimitive(Object obj) 方法定义
//         * @return
//         */
//        public boolean isPrimitive() {
//            return basal != null;
//        }
//
//        /**
//         * 基本数据类型
//         * int, double, float, long, short, boolean, byte, char， void.
//         * Short,Integer,Long,Float,Double,Byte,Boolean
//         * CharSequence,String
//         * Enum
//         */
//        private static boolean isPrimitive(Object obj) {
//            if (obj == null) {
//                return true;
//            }
//
//            if (obj.getClass().isPrimitive()) {
//                return true;
//            }
//
//            if (obj instanceof Number) {
//                return true;
//            }
//
//            if (obj instanceof Boolean) {
//                return true;
//            }
//
//            if (obj instanceof Character) {//Character类型
//                return true;
//            }
//
//            if (obj instanceof CharSequence) {//String类型
//                return true;
//            }
//
//            if (obj instanceof Enum) {
//                return true;
//            }
//
//            return false;
//        }
//
//        /**
//         * 针对非基础类型
//         * @param field
//         * @param value
//         * @return
//         */
//        public GenericPOJOBuilder put(String field, Object value) {
//            if (StringUtils.isEmpty(field)) {
//                return this;
//            }
//
//            //删除属性
//            if (value == null) {
//                obj.remove(field);
//                return this;
//            }
//
//            //针对基本类型不需要做任何的处理
//            if (isPrimitive(value)) {
//                obj.put(field,value);
//            } else if (value instanceof Collection) {
//                //说明下,此处不支持自定义派生容器类型,若自定义派生,将抛出异常
//                Collection target = convertCollection((Collection)value,ignoreError);
//                if (target != null) {
//                    obj.put(field, target);
//                }
//            } else if (value instanceof Map) {//字典仅仅支持HashMap<String,Object>
//                //字典仅仅支持HashMap<String,Object>
//                Map map = convertMap((Map)value,ignoreError);
//                if (map != null) {
//                    obj.put(field,map);
//                }
//            } else if (value.getClass().isArray()) {//
//                Object target = convertArray(value,ignoreError);
//                obj.put(field,target);
//            } else {//针对普通对象,级联处理
//                GenericPOJOBuilder pojo = new GenericPOJOBuilder(ignoreError);
//                pojo.setObject(value);
//                obj.put(field,pojo.toObject());
//            }
//
//            return this;
//        }
//
//        public GenericPOJOBuilder remove(String field) {
//            if (StringUtils.isEmpty(field)) {
//                return this;
//            }
//
//            //删除属性
//            obj.remove(field);
//            return this;
//        }
//
//        /**
//         * 直接使用对象初始化
//         * @param obj
//         * @return
//         */
//        public GenericPOJOBuilder setObject(Object obj) {
//            if (isPrimitive(obj)) {
//                this.obj.clear();
//                basal = obj;
//                return this;
//            }
//
//            //本身是容器,转成
//            if (obj instanceof Collection) {
//                Collection target = convertCollection((Collection)obj,ignoreError);
//                this.obj.clear();
//                basal = target;
//                return this;
//            }
//
//            //array
//            if (obj.getClass().isArray()) {//
//                Object target = convertArray(obj,ignoreError);
//                this.obj.clear();
//                basal = target;
//                return this;
//            }
//
//            basal = null;
//
//            //如果本身是map
//            if (obj instanceof Map) {
//
//                throw new RuntimeException("GenericPOJO not support special map  like the map class:" + obj.getClass());
//            }
//
//            //遍历所有的属性,并将其设置到obj中
//            Class clazz = obj.getClass();
//            Field[] fvs = getDeclaredFields(clazz);//.getDeclaredFields();
//            for (Field field : fvs) {
//
//                String fieldName = field.getName();
//
//                //将属性的至赋值到泛型数据中
//                Object value = null;
//                try {
//                    field.setAccessible(true);
//                    value = field.get(obj);
//                } catch (Throwable e) {continue;}
//
//                this.put(fieldName,value);
//            }
//
//            return this;
//        }
//
//        //直接返回其泛型对象
//        public Object toObject() {
//            if (basal != null) {return basal;}
//            return obj;
//        }
//
//        //将数据防止列表中
//        public Object toList() {
//            if (basal != null) {
//                if (basal instanceof List) {return basal;}
//                if (basal instanceof Set) {return basal;}
//                if (basal.getClass().isArray()) {return basal;}
//                ArrayList list = new ArrayList();
//                list.add(basal);
//                return list;
//            }
//
//            if (obj != null) {
//                ArrayList<Map<String,Object> > list = new ArrayList<Map<String, Object>>();
//                list.add(obj);
//                return list;
//            }
//
//            return null;
//        }
//
//
//        private static Collection convertCollection(Collection from, boolean ignoreError) {
//
//            //防止重新创建对象
//            if (from == null || from.size() == 0) {
//                return from;
//            } else if (from.size() > 0) {//基础类型,
//                if (isPrimitive(from.iterator().next())) {
//                    return from;
//                }
//            }
//
//            Collection target = null;
//            if (Stack.class == from.getClass()) {//
//                target = new Stack();
//            } else if (Vector.class == from.getClass()) {
//                target = new Vector();
//            } else if (ArrayList.class == from.getClass()) {
//                target = new ArrayList();
//            } else if (HashSet.class == from.getClass()) {
//                target = new HashSet();
//            } else {//不支持的自定义的容器类型(LinkedList不支持)
//                if (ignoreError) {
//                    return null;
//                }
//                throw new RuntimeException("GenericPOJO not support property type like the container class:" + from.getClass());
//            }
//
//            for (Object obj : from) {
//                GenericPOJOBuilder pojo = new GenericPOJOBuilder(ignoreError);
//                pojo.setObject(obj);
//                target.add(pojo.toObject());
//            }
//
//            return target;
//        }
//
//        private static Object convertArray(Object from, boolean ignoreError) {
//            int length = Array.getLength(from);
//            if (length == 0) {
//                return from;
//            }
//
//            Object target = null;//Array.newInstance()
//            for (int idx = 0; idx < length; idx++) {
//                Object obj = Array.get(from,idx);
//
//                if (obj == null) {continue;}
//
//                //针对基础类型
//                if (obj != null && isPrimitive(obj)) {
//                    return from;
//                }
//
//                if (target == null) {
//                    target = Array.newInstance(Map.class, length);
//                }
//
//                GenericPOJOBuilder pojo = new GenericPOJOBuilder(ignoreError);
//                pojo.setObject(obj);
//                Array.set(target,idx,pojo.toObject());
//
//            }
//
//            return target;
//        }
//
//        private static boolean valiedMap(Map map) {
//            if (map == null || map.size() == 0) {
//                return true;
//            }
//
//            if (HashMap.class != map.getClass()) {
//                return false;
//            }
//
//            for (Object key : map.keySet()) {
//                if (!(key instanceof String)) {
//                    return false;
//                }
//
//                Object o = map.get(key);
//                if (isPrimitive(o)) {
//                    return true;
//                }
//
//                //容器类型进一步判断
//                return false;
//            }
//
//            return false;
//        }
//
//        private static Map convertMap(Map from, boolean ignoreError) {
//            if (HashMap.class == from.getClass()) {
//                HashMap map = (HashMap)from;
//                if (map.size() == 0) {
//                    return null;
//                }
//
//                //遍历处理所有value
//                Map target = null;
//                for (Object key : map.keySet()) {
//                    if (!(key instanceof String)) {
//                        throw new RuntimeException("GenericPOJO not support property type like the HashMap key type:" + key.getClass());
//                    }
//
//                    Object o = map.get(key);
//                    if (isPrimitive(o)) {
//                        return from;
//                    }
//
//                    if (o instanceof List) {
//
//                    }
//
//                    if (target == null) {
//                        target = new HashMap<String,Object>();
//                    }
//
//                    GenericPOJOBuilder pojo = new GenericPOJOBuilder(ignoreError);
//                    pojo.setObject(o);
//                    target.put(key,pojo.toObject());
//                }
//            }
//            if (ignoreError) {
//                return null;
//            }
//            throw new RuntimeException("GenericPOJO not support property type like the map class:" + from.getClass());
//        }
//
//        /**
//         * 循环向上转型, 获取对象的 DeclaredField
//         * 包含所有属性 private、protected、default、public
//         * 说明:
//         * 以下两个方法去属性范畴是所有(privte,protected,default,public),但是仅限于本类,不包含父类属性
//         * public Field getDeclaredField (String name)
//         * public Field[] getDeclaredFields ()
//         *
//         * 以下两个方法取值范围为(protected,default,public),包含父类继承的属性
//         * public Field getField (String name)
//         * public Field[] getFields ()
//         *
//         * @param clazz : 子类对象
//         * @return 父类中的属性对象
//         */
//        public static Field[] getDeclaredFields(Class<?> clazz) {
//            //若父类和子类有同名属性,直接用子类的属性覆盖即可
//            if (clazz == null || clazz == Object.class) {
//                return new Field[0];
//            }
//
//            ArrayList<Field> list = new ArrayList<Field>();
//            HashSet<String> names = new HashSet<String>();
//            for(; clazz != Object.class; clazz = clazz.getSuperclass()) {
//                try {
//                    Field[] flds = clazz.getDeclaredFields() ;
//                    for (Field fld : flds) {
//                        //去重父类同名属性
//                        if (names.contains(fld.getName())) {
//                            continue;
//                        }
//
//                        names.add(fld.getName());
//                        list.add(fld);
//                    }
//                } catch (Throwable e) {
//                    //这里甚么都不要做！并且这里的异常必须这样写，不能抛出去。
//                    //如果这里的异常打印或者往外抛，则就不会执行clazz = clazz.getSuperclass(),最后就不会进入到父类中了
//                }
//            }
//
//            return list.toArray(new Field[0]);
//        }
//
//        public static Field getDeclaredField(Object object, String fieldName){
//
//            Class<?> clazz = object.getClass() ;
//            for(; clazz != Object.class; clazz = clazz.getSuperclass()) {
//                try {
//                    return clazz.getDeclaredField(fieldName) ;
//                } catch (Throwable e) {
//                    //这里甚么都不要做！并且这里的异常必须这样写，不能抛出去。
//                    //如果这里的异常打印或者往外抛，则就不会执行clazz = clazz.getSuperclass(),最后就不会进入到父类中了
//                }
//            }
//
//            return null;
//        }
//
//    }
}
