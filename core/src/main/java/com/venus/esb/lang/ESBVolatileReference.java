package com.venus.esb.lang;

/**
 * Created by lingminjun on 17/6/17.
 * 缓存易变化的临时参数,主要配合 volatile 标识符一起使用
 */
public final class ESBVolatileReference<T extends Object> {
    private final long at;
    private final T obj;

    public ESBVolatileReference(T obj) {
        this.at = System.currentTimeMillis();
        this.obj = obj;
    }

    public long getCreateAt() {
        return at;
    }

    public T get() {
        return obj;
    }
}
