package com.plotsquared.iserver.api.session;

import com.plotsquared.iserver.api.util.VariableProvider;

public interface ISession extends VariableProvider
{

    long getSessionId();

    void set(final String s, final Object o);

}
