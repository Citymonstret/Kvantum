package com.github.intellectualsites.iserver.api.session;

public interface ISessionDatabase
{

    void setup() throws Exception;

    boolean isValid(String session);

    void storeSession(String session);

    void updateSession(String session);

    void deleteSession(String session);

}
