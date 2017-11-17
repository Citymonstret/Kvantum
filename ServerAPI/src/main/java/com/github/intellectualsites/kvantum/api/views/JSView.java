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
import com.github.intellectualsites.kvantum.api.util.FileExtension;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Created 2015-04-22 for Kvantum
 *
 * @author Citymonstret
 */
public class JSView extends StaticFileView implements CacheApplicable
{

    public JSView(String filter, Map<String, Object> options)
    {
        super( filter, options, "javascript", Collections.singletonList( FileExtension.JAVASCRIPT ) );
        super.relatedFolderPath = "/assets/js";
        super.setOption( "extension", "js" );
        super.defaultFilePattern = "${file}.js";
    }

    @Override
    public boolean isApplicable(AbstractRequest r)
    {
        final Optional<Boolean> cacheApplicableBoolean = getOptionSafe( "cacheApplicable" );
        return cacheApplicableBoolean.orElse( true );
    }
}
