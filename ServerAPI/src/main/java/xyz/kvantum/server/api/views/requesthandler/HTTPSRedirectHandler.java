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
package xyz.kvantum.server.api.views.requesthandler;

import lombok.Getter;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.response.Response;
import xyz.kvantum.server.api.util.MapUtil;
import xyz.kvantum.server.api.util.ProtocolType;
import xyz.kvantum.server.api.views.RequestHandler;

import java.util.function.BiConsumer;

/**
 * {@link RequestHandler RequestHandler} responsible for {@link ProtocolType#HTTP HTTP} -> {@link ProtocolType#HTTP
 * HTTPS } rerouting
 */
public final class HTTPSRedirectHandler extends SimpleRequestHandler {

    private static final BiConsumer<AbstractRequest, Response> responseGenerator =
        (request, response) -> {
            final AbstractRequest.Query query = request.getQuery();
            final StringBuilder urlBuilder =
                new StringBuilder("https://").append(CoreConfig.webAddress);
            if (CoreConfig.SSL.port != 443) {
                urlBuilder.append(":").append(CoreConfig.SSL.port);
            }
            urlBuilder.append(query.getResource());
            if (!query.getResource().endsWith("/")) {
                urlBuilder.append("/");
            }
            if (!query.getParameters().isEmpty()) {
                urlBuilder.append("?").append(MapUtil.join(query.getParameters(), "=", "&"));
            }
            response.getHeader().redirect(urlBuilder.toString());
            if (CoreConfig.debug) {
                Logger.debug("Generated HTTPS url: " + urlBuilder.toString());
            }
            final String responseBodyBuilder =
                "<h1>Redirecting...</h1>\n<p>If the request isn't redirecting," + " click: "
                    + "<a href=\"" + urlBuilder.toString() + "\" title=\"HTTPS Redirect\">"
                    + urlBuilder.toString() + "</a>";
            response.setResponse(responseBodyBuilder);
        };

    @Getter private static final HTTPSRedirectHandler instance = new HTTPSRedirectHandler();

    private HTTPSRedirectHandler() {
        super("", responseGenerator);
    }

}
