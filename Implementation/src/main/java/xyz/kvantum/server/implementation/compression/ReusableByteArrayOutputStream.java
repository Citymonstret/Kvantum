package xyz.kvantum.server.implementation.compression;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class ReusableByteArrayOutputStream extends OutputStream {
    private final byte[] buf;
    private int count;

    public ReusableByteArrayOutputStream(byte[] buffer) {
        this.buf = buffer;
    }

    public void write(int b) {
        buf[count++] = (byte) b;
    }
    public void write(byte b[], int off, int len) {
        System.arraycopy(b, off, buf, count, len);
        count += len;
    }


    public void reset() {
        count = 0;
    }

    public byte[] getBuffer() {
        return buf;
    }

    public byte[] toByteArray() {
        return Arrays.copyOf(buf, count);
    }

    public int size() {
        return count;
    }
    public void close() throws IOException {
    }

}
