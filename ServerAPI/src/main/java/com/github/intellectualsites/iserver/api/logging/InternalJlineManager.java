package com.github.intellectualsites.iserver.api.logging;

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
                .terminal( terminal ).appName( "IntellectualServer" ).build();
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
