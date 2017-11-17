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
package com.github.intellectualsites.kvantum.api.templates;

import com.github.intellectualsites.kvantum.api.util.Assert;
import com.github.intellectualsites.kvantum.api.util.ProviderFactory;
import com.github.intellectualsites.kvantum.api.util.VariableProvider;
import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final public class TemplateManager
{

    private static TemplateManager instance;
    private final Collection<ProviderFactory<? extends VariableProvider>> providers = new ArrayList<>();

    public static TemplateManager get()
    {
        if ( instance == null )
        {
            instance = new TemplateManager();
        }
        return instance;
    }

    public void addProviderFactory(final ProviderFactory<? extends VariableProvider> factory)
    {
        Assert.notNull( factory );
        this.providers.add( factory );
    }

    public Collection<ProviderFactory<? extends VariableProvider>> getProviders()
    {
        return ImmutableList.<ProviderFactory<? extends VariableProvider>>builder().addAll( this.providers ).build();
    }
}
