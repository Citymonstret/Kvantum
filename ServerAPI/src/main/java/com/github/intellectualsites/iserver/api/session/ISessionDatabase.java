package com.github.intellectualsites.iserver.api.session;

import com.github.intellectualsites.iserver.api.config.CoreConfig;
import com.github.intellectualsites.iserver.api.logging.Logger;

import java.util.Map;

public interface ISessionDatabase
{

    void setup() throws Exception;

    long containsSession(String sessionID);

    Map<String, String> getSessionLoad(String sessionID);

    default boolean isValid(final String session)
    {
        long lastActive = containsSession( session );
        if ( lastActive == -1 )
        {
            return false;
        }
        long difference = ( System.currentTimeMillis() - lastActive ) / 1000;
        if ( difference >= CoreConfig.Sessions.sessionTimeout )
        {
            if ( CoreConfig.debug )
            {
                Logger.debug( "Deleted outdated session: %s", session );
            }
            deleteSession( session );
            return false;
        }
        return true;
    }

    void storeSession(ISession session);

    void updateSession(String session);

    void deleteSession(String session);

}
