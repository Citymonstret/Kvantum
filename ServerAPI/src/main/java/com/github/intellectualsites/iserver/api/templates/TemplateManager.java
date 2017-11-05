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
package com.github.intellectualsites.iserver.api.templates;

import com.github.intellectualsites.iserver.api.util.Assert;
import com.github.intellectualsites.iserver.api.util.ProviderFactory;
import com.github.intellectualsites.iserver.api.util.VariableProvider;
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
