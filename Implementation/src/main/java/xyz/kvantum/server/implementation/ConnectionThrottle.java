/*
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
package xyz.kvantum.server.implementation;

import lombok.Data;
import xyz.kvantum.nanotube.ConditionalTransformer;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.memguard.LeakageProne;
import xyz.kvantum.server.api.memguard.MemoryGuard;
import xyz.kvantum.server.api.response.Header;
import xyz.kvantum.server.api.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

final class ConnectionThrottle extends ConditionalTransformer<WorkerContext> implements LeakageProne
{

    private static ConnectionThrottle instance;
    private static long timeLimit = -1;
    private final Map<String, AttemptMapping> attemptMapping;

    public ConnectionThrottle() throws Throwable
    {
        super( ConnectionThrottle::shouldThrottle );
        //
        // Make sure that the instance is set!
        //
        setInstance( this );
        //
        // Initialize the map
        //
        attemptMapping = new ConcurrentHashMap<>();
        //
        // Register in the memory guard
        //
        MemoryGuard.getInstance().register( this );
    }

    private static void setInstance(final ConnectionThrottle instance) throws IllegalAccessException
    {
        if ( ConnectionThrottle.instance != null )
        {
            throw new IllegalAccessException( "Cannot set ConnectionThrottle instance when it is already set" );
        }
        ConnectionThrottle.instance = instance;
    }

    private static long getTimeLimit()
    {
        if ( timeLimit == -1 )
        {
            timeLimit = TimeUnit.valueOf( CoreConfig.Throttle.timeUnit ).toMillis( CoreConfig.Throttle.timeSpan );
        }
        return timeLimit;
    }

    private static boolean shouldThrottle(final WorkerContext workerContext)
    {
        Assert.notNull( instance );
        //
        // Make sure that throttling is enabled
        //
        if ( CoreConfig.Throttle.limit <= 0 )
        {
            return false;
        }
        final String address = workerContext.getSocketContext().getIP();
        final AttemptMapping attemptMapping;
        if ( !instance.attemptMapping.containsKey( address ) )
        {
            attemptMapping = new AttemptMapping();
            attemptMapping.setFirstAttempt( new AtomicLong( System.currentTimeMillis() ) );
            attemptMapping.setIp( address );
            instance.attemptMapping.put( address, attemptMapping );
        } else
        {
            attemptMapping = instance.attemptMapping.get( address );
        }

        if ( instance.shouldReset( attemptMapping ) )
        {
            attemptMapping.getTotalAttempts().set( 0 );
            attemptMapping.getFirstAttempt().set( System.currentTimeMillis() );
        }

        final int totalAttempts = attemptMapping.getTotalAttempts().incrementAndGet();
        return totalAttempts > CoreConfig.Throttle.limit;
    }

    private boolean shouldReset(final AttemptMapping attemptMapping)
    {
        final long timeDifference = System.currentTimeMillis() - attemptMapping.getFirstAttempt().get();
        return getTimeLimit() <= timeDifference;
    }

    @Override
    protected WorkerContext handle(final WorkerContext workerContext) throws Throwable
    {
        throw new ReturnStatus( Header.STATUS_TOO_MANY_REQUESTS, workerContext );
    }

    @Override
    public void cleanUp()
    {
        final List<AttemptMapping> shouldReset = this.attemptMapping.values().stream().filter( this::shouldReset )
                .collect( Collectors.toList() );
        shouldReset.forEach( a -> attemptMapping.remove( a.getIp() ) );
        Logger.info( "Cleaned up %s stored connection attempts!", shouldReset.size() );
    }

    @Data
    private static final class AttemptMapping
    {

        private String ip;
        private AtomicLong firstAttempt;
        private AtomicInteger totalAttempts = new AtomicInteger( 0 );
    }

}
