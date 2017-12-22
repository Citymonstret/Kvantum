/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2017 IntellectualSites
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
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class MemoryGuard implements Runnable
{

    @Getter
    private static final MemoryGuard instance = new MemoryGuard();

    private final Collection<LeakageProne> leakagePrones;

    private boolean started = false;

    private MemoryGuard()
    {
        this.leakagePrones = new ArrayList<>();
    }

    public void start()
    {
        Assert.equals( started, false );
        started = true;
        Executors.newScheduledThreadPool( 1 ).scheduleAtFixedRate( this,
                CoreConfig.MemoryGuard.runEveryMillis, CoreConfig.MemoryGuard.runEveryMillis, TimeUnit.MILLISECONDS );
    }

    public void register(final LeakageProne leakageProne)
    {
        this.leakagePrones.add( leakageProne );
    }

    @Override
    public void run()
    {
        Logger.info( "Running memory guard!" );
        this.leakagePrones.forEach( LeakageProne::cleanUp );
    }
}
