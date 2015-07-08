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
public class YamlConfiguration extends ConfigProvider implements ConfigurationFile {

    private File file;
    private Map<String, Object> map;
    private Yaml yaml;

    public YamlConfiguration(String name, File file) throws Exception {
        super(name);
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
            options.setAllowUnicode(true);
            options.setPrettyFlow(true);
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

    @SuppressWarnings("ALL")
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
        if (key.contains(".")) {
            convertToMap(key, value);
        } else {
            this.map.put(key, value);
        }
    }

    private void convertToMap(String in, Object value) {
        if (in.contains(".")) {
            Map<String, Object> lastMap = this.map;
            while (in.contains(".")) {
                String[] parts = in.split("\\.");
                if (lastMap.containsKey(parts[0])) {
                    Object o = lastMap.get(parts[0]);
                    if (o instanceof Map) {
                        lastMap = (Map) o;
                    }
                } else {
                    lastMap.put(parts[0], new HashMap<>());
                    lastMap = (Map) lastMap.get(parts[0]);
                }
                StringBuilder b = new StringBuilder();
                for (int i = 1; i < parts.length; i++) {
                    b.append(".").append(parts[i]);
                }
                in = b.toString().replaceFirst("\\.", "");
            }
            if (!lastMap.containsKey(in)) {
                lastMap.put(in, value);
            }
        }
    }

    public <T> T get(String key, T def) {
        if (!contains(key)) {
            setIfNotExists(key, def);
            return def;
        }
        return get(key);
    }

    @SuppressWarnings("ALL")
    @Override
    public <T> T get(String key) {
        if (map.containsKey(key)) {
            return (T) map.get(key);
        } else {
            if (key.contains(".")) {
                String[] parts = key.split("\\.");
                Map<String, Object> lastMap = this.map;
                for (String p : parts) {
                    if (lastMap.containsKey(p)) {
                        Object o = lastMap.get(p);
                        if (o instanceof Map) {
                            lastMap = (Map) o;
                        } else {
                            return (T) o;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean contains(String key) {
        if (map.containsKey(key)) {
            return true;
        } else {
            if (key.contains(".")) {
                String[] parts = key.split("\\.");
                Map<String, Object> lastMap = this.map;
                for (String p : parts) {
                    if (lastMap.containsKey(p)) {
                        Object o = lastMap.get(p);
                        if (o instanceof Map) {
                            lastMap = (Map) o;
                        } else {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public <T> void setIfNotExists(String key, T value) {
        if (!contains(key)) {
            set(key, value);
        }
    }
}
