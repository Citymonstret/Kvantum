package com.intellectualsites.web.config;

/**
 * Created 2015-04-22 for IntellectualServer
 *
 * @author Citymonstret
 */
public interface ConfigurationFile {

    void reload();
    void saveFile();
    void loadFile();
    <T> void set(String key, T value);
    <T> T get(String key);
    boolean contains(String key);
    <T> void setIfNotExists(String key, T value);

}
