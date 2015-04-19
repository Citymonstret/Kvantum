package com.intellectualsites.web.object;

/**
 * Created 2015-04-19 for IntellectualServer
 *
 * @author Citymonstret
 */
public interface VariableProvider {

    public boolean contains(final String variable);
    public Object get(final String variable);

}
