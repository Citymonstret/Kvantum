/*
 * Kvantum is a web server, written entirely in the Java language.
 * Copyright (C) 2017 IntellectualSites
 *
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.github.intellectualsites.kvantum.api.session;

import com.github.intellectualsites.kvantum.api.config.CoreConfig;
import com.github.intellectualsites.kvantum.api.logging.Logger;

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
