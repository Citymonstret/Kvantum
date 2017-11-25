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
package xyz.kvantum.server.api.request.post;

import org.json.simple.JSONObject;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.util.KvantumJsonFactory;

import java.util.Map;

public class JsonPostRequest extends PostRequest
{

    public JsonPostRequest(final AbstractRequest parent, final String rawRequest)
    {
        super( parent, rawRequest, false );
    }

    @Override
    protected void parseRequest(final String rawRequest)
    {
        final JSONObject jsonObject = KvantumJsonFactory.parseJSONObject( rawRequest );
        for ( final Map.Entry<String, Object> entry : jsonObject.entrySet() )
        {
            this.getVariables().put( entry.getKey(), entry.getValue().toString() );
        }
    }

    @Override
    public EntityType getEntityType()
    {
        return EntityType.JSON;
    }
}
