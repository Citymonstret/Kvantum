package com.github.intellectualsites.iserver.implementation;

import com.github.intellectualsites.iserver.api.util.Assert;
import com.github.intellectualsites.iserver.api.util.AutoCloseable;

import java.io.IOException;

final class GzipHandler extends AutoCloseable
{

    private final ReusableGzipOutputStream reusableGzipOutputStream;

    GzipHandler()
    {
        this.reusableGzipOutputStream = new ReusableGzipOutputStream();
    }

    @Override
    protected void handleClose()
    {
        try
        {
            this.reusableGzipOutputStream.close();
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }
    }

    /**
     * Compress bytes using gzip
     *
     * @param data Bytes to compress
     * @return GZIP compressed data
     * @throws IOException If compression fails
     */
    byte[] compress(final byte[] data) throws IOException
    {
        Assert.notNull( data );

        reusableGzipOutputStream.reset();
        reusableGzipOutputStream.write( data );
        reusableGzipOutputStream.finish();
        reusableGzipOutputStream.flush();

        final byte[] compressed = reusableGzipOutputStream.getData();

        Assert.equals( compressed != null && compressed.length > 0, true, "Failed to compress data" );

        return compressed;
    }


}
