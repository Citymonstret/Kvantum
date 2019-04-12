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
package xyz.kvantum.server.implementation;

import lombok.val;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.logging.LogContext;
import xyz.kvantum.server.api.logging.LogWrapper;
import xyz.kvantum.server.api.util.ColorUtil;

import java.io.PrintStream;

/**
 * The default log handler.
 */
@SuppressWarnings("WeakerAccess") public class DefaultLogWrapper implements LogWrapper {

    @Override public void log(final LogContext logContext) {
        final val map = logContext.toMap();
        final String replacedMessage = CoreConfig.Logging.logFormat
            .replace("${applicationPrefix}", map.get("applicationPrefix"))
            .replace("${logPrefix}", map.get("logPrefix")).replace("${thread}", map.get("thread"))
            .replace("${timeStamp}", map.get("timeStamp"))
            .replace("${message}", map.get("message"));

        if (ServerImplementation.hasImplementation()) {
            final PrintStream stream =
                ((SimpleServer) ServerImplementation.getImplementation()).logStream;
            if (stream != null) {
                stream.println(ColorUtil.getStripped(replacedMessage));
            }
        }
        System.out.println(ColorUtil.getReplaced(replacedMessage));
    }

    @Override public void log(final String s) {
        System.out.println(s);
        ((SimpleServer) ServerImplementation.getImplementation()).logStream.println(s);
    }

    @Override public void breakLine() {
        System.out.print("\n");
    }

}
