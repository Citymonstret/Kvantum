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

/**
 * Immutable implementation of {@link SimpleOutputStream}
 * {@inheritDoc}
 */
public final class ImmutableKvantumOutputStream extends SimpleOutputStream {

    /**
     * Construct a new {@link ImmutableKvantumOutputStream} with a fixed value
     *
     * @param bytes value that will be pushed by this response stream
     */
    public ImmutableKvantumOutputStream(final byte[] bytes) {
        super(bytes);
    }

    @Override public void push(final byte[] bytes) {
        throw new UnsupportedOperationException("Cannot write to immutable stream");
    }

}
