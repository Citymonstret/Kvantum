package com.plotsquared.iserver.api.session;

import com.plotsquared.iserver.api.request.Request;
import com.plotsquared.iserver.api.response.HeaderProvider;
import com.plotsquared.iserver.api.util.ProviderFactory;
import com.plotsquared.iserver.api.util.VariableProvider;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.util.Optional;

public interface SessionManager extends ProviderFactory<VariableProvider>
{

    Session createSession(Request r, BufferedOutputStream out);

    void deleteSession(Request r, HeaderProvider re);

    Optional<Session> getSession(Request r, OutputStream out);

}
