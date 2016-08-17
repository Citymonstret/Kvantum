package com.plotsquared.iserver.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class StringOutputStream extends OutputStream
{

    private final Consumer<String> flushAction;
    private final ByteArrayOutputStream outputStream;

    public StringOutputStream()
    {
        this( null );
    }

    public StringOutputStream(final Consumer<String> flushAction)
    {
        this.flushAction = flushAction;
        this.outputStream = new ByteArrayOutputStream();
    }

    @Override
    public void write(int b) throws IOException
    {
        this.outputStream.write( b );
    }

    @Override
    public void write(byte[] b) throws IOException
    {
        this.outputStream.write( b );
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        this.outputStream.write( b, off, len );
    }

    @Override
    public void flush() throws IOException
    {
        this.outputStream.flush();
        if ( this.flushAction != null )
        {
            this.flushAction.accept( this.toString() );
        }
    }

    @Override
    public void close() throws IOException
    {
        this.outputStream.close();
    }

    @Override
    public String toString()
    {
        return new String( this.outputStream.toByteArray(), StandardCharsets.UTF_8 );
    }
}
