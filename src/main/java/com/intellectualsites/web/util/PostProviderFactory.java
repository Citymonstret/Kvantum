package com.intellectualsites.web.util;

import com.intellectualsites.web.object.PostRequest;
import com.intellectualsites.web.object.ProviderFactory;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.VariableProvider;

/**
 * Created 2015-04-25 for IntellectualServer
 *
 * @author Citymonstret
 */
public class PostProviderFactory implements ProviderFactory<PostProviderFactory>, VariableProvider {

    private PostRequest p;

    public PostProviderFactory(PostRequest p) {
        this.p = p;
    }

    public PostProviderFactory() {}

    @Override
    public PostProviderFactory get(Request r) {
        if (r.getPostRequest() == null) {
            return null;
        }
        return new PostProviderFactory(r.getPostRequest());
    }

    @Override
    public String providerName() {
        return "post";
    }

    @Override
    public boolean contains(String variable) {
        return p.contains(variable);
    }

    @Override
    public Object get(String variable) {
        return p.get(variable);
    }
}
