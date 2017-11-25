/*
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
package xyz.kvantum.server.api.util;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import lombok.Getter;
import xyz.kvantum.server.api.core.ServerImplementation;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * A very simple metric system using <a href="http://metrics.dropwizard.io">metrics.dropwizard.io</a>
 * <p>
 * By default it only tracks requests, and their execution time. You are able to add more measurements
 * via {@link #registerMetric(String, Metric)}
 * </p>
 * <p>
 * The implementation currently supports:
 * <ul>
 * <li>{@link Gauge}</li>
 * <li>{@link Timer}</li>
 * <li>{@link Counter}</li>
 * </ul>
 * </p>
 * <p>
 * You can request a metric dump with <tt>/metrics</tt>
 * </p>
 */
@SuppressWarnings("ALL")
public class Metrics
{

    @Getter
    private final MetricRegistry registry;

    private final Timer requestsHandling;
    private final Timer compression;
    private final Timer requestPreparation;
    private final Timer contentHandling;
    private final Timer sessionPreparation;
    private final Timer workerContextHandling;
    private final Timer workerPrepare;
    private final Timer readInput;

    private final double durationFactor;

    public Metrics()
    {
        this.registry = new MetricRegistry();

        this.requestsHandling = this.registry.timer( "requestHandling" );
        this.compression = this.registry.timer( "compression" );
        this.requestPreparation = this.registry.timer( "requestPreparation" );
        this.contentHandling = this.registry.timer( "contentHandling" );
        this.sessionPreparation = this.registry.timer( "sessionPreparation" );
        this.workerContextHandling = this.registry.timer( "workerContextHandling" );
        this.workerPrepare = this.registry.timer( "workerPrepare" );
        this.readInput = this.registry.timer( "readInput" );

        this.durationFactor = 1.0 / TimeUnit.MILLISECONDS.toNanos( 1 );
    }

    /**
     * Print statistics to the logger.
     * This is a custom implementation of {@link ConsoleReporter}
     */
    public void logReport()
    {
        final Consumer<String> logger = string -> ServerImplementation.getImplementation().log( string );

        if ( !registry.getGauges().isEmpty() )
        {
            logger.accept( "----- Gauges -----" );
            for ( final Map.Entry<String, Gauge> entry : registry.getGauges().entrySet() )
            {
                logger.accept( entry.getKey() );
                logger.accept( String.format( "             value = %s", entry.getValue().getValue() ) );
            }
        }

        if ( !registry.getCounters().isEmpty() )
        {
            logger.accept( "----- Counters -----" );
            for ( final Map.Entry<String, Counter> entry : registry.getCounters().entrySet() )
            {
                logger.accept( entry.getKey() );
                logger.accept( String.format( "             value = %d", entry.getValue().getCount() ) );
            }
        }

        if ( !registry.getTimers().isEmpty() )
        {
            logger.accept( "----- Timers -----" );
            for ( final Map.Entry<String, Timer> entry : registry.getTimers().entrySet() )
            {
                logger.accept( entry.getKey() );

                final Snapshot snapshot = entry.getValue().getSnapshot();

                logger.accept( String.format( "             count = %d", entry.getValue().getCount() ) );
                logger.accept( String.format( "         mean rate = %2.2f calls/%s", convertRate( entry.getValue().getMeanRate
                                () ),
                        getRateUnit() ) );
                logger.accept( String.format( "     1-minute rate = %2.2f calls/%s", convertRate( entry.getValue().getOneMinuteRate
                        () ), getRateUnit() ) );
                logger.accept( String.format( "     5-minute rate = %2.2f calls/%s", convertRate( entry.getValue()
                        .getFiveMinuteRate() ), getRateUnit() ) );
                logger.accept( String.format( "    15-minute rate = %2.2f calls/%s", convertRate( entry.getValue()
                        .getFifteenMinuteRate() ), getRateUnit() ) );

                logger.accept( String.format( "               min = %2.2f %s", convertDuration( snapshot.getMin() ),
                        getDurationUnit() ) );
                logger.accept( String.format( "               max = %2.2f %s", convertDuration( snapshot.getMax() ),
                        getDurationUnit() ) );
                logger.accept( String.format( "              mean = %2.2f %s", convertDuration( snapshot.getMean() ),
                        getDurationUnit
                                () ) );
                logger.accept( String.format( "            stddev = %2.2f %s", convertDuration( snapshot.getStdDev() ),
                        getDurationUnit() ) );
                logger.accept( String.format( "            median = %2.2f %s", convertDuration( snapshot.getMedian() ),
                        getDurationUnit() ) );
                logger.accept( String.format( "              75%% <= %2.2f %s", convertDuration( snapshot
                        .get75thPercentile() ), getDurationUnit() ) );
                logger.accept( String.format( "              95%% <= %2.2f %s", convertDuration( snapshot
                        .get95thPercentile() ), getDurationUnit() ) );
                logger.accept( String.format( "              98%% <= %2.2f %s", convertDuration( snapshot
                        .get98thPercentile() ), getDurationUnit() ) );
                logger.accept( String.format( "              99%% <= %2.2f %s", convertDuration( snapshot
                        .get99thPercentile() ), getDurationUnit() ) );
                logger.accept( String.format( "            99.9%% <= %2.2f %s", convertDuration( snapshot
                        .get999thPercentile() ), getDurationUnit() ) );
            }
        }
    }

    public double convertRate(final double rate)
    {
        return rate * TimeUnit.SECONDS.toSeconds( 1 );
    }

    public String getRateUnit()
    {
        return TimeUnit.SECONDS.name();
    }

    public String getDurationUnit()
    {
        return TimeUnit.MILLISECONDS.name();
    }

    public double convertDuration(final double duration)
    {
        return duration * durationFactor;
    }

    /**
     * Register a measurement
     *
     * @param name   Unique Name
     * @param metric Metric implementation
     */
    public void registerMetric(final String name, final Metric metric)
    {
        this.registry.register( name, metric );
    }

    /**
     * Solely for tracking requests
     *
     * @return Context
     */
    public Timer.Context registerRequestHandling()
    {
        return requestsHandling.time();
    }

    public Timer.Context registerCompression()
    {
        return compression.time();
    }

    public Timer.Context registerRequestPreparation()
    {
        return requestPreparation.time();
    }

    public Timer.Context registerContentHandling()
    {
        return contentHandling.time();
    }

    public Timer.Context registerSessionPreparation()
    {
        return sessionPreparation.time();
    }

    public Timer.Context registerWorkerContextHandling()
    {
        return workerContextHandling.time();
    }

    public Timer.Context registerWorkerPrepare()
    {
        return workerPrepare.time();
    }

    public Timer.Context registerReadInput()
    {
        return readInput.time();
    }

}
