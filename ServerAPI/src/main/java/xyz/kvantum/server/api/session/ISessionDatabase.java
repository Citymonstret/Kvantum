/*
 *
 *    Copyright (C) 2017 IntellectualSites
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.kvantum.server.api.session;

import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.logging.Logger;

public interface ISessionDatabase
{

    void setup() throws Exception;

    SessionLoad getSessionLoad(String sessionID);

    default SessionLoad isValid(final String session)
    {
        final SessionLoad sessionLoad = getSessionLoad( session );
        if ( sessionLoad == null )
        {
            return null;
        }

        long difference = ( System.currentTimeMillis() - sessionLoad.getLastActive() ) / 1000;
        if ( difference >= CoreConfig.Sessions.sessionTimeout )
        {
            if ( CoreConfig.debug )
            {
                Logger.debug( "Deleted outdated session: %s", session );
            }
            deleteSession( session );
            return null;
        }
        return sessionLoad;
    }

    void storeSession(ISession session);

    void updateSession(String session);

    void deleteSession(String session);

}
