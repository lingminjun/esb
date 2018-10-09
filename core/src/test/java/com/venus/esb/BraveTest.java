package com.venus.esb;

import com.venus.esb.brave.OfflineSpanCollector;
import com.venus.esb.lang.ESBDispatchQueue;
import com.venus.esb.utils.ProcessUtils;
import com.twitter.zipkin.gen.Span;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by lingminjun on 17/9/14.
 */
public class BraveTest {


    @Test
    public void concurrenceTest() {
        final ESBDispatchQueue queue = new ESBDispatchQueue(1000,"brave");
        final OfflineSpanCollector spanCollector = OfflineSpanCollector.create("http://127.0.0.1",null);


        final long now = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            final int finalI = i;
            queue.execute(new Runnable() {
                @Override
                public void run() {
                    Span span = new Span();
                    span.setName("test"+ finalI);
                    span.setTimestamp(System.currentTimeMillis());
                    span.setTrace_id(finalI);
                    span.setId(finalI);
                    spanCollector.collect(span);
                    System.out.println("" + finalI + "time:" + (System.currentTimeMillis() - now));
                }
            });
        }

        try {
            Thread.sleep(10*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void multFileTest() {
        final ESBDispatchQueue queue = new ESBDispatchQueue(1000,"brave");
        final OfflineSpanCollector spanCollector = OfflineSpanCollector.create("http://127.0.0.1",null);


        for (int i = 0; i < 1000; i++) {
            final int finalI = i;

            Span span = new Span();
            span.setName("test"+ finalI);
            span.setTimestamp(System.currentTimeMillis());
            span.setTrace_id(finalI);
            span.setId(finalI);

            final long now = System.currentTimeMillis();
            spanCollector.collect(span);
            System.out.println("" + finalI + "time:" + (System.currentTimeMillis() - now));

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    @Test
    public void cmdTest() throws IOException {
        //无法获得root权限
        String out = ProcessUtils.exec("sudo ps -ef -u lingminjun -p lingminjun",1000);
        System.out.println(">>"+out);
    }
}
