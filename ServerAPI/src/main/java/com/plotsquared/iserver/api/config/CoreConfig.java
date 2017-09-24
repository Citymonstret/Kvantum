/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
 * <p>
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.plotsquared.iserver.api.config;

import com.intellectualsites.configurable.ConfigurationImplementation;
import com.intellectualsites.configurable.annotations.ConfigSection;
import com.intellectualsites.configurable.annotations.Configuration;

/**
 * This is the configuration implementation that is
 * meant to control all easily accessible variables
 * for the web server. This is generated into
 * ".iserver\config\server.yml" and is loaded on runtime
 */
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

    // Transient makes sure that this is ignored by the config factory
    private transient static boolean preConfigured = false;

    public static boolean isPreConfigured()
    {
        return preConfigured;
    }

    public static void setPreConfigured(final boolean preConfigured)
    {
        CoreConfig.preConfigured = preConfigured;
    }

    @ConfigSection(name = "crush")
    public static class Crush
    {

        public static boolean enable = true;
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
        public static int cachedAccountsExpiry = 60 * 30;
        public static int cachedAccountsHeapMB = 32;
        public static int cachedAccountIdsExpiry = 60 * 60 * 24;
        public static int cachedAccountIdsHeapMB = 32;
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
