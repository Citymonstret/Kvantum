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
package com.github.intellectualsites.kvantum.implementation;

import com.github.intellectualsites.kvantum.api.util.Assert;
import com.github.intellectualsites.kvantum.api.util.AutoCloseable;

import java.io.IOException;

/**
 * This class is responsible for the GZIP compression in {@link Worker workers}
 */
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
