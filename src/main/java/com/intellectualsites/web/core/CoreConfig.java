package com.intellectualsites.web.core;

import com.intellectualsites.configurable.ConfigurationImplementation;
import com.intellectualsites.configurable.annotations.ConfigSection;
import com.intellectualsites.configurable.annotations.Configuration;

@SuppressWarnings("all")
@Configuration (implementation = ConfigurationImplementation.YAML, name = "server")
public class CoreConfig {

    public static int port = 80;

    public static String webAddress = "http://localhost";

    public static String hostname = "localhost";

    @ConfigSection(name = "buffer")
    public static class Buffer {

        public static int in = 1024 * 1024;

        public static int out = 1024 * 1024;

    }

    public static boolean verbose = false;

    public static boolean ipv4 = false;

    public static int workers = 1;

    public static boolean disableViews = false;

    public static boolean debug = true;

    @ConfigSection(name = "cache")
    public static class Cache {

        public static boolean enabled = true;

    }

    @ConfigSection(name = "mysql")
    public static class MySQL {

        public static boolean enabled = false;

    }

    @ConfigSection(name = "application")
    public static class Application {

        public static String main = "";

    }

}
