package com.github.intellectualsites.iserver.implementation;

import com.github.intellectualsites.iserver.api.session.ISession;
import com.github.intellectualsites.iserver.api.session.ISessionDatabase;

import java.util.Map;

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
    public Map<String, String> getSessionLoad(String sessionID)
    {
        return null;
    }

    @Override
    public boolean isValid(String session)
    {
        return true;
    }

    @Override
    public void storeSession(ISession session)
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
