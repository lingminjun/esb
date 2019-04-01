package com.venus.esb.lang;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.ServerSpan;
import com.venus.esb.brave.ESBBraveFactory;//高危依赖

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lingminjun on 15/9/14.
 */
public final class ESBDispatchQueue implements Closeable {
    private static final int CORE_POOL_COUNT = 10;//默认保留线程数，尽量反复利用已有空闲线程，而不是创建线程
    private static final Logger logger = LoggerFactory.getLogger(ESBDispatchQueue.class);

    /**
     * 构造方法
     */
    public ESBDispatchQueue() {
        this(null);
    }
    public ESBDispatchQueue(String name) {
        this(CORE_POOL_COUNT,name);
    }
    public ESBDispatchQueue(int corePoolSize, String name) {

        int thread_size = corePoolSize > CORE_POOL_COUNT ? corePoolSize : CORE_POOL_COUNT;

        rejectedHanlder = new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                logger.error("task.queue","A task is rejected. " + r.toString(), null);
            }
        };
//        SingleThreadExecutor
        pool = new ScheduledThreadPoolExecutor(thread_size,new CoreThreadFactory(name), rejectedHanlder);
    }

    /**
     * 通用并发队列
     * @return
     */
    private static ESBDispatchQueue common_queue = null;
    public static ESBDispatchQueue commonQueue() {
        if (common_queue != null) return common_queue;
        synchronized (ESBDispatchQueue.class) {
            if (common_queue == null) {
                common_queue = newTaskQueue(CORE_POOL_COUNT,"common");
            }
        }
        return common_queue;
    }


    /**
     * 异步执行事件方法
     * @param r
     */
    public ScheduledFuture<?> execute(Runnable r) {
        if (r != null) {
            return _execute(r);
        }
        return null;
    }

    /**
     * 异步执行事件方法
     * @param r
     * @param delayMillis 小于等于零时不做延迟
     */
    public ScheduledFuture<?> executeDelayed(final Runnable r, final long delayMillis) {
        if (r != null) {
            return _executeDelayed(r,delayMillis);
        }
        return null;
    }

    /**
     * 主要用于主线程等待执行
     * @param task
     * @return
     */
    public <T> Future<T> submit(Callable<T> task) {
        if (task != null) {
            return _submit(task);
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        if (pool != null) {
            pool.shutdown();
        }
    }

    /*****************一下为私有实现*******************/
    static class SaftyCallable<V> implements Callable<V> {
        public SaftyCallable(Callable runnable) {
            if (runnable == null) {throw new RuntimeException("Please make sure that the incoming effective Runnable");}
            _run = runnable;
            _tid = ESBThreadLocal.get(ESBSTDKeys.TID_KEY);
            _prev_cid = ESBThreadLocal.get(ESBSTDKeys.PARENT_CID_KEY);//直接取parent
            _l10n = ESBThreadLocal.get(ESBSTDKeys.L10N_KEY);
            _aid = ESBThreadLocal.get(ESBSTDKeys.AID_KEY);
            _did = ESBThreadLocal.get(ESBSTDKeys.DID_KEY);
            _uid = ESBThreadLocal.get(ESBSTDKeys.UID_KEY);
            _acct = ESBThreadLocal.get(ESBSTDKeys.ACCT_KEY);
            _pid = ESBThreadLocal.get(ESBSTDKeys.PID_KEY);
            _guid = ESBThreadLocal.get(ESBSTDKeys.GUID_KEY);
//            _mk = ESBThreadLocal.get(ESBSTDKeys.MOCK_FLAG_KEY);

            Brave brave = ESBBraveFactory.getBrave(false);
            if (brave != null) {
                _brave = brave;
                _span = brave.serverSpanThreadBinder().getCurrentServerSpan();
            }
        }

        private Callable<V> _run;
        private String _tid;
        private String _prev_cid;
        private String _aid;
        private String _did;
        private String _uid;
        private String _acct;
        private String _pid;
        private String _guid;
        private String _l10n;
//        private String _mk;
        private Brave _brave;
        private ServerSpan _span;

        @Override
        public V call() throws Exception {
            try {
                ESBContext.removeContext();//防止取到脏数据
                ESBThreadLocal.put(ESBSTDKeys.TID_KEY,_tid);
                ESBThreadLocal.put(ESBSTDKeys.PARENT_CID_KEY,_prev_cid);
                ESBThreadLocal.put(ESBSTDKeys.L10N_KEY,_l10n);
                ESBThreadLocal.put(ESBSTDKeys.AID_KEY,_aid);
                ESBThreadLocal.put(ESBSTDKeys.DID_KEY,_did);
                ESBThreadLocal.put(ESBSTDKeys.UID_KEY,_uid);
                ESBThreadLocal.put(ESBSTDKeys.ACCT_KEY,_acct);
                ESBThreadLocal.put(ESBSTDKeys.PID_KEY,_pid);
                ESBThreadLocal.put(ESBSTDKeys.GUID_KEY,_guid);
//                ESBThreadLocal.put(ESBSTDKeys.MOCK_FLAG_KEY,_mk);
                if (_span != null && _brave != null) {
                    _brave.serverSpanThreadBinder().setCurrentSpan(_span);
                }
                return _run.call();
            } catch (Throwable e) {
                logger.error("异步线程执行失败!!",e);
                e.printStackTrace();
            } finally {
                ESBThreadLocal.remove(ESBSTDKeys.TID_KEY);//记录tid,继续透传
                ESBThreadLocal.remove(ESBSTDKeys.PARENT_CID_KEY);
                ESBThreadLocal.remove(ESBSTDKeys.AID_KEY);
                ESBThreadLocal.remove(ESBSTDKeys.DID_KEY);
                ESBThreadLocal.remove(ESBSTDKeys.UID_KEY);
                ESBThreadLocal.remove(ESBSTDKeys.ACCT_KEY);
                ESBThreadLocal.remove(ESBSTDKeys.PID_KEY);
                ESBThreadLocal.remove(ESBSTDKeys.GUID_KEY);
//                ESBThreadLocal.remove(ESBSTDKeys.MOCK_FLAG_KEY);
            }
            return null;
        }
    }
    static class SaftyRunnable implements Runnable {
        public SaftyRunnable(Runnable runnable) {
            if (runnable == null) {throw new RuntimeException("Please make sure that the incoming effective Runnable");}
            _run = runnable;
            _tid = ESBThreadLocal.get(ESBSTDKeys.TID_KEY);
            _prev_cid = ESBThreadLocal.get(ESBSTDKeys.PARENT_CID_KEY);//直接取parent
            _l10n = ESBThreadLocal.get(ESBSTDKeys.L10N_KEY);
            _aid = ESBThreadLocal.get(ESBSTDKeys.AID_KEY);
            _did = ESBThreadLocal.get(ESBSTDKeys.DID_KEY);
            _uid = ESBThreadLocal.get(ESBSTDKeys.UID_KEY);
            _acct = ESBThreadLocal.get(ESBSTDKeys.ACCT_KEY);
            _pid = ESBThreadLocal.get(ESBSTDKeys.PID_KEY);
            _guid = ESBThreadLocal.get(ESBSTDKeys.GUID_KEY);
//            _mk = ESBThreadLocal.get(ESBSTDKeys.MOCK_FLAG_KEY);
            Brave brave = ESBBraveFactory.getBrave(false);
            if (brave != null) {
                _brave = brave;
                _span = brave.serverSpanThreadBinder().getCurrentServerSpan();
            }
        }

        private Runnable _run;
        private String _tid;
        private String _prev_cid;
        private String _aid;
        private String _did;
        private String _uid;
        private String _acct;
        private String _pid;
        private String _l10n;
        private String _guid;
//        private String _mk;
        private Brave _brave;
        private ServerSpan _span;

        @Override
        public void run() {
            try {
                ESBContext.removeContext();//防止取到脏数据
                ESBThreadLocal.put(ESBSTDKeys.TID_KEY,_tid);
                ESBThreadLocal.put(ESBSTDKeys.PARENT_CID_KEY,_prev_cid);
                ESBThreadLocal.put(ESBSTDKeys.L10N_KEY,_l10n);
                ESBThreadLocal.put(ESBSTDKeys.AID_KEY,_aid);
                ESBThreadLocal.put(ESBSTDKeys.DID_KEY,_did);
                ESBThreadLocal.put(ESBSTDKeys.UID_KEY,_uid);
                ESBThreadLocal.put(ESBSTDKeys.ACCT_KEY,_acct);
                ESBThreadLocal.put(ESBSTDKeys.PID_KEY,_pid);
                ESBThreadLocal.put(ESBSTDKeys.GUID_KEY,_guid);
//                ESBThreadLocal.put(ESBSTDKeys.MOCK_FLAG_KEY,_mk);
                if (_span != null && _brave != null) {
                    _brave.serverSpanThreadBinder().setCurrentSpan(_span);
                }
                _run.run();
            } catch (Throwable e) {
                logger.error("异步线程执行失败!!",e);
                e.printStackTrace();
            } finally {
                ESBThreadLocal.remove(ESBSTDKeys.TID_KEY);//记录tid,继续透传
                ESBThreadLocal.remove(ESBSTDKeys.PARENT_CID_KEY);
                ESBThreadLocal.remove(ESBSTDKeys.AID_KEY);
                ESBThreadLocal.remove(ESBSTDKeys.DID_KEY);
                ESBThreadLocal.remove(ESBSTDKeys.UID_KEY);
                ESBThreadLocal.remove(ESBSTDKeys.ACCT_KEY);
                ESBThreadLocal.remove(ESBSTDKeys.PID_KEY);
                ESBThreadLocal.remove(ESBSTDKeys.GUID_KEY);
//                ESBThreadLocal.remove(ESBSTDKeys.MOCK_FLAG_KEY);
            }
        }
    }

    private ScheduledFuture<?> _execute(Runnable r) {
        SaftyRunnable rr = new SaftyRunnable(r);
        if (pool != null) {
            return pool.schedule(rr,0,TimeUnit.MILLISECONDS);
        }
        return null;
    }

    private <T> Future<T> _submit(Callable<T> r) {
        SaftyCallable<T> rr = new SaftyCallable<T>(r);
        if (pool != null) {
            return pool.submit(r);
        }
        return null;
    }

    private ScheduledFuture<?> _executeDelayed(final Runnable r, final long delayMillis) {
        if (pool != null) {
            if (delayMillis <= 0) {
                return pool.schedule(new SaftyRunnable(r),0,TimeUnit.MILLISECONDS);
            }
            else {
                return pool.schedule(new SaftyRunnable(r),delayMillis,TimeUnit.MILLISECONDS);
            }
        }
        return null;
    }

    private static ESBDispatchQueue newTaskQueue(int corePoolSize, String name) {
        return new ESBDispatchQueue(corePoolSize,name);
    }

    private static class CoreThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        CoreThreadFactory(String name) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            String a_name = name != null ? name : "thread";
            namePrefix = "pool-" + poolNumber.getAndIncrement() + "-" + a_name + "-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY) t.setPriority(Thread.NORM_PRIORITY);

            t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

                @Override
                public void uncaughtException(Thread thread, Throwable ex) {
                    logger.error("dispatch.queue", thread.getName() + " thrown an exception : ", ex);
                }
            });

            return t;
        }
    }

    private RejectedExecutionHandler rejectedHanlder;
    private ScheduledThreadPoolExecutor pool;
}
