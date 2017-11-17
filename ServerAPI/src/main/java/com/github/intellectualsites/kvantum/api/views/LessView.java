/*
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
package com.github.intellectualsites.kvantum.api.views;

import com.github.intellectualsites.kvantum.api.cache.CacheApplicable;
import com.github.intellectualsites.kvantum.api.request.AbstractRequest;
import com.github.intellectualsites.kvantum.api.response.Response;
import com.github.intellectualsites.kvantum.api.util.FileExtension;
import org.lesscss.LessCompiler;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class LessView extends StaticFileView implements CacheApplicable
{

    public static LessCompiler compiler;

    public LessView(String filter, Map<String, Object> options)
    {
        super( filter, options, "less", Collections.singletonList( FileExtension.LESS ) );
        super.relatedFolderPath = "./assets/less";
        super.setOption( "extension", "less" );
        super.defaultFilePattern = "${file}.less";
    }

    public static String getLess(String content)
    {
        if ( compiler == null )
        {
            compiler = new LessCompiler();
        }
        try
        {
            return compiler.compile( content );
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }

        return "";
    }

    @Override
    public void handle(final AbstractRequest r, final Response response)
    {
        super.handle( r, response );
        response.setContent( getLess( response.getContent() ) );
    }

    @Override
    public boolean isApplicable(AbstractRequest r)
    {
        final Optional<Boolean> cacheApplicableBoolean = getOptionSafe( "cacheApplicable" );
        return cacheApplicableBoolean.orElse( true );
    }
}
