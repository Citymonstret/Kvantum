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

import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.util.AsciiString;

public class ThreadCache {

    private static final byte[] CRLF = AsciiString.of("\r\n").getValue();
    private static final int MAX_LENGTH =
        AsciiString.of(Integer.toHexString(Integer.MAX_VALUE)).length() + (2 * CRLF.length);

    public static void clear() {
        CHUNK_BUFFER.clean();
        COMPRESS_BUFFER.clean();
    }

    public static Thread[] getThreads() {
        ThreadGroup rootGroup = Thread.currentThread( ).getThreadGroup( );
        ThreadGroup parentGroup;
        while ( ( parentGroup = rootGroup.getParent() ) != null ) {
            rootGroup = parentGroup;
        }
        Thread[] threads = new Thread[ rootGroup.activeCount() ];
        if (threads.length != 0) {
            while (rootGroup.enumerate(threads, true) == threads.length) {
                threads = new Thread[threads.length * 2];
            }
        }
        return threads;
    }

    public static final IterableThreadLocal<byte[]> CHUNK_BUFFER = new IterableThreadLocal<byte[]>() {
        @Override public byte[] init() {
            return new byte[getMaxLen()];
        }
    };

    public static final IterableThreadLocal<byte[]> COMPRESS_BUFFER = new IterableThreadLocal<byte[]>() {
        @Override public byte[] init() {
            return new byte[getMaxLen() + 1024]; // TODO is 1024 large enough to handle data that can't be compressed?
        }
    };

    public static final IterableThreadLocal<byte[]> BUFFER_8192 = new IterableThreadLocal<byte[]>() {
        @Override public byte[] init() {
            return new byte[8192];
        }
    };

    private static int getMaxLen() {
        int toRead = CoreConfig.Buffer.out - MAX_LENGTH;
        if (toRead <= 0) {
            Logger.warn("buffer.out is less than {}, configured value will be ignored", MAX_LENGTH);
            toRead = MAX_LENGTH + 1;
        }
        return toRead;
    }
}
