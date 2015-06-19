package com.intellectualsites.web.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A custom class loader used with plugins
 *
 * @author Citymonstret
 */
public class PluginClassLoader extends URLClassLoader {

    private final PluginLoader loader;
    private final PluginFile desc;
    private final File file, data;
    private final Map<String, Class> classes;
    public Plugin plugin, init;

    /**
     * Constructor
     *
     * @param loader PluginLoader Instance
     * @param desc   PluginFile For the plugin
     * @param file   Plugin Jar
     * @throws MalformedURLException If the jar location is invalid
     */
    public PluginClassLoader(final PluginLoader loader, final PluginFile desc,
                             final File file) throws MalformedURLException {
        super(new URL[]{file.toURI().toURL()}, loader.getClass()
                .getClassLoader());
        this.loader = loader;
        this.desc = desc;
        this.file = file;
        data = new File(file.getParent(), desc.name);
        classes = new HashMap<>();
        Class jar;
        Class<? extends Plugin> plg;
        try {
            jar = Class.forName(desc.mainClass, true, this);
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException("Could not find main class for plugin " + desc.name + ", main class: " + desc.mainClass);
        }
        try {
            plg = jar.asSubclass(Plugin.class);
        } catch (final ClassCastException e) {
            throw new RuntimeException("Plugin main class for " + desc.name + " is not instanceof Plugin");
        }
        try {
            plugin = plg.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public File getData() {
        return data;
    }

    /**
     * Load a jar file into [this] instance
     *
     * @param file Jar file
     */
    public void loadJar(final File file) throws MalformedURLException {
        if (!file.getName().endsWith(".jar"))
            throw new IllegalArgumentException(
                    file.getName() + " is of wrong type");
        try {
            super.addURL(file.toURI().toURL());
        } catch (final MalformedURLException e) {
            throw e;
        }
    }

    @Override
    protected Class<?> findClass(final String name)
            throws ClassNotFoundException {
        return this.findClass(name, true);
    }

    protected Class<?> findClass(final String name, final boolean global)
            throws ClassNotFoundException {
        Class<?> clazz = null;
        if (classes.containsKey(name))
            clazz = classes.get(name);
        else {
            if (global)
                clazz = loader.getClassByName(name);
            if (clazz == null) {
                clazz = super.findClass(name);
                if (clazz != null)
                    loader.setClass(name, clazz);
            }
            classes.put(name, clazz);
        }
        return clazz;
    }

    protected Set<String> getClasses() {
        return classes.keySet();
    }

    synchronized void create(final Plugin plugin) {
        if (init != null)
            throw new RuntimeException(plugin.getName() + " is already created");
        init = plugin;
        plugin.create(desc, data, this);
    }

    public PluginFile getDesc() {
        return desc;
    }
}
