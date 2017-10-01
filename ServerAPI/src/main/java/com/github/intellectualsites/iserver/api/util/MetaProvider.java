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
package com.github.intellectualsites.iserver.api.util;

import com.github.intellectualsites.iserver.api.request.Request;

import java.util.Map;
import java.util.Optional;

/**
 * Created 2015-04-25 for IntellectualServer
 *
 * @author Citymonstret
 */
public class MetaProvider implements ProviderFactory<MetaProvider>, VariableProvider
{

    private Request r;

    public MetaProvider()
    {
    }

    private MetaProvider(final Request r)
    {
        this.r = r;
    }

    @Override
    public Optional<MetaProvider> get(Request r)
    {
        return Optional.of( new MetaProvider( r ) );
    }

    @Override
    public String providerName()
    {
        return "meta";
    }

    @Override
    public boolean contains(String variable)
    {
        return r.getMeta( "doc." + variable ) != null;
    }

    @Override
    public Object get(String variable)
    {
        return r.getMeta( "doc." + variable );
    }

    @Override
    public Map<String, Object> getAll()
    {
        return r.getAllMeta();
    }
}
