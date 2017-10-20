/*
 * IntellectualServer is a web server, written entirely in the Java language.
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
package com.github.intellectualsites.iserver;

import com.github.intellectualsites.iserver.api.core.IntellectualServer;
import com.github.intellectualsites.iserver.api.core.ServerImplementation;
import com.github.intellectualsites.iserver.api.events.Event;
import com.github.intellectualsites.iserver.api.logging.InternalJlineManager;
import com.github.intellectualsites.iserver.api.util.AutoCloseable;
import com.github.intellectualsites.iserver.commands.Dump;
import com.github.intellectualsites.iserver.commands.Metrics;
import com.github.intellectualsites.iserver.commands.Show;
import com.github.intellectualsites.iserver.commands.Stop;
import com.intellectualsites.commands.CommandHandlingOutput;
import com.intellectualsites.commands.CommandResult;
import org.jline.reader.EndOfFileException;
import org.jline.reader.UserInterruptException;

/**
 * The thread which handles command inputs, when ran as a standalone
 * applications.
 *
 * The actual command handler is accessed by {@link IntellectualServer#getCacheManager()}
 * @author Citymonstret
 */
public final class InputThread extends Thread
{

    private volatile boolean shouldStop = false;

    @SuppressWarnings("ALL")
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
                shouldStop = true;
            }
        };
    }

    @Override
    public void run()
    {
        String line;
        for ( ; ; )
        {
            try
            {
                line = InternalJlineManager.getInstance().getLineReader().readLine( "> " );
                if ( shouldStop || ServerImplementation.getImplementation().isStopping() )
                {
                    break;
                }
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
                                ServerImplementation.getImplementation().log( "There is no such command: " + result
                                        .getInput() );
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
                    ServerImplementation.getImplementation().handleEvent( new TextEvent( line ) );
                }
            } catch ( UserInterruptException e )
            {
                e.printStackTrace();
            } catch ( EndOfFileException e )
            {
                return;
            }
        }

        /* try ( BufferedReader in = new BufferedReader( new InputStreamReader( System.in ) ) )
        {
            String line;

            for ( ; ; )
            {
                if ( shouldStop || ServerImplementation.getImplementation().isStopping() )
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
                                ServerImplementation.getImplementation().log( "There is no such command: " + result
                                        .getInput() );
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
                    ServerImplementation.getImplementation().handleEvent( new TextEvent( line ) );
                }
            }
        } catch ( final Exception ignored )
        {
        }
        */
    }

    public static class TextEvent extends Event
    {

        private final String text;

        /**
         * The name which will be used
         * to identity this event
         *
         * @param text Event Name
         */
        TextEvent(String text)
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
