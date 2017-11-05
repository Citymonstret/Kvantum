package com.github.intellectualsites.iserver.api.template;

import com.github.intellectualsites.iserver.api.config.CoreConfig;
import com.github.intellectualsites.iserver.api.config.Message;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@SuppressWarnings({ "WeakerAccess", "unused" })
@RequiredArgsConstructor
@EqualsAndHashCode(of = { "engineName" })
public abstract class TemplateHandler
{

    @Getter
    private final CoreConfig.TemplatingEngine engineEnum;
    @Getter
    private final String engineName;

    public final void load()
    {
        Message.TEMPLATING_ENGINE_STATUS.log( engineName,
                CoreConfig.Templates.status( engineEnum ) );
        if ( !CoreConfig.Templates.status( engineEnum ) )
        {
            return;
        }
        this.onLoad();
    }

    protected abstract void onLoad();

}
