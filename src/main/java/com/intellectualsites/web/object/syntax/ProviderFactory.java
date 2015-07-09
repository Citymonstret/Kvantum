package com.intellectualsites.web.object.syntax;

import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.syntax.VariableProvider;

/**
 * Created 2015-04-20 for IntellectualServer
 *
 * @author Citymonstret
 */
public interface ProviderFactory<T extends VariableProvider> {

    T get(final Request r);

    String providerName();

}
