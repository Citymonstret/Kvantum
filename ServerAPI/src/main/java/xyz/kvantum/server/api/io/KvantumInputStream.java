package xyz.kvantum.server.api.io;

import lombok.Getter;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.util.Assert;

import java.io.InputStream;

@SuppressWarnings("WeakerAccess") public class KvantumInputStream extends InputStream {

    private final Object lock = new Object();

    private final KvantumOutputStream kvantumOutputStream;
    private final int maxSize;
    private final byte[] bufferedData;

    @Getter private int totalRead = 0;
    private int bufferPointer;
    private volatile int availableData = 0;

    public KvantumInputStream(final KvantumOutputStream kvantumOutputStream, final int maxSize) {
        this(kvantumOutputStream, CoreConfig.Buffer.in, maxSize);
    }

    public KvantumInputStream(final KvantumOutputStream kvantumOutputStream, final int bufferSize, final int maxSize) {
        Assert.notNull(kvantumOutputStream, "output stream");
        this.kvantumOutputStream = kvantumOutputStream;
        this.maxSize = maxSize;
        this.bufferedData = new byte[bufferSize];
    }

    private int readData() {
        synchronized (this.lock) {
            if (this.kvantumOutputStream.isFinished() || this.kvantumOutputStream.getOffer() == -1) {
                return -1;
            }
            this.availableData = this.kvantumOutputStream.read(bufferedData);
            if (availableData == -1) {
                return -1;
            }
            // Reset read state
            this.bufferPointer = 0;
            return this.availableData;
        }
    }

    @Override public int available() {
        return this.maxSize - this.totalRead;
    }

    @Override public int read() {
        synchronized (this.lock) {
            // This is here to ensure that the stream NEVER exceeds the upper limit
            if (this.totalRead >= this.maxSize) {
                return -1;
            }

            if (this.availableData == -1) {
                return -1;
            } else if (this.availableData == 0 || this.bufferPointer == this.availableData) {
                if (this.readData() < 0) {
                    return -1;
                }
            }

            final int data = this.bufferedData[bufferPointer++] & 0xFF;

            totalRead++;
            return data;
        }
    }

}
