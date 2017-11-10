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
package com.github.intellectualsites.kvantum.api.template;

import com.github.intellectualsites.kvantum.api.config.CoreConfig;
import com.github.intellectualsites.kvantum.api.config.Message;
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
