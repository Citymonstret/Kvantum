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
