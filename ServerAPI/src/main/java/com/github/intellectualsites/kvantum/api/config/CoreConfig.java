/*
 * Kvantum is a web server, written entirely in the Java language.
 * Copyright (C) 2017 IntellectualSites
 *
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.github.intellectualsites.kvantum.api.config;

import com.intellectualsites.configurable.ConfigurationImplementation;
import com.intellectualsites.configurable.annotations.ConfigSection;
import com.intellectualsites.configurable.annotations.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * This is the configuration implementation that is
 * meant to control all easily accessible variables
 * for the web server. This is generated into
 * ".kvantum\config\server.yml" and is loaded on runtime
 */
@SuppressWarnings("all")
@Configuration(implementation = ConfigurationImplementation.YAML, name = "server")
public class CoreConfig
{

    public static int port = 80;
    public static String webAddress = "localhost";
    public static String logPrefix = "Web";
    public static String hostname = "localhost";
    public static boolean verbose = false;
    public static int workers = 1;
    public static boolean disableViews = false;
    public static boolean debug = false;
    public static boolean gzip = true;
    public static boolean enableSyntax = true;
    public static boolean contentMd5 = true;
    public static boolean enableSecurityManager = true;
    public static boolean enableInputThread = true;
    public static boolean exitOnStop = true;

    @ConfigSection(name = "sessions")
    public static class Sessions
    {

        public static boolean enableDb = true;
        public static int sessionTimeout = 86400;
        public static boolean autoLoad = true;
    }

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

    public static enum TemplatingEngine
    {
        CRUSH, VELOCITY, JTWIG, NONE
    }

    @ConfigSection(name = "templates")
    public static class Templates
    {

        public static TemplatingEngine engine = TemplatingEngine.CRUSH;

        public static List<String> applyTemplates = Arrays.asList( "ALL" );

        public static boolean status(final TemplatingEngine engine)
        {
            return Templates.engine.equals( engine );
        }
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

    @ConfigSection(name = "limits")
    public static class Limits
    {

        public static int limitRequestLines = 100;
        public static int limitRequestLineSize = 8190;
    }

    @ConfigSection(name = "cache")
    public static class Cache
    {

        public static boolean enabled = true;
        public static int cachedIncludesExpiry = 60 * 60; // 1h
        public static int cachedIncludesMaxItems = 1000;
        public static int cachedAccountsExpiry = 60 * 30;
        public static int cachedAccountsMaxItems = 1000;
        public static int cachedAccountIdsExpiry = 60 * 60 * 24;
        public static int cachedAccountIdsMaxItems = 1000;
        public static int cachedBodiesExpiry = 60 * 60;
        public static int cachedBodiesMaxItems = 1000;
        public static int cachedFilesExpiry = 60 * 60 * 24;
        public static int cachedFilesMaxItems = 1000;
        public static int cachedSessionsMaxItems = 1000;

    }

    @ConfigSection(name = "mongodb")
    public static class MongoDB
    {

        public static boolean enabled = false;
        public static String uri = "mongodb://localhost:27017";

        public static String dbSessions = "isites";
        public static String dbMorphia = "isites";
        public static String collectionSessions = "sessions";
    }

    @ConfigSection(name = "application")
    public static class Application
    {
        public static String main = "";
        public static String databaseImplementation = "sqlite";
    }

}
