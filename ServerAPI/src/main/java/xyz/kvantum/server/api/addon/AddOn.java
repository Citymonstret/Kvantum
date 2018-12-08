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
package xyz.kvantum.server.api.addon;

import lombok.*;
import net.sf.oval.constraint.NotNull;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.logging.LogProvider;

import java.util.UUID;

/**
 * Abstract class used to declare an addon. All addons should have a main class that extends this class.
 */
@SuppressWarnings("WeakerAccess") @EqualsAndHashCode @ToString public abstract class AddOn
    implements LogProvider {

    @Getter private final UUID uuid = UUID.randomUUID();
    @Getter @Setter(AccessLevel.PACKAGE) private boolean enabled;
    @Getter @Setter(AccessLevel.PACKAGE) private AddOnClassLoader classLoader;
    @Getter @Setter(AccessLevel.PACKAGE) private String name;

    /**
     * Enable the plugin
     */
    void enable() {
        if (this.isEnabled()) {
            throw new IllegalStateException("Cannot enable the addon when it's already enabled");
        }
        this.onEnable();
        this.setEnabled(true);
    }

    /**
     * Disable the plugin
     */
    void disable() {
        if (!this.isEnabled()) {
            throw new IllegalStateException("Cannot disable the addon when it isn't enabled");
        }
        this.onDisable();
        this.setEnabled(false);
    }

    /**
     * Called when the addon is enabled
     */
    protected abstract void onEnable();

    /**
     * Called when the addon is disabled
     */
    protected abstract void onDisable();

    @Override public final String getLogIdentifier() {
        return this.name;
    }

    /**
     * Log a message
     *
     * @param message Message to be logged
     * @see xyz.kvantum.server.api.core.Kvantum#log(String, Object...)
     */
    public void log(@NotNull final String message, final Object... args) {
        ServerImplementation.getImplementation().log(this, message, args);
    }

}
