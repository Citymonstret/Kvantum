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
package com.github.intellectualsites.kvantum.implementation;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.github.intellectualsites.kvantum.api.config.CoreConfig;
import com.github.intellectualsites.kvantum.api.core.Kvantum;
import com.github.intellectualsites.kvantum.api.logging.LogContext;
import com.github.intellectualsites.kvantum.api.logging.LogWrapper;
import com.github.intellectualsites.kvantum.api.util.RequestManager;
import com.github.intellectualsites.kvantum.api.util.TimeUtil;
import com.github.intellectualsites.kvantum.implementation.error.KvantumInitializationException;
import com.github.intellectualsites.kvantum.implementation.example.Examples;
import org.apache.commons.lang3.SystemUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Optional;

@SuppressWarnings("ALL")
final public class KvantumMain
{

    /**
     * Launcher method
     *
     * @param args Command line arguments
     */
    public static void main(final String[] args) throws Throwable
    {
        if ( SystemUtils.IS_OS_LINUX )
        {
            System.out.println( "Server running on Linux! Checking privileges..." );

            boolean isPrivileged = false;

            final Process process = Runtime.getRuntime().exec( new String[]{ "id", "-u" } );
            try ( BufferedReader reader = new BufferedReader( new InputStreamReader( process.getInputStream() ) ) )
            {
                String line = reader.readLine();
                if ( line != null )
                {
                    isPrivileged = line.equals( "0" );
                }
            }
            process.destroyForcibly();

            if ( !isPrivileged )
            {
                System.out.println(
                        "\nWARNING\n" +
                                "The server is not privileged, and might therefore not be able to bind to port 80.\n" +
                                "Not running as a previleged user may also cause complications with file creation. Beware.\n"
                );
            }
        }

        final Options options = new Options();
        final JCommander jCommander = new JCommander( options );
        jCommander.parse( args );

        jCommander.setProgramName( "Kvantum" );
        if ( options.help )
        {
            final LogWrapper logWrapper = new DefaultLogWrapper();
            final StringBuilder message = new StringBuilder();
            jCommander.usage( message );

            logWrapper.log( LogContext.builder().applicationPrefix( "Help" ).logPrefix( "Info" ).timeStamp( TimeUtil
                    .getTimeStamp() ).message( message.toString() ).thread( "main" ).build() );

            System.exit( 0 );
        } else
        {
            String folder = options.folder;
            if ( folder.isEmpty() )
            {
                if ( System.getenv().containsKey( "KVANTUM_HOME" ) )
                {
                    folder = System.getenv( "KVANTUM_HOME" );
                } else
                {
                    folder = "./";
                }
            }

            final File file = new File( folder );

            System.out.printf( "INFO\nUsing server folder: %s\n\n", file.getAbsolutePath() );

            final Optional<Kvantum> server = ServerContext.builder().coreFolder( file )
                    .standalone( true ).logWrapper( new DefaultLogWrapper() ).router( RequestManager.builder().build() )
                    .build().create();
            if ( server.isPresent() )
            {
                if ( !options.debug.isEmpty() )
                {
                    CoreConfig.debug = true;
                    CoreConfig.verbose = true;
                }
                if ( options.port != -1 )
                {
                    CoreConfig.port = options.port;
                }
                if ( options.workers != -1 )
                {
                    CoreConfig.workers = options.workers;
                }
                if ( !options.example.isEmpty() )
                {
                    Examples.loadExample( options.example );
                }
                try
                {
                    server.get().start();
                } catch ( final Exception e )
                {
                    throw new KvantumInitializationException( "Failed to start the server instance", e );
                }
            } else
            {
                throw new KvantumInitializationException( "Failed to create a server instance" );
            }
        }
    }

    public static Optional<? extends Kvantum> start(final File coreFolder)
    {
        return start( ServerContext.builder().standalone( false ).coreFolder( coreFolder )
                .logWrapper( new DefaultLogWrapper() ).router( RequestManager.builder().build() ).build() );
    }

    public static Optional<? extends Kvantum> start()
    {
        return start( new File( "./" ) );
    }

    /**
     * Create & Start the server
     * @param standalone If the server should run as a singleton application,
     *                   or if its embedded as a library
     * @param coreFolder The core folder, in which the ".isites" folder is created
     * @param wrapper The log wrapper / handler
     * @return Optional of nullable server
     */
    public static Optional<? extends Kvantum> start(final ServerContext serverContext)
    {
        Optional<? extends Kvantum> server = serverContext.create();
        try
        {
            if ( server.isPresent() )
            {
                server.get().start();
            }
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }
        return server;
    }

    /**
     * Command line arguments
     */
    private static class Options
    {

        @Parameter(names = "-folder", description = "Application base folder path")
        private String folder = "";

        @Parameter(names = "-help", description = "Show this list")
        private boolean help = false;

        @Parameter(names = "-port", description = "The server port")
        private int port = -1;

        @Parameter(names = "-debug", description = "Enable debugging")
        private String debug = "";

        @Parameter(names = "-workers", description = "Number of workers")
        private int workers = -1;

        @Parameter(names = "-example", description = "Run an example view. Current examples: usersearch")
        private String example = "";

    }
}
