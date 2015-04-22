package com.intellectualsites.web.config;

/**
 * Created 2015-04-22 for IntellectualServer
 *
 * @author Citymonstret
 */
public abstract class ConfigProvider implements ConfigurationFile {

    private String name;

    public ConfigProvider(final String name) {
        this.name = name;
        ConfigVariableProvider.getInstance().add(this);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
