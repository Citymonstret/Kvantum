package com.intellectualsites.web.util;

import com.intellectualsites.web.object.ProviderFactory;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.VariableProvider;

/**
 * Created 2015-04-20 for IntellectualServer
 *
 * @author Citymonstret
 */
public class ServerProvider implements ProviderFactory<ServerProvider>, VariableProvider {

    public ServerProvider get(Request r) {
        return this;
    }

    public String providerName() {
        return "system";
    }

    public boolean contains(String variable) {
        switch(variable.toLowerCase()) {
            case "authors":
            case "filters":
            case "time":
                return true;
            default:
                return false;
        }
    }

    public Object get(String variable) {
        switch (variable.toLowerCase()) {
            case "time":
                return TimeUtil.getHTTPTimeStamp();
            case "authors":
                return new String[] { "Citymonstret", "IntellectualSites" };
            case "filters":
                return new String[] { "LIST", "UPPERCASE", "LOWERCASE" };
            default:
                return "";
        }
    }
}
