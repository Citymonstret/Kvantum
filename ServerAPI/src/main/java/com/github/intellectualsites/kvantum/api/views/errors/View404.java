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
package com.github.intellectualsites.kvantum.api.views.errors;

import com.github.intellectualsites.kvantum.api.config.CoreConfig;

/**
 * Created 2015-04-19 for Kvantum
 *
 * @author Citymonstret
 */
public class View404 extends Error
{

    private View404(final String url)
    {
        super( 404, "Not Found: " + url );
    }

    public static View404 construct(final String url)
    {
        final String webAddress = CoreConfig.webAddress.endsWith( "/" ) ?
                CoreConfig.webAddress.substring( 0, CoreConfig.webAddress.length() - 1 ) : CoreConfig.webAddress;
        return new View404( webAddress + url );
    }

}
