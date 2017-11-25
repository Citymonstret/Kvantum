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
package xyz.kvantum.server.implementation.sqlite;

import lombok.RequiredArgsConstructor;
import xyz.kvantum.server.api.session.ISession;
import xyz.kvantum.server.api.session.ISessionDatabase;
import xyz.kvantum.server.api.session.SessionLoad;
import xyz.kvantum.server.implementation.SQLiteApplicationStructure;

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
