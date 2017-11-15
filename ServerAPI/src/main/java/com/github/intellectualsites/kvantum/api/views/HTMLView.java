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
package com.github.intellectualsites.kvantum.api.views;

import com.github.intellectualsites.kvantum.api.cache.CacheApplicable;
import com.github.intellectualsites.kvantum.api.request.AbstractRequest;
import com.github.intellectualsites.kvantum.api.util.FileExtension;
import com.github.intellectualsites.kvantum.api.util.ProviderFactory;
import com.github.intellectualsites.kvantum.api.util.VariableProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created 2015-04-19 for Kvantum
 *
 * @author Citymonstret
 */
public class HTMLView extends StaticFileView implements CacheApplicable
{

    public HTMLView(String filter, Map<String, Object> options)
    {
        super( filter, options, "html", Collections.singletonList( FileExtension.HTML ) );
        super.setOption( "extension", "html" );
        super.defaultFilePattern = "${file}.html";
    }

    @Override
    public HTMLProvider getFactory(final AbstractRequest r)
    {
        return new HTMLProvider( r );
    }

    @Override
    public boolean isApplicable(AbstractRequest r)
    {
        final Optional<Boolean> cacheApplicableBoolean = getOptionSafe( "cacheApplicable" );
        return cacheApplicableBoolean.orElse( true );
    }

    public class HTMLProvider implements ProviderFactory<HTMLProvider>, VariableProvider
    {

        private final Map<String, String> storage = new HashMap<>();

        public HTMLProvider(final AbstractRequest r)
        {
            storage.put( "name", r.getMeta( "html_file" ) + ".html" );
        }

        @Override
        public Optional<HTMLProvider> get(AbstractRequest r)
        {
            return Optional.of( this );
        }

        @Override
        public String providerName()
        {
            return "document";
        }

        @Override
        public boolean contains(String variable)
        {
            return storage.containsKey( variable );
        }

        @Override
        public Object get(String variable)
        {
            return storage.get( variable );
        }

        @Override
        public Map<String, Object> getAll()
        {
            return new HashMap<>( this.storage );
        }
    }
}
