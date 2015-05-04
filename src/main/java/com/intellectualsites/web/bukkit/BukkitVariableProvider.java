package com.intellectualsites.web.bukkit;

import com.intellectualsites.web.object.ProviderFactory;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.VariableProvider;
import org.bukkit.Bukkit;

/**
 * Created 2015-05-04 for IntellectualServer
 *
 * @author Citymonstret
 */
public class BukkitVariableProvider implements ProviderFactory<BukkitVariableProvider>, VariableProvider {

    @Override
    public BukkitVariableProvider get(Request r) {
        return this;
    }

    @Override
    public String providerName() {
        return "bukkit";
    }

    @Override
    public boolean contains(String variable) {
        switch (variable.toLowerCase()) {
            case "version":
                return true;
            default:
                return false;
        }
    }

    @Override
    public Object get(String variable) {
        switch (variable.toLowerCase()) {
            case "version":
                return Bukkit.getVersion();
            default:
                return null;
        }
    }

}
