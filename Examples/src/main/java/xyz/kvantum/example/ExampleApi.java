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
package xyz.kvantum.example;

import xyz.kvantum.example.object.FileContext;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.orm.KvantumObjectFactory;
import xyz.kvantum.server.api.orm.KvantumObjectParserResult;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.request.HttpMethod;
import xyz.kvantum.server.api.response.Header;
import xyz.kvantum.server.api.response.Response;
import xyz.kvantum.server.api.util.ParameterScope;
import xyz.kvantum.server.api.views.annotatedviews.ViewMatcher;

import java.util.Collection;
import java.util.Collections;

/**
 * Simple REST gateway for https://github.com/Sauilitired/FileServer
 */
@SuppressWarnings("unused") public class ExampleApi {

    //
    // Factory class which parses request parameters into objects, in this
    // case; FileContexts
    //
    private final KvantumObjectFactory<FileContext> factory =
        KvantumObjectFactory.from(FileContext.class);

    //
    // Valid password tokens
    //
    private final Collection<String> tokens;

    ExampleApi() {
        //
        // Scan the current instance for @ViewMather annotations
        //
        ServerImplementation.getImplementation().getRouter().scanAndAdd(this);
        this.tokens = Collections.singletonList("randomtokenhere");
    }

    //
    // Match POST requests to "/file/update"
    //
    @ViewMatcher(filter = "file/update", httpMethod = HttpMethod.POST) public void onFileUpdate(
        final AbstractRequest request, final Response response) {
        //
        // Parse the request
        //
        final KvantumObjectParserResult<FileContext> result =
            factory.build(ParameterScope.POST).parseRequest(request);
        //
        // Check to see if the parsing was successful, and then generate
        // a response
        //
        if (result.isSuccess()) {
            final FileContext attempt = result.getParsedObject();
            if (!tokens.contains(attempt.getToken())) {
                Logger.error("Invalid file update request (Could not Authenticate)");
                response.getHeader().setStatus(Header.STATUS_ACCESS_DENIED);
                response.setResponse("Invalid access token...");
            } else {
                Logger.info("File update: " + attempt.getName());
                Logger.info("File update type: " + attempt.getType());
                response.getHeader().setStatus(Header.STATUS_OK);
                response.setResponse("File change accepted");
            }
        } else {
            //
            // Request parameters couldn't be parsed into a FileContext
            //
            Logger.error("Invalid file update request (Cannot Parse)");
            response.getHeader().setStatus(Header.STATUS_BAD_REQUEST);
            response.setResponse("Not a valid request...");
        }
    }

}
