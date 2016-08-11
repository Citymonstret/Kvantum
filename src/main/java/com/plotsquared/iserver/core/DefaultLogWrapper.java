package com.plotsquared.iserver.core;

import com.plotsquared.iserver.object.LogWrapper;
import com.sun.media.jfxmedia.logging.Logger;
import print.color.Ansi;
import print.color.ColoredPrinter;
import print.color.ColoredPrinterI;
import print.color.ColoredPrinterWIN;

import java.text.SimpleDateFormat;

public class DefaultLogWrapper implements LogWrapper
{

    private final ColoredPrinterI coloredPrinter;

    public DefaultLogWrapper()
    {
        if ( System.getProperty( "os.name" ).startsWith( "win" ) )
        {
            this.coloredPrinter = new ColoredPrinterWIN.Builder( Logger.INFO, false )
                    .withFormat( new SimpleDateFormat() ).build();
        } else
        {
            this.coloredPrinter = new ColoredPrinter.Builder( Logger.INFO, false )
                    .withFormat( new SimpleDateFormat() ).build();
        }
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

        coloredPrinter.print( "[", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.NONE );
        coloredPrinter.print( prefix, Ansi.Attribute.NONE, Ansi.FColor.WHITE, Ansi.BColor.NONE );
        coloredPrinter.print( "]", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.NONE );
        coloredPrinter.print( "[", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.NONE );
        coloredPrinter.print( thread, Ansi.Attribute.NONE, Ansi.FColor.WHITE, Ansi.BColor.NONE );
        coloredPrinter.print( "]", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.NONE );
        coloredPrinter.print( "[", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.NONE );
        coloredPrinter.print( timeStamp, Ansi.Attribute.NONE, Ansi.FColor.WHITE, Ansi.BColor.NONE );
        coloredPrinter.print( "] ", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.NONE );
        coloredPrinter.print( prefix1, Ansi.Attribute.BOLD, priorityColor, Ansi.BColor.NONE );
        coloredPrinter.print( " > ", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.NONE );
        coloredPrinter.print( message, Ansi.Attribute.NONE, Ansi.FColor.NONE, Ansi.BColor.NONE );
        coloredPrinter.print( System.lineSeparator() );
        coloredPrinter.clear();

        // System.out.printf("[%s][%s][%s][%s] %s%s", prefix, prefix1, thread, timeStamp, message, System.lineSeparator());
        // coloredPrinter.println("Hello", Ansi.Attribute.BOLD, Ansi.FColor.GREEN, Ansi.BColor.YELLOW)
    }

    @Override
    public void log(String s)
    {
        System.out.println( s );
    }

}
