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
package com.plotsquared.iserver.core;

import com.intellectualsites.commands.CommandHandlingOutput;
import com.intellectualsites.commands.CommandResult;
import com.plotsquared.iserver.commands.Dump;
import com.plotsquared.iserver.commands.Metrics;
import com.plotsquared.iserver.commands.Show;
import com.plotsquared.iserver.commands.Stop;
import com.plotsquared.iserver.events.Event;
import com.plotsquared.iserver.object.AutoCloseable;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * The thread which handles command inputs, when ran as a standalone
 * applications.
 *
 * The actual command handler is accessed by {@link IntellectualServer#getCacheManager()}
 * @author Citymonstret
 */
@SuppressWarnings("all")
public final class InputThread extends Thread
{
    public String currentString = "";

    InputThread()
    {
        ServerImplementation.getImplementation().getCommandManager().createCommand( new Stop() );
        ServerImplementation.getImplementation().getCommandManager().createCommand( new Dump() );
        ServerImplementation.getImplementation().getCommandManager().createCommand( new Metrics() );
        ServerImplementation.getImplementation().getCommandManager().createCommand( new Show() );

        new AutoCloseable() {

            @Override
            @SuppressWarnings( "deprecated" )
            public void handleClose()
            {
                InputThread.this.stop();
            }
        };
    }

    @Override
    public void run()
    {
        try ( BufferedReader in = new BufferedReader( new InputStreamReader( System.in ) ) )
        {
            String line;

            for ( ; ; )
            {
                if ( ServerImplementation.getImplementation().isStopping() )
                {
                    break;
                }

                line = in.readLine();
                if ( line == null || line.isEmpty() )
                {
                    continue;
                }
                if ( line.startsWith( "/" ) )
                {
                    line = line.replace( "/", "" ).toLowerCase();
                    final CommandResult result = ServerImplementation.getImplementation().getCommandManager().handle(
                            ServerImplementation.getImplementation(), line );

                    switch ( result.getCommandResult() )
                    {
                        case CommandHandlingOutput.NOT_PERMITTED:
                            ServerImplementation.getImplementation().log( "Command Error: You are not allowed to execute that command!" );
                            break;
                        case CommandHandlingOutput.ERROR:
                            ServerImplementation.getImplementation().log( "Something went wrong when executing the command!" );
                            result.getStacktrace().printStackTrace();
                            break;
                        case CommandHandlingOutput.NOT_FOUND:
                            if ( result.getClosestMatch() != null )
                            {
                                ServerImplementation.getImplementation().log( "Did you mean: /%s", result
                                        .getClosestMatch().getCommand() );
                            } else
                            {
                                ServerImplementation.getImplementation().log( "There is no such command!" );
                            }
                            break;
                        case CommandHandlingOutput.WRONG_USAGE:
                            ServerImplementation.getImplementation().log( "Command Usage: " + result.getCommand().getUsage() );
                            break;
                        case CommandHandlingOutput.SUCCESS:
                            break;
                        default:
                            ServerImplementation.getImplementation().log( "Unknown command result: " + CommandHandlingOutput.nameField(
                                    result.getCommandResult() ) );
                            break;
                    }
                } else
                {
                    currentString = line;
                    com.plotsquared.iserver.core.ServerImplementation.getImplementation().handleEvent( new TextEvent( line ) );
                }
            }
        } catch ( final Exception ignored )
        {
        }
    }

    public static class TextEvent extends Event
    {

        private final String text;

        /**
         * The name which will be used
         * to identity this event
         *
         * @param name Event Name
         */
        protected TextEvent(String text)
        {
            super( "inputtextevent" );
            this.text = text;
        }

        final public String getText()
        {
            return this.text;
        }
    }
}
