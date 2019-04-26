package com.venus.esb.dubbo.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.beanutil.JavaBeanAccessor;
import com.alibaba.dubbo.common.beanutil.JavaBeanDescriptor;
import com.alibaba.dubbo.common.beanutil.JavaBeanSerializeUtil;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.io.UnsafeByteArrayInputStream;
import com.alibaba.dubbo.common.io.UnsafeByteArrayOutputStream;
import com.alibaba.dubbo.common.serialize.Serialization;
import com.alibaba.dubbo.common.utils.PojoUtils;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.ServiceConfig;
import com.alibaba.dubbo.config.invoker.DelegateProviderMetaDataInvoker;
import com.alibaba.dubbo.registry.integration.RegistryProtocol;
import com.alibaba.dubbo.rpc.*;
import com.alibaba.dubbo.rpc.service.GenericException;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.alibaba.dubbo.rpc.support.ProtocolUtils;
import com.venus.esb.lang.ESBT;
import com.venus.esb.utils.Injects;
import com.venus.esb.utils.Pickup;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created with IntelliJ IDEA.
 * Description: 修复dubbo自带的GenericFilter无法支持基础类型泛化转实例问题
 * User: lingminjun
 * Date: 2019-04-16
 * Time: 11:05 PM
 */
@Activate(group = Constants.PROVIDER, order = -20001)
public class ESBGenericFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation inv) throws RpcException {
        if (inv.getMethodName().equals(Constants.$INVOKE)
                && inv.getArguments() != null
                && inv.getArguments().length == 3
                && !invoker.getInterface().equals(GenericService.class)) {
            String name = ((String) inv.getArguments()[0]).trim();
            String[] types = (String[]) inv.getArguments()[1];
            Object[] args = (Object[]) inv.getArguments()[2];
            try {
                Method method = ReflectUtils.findMethodByMethodSignature(invoker.getInterface(), name, types);
                Class<?>[] params = method.getParameterTypes();
                if (args == null) {
                    args = new Object[params.length];
                }
                String generic = inv.getAttachment(Constants.GENERIC_KEY);

                if (StringUtils.isBlank(generic)) {
                    generic = RpcContext.getContext().getAttachment(Constants.GENERIC_KEY);
                }

                // 修复泛型无法实现基础类型互转问题 PojoUtils.realize: Caused by: java.lang.IllegalArgumentException: argument type mismatch
                if (StringUtils.isEmpty(generic) || ProtocolUtils.isDefaultGenericSerialization(generic)) {
                    args = ESBT.realize(args, params, method.getGenericParameterTypes());
                } else if (ProtocolUtils.isJavaGenericSerialization(generic)) {
                    for (int i = 0; i < args.length; i++) {
                        if (byte[].class == args[i].getClass()) {
                            try {
                                UnsafeByteArrayInputStream is = new UnsafeByteArrayInputStream((byte[]) args[i]);
                                args[i] = ExtensionLoader.getExtensionLoader(Serialization.class)
                                        .getExtension(Constants.GENERIC_SERIALIZATION_NATIVE_JAVA)
                                        .deserialize(null, is).readObject();
                            } catch (Exception e) {
                                throw new RpcException("Deserialize argument [" + (i + 1) + "] failed.", e);
                            }
                        } else {
                            throw new RpcException(
                                    "Generic serialization [" +
                                            Constants.GENERIC_SERIALIZATION_NATIVE_JAVA +
                                            "] only support message type " +
                                            byte[].class +
                                            " and your message type is " +
                                            args[i].getClass());
                        }
                    }
                } else if (ProtocolUtils.isBeanGenericSerialization(generic)) {
                    for (int i = 0; i < args.length; i++) {
                        if (args[i] instanceof JavaBeanDescriptor) {
                            args[i] = JavaBeanSerializeUtil.deserialize((JavaBeanDescriptor) args[i]);
                        } else {
                            throw new RpcException(
                                    "Generic serialization [" +
                                            Constants.GENERIC_SERIALIZATION_BEAN +
                                            "] only support message type " +
                                            JavaBeanDescriptor.class.getName() +
                                            " and your message type is " +
                                            args[i].getClass().getName());
                        }
                    }
                }
                Result result = invoker.invoke(new RpcInvocation(method, args, inv.getAttachments()));
                boolean isJson = isJsonSerialization(invoker,inv);
                if (result.hasException()) {
                    if (result.getException() instanceof GenericException) {
                        if (result.getException().getClass() != ESBGenericException.class) {
                            return new RpcResult(new ESBGenericException((GenericException)result.getException()));
                        } else {
                            return result;
                        }
                    } else {
                        return new RpcResult(new ESBGenericException(result.getException()));
                    }
                }
                if (ProtocolUtils.isJavaGenericSerialization(generic)) {
                    try {
                        UnsafeByteArrayOutputStream os = new UnsafeByteArrayOutputStream(512);
                        ExtensionLoader.getExtensionLoader(Serialization.class)
                                .getExtension(Constants.GENERIC_SERIALIZATION_NATIVE_JAVA)
                                .serialize(null, os).writeObject(result.getValue());
                        return new RpcResult(os.toByteArray());
                    } catch (IOException e) {
                        throw new RpcException("Serialize result failed.", e);
                    }
                } else if (ProtocolUtils.isBeanGenericSerialization(generic)) {
                    return new RpcResult(JavaBeanSerializeUtil.serialize(result.getValue(), JavaBeanAccessor.METHOD));
                } else {
                    // 泛化过程优化，无需将返回值泛化，采用fastjson直接转换
                    if (isJson) {
                        return result;
                    } else {
                        return new RpcResult(PojoUtils.generalize(result.getValue()));
                    }
                }
            } catch (NoSuchMethodException e) {
                throw new RpcException(e.getMessage(), e);
            } catch (ClassNotFoundException e) {
                throw new RpcException(e.getMessage(), e);
            }
        }
        return invoker.invoke(inv);
    }

    private boolean isJsonSerialization(Invoker<?> invoker, Invocation inv) {

        // 此处取
        Object obj = Pickup.get(invoker,"val$invoker");
        Invoker<?> temp = null;
        if (obj != null && obj instanceof Invoker) {
            temp = (Invoker<?>)obj;
        } else {
            return false;
        }

        while (temp instanceof RegistryProtocol.InvokerDelegete) {
            temp = ((RegistryProtocol.InvokerDelegete) temp).getInvoker();
        }

        if (temp == null || !(temp instanceof DelegateProviderMetaDataInvoker)) {
            return false;
        }

        DelegateProviderMetaDataInvoker provider = (DelegateProviderMetaDataInvoker)temp;
        ServiceConfig config = provider.getMetadata();
        if (config == null) {
            return false;
        }

        String serialization = config.getSerialization();
        if (serialization == null && config.getProvider() != null) {
            serialization = config.getProvider().getSerialization();
        }

        // 因为支持多个协议，一般是需要与调用对应，找到一个一致的协议，我们这里简单取一个，可能会产生bug
        if (serialization == null && config.getProtocol() != null) {
            serialization = config.getProtocol().getSerialization();
        }

        /**
         *
         dubbo=com.alibaba.dubbo.common.serialize.support.dubbo.DubboSerialization
         hessian2=com.alibaba.dubbo.common.serialize.support.hessian.Hessian2Serialization
         java=com.alibaba.dubbo.common.serialize.support.java.JavaSerialization
         compactedjava=com.alibaba.dubbo.common.serialize.support.java.CompactedJavaSerialization
         json=com.alibaba.dubbo.common.serialize.support.json.JsonSerialization
         fastjson=com.alibaba.dubbo.common.serialize.support.json.FastJsonSerialization
         nativejava=com.alibaba.dubbo.common.serialize.support.nativejava.NativeJavaSerialization
         kryo=com.alibaba.dubbo.common.serialize.support.kryo.KryoSerialization
         fst=com.alibaba.dubbo.common.serialize.support.fst.FstSerialization
         jackson=com.alibaba.dubbo.common.serialize.support.json.JacksonSerialization
         *
         */
        return "fastjson".equals(serialization) || "json".equals(serialization) || "jackson".equals(serialization);
    }
}
