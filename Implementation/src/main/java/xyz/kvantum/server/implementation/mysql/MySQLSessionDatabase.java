package xyz.kvantum.server.implementation.mysql;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import xyz.kvantum.server.api.session.ISession;
import xyz.kvantum.server.api.session.ISessionDatabase;
import xyz.kvantum.server.api.session.SessionLoad;
import xyz.kvantum.server.api.util.AsciiString;
import xyz.kvantum.server.implementation.MySQLApplicationStructure;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@RequiredArgsConstructor
public class MySQLSessionDatabase implements ISessionDatabase
{

    private final MySQLApplicationStructure applicationStructure;

    @Override
    public void setup() throws Exception
    {
        this.applicationStructure.getDatabaseManager().executeUpdate(
                "CREATE TABLE IF NOT EXISTS `sessions` (" +
                        "session_id INTEGER NULL AUTO_INCREMENT," +
                        "id varchar(64) NOT NULL," +
                        "last_active TIME DEFAULT CURRENT_TIMESTAMP NOT NULL," +
                        "session_key varchar(64) NOT NULL," +
                        "CONSTRAINT sessions_PK PRIMARY KEY (session_id)," +
                        "CONSTRAINT sessions_UN UNIQUE KEY (id)" +
                        ")"
        );
    }

    @Override
    public SessionLoad getSessionLoad(@NonNull final AsciiString sessionID)
    {
        SessionLoad sessionLoad = null;
        try ( final Connection connection = applicationStructure.getDatabaseManager().getConnection() )
        {
            try ( final PreparedStatement statement = connection
                    .prepareStatement( "SELECT * FROM sessions WHERE id = ?" ) )
            {
                statement.setString( 1, sessionID.toString() );
                try ( final ResultSet resultSet = statement.executeQuery() )
                {
                    if ( resultSet.next() )
                    {
                        sessionLoad = new SessionLoad( resultSet.getString( "session_key" ),
                                resultSet.getLong( "last_active" ) );
                    }
                }
            } catch ( final Exception e )
            {
                e.printStackTrace();
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
        if ( getSessionLoad( (AsciiString) session.get( "id" ) ) != null )
        {
            updateSession( (AsciiString) session.get( "id" ) );
        } else
        {
            try ( final Connection connection = applicationStructure.getDatabaseManager().getConnection() )
            {
                try ( final PreparedStatement statement = connection.prepareStatement( "INSERT INTO sessions(`id`," +
                        "`last_active`, `session_key`) VALUES(?, ?, ?)" ) )
                {
                    statement.setString( 1, session.get( "id" ).toString() );
                    statement.setLong( 2, System.currentTimeMillis() );
                    statement.setString( 3, session.getSessionKey().toString() );
                    statement.executeUpdate();
                } catch ( final Exception e )
                {
                    e.printStackTrace();
                }
            } catch ( final Exception e )
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void deleteSession(@NonNull final AsciiString session)
    {
        try ( final Connection connection = applicationStructure.getDatabaseManager().getConnection() )
        {
            try ( final PreparedStatement statement = connection
                    .prepareStatement( "DELETE FROM sessions WHERE id = ?" ) )
            {
                statement.setString( 1, session.toString() );
                statement.executeUpdate();
            } catch ( final Exception e )
            {
                e.printStackTrace();
            }
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }
    }

    @Override
    public void updateSession(final AsciiString session)
    {
        try ( final Connection connection = applicationStructure.getDatabaseManager().getConnection() )
        {
            try ( final PreparedStatement statement = connection
                    .prepareStatement( "UPDATE sessions SET last_active = ? WHERE id = ?" ) )
            {
                statement.setLong( 1, System.currentTimeMillis() );
                statement.setString( 2, session.toString() );
                statement.executeUpdate();
            } catch ( final Exception e )
            {
                e.printStackTrace();
            }
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }
    }
}
