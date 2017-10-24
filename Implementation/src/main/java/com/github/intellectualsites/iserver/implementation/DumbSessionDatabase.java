package com.github.intellectualsites.iserver.implementation;

import com.github.intellectualsites.iserver.api.session.ISessionDatabase;

public class DumbSessionDatabase implements ISessionDatabase
{

    @Override
    public void setup() throws Exception
    {
    }

    @Override
    public long containsSession(String sessionId)
    {
        return -1;
    }

    @Override
    public boolean isValid(String session)
    {
        return true;
    }

    @Override
    public void storeSession(String session)
    {
    }

    @Override
    public void updateSession(String session)
    {
    }

    @Override
    public void deleteSession(String session)
    {
    }
}
