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
package xyz.kvantum.server.api.event;

import lombok.NonNull;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.util.ListMultiMap;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple {@link EventBus} implementation using a cached thread pool to handle
 * asynchronous events
 * {@inheritDoc}
 */
public final class SimpleEventBus extends EventBus {

    private final Object lock = new Object();

    private final ExecutorService executorService;
    private final ListMultiMap<String, ListenerMethod> listenerMethodMultimap =
        new ListMultiMap<>();

    public SimpleEventBus() {
        super(true);
        this.executorService = Executors.newCachedThreadPool(new ThreadFactory() {
            private AtomicInteger number = new AtomicInteger(1);

            @Override public Thread newThread(Runnable runnable) {
                final Thread thread = new Thread(runnable,
                    String.format("kvantum-events-%s", number.getAndIncrement()));
                thread.setDaemon(true);
                return thread;
            }
        });
    }

    @Override protected void registerListenersInternally(
        Collection<ListenerMethod> listenerMethods) {
        synchronized (this.lock) {
            for (final ListenerMethod listenerMethod : listenerMethods) {
                if (listenerMethodMultimap
                    .containsEntry(getClassName(listenerMethod.getEventType()), listenerMethod)) {
                    Logger.error(
                        "Listener method with name {} has already been registered in event bus. Skipping.",
                        listenerMethod.toString());
                    return;
                }
                listenerMethodMultimap
                    .put(getClassName(listenerMethod.getEventType()), listenerMethod);
            }
        }
    }

    public final Collection<ListenerMethod> getMethods(final String eventType) {
        final Collection<ListenerMethod> methods;
        synchronized (this.lock) {
            methods = this.listenerMethodMultimap.get(eventType);
        }
        return methods;
    }


    private <T> Callable<T> createRunnable(@NonNull final Collection<ListenerMethod> methods,
        final T event) {
        return () -> {
            for (final ListenerMethod method : methods) {
                method.invoke(event);
            }
            return event;
        };
    }

    private String getClassName(final Class clazz) {
        return clazz.getName();
    }

    @Override protected <T> Future<T> throwAsync(T event) {
        final Collection<ListenerMethod> methods = getMethods(getClassName(event.getClass()));
        return this.executorService.submit(createRunnable(methods, event));
    }

    @Override protected <T> T throwSync(T event) {
        try {
            final Collection<ListenerMethod> methods = getMethods(getClassName(event.getClass()));
            return this.createRunnable(methods, event).call();
        } catch (final Throwable throwable) {
            Logger.error("Failed to call event of type {}", event.getClass());
            throwable.printStackTrace();
        }
        return event;
    }

}
