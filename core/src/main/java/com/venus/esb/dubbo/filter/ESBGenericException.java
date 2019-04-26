/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.venus.esb.dubbo.filter;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.rpc.service.GenericException;
import com.alibaba.fastjson.annotation.JSONField;
import com.venus.esb.lang.ESBT;

/**
 * GenericException
 *
 * @export
 */
public final class ESBGenericException extends GenericException {
    public ESBGenericException() {
    }

    public ESBGenericException(String exceptionClass, String exceptionMessage) {
        super(exceptionClass,exceptionMessage);
    }

    public ESBGenericException(Throwable cause) {
        super(cause);
    }

    public ESBGenericException(GenericException cause) {
        super(cause.getExceptionClass(),cause.getExceptionMessage());
        setDetailMessage(cause.getMessage());
        setStackTrace(cause.getStackTrace());
    }

    @Override
    @JSONField(name = "detailMessage")
    public String getMessage() {
        return super.getMessage();
    }

    @JSONField(serialize = false, deserialize = false)
    @Override
    public String getLocalizedMessage() {
        return super.getLocalizedMessage();
    }

    public final void setDetailMessage(String message) {
        ESBT.setValueForFieldPath(this,"detailMessage",message);
    }
}