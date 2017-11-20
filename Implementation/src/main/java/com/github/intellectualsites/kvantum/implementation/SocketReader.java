package com.github.intellectualsites.kvantum.implementation;

import com.github.intellectualsites.kvantum.api.socket.SocketContext;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

@RequiredArgsConstructor
abstract class SocketReader
{

    protected final SocketContext socketContext;
    protected final RequestReader requestReader;

    boolean isDone()
    {
        return requestReader.isDone();
    }

    abstract void tick() throws Exception;

    Collection<String> getLines()
    {
        return this.requestReader.getLines();
    }
}
