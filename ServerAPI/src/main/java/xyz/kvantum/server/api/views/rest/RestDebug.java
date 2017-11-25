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
package xyz.kvantum.server.api.views.rest;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.json.simple.JSONObject;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.matching.ViewPattern;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.request.HttpMethod;

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
        public JSONObject generate(AbstractRequest request)
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
