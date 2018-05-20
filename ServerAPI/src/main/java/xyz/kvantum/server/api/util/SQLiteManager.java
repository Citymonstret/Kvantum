/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
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
package xyz.kvantum.server.api.util;

import lombok.EqualsAndHashCode;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.exceptions.KvantumException;

import java.io.File;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class for dealing with common SQLite operations
 */
@SuppressWarnings({ "unused", "WeakerAccess" })
@EqualsAndHashCode(of = "name", callSuper = false)
public class SQLiteManager extends AutoCloseable
{

    private final Connection connection;
    private final String name;

    public SQLiteManager(final String name) throws IOException, SQLException
    {
        this.name = name + ".db";
        File file = new File( new File( ServerImplementation.getImplementation().getCoreFolder(), "storage" ),
                this.name );
        if ( !file.exists() && ( !( file.getParentFile().exists() || file.getParentFile().mkdir() ) || !file
                .createNewFile() ) )
        {
            throw new KvantumException( "Couldn't create: " + this.name );
        }
        this.connection = DriverManager.getConnection( "jdbc:sqlite:" + file.getAbsolutePath() );
    }

    public void executeUpdate(final String sql) throws SQLException
    {
        Statement statement = this.connection.createStatement();
        statement.executeUpdate( sql );
        statement.close();
    }

    public PreparedStatement prepareStatement(final String statement) throws SQLException
    {
        return connection.prepareStatement( statement );
    }

    public Blob createBlob()
    {
        try
        {
            return connection.createBlob();
        } catch ( SQLException e )
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void handleClose()
    {
        try
        {
            connection.close();
        } catch ( SQLException e )
        {
            e.printStackTrace();
        }
    }
}
