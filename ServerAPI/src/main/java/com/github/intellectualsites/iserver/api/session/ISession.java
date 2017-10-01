package com.github.intellectualsites.iserver.api.session;

import com.github.intellectualsites.iserver.api.util.VariableProvider;

public interface ISession extends VariableProvider
{

    long getSessionId();

    void set(final String s, final Object o);

}
