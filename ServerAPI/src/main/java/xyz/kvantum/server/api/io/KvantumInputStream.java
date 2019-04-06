package xyz.kvantum.server.api.io;

import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.util.Assert;

import java.io.InputStream;

@SuppressWarnings("WeakerAccess") public class KvantumInputStream extends InputStream {

    private final KvantumOutputStream kvantumOutputStream;
    private final int bufferSize;
    private final int maxSize;

    private int totalRead = 0;
    private int bufferPointer;
    private byte[] bufferedData;

    public KvantumInputStream(final KvantumOutputStream kvantumOutputStream, final int maxSize) {
        this(kvantumOutputStream, CoreConfig.Buffer.in, maxSize);
    }

    public KvantumInputStream(final KvantumOutputStream kvantumOutputStream, final int bufferSize, final int maxSize) {
        Assert.notNull(kvantumOutputStream, "output stream");
        this.kvantumOutputStream = kvantumOutputStream;
        this.bufferSize = bufferSize;
        this.maxSize = maxSize;
    }

    private int readData() {
        if (this.kvantumOutputStream.isFinished()) {
            return -1;
        }
        final int offer = this.kvantumOutputStream.getOffer();
        if (offer == -1) {
            return -1;
        }
        final int readable;
        if (this.bufferSize != -1) {
            readable = Math.min(this.bufferSize, offer);
        } else {
            readable = offer;
        }
        this.bufferedData = this.kvantumOutputStream.read(readable);
        this.bufferPointer = 0;
        return this.bufferedData.length;
    }

    @Override public int read() {
        // This is here to ensure that the stream NEVER exceeds the upper limit
        // if (this.totalRead >= this.maxSize) {
        //     Logger.info("EXCEEDING MAX SIZE ({0} >= {0})", this.totalRead, this.maxSize);
        //     return -1;
        // }
        if (this.bufferedData == null || this.bufferPointer == bufferedData.length) {
            if (this.readData() <= 0) {
                return -1;
            }
        }
        totalRead++;
        return this.bufferedData[bufferPointer++];
    }

}
