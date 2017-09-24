package com.plotsquared.iserver.api.socket;

import java.net.Socket;

public interface SocketHandler
{

    void acceptSocket(Socket s);

    void breakSocketConnection(Socket s);

    void handleShutdown();
}
