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
package xyz.kvantum.server.implementation.compression;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import lombok.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A multi-threaded version of {@link GZIPOutputStream}.
 * <p>
 * Originally published at https://github.com/shevek/parallelgzip, under the Apache 2.0 license
 * by author shevek, and modified by boy0001.
 *
 * @author shevek, boy0001
 */
@SuppressWarnings("NullableProblems") public class ParallelGZIPOutputStream
    extends FilterOutputStream {

    private static final int GZIP_MAGIC = 0x8b1f;
    private static final int SIZE = 64 * 1024;

    private static Deflater newDeflater() {
        return new Deflater(Deflater.BEST_SPEED, true);
    }

    /**
     * This ThreadLocal avoids the recycling of a lot of memory, causing lumpy performance.
     */
    private static final ThreadLocal<State> STATE = ThreadLocal.withInitial(State::new);
    @NonNull private Block block = new Block();
    @CheckForNull private Block freeBlock = null;
    /**
     * Used as a sentinel for 'closed'.
     */
    private long bytesWritten = 0;


    // Master thread only
    @Deprecated // Doesn't really use the given number of threads.
    public ParallelGZIPOutputStream(@NonNull OutputStream out, @NonNull ExecutorService executor,
        int nThreads) throws IOException {
        super(out);
        this.executor = executor;
        // Some blocks compress faster than others; allow a long enough queue to keep all CPUs busy at least for a bit.
        this.emitQueueSize = nThreads * 3;
        this.emitQueue = new ArrayBlockingQueue<>(emitQueueSize);
        writeHeader();
    }


    private static int getThreadCount(@NonNull ExecutorService executor) {
        if (executor instanceof ThreadPoolExecutor)
            return ((ThreadPoolExecutor) executor).getMaximumPoolSize();
        return Runtime.getRuntime().availableProcessors();
    }

    // TODO: Share, daemonize.
    private final ExecutorService executor;
    private final CRC32 crc = new CRC32();
    private final int emitQueueSize;
    private final BlockingQueue<Future<Block>> emitQueue;

    /**
     * Creates a ParallelGZIPOutputStream
     * using {@link ParallelGZIPEnvironment#getSharedThreadPool()}.
     *
     * @param out the eventual output stream for the compressed data.
     * @throws IOException if it all goes wrong.
     */
    @Deprecated // Doesn't really use the given number of threads.
    public ParallelGZIPOutputStream(@NonNull OutputStream out, int nthreads) throws IOException {
        this(out, ParallelGZIPEnvironment.getSharedThreadPool(), nthreads);
    }

    private ParallelGZIPOutputStream(@NonNull OutputStream out, @NonNull ExecutorService executor)
        throws IOException {
        this(out, executor, getThreadCount(executor));
    }

    private static DeflaterOutputStream newDeflaterOutputStream(@NonNull final OutputStream out,
        @NonNull final Deflater deflater) {
        return ParallelGZIPEnvironment.newDeflaterOutputStream(out, deflater);
    }

    /*
     * @see http://www.gzip.org/zlib/rfc-gzip.html#file-format
     */
    private void writeHeader() throws IOException {
        out.write(new byte[] {(byte) GZIP_MAGIC, // ID1: Magic number (little-endian short)
            (byte) (GZIP_MAGIC >> 8), // ID2: Magic number (little-endian short)
            Deflater.DEFLATED, // CM: Compression method
            0, // FLG: Flags (byte)
            0, 0, 0, 0, // MTIME: Modification time (int)
            0, // XFL: Extra flags
            3 // OS: Operating system (3 = Linux)
        });
    }

    public void reset() throws IOException {
        crc.reset();
        freeBlock = null;
        block.reset();
        bytesWritten = 0;
        emitQueue.clear();
        writeHeader();
    }

    // Master thread only
    @Override public void write(int b) throws IOException {
        byte[] single = new byte[1];
        single[0] = (byte) (b & 0xFF);
        write(single);
    }

    // Master thread only
    @Override public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    /**
     * Creates a ParallelGZIPOutputStream
     * using {@link ParallelGZIPEnvironment#getSharedThreadPool()}.
     *
     * @param out the eventual output stream for the compressed data.
     * @throws IOException if it all goes wrong.
     */
    public ParallelGZIPOutputStream(@NonNull OutputStream out) throws IOException {
        this(out, ParallelGZIPEnvironment.getSharedThreadPool());
    }

    // Master thread only
    @Override public void write(byte[] b, int off, int len) throws IOException {
        crc.update(b, off, len);
        bytesWritten += len;

        while (len > 0) {
            final byte[] blockBuf = block.buf;
            // assert block.in_length < block.in.length
            int capacity =
                SIZE - block.bufLength; // Make sure we don't grow the block buf repeatedly.
            if (len >= capacity) {
                System.arraycopy(b, off, blockBuf, block.bufLength, capacity);
                block.bufLength += capacity;   // == block.in.length
                off += capacity;
                len -= capacity;
                submit();
            } else {
                System.arraycopy(b, off, blockBuf, block.bufLength, len);
                block.bufLength += len;
                // off += len;
                // len = 0;
                break;
            }
        }
    }

    // Emit If Available - submit always
    // Emit At Least one - submit when executor is full
    // Emit All Remaining - flush(), close()
    // Master thread only
    private void tryEmit() throws IOException, InterruptedException, ExecutionException {
        for (; ; ) {
            Future<Block> future = emitQueue.peek();
            // LOG.info("Peeked future " + future);
            if (future == null) {
                return;
            }
            if (!future.isDone()) {
                return;
            }
            // It's an ordered queue. This MUST be the same element as above.
            Block b = emitQueue.remove().get();
            // System.out.println("Chance-emitting block " + b);
            out.write(b.buf, 0, b.bufLength);
            b.bufLength = 0;
            freeBlock = b;
        }
    }

    /**
     * Emits any opportunistically available blocks. Furthermore, emits blocks until the number of executing tasks is less than taskCountAllowed.
     */
    private void emitUntil(int taskCountAllowed) throws IOException {
        try {
            while (emitQueue.size() > taskCountAllowed) {
                // LOG.info("Waiting for taskCount=" + emitQueue.getCount() + " -> " + taskCountAllowed);
                Block b = emitQueue.remove().get();  // Valid because emitQueue.getCount() > 0
                // System.out.println("Force-emitting block " + b);
                out.write(b.buf, 0, b.bufLength);  // Blocks until this task is done.
                b.bufLength = 0;
                freeBlock = b;
            }
            // We may have achieved more opportunistically available blocks
            // while waiting for a block above. Let's emit them here.
            tryEmit();
        } catch (ExecutionException e) {
            throw new IOException(e);
        } catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
    }

    // Master thread only
    @Override public void flush() throws IOException {
        // LOG.info("Flush: " + block);
        if (block.bufLength > 0) {
            submit();
        }
        emitUntil(0);
        super.flush();
    }

    // Master thread only
    private void submit() throws IOException {
        emitUntil(emitQueueSize - 1);
        emitQueue.add(executor.submit(block));
        Block b = freeBlock;
        if (b != null)
            freeBlock = null;
        else
            b = new Block();
        block = b;
    }

    // Master thread only
    @Override public void close() throws IOException {
        // LOG.info("Closing: bytesWritten=" + bytesWritten);
        if (bytesWritten >= 0) {
            flush();

            try (final OutputStream outputStream = out) {
                newDeflaterOutputStream(outputStream, newDeflater()).finish();

                final ByteBuffer buf = ByteBuffer.allocate(8);
                buf.order(ByteOrder.LITTLE_ENDIAN);
                // LOG.info("CRC is " + crc.getValue());
                buf.putInt((int) crc.getValue());
                buf.putInt((int) (bytesWritten % 4294967296L));
                outputStream.write(buf.array()); // allocate() guarantees a backing array.
                // LOG.info("trailer is " + Arrays.toString(buf.array()));

                outputStream.flush();
            }

            bytesWritten = Integer.MIN_VALUE;
            // } else {
            // LOG.warn("Already closed.");

            freeBlock = null;
        }
    }

    // Master thread only


    /* Allow write into byte[] directly */
    private static class ByteArrayOutputStreamExposed extends ByteArrayOutputStream {

        ByteArrayOutputStreamExposed(int size) {
            super(size);
        }

        void writeTo(@NonNull byte[] buf) {
            System.arraycopy(this.buf, 0, buf, 0, count);
        }
    }


    private static class State {

        private final Deflater def = newDeflater();
        private final ByteArrayOutputStreamExposed buf =
            new ByteArrayOutputStreamExposed(SIZE + (SIZE >> 3));
        private final DeflaterOutputStream str = newDeflaterOutputStream(buf, def);
    }


    private static class Block implements Callable<Block> {

        // private final int index;
        private byte[] buf = new byte[SIZE + (SIZE >> 3)];
        private int bufLength = 0;

        void reset() {
            bufLength = 0;
        }

        /*
         public Block( int index) {
         this.index = index;
         }
         */
        // Only on worker thread
        @Override public Block call() throws IOException {
            // LOG.info("Processing " + this + " on " + Thread.currentThread());

            State state = STATE.get();
            // ByteArrayOutputStream buf = new ByteArrayOutputStream(in.length);   // Overestimate output getCount required.
            // DeflaterOutputStream def = newDeflaterOutputStream(buf);
            state.def.reset();
            state.buf.reset();
            state.str.write(buf, 0, bufLength);
            state.str.flush();

            // int in_length = bufLength;
            int outLength = state.buf.size();
            if (outLength > buf.length)
                this.buf = new byte[outLength];
            // System.out.println("Compressed " + in_length + " to " + out_length + " bytes.");
            this.bufLength = outLength;
            state.buf.writeTo(buf);

            // return Arrays.copyOf(in, in_length);
            return this;
        }

        @Override public String toString() {
            return "Block" /* + index */ + "(" + bufLength + "/" + buf.length + " bytes)";
        }
    }

}
