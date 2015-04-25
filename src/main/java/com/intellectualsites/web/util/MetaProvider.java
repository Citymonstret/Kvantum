package com.intellectualsites.web.util;

import com.intellectualsites.web.object.ProviderFactory;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.VariableProvider;

/**
 * Created 2015-04-25 for IntellectualServer
 *
 * @author Citymonstret
 */
public class MetaProvider implements ProviderFactory<MetaProvider>, VariableProvider {

    private Request r;
    public MetaProvider(final Request r) {
        this.r = r;
    }
    public MetaProvider() {}

    @Override
    public MetaProvider get(Request r) {
        return new MetaProvider(r);
    }

    @Override
    public String providerName() {
        return "meta";
    }

    @Override
    public boolean contains(String variable) {
        return r.getMeta("doc." + variable) != null;
    }

    @Override
    public Object get(String variable) {
        return r.getMeta("doc." + variable);
    }
}
