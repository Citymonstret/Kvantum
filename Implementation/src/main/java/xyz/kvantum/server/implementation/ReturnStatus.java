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
package xyz.kvantum.server.implementation;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import xyz.kvantum.server.api.util.AsciiString;

import javax.annotation.Nullable;

final class ReturnStatus extends Throwable {

    private static final String MESSAGE_FORMAT = "Status: %s";

    @Getter private AsciiString status;

    @Setter @Getter private WorkerContext applicableContext;

    ReturnStatus(@NonNull final AsciiString status,
        @Nullable final WorkerContext applicableContext) {
        super(String.format(MESSAGE_FORMAT, status));
        this.status = status;
        this.applicableContext = applicableContext;
    }

    ReturnStatus(@NonNull final AsciiString status, @Nullable final WorkerContext applicableContext,
        @NonNull final Throwable cause) {
        super(String.format(MESSAGE_FORMAT, status), cause);
        this.status = status;
        this.applicableContext = applicableContext;
    }

}
