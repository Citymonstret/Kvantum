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
package com.github.intellectualsites.iserver.implementation;

import com.diogonunes.jcdp.color.ColoredPrinter;
import com.diogonunes.jcdp.color.api.Ansi;
import com.github.intellectualsites.iserver.api.core.ServerImplementation;
import com.github.intellectualsites.iserver.api.logging.LogWrapper;
import com.sun.media.jfxmedia.logging.Logger;

import java.text.SimpleDateFormat;

/**
 * The default log handler. UsesAnsi.FColor for colored output
 */
public class DefaultLogWrapper implements LogWrapper
{

    private final ColoredPrinter printer;

    public DefaultLogWrapper()
    {
        printer = new ColoredPrinter.Builder( Logger.INFO, false ).withFormat( new SimpleDateFormat() ).build();
    }

    @Override
    public void log(String prefix, String prefix1, String timeStamp, String message, String thread)
    {
        final Ansi.FColor priorityColor;
        switch ( prefix1 )
        {
            case "Debug":
                priorityColor = Ansi.FColor.CYAN;
                break;
            case "Info":
                priorityColor = Ansi.FColor.WHITE;
                break;
            case "Error":
                priorityColor = Ansi.FColor.RED;
                break;
            case "Warning":
                priorityColor = Ansi.FColor.YELLOW;
                break;
            default:
                priorityColor = Ansi.FColor.NONE;
                break;
        }


        printer.print( "[", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.NONE );
        printer.print( prefix, Ansi.Attribute.NONE, Ansi.FColor.WHITE, Ansi.BColor.NONE );
        printer.print( "]", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.NONE );
        printer.print( "[", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.NONE );
        printer.print( thread, Ansi.Attribute.NONE, Ansi.FColor.WHITE, Ansi.BColor.NONE );
        printer.print( "]", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.NONE );
        printer.print( "[", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.NONE );
        printer.print( timeStamp, Ansi.Attribute.NONE, Ansi.FColor.WHITE, Ansi.BColor.NONE );
        printer.print( "] ", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.NONE );
        printer.print( prefix1, Ansi.Attribute.BOLD, priorityColor, Ansi.BColor.NONE );
        printer.print( " > ", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.NONE );
        printer.print( message, Ansi.Attribute.NONE, Ansi.FColor.NONE, Ansi.BColor.NONE );
        printer.print( System.lineSeparator() );
        printer.clear();

        ( (Server) ServerImplementation.getImplementation() ).logStream.printf( "[%s][%s][%s][%s] %s%s", prefix, prefix1, thread, timeStamp,
                message, System.lineSeparator() );
        // System.out.printf("[%s][%s][%s][%s] %s%s", prefix, prefix1, thread, timeStamp, message, System.lineSeparator());
        // printer.println("Hello",Ansi.Attribute.BOLD,Ansi.FColor.GREEN,Ansi.BColor.YELLOW)
    }

    @Override
    public void log(String s)
    {
        System.out.println( s );
        ( (Server) ServerImplementation.getImplementation() ).logStream.println( s );
    }

}
