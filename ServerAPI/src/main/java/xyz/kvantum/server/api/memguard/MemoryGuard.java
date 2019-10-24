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
package xyz.kvantum.server.api.memguard;

import lombok.Getter;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.util.Assert;
import xyz.kvantum.server.api.util.AutoCloseable;

import java.util.ArrayList;
import java.util.Collection;

public final class MemoryGuard extends AutoCloseable implements Runnable {

    @Getter private static final MemoryGuard instance = new MemoryGuard();

    private final Collection<LeakageProne> leakagePrones;
    private boolean started = false;

    private final Thread thread;

    private MemoryGuard() {
        this.leakagePrones = new ArrayList<>();
        thread = new Thread(this, "memory-guard");
    }

    @Override protected void handleClose() {
        this.thread.stop();
    }

    public void start() {
        Assert.equals(started, false);
        started = true;
        thread.setDaemon(true);
        thread.start();
    }

    public void register(final LeakageProne leakageProne) {
        this.leakagePrones.add(leakageProne);
    }

    @Override public void run() {
        try {
            Thread.sleep(CoreConfig.MemoryGuard.runEveryMillis);
        } catch (InterruptedException e) {
            ServerImplementation.getImplementation().getErrorDigest().digest(e);
        }
        Logger.info("Running memory guard!");
        this.leakagePrones.forEach(LeakageProne::cleanUp);
    }

}
