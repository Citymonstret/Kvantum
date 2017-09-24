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
package com.plotsquared.iserver.api.request;

import com.plotsquared.iserver.api.util.Assert;
import com.plotsquared.iserver.api.util.MapUtil;
import com.plotsquared.iserver.api.util.ProviderFactory;
import com.plotsquared.iserver.api.util.VariableProvider;

import java.util.Map;

/**
 * Created 2015-04-25 for IntellectualServer
 *
 * @author Citymonstret
 */
public class PostProviderFactory implements ProviderFactory<PostProviderFactory>, VariableProvider
{

    private PostRequest p;

    public PostProviderFactory()
    {
    }

    private PostProviderFactory(final PostRequest p)
    {
        this.p = p;
    }

    @Override
    public PostProviderFactory get(final Request r)
    {
        Assert.notNull( r );

        if ( r.getPostRequest() == null )
        {
            return null;
        }
        return new PostProviderFactory( r.getPostRequest() );
    }

    @Override
    public String providerName()
    {
        return "post";
    }

    @Override
    public boolean contains(final String variable)
    {
        Assert.notNull( variable );

        return p.contains( variable );
    }

    @Override
    public Object get(final String variable)
    {
        Assert.notNull( variable );

        return p.get( variable );
    }

    @Override
    public Map<String, Object> getAll()
    {
        return MapUtil.convertMap( this.p.get(), (s) -> s );
    }
}
