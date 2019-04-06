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

import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.request.AbstractRequest;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public final class UrlEncodedPostRequest extends RequestEntity {

    public UrlEncodedPostRequest(final AbstractRequest parent, final String request) {
        super(parent, request, false);
    }

    @Override protected void parseRequest(final String request) {
        String fixedRequest;
        try {
            fixedRequest = URLDecoder.decode(request, StandardCharsets.US_ASCII.toString());
        } catch (final Exception e) {
            if (CoreConfig.debug) {
                e.printStackTrace();
            }
            fixedRequest = request;
        }
        this.setRequest(fixedRequest);
        for (final String s : fixedRequest.split("&")) {
            if (!s.isEmpty()) {
                final String[] p = s.split("=");
                if (p.length < 2) {
                    continue;
                }
                getVariables().put(p[0], p[1].replace("+", " "));
            }
        }
    }

    @Override public EntityType getEntityType() {
        return EntityType.FORM_URLENCODED;
    }

}
