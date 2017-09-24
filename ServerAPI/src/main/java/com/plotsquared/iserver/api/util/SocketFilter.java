package com.plotsquared.iserver.api.util;

import java.net.Socket;

public interface SocketFilter
{

    boolean filter(final Socket Socket);

}
