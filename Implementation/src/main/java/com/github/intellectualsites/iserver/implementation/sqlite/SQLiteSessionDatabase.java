package com.github.intellectualsites.iserver.implementation.sqlite;

import com.github.intellectualsites.iserver.api.session.ISessionDatabase;
import com.github.intellectualsites.iserver.api.util.SQLiteApplicationStructure;
import lombok.RequiredArgsConstructor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

@RequiredArgsConstructor
public class SQLiteSessionDatabase implements ISessionDatabase
{

    private final SQLiteApplicationStructure applicationStructure;

    @Override
    public void setup() throws Exception
    {
        this.applicationStructure.getDatabaseManager().executeUpdate(
                "CREATE TABLE IF NOT EXISTS sessions (" +
                        " session_id INTEGER PRIMARY KEY," +
                        " id VARCHAR (64) UNIQUE NOT NULL," +
                        " last_active TIME DEFAULT (CURRENT_TIMESTAMP)"
        );
    }

    public long containsSession(final String sessionID)
    {
        long ret = -1;
        try ( final PreparedStatement statement = this.applicationStructure
                .getDatabaseManager().prepareStatement( "SELECT last_active FROM sessions WHERE id = ?" ) )
        {
            statement.setString( 1, sessionID );
            try ( final ResultSet resultSet = statement.executeQuery() )
            {
                if ( resultSet.next() )
                {
                    ret = resultSet.getLong( "last_active" );
                }
            }
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public void storeSession(final String session)
    {
        if ( containsSession( session ) != -1 )
        {
            updateSession( session );
        } else
        {
            try ( final PreparedStatement statement = this.applicationStructure.getDatabaseManager()
                    .prepareStatement( "INSERT INTO sessions(`id`,`last_active`) VALUES(?, ?)" ) )
            {
                statement.setString( 1, session );
                statement.setLong( 2, System.currentTimeMillis() );
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
