package com.github.intellectualsites.iserver.api.socket;

import java.net.Socket;

public interface SocketFilter
{

    boolean filter(final Socket Socket);

}
