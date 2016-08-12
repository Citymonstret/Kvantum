//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//     IntellectualServer is a web server, written entirely in the Java language.                            /
//     Copyright (C) 2015 IntellectualSites                                                                  /
//                                                                                                           /
//     This program is free software; you can redistribute it and/or modify                                  /
//     it under the terms of the GNU General Public License as published by                                  /
//     the Free Software Foundation; either version 2 of the License, or                                     /
//     (at your option) any later version.                                                                   /
//                                                                                                           /
//     This program is distributed in the hope that it will be useful,                                       /
//     but WITHOUT ANY WARRANTY; without even the implied warranty of                                        /
//     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                         /
//     GNU General Public License for more details.                                                          /
//                                                                                                           /
//     You should have received a copy of the GNU General Public License along                               /
//     with this program; if not, write to the Free Software Foundation, Inc.,                               /
//     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.                                           /
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.plotsquared.iserver.core;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.plotsquared.iserver.gui.GuiMain;
import com.plotsquared.iserver.object.LogWrapper;
import com.plotsquared.iserver.util.Assert;
import com.plotsquared.iserver.util.Bootstrap;
import com.plotsquared.iserver.util.TimeUtil;

import java.io.File;

@SuppressWarnings("ALL")
@Bootstrap
public class IntellectualServerMain
{

    /**
     * Launcher method
     *
     * @param args arguments
     */
    public static void main(String[] args)
    {
        final Options options = new Options();
        final JCommander jCommander = new JCommander( options, args );
        jCommander.setProgramName( "IntellectualServer" );
        if ( options.help )
        {
            final LogWrapper logWrapper = new DefaultLogWrapper();
            final StringBuilder message = new StringBuilder();
            jCommander.usage( message );
            logWrapper.log( "Help", "Info", TimeUtil.getTimeStamp(), message.toString(), "Main" );
        } else
        {
            final File file = new File( options.folder );
            if ( options.gui )
            {
                GuiMain.main( args, file );
            } else
            {
                startServer( true, file, new DefaultLogWrapper() );
            }
        }
    }

    public static Server createServer(final boolean standalone, final File coreFolder, final LogWrapper wrapper)
    {
        Assert.equals( coreFolder.getAbsolutePath().indexOf( '!' ) == -1, true,
                "Cannot use a folder with '!' path as core folder" );

        Server server = null;
        try
        {
            server = new Server( standalone, coreFolder, wrapper );
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }
        return server;
    }

    /**
     * Start a server, and get the instance
     *
     * @param standalone Should it run as a standalone application, or be integrated
     * @return the started server | null
     */
    public static IntellectualServer startServer(boolean standalone, File coreFolder, LogWrapper wrapper)
    {
        Server server = null;
        try
        {
            server = createServer( standalone, coreFolder, wrapper );
            server.start();
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }
        return server;
    }

    private static class Options
    {

        @Parameter(names = "-gui", description = "Launch with a GUI ( W.I.P )")
        private boolean gui = false;

        @Parameter(names = "-folder", description = "Application base folder path")
        private String folder = "./";

        @Parameter(names = "-help", description = "Show this list")
        private boolean help = false;

    }
}
