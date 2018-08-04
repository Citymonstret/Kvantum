/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2018 IntellectualSites
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
package xyz.kvantum.server.api.views.annotatedviews.converters;

import lombok.NonNull;
import xyz.kvantum.server.api.response.Header;
import xyz.kvantum.server.api.response.Response;
import xyz.kvantum.server.api.views.annotatedviews.AnnotatedViewManager;
import xyz.kvantum.server.api.views.annotatedviews.OutputConverter;

final class HtmlConverter extends OutputConverter
{

    HtmlConverter(@NonNull final AnnotatedViewManager annotatedViewManager)
    {
        super( "html", String.class );
        annotatedViewManager.registerConverter( this );
    }

    @Override
    protected Response generateResponse(final Object input)
    {
        final Response response = new Response();
        response.setContent( input.toString() );
        response.getHeader().set( Header.HEADER_CONTENT_TYPE, Header.CONTENT_TYPE_HTML );
        return response;
    }

}
