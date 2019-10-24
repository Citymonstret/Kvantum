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
package xyz.kvantum.server.implementation.netty;

import io.netty.util.internal.logging.AbstractInternalLogger;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.logging.Logger;

public final class NettyLogger extends AbstractInternalLogger {

    static final NettyLogger instance = new NettyLogger();

    private NettyLogger() {
        super("netty");
    }

    @Override public boolean isTraceEnabled() {
        return CoreConfig.debug && CoreConfig.verbose;
    }

    @Override public void trace(final String msg) {
        if (this.isTraceEnabled()) {
            Logger.info(msg);
        }
    }

    @Override public void trace(final String format, final Object arg) {
        if (this.isTraceEnabled()) {
            Logger.info(format, arg);
        }
    }

    @Override public void trace(final String format, final Object argA, final Object argB) {
        if (this.isTraceEnabled()) {
            Logger.info(format, argA, argB);
        }
    }

    @Override public void trace(final String format, final Object... arguments) {
        if (this.isTraceEnabled()) {
            Logger.info(format, arguments);
        }
    }

    @Override public void trace(final String msg, final Throwable t) {
        if (this.isTraceEnabled()) {
            Logger.info(msg);
            ServerImplementation.getImplementation().getErrorDigest().digest(t);
        }
    }

    @Override public boolean isDebugEnabled() {
        return CoreConfig.debug && CoreConfig.verbose;
    }

    @Override public void debug(final String msg) {
        if (this.isDebugEnabled()) {
            Logger.debug(msg);
        }
    }

    @Override public void debug(final String format, final Object arg) {
        if (this.isDebugEnabled()) {
            Logger.debug(format, arg);
        }
    }

    @Override public void debug(final String format, final Object argA, final Object argB) {
        if (this.isDebugEnabled()) {
            Logger.debug(format, argA, argB);
        }
    }

    @Override public void debug(final String format, final Object... arguments) {
        if (this.isDebugEnabled()) {
            Logger.debug(format, arguments);
        }
    }

    @Override public void debug(final String msg, final Throwable t) {
        if (this.isDebugEnabled()) {
            Logger.debug(msg);
            ServerImplementation.getImplementation().getErrorDigest().digest(t);
        }
    }

    @Override public boolean isInfoEnabled() {
        return true;
    }

    @Override public void info(final String msg) {
        Logger.info(msg);
    }

    @Override public void info(final String format, final Object arg) {
        Logger.info(format, arg);
    }

    @Override public void info(final String format, final Object argA, final Object argB) {
        Logger.info(format, argA, argB);
    }

    @Override public void info(final String format, final Object... arguments) {
        Logger.info(format, arguments);
    }

    @Override public void info(final String msg, final Throwable t) {
        Logger.info(msg);
        ServerImplementation.getImplementation().getErrorDigest().digest(t);
    }

    @Override public boolean isWarnEnabled() {
        return false;
    }

    @Override public void warn(final String msg) {
        if (this.isWarnEnabled()) {
            Logger.warn(msg);
        }
    }

    @Override public void warn(final String format, final Object arg) {
        if (this.isWarnEnabled()) {
            Logger.warn(format, arg);
        }
    }

    @Override public void warn(final String format, final Object... arguments) {
        if (this.isWarnEnabled()) {
            Logger.warn(format, arguments);
        }
    }

    @Override public void warn(final String format, final Object argA, final Object argB) {
        if (this.isWarnEnabled()) {
            Logger.warn(format, argA, argB);
        }
    }

    @Override public void warn(final String msg, final Throwable t) {
        Logger.warn(msg, t);
    }

    @Override public boolean isErrorEnabled() {
        return true;
    }

    @Override public void error(final String msg) {
        Logger.error(msg);
    }

    @Override public void error(final String format, final Object arg) {
        Logger.error(format, arg);
    }

    @Override public void error(final String format, final Object argA, final Object argB) {
        Logger.error(format, argA, argB);
    }

    @Override public void error(final String format, final Object... arguments) {
        Logger.error(format, arguments);
    }

    @Override public void error(final String msg, final Throwable t) {
        Logger.error(msg);
        ServerImplementation.getImplementation().getErrorDigest().digest(t);
    }
}
