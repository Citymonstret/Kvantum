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
import lombok.NonNull;
import xyz.kvantum.server.api.response.KnownLengthStream;

import javax.annotation.Nonnull;

/**
 * Response stream with a single input write
 * {@inheritDoc}
 */
public class SimpleOutputStream extends KvantumOutputStream implements KnownLengthStream {

    @Getter private byte[] internalBytes;
    private int read = 0;

    public SimpleOutputStream(@Nonnull final byte[] bytes) {
        this.internalBytes = new byte[bytes.length];
        System.arraycopy(bytes, 0, this.internalBytes, 0, bytes.length);
    }

    @Override public int read(@Nonnull final byte[] buffer) {
        final int toRead = Math.min(this.getOffer(), buffer.length);
        System.arraycopy(this.internalBytes, read, buffer, 0, toRead);
        this.read += toRead;
        if (this.internalBytes.length <= this.read) {
            this.finish();
        }
        return toRead;
    }

    @Override public int getOffer() {
        return this.internalBytes.length - read;
    }

    @Override public int getLength() {
        return this.getInternalBytes().length;
    }

    @Nonnull @Override public byte[] getAll() {
        return this.getInternalBytes();
    }

    @Override public void replaceBytes(@NonNull final byte[] bytes) {
        this.internalBytes = bytes;
        this.read = 0;
    }

}
