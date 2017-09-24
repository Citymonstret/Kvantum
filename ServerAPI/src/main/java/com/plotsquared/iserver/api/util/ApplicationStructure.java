/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
 * <p>
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.plotsquared.iserver.api.util;

import com.plotsquared.iserver.api.account.AccountManager;
import com.plotsquared.iserver.api.core.IntellectualServer;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created 11/13/2015 for IntellectualServer
 *
 * @author Citymonstret
 */
@SuppressWarnings("unused")
public abstract class ApplicationStructure
{

    private final AccountManager accountManager;
    private final String applicationName;
    private SQLiteManager database;

    public ApplicationStructure(final String applicationName)
    {
        this.applicationName = Assert.notEmpty( applicationName );
        try
        {
            this.database = new SQLiteManager( this.applicationName );
        } catch ( final IOException | SQLException e )
        {
            throw new RuntimeException( e );
        }
        this.accountManager = createNewAccountManager();
    }

    public abstract AccountManager createNewAccountManager();

    public AccountManager getAccountManager()
    {
        return this.accountManager;
    }

    public SQLiteManager getDatabaseManager()
    {
        return this.database;
    }

    @Override
    public String toString()
    {
        return this.applicationName;
    }

    public void registerViews(IntellectualServer server)
    {
        // Override me!
    }
}
