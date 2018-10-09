package com.venus.esb;

import com.venus.esb.lang.ESBException;
import com.venus.esb.lang.ESBExceptionCodes;

import java.io.Serializable;

/**
 * Created by lingminjun on 17/8/13.
 * 规则映射节点
 */
public final class ESBRuleNode implements Serializable {
    private static final long serialVersionUID = -2502233352465511396L;

//    public String path;        //返回值的部分path, 为空是表示 invocations
    public String invocation; //
    public final String rule;
    public final int level;//规则顺序

    public ESBRuleNode(String rule) throws ESBException {
        this(rule,null);
    }

    public ESBRuleNode(String rule, String invokeMd5) throws ESBException {
        if (rule == null || rule.length() == 0) {
            throw ESBExceptionCodes.PARAMETER_ERROR("请填写正确的规则,否则无法构造ESBRuleNode");
        }
        this.level = ESBRule.judgeSortValue(rule);
        this.rule = rule;
        this.invocation = invokeMd5;
    }
}
