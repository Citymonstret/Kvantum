/*
 * Kvantum is a web server, written entirely in the Java language.
 * Copyright (C) 2017 IntellectualSites
 *
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.github.intellectualsites.kvantum.api.request.post;

import com.github.intellectualsites.kvantum.api.request.AbstractRequest;
import com.github.intellectualsites.kvantum.api.util.KvantumJsonFactory;
import org.json.simple.JSONObject;

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
