package com.venus.gen.service;

import com.venus.esb.annotation.ESBDesc;

import java.io.Serializable;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: lingminjun
 * Date: 2018-06-16
 * Time: 下午11:16
 */
@ESBDesc("一页结果集,请子类化")
public abstract class PageResults<T extends Serializable> {
    @ESBDesc("总数")
    private long total;//总数

    @ESBDesc("当前页数，从1开始")
    private int index;//

    @ESBDesc("一页显示条数")
    private int size;//

    @ESBDesc("数据集")
    private List<T> results;//

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<T> getResults() {
        return results;
    }

    public void setResults(List<T> results) {
        this.results = results;
    }
}
