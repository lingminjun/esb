package com.venus.esb.lang;

/**
 * Created by lingminjun on 17/6/17.
 * 缓存易变化的临时参数,主要配合 volatile 标识符一起使用
 */
public final class ESBVolatileReference<T extends Object> {
    private final long at;
    private final long exp;
    private final T obj;

    public ESBVolatileReference(T obj) {
        this.at = System.currentTimeMillis();
        this.exp = 0;
        this.obj = obj;
    }

    public ESBVolatileReference(T obj, long age/*ms*/) {
        this.at = System.currentTimeMillis();
        this.exp = this.at + age;
        this.obj = obj;
    }

    public long getCreateAt() {
        return at;
    }

    public boolean isExpired() {
        if (exp <= 0) {
            return false;
        }
        return System.currentTimeMillis() >= this.exp;
    }

    public T get() {
        if (isExpired()) {return null;}
        return obj;
    }

    public T access() {
        return obj;
    }
}
