/*
 * IntellectualServer is a web server, written entirely in the Java language.
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
package com.github.intellectualsites.iserver.api.util;

import com.github.intellectualsites.iserver.api.account.IAccountManager;
import com.github.intellectualsites.iserver.api.core.IntellectualServer;
import com.github.intellectualsites.iserver.api.session.ISessionDatabase;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EqualsAndHashCode(of = "applicationName")
public abstract class ApplicationStructure
{

    protected final String applicationName;

    @Getter
    protected IAccountManager accountManager;

    @Getter
    protected ISessionDatabase sessionDatabase;

    public abstract IAccountManager createNewAccountManager();


    @Override
    public String toString()
    {
        return this.applicationName;
    }

    /**
     * Register application specific views, this is just a placeholder method
     * that makes sure that the views are added after all dependencies are setup
     *
     * @param server Server implementation (utility reference)
     */
    public void registerViews(final IntellectualServer server)
    {
        // Override me
    }
}
