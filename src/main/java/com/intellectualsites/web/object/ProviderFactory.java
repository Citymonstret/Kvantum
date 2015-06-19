package com.intellectualsites.web.object;

/**
 * Created 2015-04-20 for IntellectualServer
 *
 * @author Citymonstret
 */
public interface ProviderFactory<T extends VariableProvider> {

    T get(final Request r);

    String providerName();

}
