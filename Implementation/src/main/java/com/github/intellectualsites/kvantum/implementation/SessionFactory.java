package com.github.intellectualsites.kvantum.implementation;

import com.github.intellectualsites.kvantum.api.session.ISession;
import com.github.intellectualsites.kvantum.api.session.ISessionCreator;

final class SessionFactory implements ISessionCreator
{

    @Override
    public ISession createSession()
    {
        return new Session();
    }
}
