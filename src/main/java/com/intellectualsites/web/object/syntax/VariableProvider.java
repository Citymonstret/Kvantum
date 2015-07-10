package com.intellectualsites.web.object.syntax;

/**
 * The variable provider class - Can get quite confusing.
 *
 * This generates a variable based on a key, and is accessed like
 ** <pre>
 * {@code
 * {{PROVIDER.NAME}}
 * }
 * </pre>
 * whereas the name would be the variable key
 *
 * @author Citymonstret
 */
public interface VariableProvider {

    /**
     * Does the provider contain this variable?
     *
     * @param variable Variable Key
     * @return True if the variable exists
     */
    boolean contains(final String variable);

    /**
     * Get the variable
     *
     * @param variable Variable Key
     * @return The object (or null)
     * @see #contains(String) - Use this to check if it exists
     */
    Object get(final String variable);

}
