package com.github.intellectualsites.kvantum.api.mocking;

import com.github.intellectualsites.kvantum.api.request.AbstractRequest;

public class MockRequest extends AbstractRequest
{

    public MockRequest(final Query query)
    {
        this.setQuery( query );
    }

    @Override
    protected AbstractRequest newRequest(String query)
    {
        return null;
    }

    @Override
    public void requestSession()
    {
    }
}
