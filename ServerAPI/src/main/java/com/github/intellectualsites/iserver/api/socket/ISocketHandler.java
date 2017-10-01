package com.github.intellectualsites.iserver.api.socket;

import java.net.Socket;

public interface ISocketHandler
{

    void acceptSocket(Socket s);

    void breakSocketConnection(Socket s);

    void handleShutdown();
}
