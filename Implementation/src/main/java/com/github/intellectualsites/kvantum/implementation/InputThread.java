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
package com.github.intellectualsites.kvantum.implementation;

import com.github.intellectualsites.kvantum.api.core.Kvantum;
import com.github.intellectualsites.kvantum.api.core.ServerImplementation;
import com.github.intellectualsites.kvantum.api.events.Event;
import com.github.intellectualsites.kvantum.api.logging.InternalJlineManager;
import com.github.intellectualsites.kvantum.api.util.AutoCloseable;
import com.github.intellectualsites.kvantum.implementation.commands.*;
import com.intellectualsites.commands.CommandHandlingOutput;
import com.intellectualsites.commands.CommandResult;
import lombok.Getter;
import org.jline.reader.EndOfFileException;
import org.jline.reader.UserInterruptException;

/**
 * The thread which handles command inputs, when ran as a standalone
 * applications.
 *
 * The actual command handler is accessed by {@link Kvantum#getCacheManager()}
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
        ServerImplementation.getImplementation().getCommandManager().createCommand( new Generate() );
        ServerImplementation.getImplementation().getCommandManager().addCommand( new Help( ServerImplementation
                .getImplementation().getCommandManager() ) );

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
                ServerImplementation.getImplementation().getLogWrapper().breakLine();
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
    }

    public static final class TextEvent extends Event
    {

        @Getter
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
    }
}
