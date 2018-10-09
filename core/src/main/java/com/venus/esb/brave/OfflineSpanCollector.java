package com.venus.esb.brave;

import com.github.kristofa.brave.SpanCollector;
import com.venus.esb.lang.ESBVolatileReference;
import com.venus.esb.sign.ESBUUID;
import com.venus.esb.utils.ProcessUtils;
import com.twitter.zipkin.gen.Span;
import com.twitter.zipkin.gen.SpanCodec;
import org.slf4j.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.concurrent.*;
import java.util.logging.*;
import java.util.logging.Logger;

/**
 * Created by lingminjun on 17/9/11.
 */
public final class OfflineSpanCollector implements SpanCollector,Closeable {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(OfflineSpanCollector.class);

    private static final long FlUSH_INTERVAL = 1000l;//1秒
    private static final long SWITCH_INTERVAL = 60 * 1000l;//1分钟上报一次
    private static final int LOG_FILE_MAX_SIZE = 1*1024*1024;//log文件大小
    private static final int LOG_FILE_MAX_COUNT = 10000;//log文件大小

    private final Logger log;
    private final String baseUrl;
    private final String dir;

    private final SpanCodec spanCodec = SpanCodec.JSON;//用于json序列化
    private final BlockingQueue<Span> pending = new LinkedBlockingQueue(1000);//防止积压,尽量保持
    private ESBVolatileReference<FileHandler> handler;//用于处理日志写入

    final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    public static OfflineSpanCollector create(String baseUrl, String pathDir) {
        return new OfflineSpanCollector(baseUrl, pathDir);
    }

    @Override
    public void close() throws IOException {
        scheduler.shutdown();
    }

    private static class SpanLoggerHander extends Formatter {
        @Override
        public String format(LogRecord record) {
            return record.getMessage()+"\n";
        }
    }

    OfflineSpanCollector(String baseUrl, String pathDir) {

        String dirPath = pathDir;
        if (dirPath == null || dirPath.length() == 0) {
            dirPath = System.getProperties().getProperty("user.home") + "/logs/brave/";
            if (!File.separator.equals("/")) {//防止windows
                dirPath = System.getProperties().getProperty("user.home") + "\\logs\\brave\\";
            }
        } else if (!dirPath.endsWith(File.separator)) {
            dirPath = dirPath + File.separator;
        }

        //加上进程号区分引用
        this.dir = dirPath + ESBUUID.getProcessID() + File.separator;
        File logFile = new File(this.dir);
        if (logFile.exists() == false) {
            logFile.mkdirs();
        }

        this.baseUrl = baseUrl;
//        this.url = baseUrl + (baseUrl.endsWith("/")?"":"/") + "api/v1/spans";

        Logger log = Logger.getLogger(OfflineSpanCollector.class.getName());

        log.setLevel(Level.INFO);

        //debug情况时打印出来
        log.setUseParentHandlers(false);//不在输出控制台

        this.log = log;

        //开启work线程,等待事务进来
        scheduler.execute(working);

        //启动监听进程
        if (baseUrl == null || baseUrl.length() > 0) {
            boolean debug = false;//上线注释掉
            ProcessUtils.fork(HttpBrave.class, new String[]{baseUrl, dirPath}, true, debug);
        }
    }

    //必须单线程调用,所以不考虑线程问题
    protected final Logger getLogger() {
        //判断是否间隔时间
        long now = System.currentTimeMillis();
        //说明超过一分钟了,需要返回一个logger文件
        if (handler == null || handler.getCreateAt() + SWITCH_INTERVAL < now) {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
            String trace = sdf.format(System.currentTimeMillis());
            String file = this.dir + trace + "-%g.log";//文件名
            FileHandler fileHandler = null;
            try {
                fileHandler = new FileHandler(file,LOG_FILE_MAX_SIZE, LOG_FILE_MAX_COUNT, false);
            } catch (Throwable e) {
                logger.error("zipkin brave logger 初始化失败, 请确认brave目录" + file + "设置是正确的", e);
                return log;
            }

            if (fileHandler != null) {
                fileHandler.setLevel(Level.INFO);
                fileHandler.setFormatter(new SpanLoggerHander());

                FileHandler oldler = null;
                if (handler != null) {
                    oldler = handler.get();
                }
                log.addHandler(fileHandler);//先加入,保证数据不遗漏,日志分析端需要去重

                handler = new ESBVolatileReference<FileHandler>(fileHandler);

                if (oldler != null) {
                    log.removeHandler(oldler);
                    oldler.close();
                }
            }

//            System.out.println("统计初始化一个handler所发的时间:"+ (System.currentTimeMillis()-now));
            return log;

        } else {
            return log;
        }
    }

    public void collect(final Span span) {
        if (span != null) {
            if(!this.pending.offer(span)) {
                this.scheduler.scheduleWithFixedDelay(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            collect(span);
                        } catch (Throwable e) {}
                    }
                }, 0l, (long) FlUSH_INTERVAL, TimeUnit.MILLISECONDS);
            }
        }
    }

    private void offline(Span span) {
        byte[] bytes = spanCodec.writeSpan(span);
        if (bytes != null) {

//            try {
//                HttpBrave.post(this.url,new String(bytes));
//            } catch (Throwable e) {}

            String js = new String(bytes);
            this.getLogger().info(js+",");
        }
    }

    private Runnable working = new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    //注意此处
                    Span span = pending.take(); // 等到有数据才继续
//                    long now = System.currentTimeMillis();
//                    System.out.println(">>>>>>>> " + span.getName());
                    offline(span);
//                    System.out.println("======== " + span.getName() + " " + (System.currentTimeMillis() - now));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    logger.error("写入span到磁盘终端", e);
                    break;
                } catch (Throwable e) {
                    e.printStackTrace();
                    logger.error("写入span到磁盘失败", e);
                }
            }
        }
    };

    @Deprecated
    public void addDefaultAnnotation(String key, String value) {
        throw new UnsupportedOperationException();
    }
}
