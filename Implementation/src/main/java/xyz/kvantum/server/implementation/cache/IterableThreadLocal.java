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
package xyz.kvantum.server.implementation.cache;

import java.lang.ref.Reference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

public abstract class IterableThreadLocal<T> extends ThreadLocal<T> implements Iterable<T> {

    private ThreadLocal<T> flag;
    private ConcurrentLinkedDeque<T> allValues = new ConcurrentLinkedDeque<T>();

    @Override protected final T initialValue() {
        T value = init();
        if (value != null) {
            allValues.add(value);
        }
        return value;
    }

    @Override public final Iterator<T> iterator() {
        return getAll().iterator();
    }

    public T init() {
        return null;
    }

    public void clean() {
        IterableThreadLocal.clean(this);
    }

    public static void clean(ThreadLocal instance) {
        try {
            Thread[] threads = ThreadCache.getThreads();
            Field tl = Thread.class.getDeclaredField("threadLocals");
            tl.setAccessible(true);
            Method methodRemove = null;
            for (Thread thread : threads) {
                if (thread != null) {
                    Object tlm = tl.get(thread);
                    if (tlm != null) {
                        if (methodRemove == null) {
                            methodRemove = tlm.getClass().getDeclaredMethod("remove", ThreadLocal.class);
                            methodRemove.setAccessible(true);
                        }
                        if (methodRemove != null) {
                            try {
                                methodRemove.invoke(tlm, instance);
                            } catch (Throwable ignore) {}
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void cleanAll() {
        try {
            // Get a reference to the thread locals table of the current thread
            Thread thread = Thread.currentThread();
            Field threadLocalsField = Thread.class.getDeclaredField("threadLocals");
            threadLocalsField.setAccessible(true);
            Object threadLocalTable = threadLocalsField.get(thread);

            // Get a reference to the array holding the thread local variables inside the
            // ThreadLocalMap of the current thread
            Class threadLocalMapClass = Class.forName("java.lang.ThreadLocal$ThreadLocalMap");
            Field tableField = threadLocalMapClass.getDeclaredField("table");
            tableField.setAccessible(true);
            Object table = tableField.get(threadLocalTable);

            // The key to the ThreadLocalMap is a WeakReference object. The referent field of this object
            // is a reference to the actual ThreadLocal variable
            Field referentField = Reference.class.getDeclaredField("referent");
            referentField.setAccessible(true);

            for (int i = 0; i < Array.getLength(table); i++) {
                // Each entry in the table array of ThreadLocalMap is an Entry object
                // representing the thread local reference and its value
                Object entry = Array.get(table, i);
                if (entry != null) {
                    // Get a reference to the thread local object and remove it from the table
                    ThreadLocal threadLocal = (ThreadLocal)referentField.get(entry);
                    clean(threadLocal);
                }
            }
        } catch(Exception e) {
            // We will tolerate an exception here and just log it
            throw new IllegalStateException(e);
        }
    }

    public final Collection<T> getAll() {
        return Collections.unmodifiableCollection(allValues);
    }

    @Override protected void finalize() throws Throwable {
        clean(this);
        super.finalize();
    }
}
