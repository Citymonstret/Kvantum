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
package xyz.kvantum.server.api.config;

import lombok.NonNull;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.util.ProviderFactory;
import xyz.kvantum.server.api.util.VariableProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This allows you to access configuration variables through the variable syntax
 * {@inheritDoc}
 */
public class ConfigVariableProvider
    implements ProviderFactory<ConfigVariableProvider>, VariableProvider {

    private static ConfigVariableProvider instance;
    private final Map<String, WeakReference<ConfigProvider>> configurations;

    private ConfigVariableProvider() {
        configurations = new HashMap<>();
    }

    public static ConfigVariableProvider getInstance() {
        if (instance == null) {
            instance = new ConfigVariableProvider();
        }
        return instance;
    }

    /**
     * Add a configuration provider to this configuration variable provider
     *
     * @param provider Provider to add
     */
    public void add(@Nonnull @NonNull final ConfigProvider provider) {
        configurations.put(provider.toString(), new WeakReference<>(provider));
    }

    @Override public Optional<ConfigVariableProvider> get(final AbstractRequest r) {
        return Optional.of(this);
    }

    @Override public String providerName() {
        return "cfg";
    }

    @Override public boolean contains(final String variable) {
        final String[] parts = variable.split("@");
        if (configurations.containsKey(parts[0])) {
            final WeakReference<ConfigProvider> reference = configurations.get(parts[0]);
            final ConfigProvider provider = reference.get();
            if (provider != null) {
                return provider.contains(parts[1]);
            } else {
                this.configurations.remove(parts[0]);
            }
        }
        return false;
    }

    @Nullable @Override public Object get(final String variable) {
        String[] parts = variable.split("@");
        final WeakReference<ConfigProvider> reference = configurations.get(parts[0]);
        final ConfigProvider provider = reference.get();
        if (provider != null) {
            return provider.get(parts[1]);
        } else {
            this.configurations.remove(parts[0]);
        }
        return null; // Nullable
    }

    @Override public Map<String, Object> getAll() {
        final Map<String, Object> all = new HashMap<>();
        for (final Map.Entry<String, WeakReference<ConfigProvider>> entry1 : configurations
            .entrySet()) {
            final ConfigProvider provider = entry1.getValue().get();
            if (provider == null) {
                continue;
            }
            for (final Map.Entry<String, Object> entry2 : provider.getAll().entrySet()) {
                all.put(entry1.getKey() + "@" + entry2.getKey(), entry2.getValue());
            }
        }
        return all;
    }
}
