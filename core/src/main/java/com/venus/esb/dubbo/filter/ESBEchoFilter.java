/*
 * Copyright 1999-2011 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.venus.esb.dubbo.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import com.alibaba.dubbo.rpc.*;
import com.alibaba.dubbo.rpc.support.ProtocolUtils;

import java.lang.reflect.Method;

/**
 * EchoInvokerFilter
 * 
 * @author lingminjun 兼容泛型调用时回声测试 GenericFilter order=-20000; EchoFilter order=-110000
 */
@Activate(group = Constants.PROVIDER, order = -110001)
public class ESBEchoFilter implements Filter {
	public ESBEchoFilter() {
	}
	/**
	 * 1、回声测试:测试服务可用
	 * MemberService memberService = ctx.getBean("memberService"); // 远程服务引用
	 * EchoService echoService = (EchoService) memberService; // 强制转型为EchoService
	 * String status = echoService.$echo("OK"); // 回声测试可用性
	 * assert(status.equals("OK"));
	 *
	 * 2、回声测试:测试服务方法存在
	 * MemberService memberService = ctx.getBean("memberService"); // 远程服务引用
	 * EchoService echoService = (EchoService) memberService; // 强制转型为EchoService
	 * String status = echoService.$echo( ESBEchoFilter.$EXIST_METHOD + "sayHello|java.lang.String,boolean"); // 回声测试方法 "boolean sayHello(String var1,boolean var2)" 可用
	 * assert(status.equals( ESBEchoFilter.$EXIST ));
	 */
	public static final String $EXIST_METHOD = "$exist_m:";
	public static final String $EXIST = "exist";
	public static final String $NOT_FOUND = "not found";

	public Result invoke(Invoker<?> invoker, Invocation inv) throws RpcException {
		if(Constants.$ECHO.equals(inv.getMethodName())
				&& inv.getArguments() != null
				&& inv.getArguments().length == 1) {
			return testing(invoker.getInterface(),inv.getArguments()[0]);
		} else if (Constants.$INVOKE.equals(inv.getMethodName())
				&& inv.getArguments() != null
				&& inv.getArguments().length == 3
				&& !ProtocolUtils.isGeneric(invoker.getUrl().getParameter(Constants.GENERIC_KEY))) {
			String name = ((String) inv.getArguments()[0]).trim();
			Object[] args = (Object[]) inv.getArguments()[2];
			if (Constants.$ECHO.equals(name)
					&& args != null
					&& args.length == 1) {
				return testing(invoker.getInterface(),args[0]);
			}
		}
		return invoker.invoke(inv);
	}

	private Result testing(Class<?> clazz, Object ok) {
		if (ok instanceof String) {
			if (((String) ok).startsWith($EXIST_METHOD)) {
				try {
					String str = ((String) ok).substring($EXIST_METHOD.length());
					String[] strs = str.split("\\|");
					String name = strs[0];
					String[] types = strs[1].split(",");
					Method method = ReflectUtils.findMethodByMethodSignature(clazz, name, types);
					if (method != null) {
						return new RpcResult($EXIST);
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
				return new RpcResult($NOT_FOUND);
			}
		}
		return new RpcResult(ok);
	}
}