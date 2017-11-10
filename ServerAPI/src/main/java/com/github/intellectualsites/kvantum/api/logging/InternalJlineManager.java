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
package com.github.intellectualsites.kvantum.api.logging;

import lombok.Getter;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.util.logging.Level;
import java.util.logging.Logger;

public class InternalJlineManager
{

    private static InternalJlineManager instance;
    @Getter
    private final Terminal terminal;
    @Getter
    private final LineReader lineReader;

    private InternalJlineManager() throws Exception
    {
        Logger.getLogger( "org.jline" ).setLevel( Level.ALL );
        terminal = TerminalBuilder.builder().dumb( true ).build();
        lineReader = LineReaderBuilder.builder()
                .terminal( terminal ).appName( "Kvantum" ).build();
    }

    public static InternalJlineManager getInstance()
    {
        if ( instance == null )
        {
            try
            {
                instance = new InternalJlineManager();
            } catch ( Exception e )
            {
                e.printStackTrace();
            }
        }
        return instance;
    }

}
