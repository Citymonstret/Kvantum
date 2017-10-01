package com.github.intellectualsites.iserver.api.session;

@FunctionalInterface
public interface ISessionCreator
{

    ISession createSession();

}
