package com.intellectualsites.web.config;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created 2015-04-22 for IntellectualServer
 *
 * @author Citymonstret
 */
public class YamlConfiguration implements ConfigurationFile {

    private File file;
    private Map<String, Object> map;
    private Yaml yaml;

    public YamlConfiguration(File file) throws Exception {
        this.file = file;
        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                throw new RuntimeException("Couldn't create parents for " + file.getAbsolutePath());
            }
        }
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new RuntimeException("Couldn't create " + file.getAbsolutePath());
            }
        }
        this.map = new HashMap<>();
    }

    private Yaml getYaml() {
        if (yaml == null) {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setAllowReadOnlyProperties(true);
            this.yaml = new Yaml(options);
        }
        return yaml;
    }

    @Override
    public void reload() {
        this.map = new HashMap<>();
        loadFile();
    }

    @Override
    public void saveFile() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            getYaml().dump(map, writer);
            writer.close();
        } catch(final Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadFile() {
        try {
            BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file));
            Object o = getYaml().load(stream);
            if (o != null) {
                this.map.putAll((HashMap<String, Object>) o);
            }
            stream.close();
        } catch(final Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public <T> void set(String key, T value) {
        this.map.put(key, value);
    }

    @Override
    public <T> T get(String key) {
        if (map.containsKey(key)) {
            return (T) map.get(key);
        } else {
            return null;
        }
    }

    @Override
    public boolean contains(String key) {
        return map.containsKey(key);
    }

    @Override
    public <T> void setIfNotExists(String key, T value) {
        if (!contains(key)) {
            set(key, value);
        }
    }
}
