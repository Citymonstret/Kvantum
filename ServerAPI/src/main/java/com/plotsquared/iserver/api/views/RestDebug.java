/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
 * <p>
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.plotsquared.iserver.api.views;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.plotsquared.iserver.api.core.ServerImplementation;
import com.plotsquared.iserver.api.matching.ViewPattern;
import com.plotsquared.iserver.api.request.HttpMethod;
import com.plotsquared.iserver.api.request.Request;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Just an example class of how to use the rest system
 */
public final class RestDebug extends RestHandler
{

    private Counter metricCalls = new Counter();

    public RestDebug()
    {
        registerHandler( new MetricReturn() );

        ServerImplementation.getImplementation().getMetrics().registerMetric( "debugCalls", metricCalls );
    }

    private final class MetricReturn extends RestResponse
    {

        public MetricReturn()
        {
            super( HttpMethod.GET, new ViewPattern( "debug/metrics" ) );
        }

        @Override
        public JSONObject generate(Request request) throws JSONException
        {
            final JSONObject object = new JSONObject();
            final MetricRegistry registry = ServerImplementation.getImplementation().getMetrics().getRegistry();

            metricCalls.inc();

            JSONObject gauges = new JSONObject();
            for ( Map.Entry<String, Gauge> gauge : registry.getGauges().entrySet() )
            {
                gauges.put( gauge.getKey(), gauge.getValue().getValue() );
            }
            object.put( "gauges", gauges );

            JSONObject counters = new JSONObject();
            for ( Map.Entry<String, Counter> counter : registry.getCounters().entrySet() )
            {
                counters.put( counter.getKey(), counter.getValue().getCount() );
            }
            object.put( "counters", counters );

            JSONObject timers = new JSONObject();
            for ( final Map.Entry<String, Timer> timer : registry.getTimers().entrySet() )
            {
                JSONObject t = new JSONObject();
                t.put( "count", timer.getValue().getCount() );
                timers.put( timer.getKey(), t );
            }
            object.put( "timers", timers );

            return object;
        }
    }

}
