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
package xyz.kvantum.server.implementation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * Borrowed from https://github.com/oakes/Nightweb/
 */
class ReusableGzipOutputStream extends DeflaterOutputStream
{

    private static final byte[] HEADER = new byte[]{
            (byte) 0x1F, (byte) 0x8b, // magic bytes
            0x08,                   // compression format == DEFLATE
            0x00,                   // flags (NOT using CRC16, filename, etc)
            0x00, 0x00, 0x00, 0x00, // no modification time available (don't leak this!)
            0x02,                   // maximum compression
            (byte) 0xFF              // unknown creator OS (!!!)
    };
    private final ByteArrayOutputStream bufferStream;
    private final CRC32 crc32;
    private boolean headerWritten;
    private long writtenSize;
    private boolean written = false;

    ReusableGzipOutputStream()
    {
        super( new ByteArrayOutputStream(), new Deflater( Deflater.BEST_SPEED, true ) );
        this.crc32 = new CRC32();
        this.bufferStream = (ByteArrayOutputStream) out;
    }

    void reset()
    {
        if ( this.written )
        {
            this.def.reset();
            this.crc32.reset();
            this.writtenSize = 0;
            this.headerWritten = false;
            this.bufferStream.reset();
            this.written = false;
        }
    }

    byte[] getData()
    {
        return this.bufferStream.toByteArray();
    }

    private void ensureWritten() throws IOException
    {
        if ( headerWritten )
        {
            return;
        }
        this.out.write( HEADER );
        this.headerWritten = true;
    }

    private void writeFooter() throws IOException
    {
        final long crcVal = this.crc32.getValue();
        out.write( (int) ( crcVal & 0xFF ) );
        out.write( (int) ( ( crcVal >>> 8 ) & 0xFF ) );
        out.write( (int) ( ( crcVal >>> 16 ) & 0xFF ) );
        out.write( (int) ( ( crcVal >>> 24 ) & 0xFF ) );

        final long sizeVal = this.writtenSize;
        out.write( (int) ( sizeVal & 0xFF ) );
        out.write( (int) ( ( sizeVal >>> 8 ) & 0xFF ) );
        out.write( (int) ( ( sizeVal >>> 16 ) & 0xFF ) );
        out.write( (int) ( ( sizeVal >>> 24 ) & 0xFF ) );
        out.flush();
    }

    @Override
    public void close() throws IOException
    {
        finish();
        super.close();
    }

    @Override
    public void finish() throws IOException
    {
        ensureWritten();
        super.finish();
        writeFooter();
    }

    @Override
    public void write(final int b) throws IOException
    {
        this.written = true;
        this.ensureWritten();
        this.crc32.update( b );
        this.writtenSize++;
        super.write( b );
    }

    @Override
    public void write(final byte[] b) throws IOException
    {
        write( b, 0, b.length );
    }

    @Override
    public void write(final byte[] buf, final int off, final int len) throws IOException
    {
        this.written = true;
        this.ensureWritten();
        this.crc32.update( buf, off, len );
        this.writtenSize += len;
        super.write( buf, off, len );
    }
}
