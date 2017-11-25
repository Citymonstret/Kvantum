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
