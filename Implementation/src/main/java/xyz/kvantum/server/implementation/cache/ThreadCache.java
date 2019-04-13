package xyz.kvantum.server.implementation.cache;

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
        public int[] init() {
            return new byte[getMaxLen()];
        }
    };

    public static final IterableThreadLocal<byte[]> COMPRESS_BUFFER = new IterableThreadLocal<byte[]>() {
        @Override
        public int[] init() {
            return new byte[getMaxLen()];
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