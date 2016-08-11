//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//     IntellectualServer is a web server, written entirely in the Java language.                            /
//     Copyright (C) 2015 IntellectualSites                                                                  /
//                                                                                                           /
//     This program is free software; you can redistribute it and/or modify                                  /
//     it under the terms of the GNU General Public License as published by                                  /
//     the Free Software Foundation; either version 2 of the License, or                                     /
//     (at your option) any later version.                                                                   /
//                                                                                                           /
//     This program is distributed in the hope that it will be useful,                                       /
//     but WITHOUT ANY WARRANTY; without even the implied warranty of                                        /
//     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                         /
//     GNU General Public License for more details.                                                          /
//                                                                                                           /
//     You should have received a copy of the GNU General Public License along                               /
//     with this program; if not, write to the Free Software Foundation, Inc.,                               /
//     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.                                           /
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.plotsquared.iserver.util;

import com.plotsquared.iserver.core.Server;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class SQLiteManager
{

    public static Set<SQLiteManager> sessions = new HashSet<>();

    private final String name;
    private final Connection connection;
    private final File file;

    public SQLiteManager(final String name) throws IOException, SQLException
    {
        sessions.add( this );

        this.name = name + ".db";
        this.file = new File( new File( Server.getInstance().coreFolder, "storage" ), this.name );
        if ( !file.exists() )
        {
            if ( !( file.getParentFile().exists() || file.getParentFile().mkdir() ) || !file.createNewFile() )
            {
                throw new RuntimeException( "Couldn't create: " + name );
            }
        }
        this.connection = DriverManager.getConnection( "jdbc:sqlite:" + file.getAbsolutePath() );
    }

    public void executeUpdate(String sql) throws SQLException
    {
        Statement statement = this.connection.createStatement();
        statement.executeUpdate( sql );
        statement.close();
    }

    public PreparedStatement prepareStatement(String statement) throws SQLException
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

    public void close()
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
