package com.plotsquared.iserver.core;

import com.intellectualsites.configurable.ConfigurationImplementation;
import com.intellectualsites.configurable.annotations.ConfigSection;
import com.intellectualsites.configurable.annotations.Configuration;

@SuppressWarnings("all")
@Configuration(implementation = ConfigurationImplementation.YAML, name = "server")
public class CoreConfig
{

    public static int port = 80;
    public static String webAddress = "http://localhost";
    public static String logPrefix = "Web";
    public static String hostname = "localhost";
    public static boolean verbose = false;
    public static int workers = 1;
    public static boolean disableViews = false;
    public static boolean debug = true;
    public static boolean gzip = true;
    public static boolean enableSyntax = true;
    public static boolean contentMd5 = true;
    public static boolean enableSecurityManager = true;
    private static boolean preConfigured = false;

    public static boolean isPreConfigured()
    {
        return preConfigured;
    }

    public static void setPreConfigured(boolean preConfigured)
    {
        CoreConfig.preConfigured = preConfigured;
    }

    @ConfigSection(name = "ssl")
    public static class SSL
    {

        public static boolean enable = false;
        public static int port = 443;
        public static String keyStore = "keyStore";
        public static String keyStorePassword = "password";

    }

    @ConfigSection(name = "buffer")
    public static class Buffer
    {

        public static int in = 1024 * 1024;
        public static int out = 1024 * 1024;

    }

    @ConfigSection(name = "cache")
    public static class Cache
    {

        public static boolean enabled = true;
        public static int cachedIncludesExpiry = 60 * 60; // 1h
        public static int cachedIncludesHeapMB = 32;
        public static int cachedBodiesExpiry = 60 * 60;
        public static int cachedBodiesHeapMB = 32;
        public static int cachedFilesExpiry = 60 * 60 * 24;
        public static int cachedFilesHeapMB = 32;

    }

    @ConfigSection(name = "mysql")
    public static class MySQL
    {

        public static boolean enabled = false;

    }

    @ConfigSection(name = "application")
    public static class Application
    {

        public static String main = "";

    }

}
