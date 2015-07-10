package com.intellectualsites.web.util;

import com.intellectualsites.web.object.syntax.ProviderFactory;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.syntax.VariableProvider;

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
            case "true":
            case "false":
            case "totalram":
            case "usedram":
            case "freeram":
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
            case "true":
                return true;
            case "false":
                return false;
            case "totalram":
                return (Runtime.getRuntime().totalMemory() / 1024) / 1024;
            case "usedram":
                return ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024) / 1024;
            case "freeram":
                return (Runtime.getRuntime().freeMemory() / 1024) / 1024;
            default:
                return "";
        }
    }
}
