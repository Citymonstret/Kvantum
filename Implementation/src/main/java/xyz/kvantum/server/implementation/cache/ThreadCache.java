package xyz.kvantum.server.implementation.cache;

import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.implementation.KvantumServerHandler;

public class ThreadCache {
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
        @Override
        public byte[] init() {
            return new byte[getMaxLen()];
        }
    };

    public static final IterableThreadLocal<byte[]> COMPRESS_BUFFER = new IterableThreadLocal<byte[]>() {
        @Override
        public byte[] init() {
            return new byte[getMaxLen() + 1024]; // TODO is 1024 large enough to handle data that can't be compressed?
        }
    };

    public static final IterableThreadLocal<byte[]> BUFFER_8192 = new IterableThreadLocal<byte[]>() {
        @Override
        public byte[] init() {
            return new byte[8192];
        }
    };

    private static int getMaxLen() {
        int toRead = CoreConfig.Buffer.out - KvantumServerHandler.MAX_LENGTH;
        if (toRead <= 0) {
            Logger.warn("buffer.out is less than {}, configured value will be ignored",
                    KvantumServerHandler.MAX_LENGTH);
            toRead = KvantumServerHandler.MAX_LENGTH + 1;
        }
        return toRead;
    }
}