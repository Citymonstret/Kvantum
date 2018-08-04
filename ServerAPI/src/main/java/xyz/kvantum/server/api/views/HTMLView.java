/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2018 IntellectualSites
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
package xyz.kvantum.server.api.views;

import xyz.kvantum.server.api.cache.CacheApplicable;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.util.FileExtension;
import xyz.kvantum.server.api.util.ProviderFactory;
import xyz.kvantum.server.api.util.VariableProvider;

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

    public static class HTMLProvider implements ProviderFactory<HTMLProvider>, VariableProvider
    {

        private final Map<String, String> storage = new HashMap<>();

        HTMLProvider(final AbstractRequest r)
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
