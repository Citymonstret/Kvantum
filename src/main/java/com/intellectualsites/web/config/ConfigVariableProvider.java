package com.intellectualsites.web.config;

import com.intellectualsites.web.object.ProviderFactory;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.VariableProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Created 2015-04-22 for IntellectualServer
 *
 * @author Citymonstret
 */
public class ConfigVariableProvider implements ProviderFactory<ConfigVariableProvider>, VariableProvider {

    private static ConfigVariableProvider instance;
    public static ConfigVariableProvider getInstance() {
        if (instance == null) {
            instance = new ConfigVariableProvider();
        }
        return instance;
    }

    private Map<String, ConfigProvider> configurations;

    public ConfigVariableProvider() {
        configurations = new HashMap<>();
    }

    public void add(final ConfigProvider provider) {
        configurations.put(provider.toString(), provider);
    }

    @Override
    public ConfigVariableProvider get(Request r) {
        return this;
    }

    @Override
    public String providerName() {
        return "cfg";
    }

    @Override
    public boolean contains(String variable) {
        String[] parts = variable.split("@");
        return configurations.containsKey(parts[0]) && configurations.get(parts[0]).contains(parts[1]);
    }

    @Override
    public Object get(String variable) {
        String[] parts = variable.split("@");
        return configurations.get(parts[0]).get(parts[1]);
    }
}
