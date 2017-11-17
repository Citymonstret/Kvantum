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
package com.github.intellectualsites.kvantum.api.util;

import com.github.intellectualsites.kvantum.api.account.IAccountManager;
import com.github.intellectualsites.kvantum.api.core.Kvantum;
import com.github.intellectualsites.kvantum.api.session.ISessionDatabase;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EqualsAndHashCode(of = "applicationName")
public abstract class ApplicationStructure
{

    protected final String applicationName;

    @Getter
    protected IAccountManager accountManager;

    @Getter
    protected ISessionDatabase sessionDatabase;

    public abstract IAccountManager createNewAccountManager();


    @Override
    public String toString()
    {
        return this.applicationName;
    }

    /**
     * Register application specific views, this is just a placeholder method
     * that makes sure that the views are added after all dependencies are setup
     *
     * @param server Server implementation (utility reference)
     */
    public void registerViews(final Kvantum server)
    {
        // Override me
    }
}
