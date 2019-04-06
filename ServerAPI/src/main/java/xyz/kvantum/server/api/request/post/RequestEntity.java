/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2019 Alexander Söderberg
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
package xyz.kvantum.server.api.request.post;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.request.RequestChild;
import xyz.kvantum.server.api.util.Assert;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"unused", "WeakerAccess "}) public abstract class RequestEntity
    implements RequestChild {

    @Getter private final AbstractRequest parent;
    @Getter(AccessLevel.PROTECTED) private final String rawRequest;
    @Getter private final Map<String, String> variables;
    @Getter @Setter(AccessLevel.PROTECTED) private String request;
    private boolean loaded;

    protected RequestEntity(final AbstractRequest parent, final String rawRequest, boolean lazyLoad) {
        Assert.notNull(parent);
        Assert.notNull(rawRequest);

        this.loaded = false;
        this.parent = parent;
        this.rawRequest = rawRequest;
        this.request = rawRequest; // Can be overridden
        this.variables = new HashMap<>();
        if (!lazyLoad) {
            this.load();
        }
    }

    public void load() {
        if (this.loaded) {
            return;
        }
        this.parseRequest(rawRequest);
        this.loaded = true;
    }

    protected abstract void parseRequest(String rawRequest);

    public abstract EntityType getEntityType();

    /**
     * Get a parameter
     *
     * @param key Parameter key
     * @return Parameter value if found, else null
     */
    @Nullable public String get(final String key) {
        Assert.notNull(key);

        this.load();

        if (!this.getVariables().containsKey(key)) {
            return null; // Nullable
        }
        return this.getVariables().get(key);
    }

    /**
     * Check if a parameter is stored
     *
     * @param key Parameter key
     * @return True of the parameter is stored; else false
     */
    public boolean contains(final String key) {
        Assert.notNull(key);
        this.load();
        return this.getVariables().containsKey(key);
    }

    /**
     * Get a copy of the internal parameter map
     *
     * @return copy of the internal map
     */
    final public Map<String, String> get() {
        this.load();

        return new HashMap<>(this.variables);
    }

}
