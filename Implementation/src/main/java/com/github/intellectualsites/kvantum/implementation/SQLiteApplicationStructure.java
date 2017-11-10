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
package com.github.intellectualsites.kvantum.implementation;

import com.github.intellectualsites.kvantum.api.exceptions.KvantumException;
import com.github.intellectualsites.kvantum.api.logging.Logger;
import com.github.intellectualsites.kvantum.api.util.ApplicationStructure;
import com.github.intellectualsites.kvantum.api.util.SQLiteManager;
import lombok.Getter;

import java.io.IOException;
import java.sql.SQLException;

public abstract class SQLiteApplicationStructure extends ApplicationStructure
{

    @Getter
    private final SQLiteManager databaseManager;

    public SQLiteApplicationStructure(final String applicationName)
    {
        super( applicationName );
        try
        {
            this.databaseManager = new SQLiteManager( this.applicationName );
        } catch ( final IOException | SQLException e )
        {
            throw new KvantumException( e );
        }
        this.accountManager = createNewAccountManager();
        Logger.info( "Initialized SQLiteApplicationStructure: %s", this.applicationName );
    }

}
