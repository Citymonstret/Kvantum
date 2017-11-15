package com.github.intellectualsites.kvantum.api.request.post;

import com.github.intellectualsites.kvantum.api.request.AbstractRequest;

final public class DummyPostRequest extends PostRequest
{

    public DummyPostRequest(final AbstractRequest parent, final String rawRequest)
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
