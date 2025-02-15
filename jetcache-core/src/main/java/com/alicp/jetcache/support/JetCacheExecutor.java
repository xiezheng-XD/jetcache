package com.alicp.jetcache.support;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created on 2017/5/3.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class JetCacheExecutor {
    protected volatile static ScheduledExecutorService defaultExecutor;
    protected volatile static ScheduledExecutorService heavyIOExecutor;

    private static AtomicInteger threadCount = new AtomicInteger(0);

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (defaultExecutor != null) {
                    defaultExecutor.shutdownNow();
                }
                if (heavyIOExecutor != null) {
                    heavyIOExecutor.shutdownNow();
                }
            }
        });
    }

    public static ScheduledExecutorService defaultExecutor() {
        if (defaultExecutor != null) {
            return defaultExecutor;
        }
        synchronized (JetCacheExecutor.class) {
            if (defaultExecutor == null) {
                ThreadFactory tf = r -> {
                    Thread t = new Thread(r, "JetCacheDefaultExecutor");
                    t.setDaemon(true);
                    return t;
                };
                defaultExecutor = new ScheduledThreadPoolExecutor(
                        1, tf, new ThreadPoolExecutor.DiscardPolicy());
            }
        }
        return defaultExecutor;
    }

    public static ScheduledExecutorService heavyIOExecutor() {
        if (heavyIOExecutor != null) {
            return heavyIOExecutor;
        }
        synchronized (JetCacheExecutor.class) {
            if (heavyIOExecutor == null) {
                ThreadFactory tf = r -> {
                    Thread t = new Thread(r, "JetCacheHeavyIOExecutor" + threadCount.getAndIncrement());
                    t.setDaemon(true);
                    return t;
                };
                heavyIOExecutor = new ScheduledThreadPoolExecutor(
                        10, tf, new ThreadPoolExecutor.DiscardPolicy());
            }
        }
        return heavyIOExecutor;
    }

    public static void setDefaultExecutor(ScheduledExecutorService executor) {
        JetCacheExecutor.defaultExecutor = executor;
    }

    public static void setHeavyIOExecutor(ScheduledExecutorService heavyIOExecutor) {
        JetCacheExecutor.heavyIOExecutor = heavyIOExecutor;
    }
}
