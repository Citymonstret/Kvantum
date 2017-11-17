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
package com.github.intellectualsites.kvantum.implementation.commands;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.github.intellectualsites.kvantum.api.core.ServerImplementation;
import com.github.intellectualsites.kvantum.api.logging.Logger;
import com.github.intellectualsites.kvantum.files.Path;
import com.intellectualsites.commands.Command;
import com.intellectualsites.commands.CommandDeclaration;
import com.intellectualsites.commands.CommandInstance;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
/**
 * Prints {@link com.github.intellectualsites.kvantum.api.util.Metrics} information to
 * the logger
 */
@CommandDeclaration(
        command = "metrics",
        usage = "/metrics",
        description = "View server metrics"
)
public class Metrics extends Command
{

    @Override
    public boolean onCommand(CommandInstance instance)
    {
        final com.github.intellectualsites.kvantum.api.util.Metrics metrics = ServerImplementation.getImplementation().getMetrics();
        if ( instance.getArguments().length > 0 )
        {
            if ( instance.getArguments()[ 0 ].equalsIgnoreCase( "printjson" ) )
            {
                final JSONObject jsonObject = new JSONObject();
                final JSONObject timers = new JSONObject();
                final JSONArray timerList = new JSONArray();
                final MetricRegistry registry = metrics.getRegistry();
                for ( final Map.Entry<String, Timer> timer : registry.getTimers().entrySet() )
                {
                    final JSONObject timerObject = new JSONObject();
                    final Snapshot snapshot = timer.getValue().getSnapshot();
                    timerObject.put( "name", timer.getKey() );
                    timerObject.put( "count", timer.getValue().getCount() );
                    timerObject.put( "min", metrics.convertDuration( snapshot.getMin() ) );
                    timerObject.put( "max", metrics.convertDuration( snapshot.getMax() ) );
                    timerObject.put( "mean", metrics.convertDuration( snapshot.getMean() ) );
                    timers.put( timer.getKey(), timerObject );
                    timerList.add( timer.getKey() );
                }
                jsonObject.put( "timers", timers );
                final Path folder = ServerImplementation.getImplementation().getFileSystem().getPath( "metrics" );
                if ( !folder.exists() )
                {
                    if ( !folder.create() )
                    {
                        Logger.info( "Failed to create .kvantum/metrics folder..." );
                        return true;
                    }
                }
                final Path path = folder.getPath( "metric_dump_" + System.currentTimeMillis() + ".json" );
                if ( !path.exists() )
                {
                    if ( !path.create() || !path.exists() )
                    {
                        Logger.error( "Failed to create metric dump..." );
                        return true;
                    }
                }
                try
                {
                    Files.write( path.getJavaPath(), jsonObject.toJSONString().getBytes( StandardCharsets.UTF_8 ) );
                } catch ( IOException e )
                {
                    e.printStackTrace();
                }

                Logger.info( "Printed metrics to file '%s'", path.getJavaPath().toString() );
                return true;
            }
        }
        metrics.logReport();
        return true;
    }

}
