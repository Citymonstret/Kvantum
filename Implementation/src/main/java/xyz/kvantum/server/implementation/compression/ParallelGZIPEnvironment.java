package xyz.kvantum.server.implementation.compression;

import lombok.NonNull;
import xyz.kvantum.server.implementation.cache.IterableThreadLocal;
import xyz.kvantum.server.implementation.cache.ThreadCache;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 *
 * @author shevek
 */
public class ParallelGZIPEnvironment {

    private static final Field fieldBuf;

    static {
        try {
            fieldBuf = DeflaterOutputStream.class.getDeclaredField("buf");
            fieldBuf.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static DeflaterOutputStream newDeflaterOutputStream(@NonNull OutputStream out, @NonNull Deflater deflater) {
        try {
            DeflaterOutputStream dos = new DeflaterOutputStream(out, deflater, 1, true);
            fieldBuf.set(dos, ThreadCache.BUFFER_8192.get());
            return dos;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return new DeflaterOutputStream(out, deflater, 512, true);
        }
    }

    private static class ThreadFactoryHolder {

        private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
            private final ThreadFactory defaultThreadFactory = Executors.defaultThreadFactory();
            private final AtomicLong counter = new AtomicLong(0);

            @Override
            public Thread newThread(@NonNull Runnable r) {
                Thread thread = defaultThreadFactory.newThread(r);
                thread.setName("parallelgzip-" + counter.getAndIncrement());
                thread.setDaemon(true);
                return thread;
            }
        };
    }

    @NonNull
    public static ThreadPoolExecutor newThreadPoolExecutor(int nthreads) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(nthreads, nthreads,
                1L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(nthreads * 20),
                ThreadFactoryHolder.THREAD_FACTORY,
                new ThreadPoolExecutor.CallerRunsPolicy());
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

    private static class ThreadPoolHolder {

        private static final ExecutorService EXECUTOR = newThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
    }

    @NonNull
    public static ExecutorService getSharedThreadPool() {
        return ThreadPoolHolder.EXECUTOR;
    }
}