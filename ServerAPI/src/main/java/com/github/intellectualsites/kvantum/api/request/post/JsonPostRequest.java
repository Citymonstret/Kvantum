package com.github.intellectualsites.kvantum.api.request.post;

import com.github.intellectualsites.kvantum.api.request.Request;
import com.github.intellectualsites.kvantum.api.util.KvantumJsonFactory;
import org.json.simple.JSONObject;

import java.util.Map;

public class JsonPostRequest extends PostRequest
{

    public JsonPostRequest(final Request parent, final String rawRequest)
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
