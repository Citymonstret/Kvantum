/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2018 Alexander Söderberg
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
package xyz.kvantum.server.api.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * Our own "smart" implementation of AutoCloseable. Extend this class to act on server shutdown.
 * <p>
 * Uses WeakReferences, so this will not force your objects to remain loaded.
 * <p>
 * This class implements {@link java.lang.AutoCloseable} so any {@link AutoCloseable} objects may be used in
 * try-with-resources
 */
public abstract class AutoCloseable implements java.lang.AutoCloseable {

    private static final Collection<WeakReference<AutoCloseable>> closeable = new ArrayList<>();
    private static Consumer<WeakReference<AutoCloseable>> close = reference -> {
        //noinspection ConstantConditions
        reference.get().close();
    };

    private boolean closed = false;

    protected AutoCloseable() {
        closeable.add(new WeakReference<>(this));
    }

    /**
     * Close all loaded AutoCloseables
     */
    public static void closeAll() {
        closeable.stream().filter(AutoCloseable::exists).forEach(close);
    }

    private static <T> boolean exists(final WeakReference<T> reference) {
        return reference.get() != null;
    }

    /**
     * This is where you define what your object should do, when the server requests {@link #close()}
     */
    protected abstract void handleClose();

    /**
     * Close the AutoCloseable Can only be called once per instance
     */
    final public void close() {
        if (closed) {
            return;
        }
        this.handleClose();
        this.closed = true;
    }

}
