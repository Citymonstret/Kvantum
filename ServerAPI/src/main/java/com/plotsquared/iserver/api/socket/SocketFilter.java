package com.plotsquared.iserver.api.socket;

import java.net.Socket;

public interface SocketFilter
{

    boolean filter(final Socket Socket);

}
