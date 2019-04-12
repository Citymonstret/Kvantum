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
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

/**
 * Custom stream implementation used to write HTTP response bodies
 */
@RequiredArgsConstructor public class KvantumOutputStream {

    private static final long MAX_WAIT = 500L; // Wait time of 500ms

    @Getter private boolean finished = false;

    @Getter private int read = 0;
    private int offer = -1;
    private Consumer<Integer> offerAction, finalizedAction;

    private byte[] buffer; // internal buffer

    /**
     * Get the last pushed offer (available data length)
     *
     * @return Length of available data
     */
    public int getOffer() {
        return this.offer;
    }

    /**
     * Mark the stream as finished, which means it cannot be written to anymore
     *
     * @throws IllegalStateException If the stream has already been finished.
     */
    public void finish() {
        if (this.finished) {
            throw new IllegalStateException("Cannot finish the stream when it's already finished");
        }
        this.finished = true;
    }

    /**
     * Offer a given amount of bytes to the receiver
     *
     * @param amount          Length of available data. Must be bigger than 0.
     * @param acceptedAction  Action to run when the client has accepted the offer
     * @param finalizedAction Action to run when the client has read the offered data
     * @throws IllegalArgumentException If the specified amount is less than or equal to 0.
     * @throws IllegalStateException    If the method is called after the stream has been marked as finished.
     */
    public void offer(final int amount, final Consumer<Integer> acceptedAction,
        final Consumer<Integer> finalizedAction) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be bigger than 0");
        }
        this.checkState();
        this.offer = amount;
        this.offerAction = acceptedAction;
        if (finalizedAction != null) {
            this.finalizedAction = finalizedAction;
        }
    }

    /**
     * Push data to the stream
     *
     * @param bytes Data. Length should not be larger than the offer. Cannot be null.
     * @throws IllegalArgumentException If the length of the pushed data exceeds the last offer.
     */
    public void push(final byte[] bytes) {
        if (bytes.length > this.offer) {
            throw new IllegalArgumentException("Pushed data size cannot be larger than offer");
        }
        this.buffer = bytes;
    }

    /**
     * Read data from the stream, with a specified maximal length. Check return array size for actual length of read
     * data.
     *
     * @param buffer Buffer to write the data into, can't be null and has to have a non-zero length
     * @return the amount of data written, -1 if the stream is finished
     * @throws IllegalArgumentException If amount is less than or equal to 0.
     * @throws IllegalStateException    If content isn't written to the stream within 500ms of the method call.
     */
    public int read(byte[] buffer) {
        if (buffer.length <= 0) {
            throw new IllegalArgumentException("Amount must be bigger than 0");
        }

        if (this.finished) {
            return -1;
        }

        // Make sure that we don't try to read more than the offered data
        final int amount = Math.min(buffer.length, this.offer);

        if (this.offerAction != null) {
            this.offerAction.accept(amount);
        }
        // This makes sure that the offer has written to the stream, or else it'll block until MAX_TIME
        final long startTime = System.currentTimeMillis();
        while (this.buffer == null) {
            if (System.currentTimeMillis() - startTime > MAX_WAIT) {
                throw new IllegalStateException("Time out");
            }
        }

        read += this.buffer.length;

        this.offer = 0;
        this.offerAction = null;

        if (this.finalizedAction != null) {
            this.finalizedAction.accept(read);
        }

        System.arraycopy(this.buffer, 0, buffer, 0, amount);
        return amount;
    }

    private void checkState() {
        if (this.finished) {
            throw new IllegalStateException(
                "Cannot perform I/O actions on a closed response stream");
        }
    }

}
