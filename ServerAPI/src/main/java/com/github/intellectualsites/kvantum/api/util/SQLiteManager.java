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
package com.github.intellectualsites.kvantum.api.util;

import com.github.intellectualsites.kvantum.api.core.ServerImplementation;
import com.github.intellectualsites.kvantum.api.exceptions.KvantumException;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class SQLiteManager extends AutoCloseable
{

    public static Set<SQLiteManager> sessions = new HashSet<>();

    private final Connection connection;

    public SQLiteManager(final String name) throws IOException, SQLException
    {
        sessions.add( this );

        String name1 = name + ".db";
        File file = new File( new File( ServerImplementation.getImplementation().getCoreFolder(), "storage" ), name1 );
        if ( !file.exists() && ( !( file.getParentFile().exists() || file.getParentFile().mkdir() ) || !file
                .createNewFile() ) )
        {
            throw new KvantumException( "Couldn't create: " + name );
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
