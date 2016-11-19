package com.plotsquared.iserver.views;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.plotsquared.iserver.core.Server;
import com.plotsquared.iserver.http.HttpMethod;
import com.plotsquared.iserver.matching.ViewPattern;
import com.plotsquared.iserver.object.Request;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public final class RestDebug extends RestHandler
{

    private Counter metricCalls = new Counter();

    public RestDebug()
    {
        registerHandler( new MetricReturn() );

        Server.getInstance().getMetrics().registerMetric( "debugCalls", metricCalls );
    }

    private final class MetricReturn extends RestResponse
    {

        public MetricReturn()
        {
            super ( HttpMethod.GET, new ViewPattern( "debug/metrics" ) );
        }

        @Override
        public JSONObject generate(Request request) throws JSONException
        {
            final JSONObject object = new JSONObject();
            final MetricRegistry registry = Server.getInstance().getMetrics().getRegistry();

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
                // TODO: Add More
                timers.put( timer.getKey(), t );
            }
            object.put( "timers", timers );

            return object;
        }
    }

}
