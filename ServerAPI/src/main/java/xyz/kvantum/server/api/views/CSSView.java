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
package xyz.kvantum.server.api.views;

import xyz.kvantum.server.api.cache.CacheApplicable;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.util.FileExtension;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Static file view server CSS files, without any preprocessing.
 * {@inheritDoc}
 */
public final class CSSView extends StaticFileView implements CacheApplicable {

    public CSSView(final String filter, final Map<String, Object> options) {
        super(filter, options, "css", Collections.singletonList(FileExtension.CSS));
        super.relatedFolderPath = "/assets/css";
        super.setOption("extension", "css");
        super.defaultFilePattern = "${file}.css";
    }

    @Override public boolean isApplicable(final AbstractRequest r) {
        final Optional<Boolean> cacheApplicableBoolean = getOptionSafe("cacheApplicable");
        return cacheApplicableBoolean.orElse(true);
    }

}
