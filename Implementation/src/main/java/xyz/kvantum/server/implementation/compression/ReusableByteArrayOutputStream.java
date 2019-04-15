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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.OutputStream;
import java.util.Arrays;

@SuppressWarnings("NullableProblems") @RequiredArgsConstructor
public final class ReusableByteArrayOutputStream extends OutputStream {

    @Getter private final byte[] buffer;
    @Getter private int count;

    public void write(int b) {
        this.buffer[this.count++] = (byte) b;
    }

    public void write(byte[] b, int off, int len) {
        System.arraycopy(b, off, this.buffer, this.count, len);
        this.count += len;
    }

    public void reset() {
        this.count = 0;
    }

    public byte[] toByteArray() {
        return Arrays.copyOf(this.buffer, this.count);
    }

    public void close() {
    }

}
