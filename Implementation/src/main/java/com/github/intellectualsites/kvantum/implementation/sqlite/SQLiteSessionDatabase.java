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
package com.github.intellectualsites.kvantum.implementation.sqlite;

import com.github.intellectualsites.kvantum.api.session.ISession;
import com.github.intellectualsites.kvantum.api.session.ISessionDatabase;
import com.github.intellectualsites.kvantum.api.session.SessionLoad;
import com.github.intellectualsites.kvantum.implementation.SQLiteApplicationStructure;
import lombok.RequiredArgsConstructor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

@RequiredArgsConstructor
final public class SQLiteSessionDatabase implements ISessionDatabase
{

    private final SQLiteApplicationStructure applicationStructure;

    @Override
    public void setup() throws Exception
    {
        this.applicationStructure.getDatabaseManager().executeUpdate(
                "CREATE TABLE IF NOT EXISTS sessions (" +
                        " session_id INTEGER PRIMARY KEY," +
                        " id VARCHAR (64) UNIQUE NOT NULL," +
                        " last_active TIME DEFAULT (CURRENT_TIMESTAMP)," +
                        " session_key VARCHAR (64) NOT NULL )"
        );
    }

    @Override
    public SessionLoad getSessionLoad(String sessionID)
    {
        SessionLoad sessionLoad = null;
        try ( final PreparedStatement statement = this.applicationStructure
                .getDatabaseManager().prepareStatement( "SELECT * FROM sessions WHERE id = ?" ) )
        {
            statement.setString( 1, sessionID );
            try ( final ResultSet resultSet = statement.executeQuery() )
            {
                if ( resultSet.next() )
                {
                    sessionLoad = new SessionLoad( resultSet.getInt( "session_id" ),
                            resultSet.getString( "session_key" ), resultSet.getLong( "last_active" ) );
                }
            }
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }
        return sessionLoad;
    }

    @Override
    public void storeSession(final ISession session)
    {
        if ( getSessionLoad( session.get( "id" ).toString() ) != null )
        {
            updateSession( session.get( "id" ).toString() );
        } else
        {
            try ( final PreparedStatement statement = this.applicationStructure.getDatabaseManager()
                    .prepareStatement( "INSERT INTO sessions(`id`,`last_active`, `session_key`) VALUES(?, ?, ?)" ) )
            {
                statement.setString( 1, session.get( "id" ).toString() );
                statement.setLong( 2, System.currentTimeMillis() );
                statement.setString( 3, session.getSessionKey() );
                statement.executeUpdate();
            } catch ( final Exception e )
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void deleteSession(String session)
    {
        try ( final PreparedStatement statement = this.applicationStructure.getDatabaseManager()
                .prepareStatement( "DELETE FROM sessions WHERE id = ?" ) )
        {
            statement.setString( 1, session );
            statement.executeUpdate();
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }
    }

    @Override
    public void updateSession(final String session)
    {
        try ( final PreparedStatement statement = this.applicationStructure.getDatabaseManager()
                .prepareStatement( "UPDATE sessions SET last_active = ? WHERE id = ?" ) )
        {
            statement.setLong( 1, System.currentTimeMillis() );
            statement.setString( 2, session );
            statement.executeUpdate();
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }
    }
}
