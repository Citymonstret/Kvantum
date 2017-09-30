package com.plotsquared.iserver.api.session;

@FunctionalInterface
public interface ISessionCreator
{

    ISession createSession();

}
