/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2018 Alexander Söderberg
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
package xyz.kvantum.server.api.templates;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import xyz.kvantum.server.api.util.ProviderFactory;
import xyz.kvantum.server.api.util.VariableProvider;

import java.util.ArrayList;
import java.util.Collection;

@NoArgsConstructor(access = AccessLevel.PRIVATE) final public class TemplateManager {

    private static TemplateManager instance;
    private final Collection<ProviderFactory<? extends VariableProvider>> providers =
        new ArrayList<>();

    public static TemplateManager get() {
        if (instance == null) {
            instance = new TemplateManager();
        }
        return instance;
    }

    public void addProviderFactory(
        @NonNull final ProviderFactory<? extends VariableProvider> factory) {
        for (final ProviderFactory<?> registeredFactory : this.providers) {
            if (registeredFactory.providerName().equals(factory.providerName())) {
                throw new IllegalArgumentException("Cannot register a provider factory twice");
            }
        }
        this.providers.add(factory);
    }

    public Collection<ProviderFactory<? extends VariableProvider>> getProviders() {
        return ImmutableList.<ProviderFactory<? extends VariableProvider>>builder()
            .addAll(this.providers).build();
    }
}
