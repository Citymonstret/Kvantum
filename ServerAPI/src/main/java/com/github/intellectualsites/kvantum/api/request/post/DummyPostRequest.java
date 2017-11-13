package com.github.intellectualsites.kvantum.api.request.post;

import com.github.intellectualsites.kvantum.api.request.Request;

final public class DummyPostRequest extends PostRequest
{

    public DummyPostRequest(final Request parent, final String rawRequest)
    {
        super( parent, rawRequest, false );
    }

    @Override
    protected void parseRequest(String rawRequest)
    {
    }

    @Override
    public EntityType getEntityType()
    {
        return EntityType.NONE;
    }

}
