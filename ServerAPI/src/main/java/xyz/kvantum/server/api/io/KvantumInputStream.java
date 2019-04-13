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
package xyz.kvantum.server.api.io;

import lombok.Getter;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.util.Assert;

import java.io.InputStream;

/**
 * Input stream that reads data from a {@link KvantumOutputStream}.
 * Reading from, and writing to the stream is synchronized.
 * {@inheritDoc}
 */
@SuppressWarnings("WeakerAccess") public class KvantumInputStream extends InputStream {

    private final Object lock = new Object();

    private final KvantumOutputStream kvantumOutputStream;
    private final int maxSize;
    private final byte[] bufferedData;

    @Getter private int totalRead = 0;
    private int bufferPointer;
    private volatile int availableData = 0;

    /**
     * Construct a new KvantumInputStream, using the file configured buffered
     * input size
     *
     * @param kvantumOutputStream Output stream to read from. Cannot be null.
     * @param maxSize             Size of the data that is to be read. The stream will never
     *                            read beyond this point. Has to be positive.
     */
    public KvantumInputStream(final KvantumOutputStream kvantumOutputStream, final int maxSize) {
        this(kvantumOutputStream, CoreConfig.Buffer.in, maxSize);
    }

    /**
     * Construct a new KvantumInputStream
     *
     * @param kvantumOutputStream Output stream to read from. Cannot be null.
     * @param bufferSize          Size of the read buffer. Has to be positive.
     * @param maxSize             Size of the data that is to be read. The stream will never
     *                            read beyond this point. Has to be positive.
     */
    public KvantumInputStream(final KvantumOutputStream kvantumOutputStream, final int bufferSize, final int maxSize) {
        this.kvantumOutputStream = Assert.notNull(kvantumOutputStream, "output stream");
        this.maxSize = Assert.isPositive(maxSize);
        this.bufferedData = new byte[Assert.isPositive(bufferSize)];
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
