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
package xyz.kvantum.server.api.logging;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import xyz.kvantum.server.api.util.MapBuilder;

import java.util.HashMap;
import java.util.Map;

@Getter @Builder @EqualsAndHashCode public class LogContext {

    private String applicationPrefix;

    private String logPrefix;

    private String thread;

    private String timeStamp;

    private String message;

    public final Map<String, String> toMap() {
        return MapBuilder.<String, String>newUnmodifableMap(HashMap::new)
            .put("applicationPrefix", applicationPrefix)
            .put("logPrefix", logPrefix).put("thread", thread).put("timeStamp", timeStamp)
            .put("message", message).get();
    }
}
