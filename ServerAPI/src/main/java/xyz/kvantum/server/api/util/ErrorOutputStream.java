/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2018 Alexander Söderberg
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
package xyz.kvantum.server.api.util;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import xyz.kvantum.server.api.logging.LogWrapper;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Extremely hacky solution that enables file logging for exceptions
 *
 * @author Citymonstret
 */
@RequiredArgsConstructor public final class ErrorOutputStream extends ByteArrayOutputStream {

    @NonNull private final LogWrapper logWrapper;

    @Override public void flush() {
        String message = new String(toByteArray(), StandardCharsets.UTF_8);
        if (message.endsWith(System.lineSeparator())) {
            message = message.substring(0, message.length() - System.lineSeparator().length());
        }
        if (!message.isEmpty()) {
            logWrapper.log(new String(toByteArray(), StandardCharsets.UTF_8));
        }
        super.reset();
    }
}
