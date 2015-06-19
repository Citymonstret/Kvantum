package com.intellectualsites.web.plugin;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

/**
 * The plugin description file
 *
 * @author Citymonstret
 */
public class PluginFile {

    public final String name;
    public final String mainClass;
    public final String author;
    public final String version;

    /**
     * Constructor
     *
     * @param stream Stream with desc.json incoming
     * @throws Exception If anything bad happens
     */
    public PluginFile(final InputStream stream, Yaml yaml) throws Exception {
        Map<String, Object> info = (Map<String, Object>) yaml.load(stream);
        name = info.get("name").toString();
        mainClass = info.get("main").toString();
        author = info.get("author").toString();
        version = info.get("version").toString();
        stream.close();
    }

}