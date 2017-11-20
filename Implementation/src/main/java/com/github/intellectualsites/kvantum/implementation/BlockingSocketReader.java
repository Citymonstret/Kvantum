package com.github.intellectualsites.kvantum.implementation;

import com.github.intellectualsites.kvantum.api.config.CoreConfig;
import com.github.intellectualsites.kvantum.api.socket.SocketContext;
import lombok.Getter;

import java.io.BufferedInputStream;

final class BlockingSocketReader extends SocketReader
{

    @Getter
    private final BufferedInputStream inputStream;

    BlockingSocketReader(final SocketContext socketContext, final RequestReader requestReader) throws Exception
    {
        super( socketContext, requestReader );
        this.inputStream = new BufferedInputStream( socketContext.getSocket().getInputStream(), CoreConfig.Buffer.in );
    }

    @Override
    void tick() throws Exception
    {
        this.requestReader.readByte( inputStream.read() );
    }
}
