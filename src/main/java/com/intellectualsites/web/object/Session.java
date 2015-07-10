package com.intellectualsites.web.object;

import com.intellectualsites.web.object.syntax.VariableProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Created 2015-04-19 for IntellectualServer
 *
 * @author Citymonstret
 */
public class Session implements VariableProvider {

    private Map<String, Object> sessionStorage;

    public Session() {
        sessionStorage = new HashMap<>();
    }

    public boolean contains(String variable) {
        return sessionStorage.containsKey(variable.toLowerCase());
    }

    public Object get(String variable) {
        return sessionStorage.get(variable.toLowerCase());
    }

    public void set(final String s, final Object o) {
        sessionStorage.put(s.toLowerCase(), o);
    }
}
