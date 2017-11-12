/*
 * Kvantum is a web server, written entirely in the Java language.
 * Copyright (C) 2017 IntellectualSites
 *
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.github.intellectualsites.kvantum.api.util;

import com.codahale.metrics.*;
import com.github.intellectualsites.kvantum.api.core.ServerImplementation;
import lombok.Getter;

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

    private final double durationFactor;

    public Metrics()
    {
        this.registry = new MetricRegistry();

        this.requestsHandling = this.registry.timer( "requestHandling" );
        this.compression = this.registry.timer( "compression" );
        this.requestPreparation = this.registry.timer( "requestPreparation" );
        this.contentHandling = this.registry.timer( "contentHandling" );
        this.sessionPreparation = this.registry.timer( "sessionPreparation" );

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

}
