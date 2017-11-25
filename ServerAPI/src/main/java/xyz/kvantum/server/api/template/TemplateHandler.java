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
package xyz.kvantum.server.api.template;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.config.Message;

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
