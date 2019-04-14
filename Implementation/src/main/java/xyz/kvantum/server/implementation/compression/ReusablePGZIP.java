package xyz.kvantum.server.implementation.compression;

import lombok.NonNull;
import xyz.kvantum.server.implementation.cache.ThreadCache;

import java.io.IOException;
import java.io.OutputStream;

public class ReusablePGZIP extends ParallelGZIPOutputStream {
    public ReusablePGZIP() throws IOException {
        super(new ReusableByteArrayOutputStream(ThreadCache.COMPRESS_BUFFER.get()));
    }
}
