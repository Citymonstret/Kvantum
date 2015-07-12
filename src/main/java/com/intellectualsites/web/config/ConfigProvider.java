package com.intellectualsites.web.config;

/**
 * This is the configuration file that allows
 * us to access configuration file variables
 *
 * @author Citymonstret
 */
public abstract class ConfigProvider implements ConfigurationFile {

    private final String name;

    /**
     * ConfigurationProvider Constructor
     * @param name Configuration file name
     */
    public ConfigProvider(final String name) {
        this.name = name;
        ConfigVariableProvider.getInstance().add(this);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
