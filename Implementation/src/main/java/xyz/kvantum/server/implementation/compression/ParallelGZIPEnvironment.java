/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2019 Alexander Söderberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.kvantum.server.implementation.compression;


import lombok.NonNull;
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
 * Originally published at https://github.com/shevek/parallelgzip, under the Apache 2.0 license
 * by author shevek, and modified by boy0001.
 *
 * @author shevek, boy0001
 */
class ParallelGZIPEnvironment {

    private static final Field fieldBuf;

    static {
        try {
            fieldBuf = DeflaterOutputStream.class.getDeclaredField("buf");
            fieldBuf.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static DeflaterOutputStream newDeflaterOutputStream(@NonNull final OutputStream out,
        @NonNull final Deflater deflater) {
        try {
            DeflaterOutputStream dos = new DeflaterOutputStream(out, deflater, 1, true);
            fieldBuf.set(dos, ThreadCache.BUFFER_8192.get());
            return dos;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return new DeflaterOutputStream(out, deflater, 512, true);
        }
    }

    private static ThreadPoolExecutor newThreadPoolExecutor(final int nThreads) {
        ThreadPoolExecutor executor =
            new ThreadPoolExecutor(nThreads, nThreads, 1L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(nThreads * 20), ThreadFactoryHolder.THREAD_FACTORY,
                new ThreadPoolExecutor.CallerRunsPolicy());
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

    public static ExecutorService getSharedThreadPool() {
        return ThreadPoolHolder.EXECUTOR;
    }


    private static class ThreadFactoryHolder {

        private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
            private final ThreadFactory defaultThreadFactory = Executors.defaultThreadFactory();
            private final AtomicLong counter = new AtomicLong(0);

            @Override public Thread newThread(final Runnable r) {
                Thread thread = defaultThreadFactory.newThread(r);
                thread.setName("kvantum-pgzip-" + counter.getAndIncrement());
                thread.setDaemon(true);
                return thread;
            }
        };
    }


    private static class ThreadPoolHolder {

        private static final ExecutorService EXECUTOR =
            newThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
    }

}
