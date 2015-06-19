package com.intellectualsites.web.util;

import com.intellectualsites.web.object.ProviderFactory;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.VariableProvider;

/**
 * Created 2015-06-19 for IntellectualServer
 *
 * @author Liam Heeger (peelsh)
 */
public class MySQLProvider implements ProviderFactory<MySQLProvider>, VariableProvider {

    public MySQLProvider get(Request r) {
        return this;
    }

    public String providerName() {
        return "mysql";
    }

    public boolean contains(String variable) {
        switch(variable.toLowerCase()) {
            case "db":
            case "table":
            case "query":
            case "true":
            case "false":
                return true;
            default:
                return false;
        }
    }

    public Object get(String variable) {
        switch (variable.toLowerCase()) {
            case "null":
                return null;
            case "true":
                return true;
            case "false":
                return false;
            default:
                return "";
        }
    }
}
