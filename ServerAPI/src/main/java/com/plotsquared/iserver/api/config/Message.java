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

import com.plotsquared.iserver.api.core.ServerImplementation;

import static com.plotsquared.iserver.api.logging.LogModes.*;

/**
 * These are logging messages
 */
public enum Message
{
    WAITING_FOR_EXECUTOR_SERVICE( "Waiting for the executor service to shutdown", MODE_INFO ),
    TEMPLATING_ENGINE_STATUS( "Templating language '%s': %s", MODE_INFO ),
    DISABLING_PLUGINS( "Disabling all plugins", MODE_INFO ),
    DISABLED_PLUGIN( "Disabled plugin '%s'", MODE_INFO ),
    MD5_DIGEST_NOT_FOUND( "Could not load the MD5 MessageDigest Instance %s", MODE_ERROR ),
    CLIENT_NOT_ACCEPTING_GZIP( "The client does not accept GZIP encoding %s", MODE_DEBUG ),
    SSL_NOT_ENOUGH_WORKERS( "SSL is enabled. It is recommended to use at least 3 worker threads ( Not Required )", MODE_WARNING ),
    COULD_NOT_CREATE_FOLDER( "Couldn't create the %s folder", MODE_WARNING ),
    INVALID_VIEW( "Invalid view ('%s') - - Constructor has to be #(String.class, Map.class)", MODE_WARNING ),
    STANDALONE_NO_EVENT_CALLER( "STANDALONE = TRUE; but there is no alternate event caller set", MODE_ERROR ),
    COULD_NOT_CREATE_PLUGIN_FOLDER( "Couldn't create %s - No plugins were loaded", MODE_ERROR ),
    CALLING_EVENT( "Calling <%s> event", MODE_INFO ),
    VALIDATING_VIEWS( "Validating Views...", MODE_INFO ),
    OUTPUT_BUFFER_INFO( "Output buffer size: %skb | Input buffer size: %skb", MODE_INFO ),
    ACCEPTING_CONNECTIONS_ON( "Accepting connections on 'http://%s", MODE_INFO ),
    ACCEPTING_SSL_CONNECTIONS_ON( "Accepting SSL connections on 'https://%s'", MODE_INFO ),
    SHUTTING_DOWN( "Shutting down", MODE_INFO ),
    STARTING_ON_PORT( "Starting the HTTP server on port %s", MODE_INFO ),
    STARTING_SSL_ON_PORT( "Starting the HTTPS server on port %s", MODE_INFO ),
    SERVER_STARTED( "The server is started", MODE_INFO ),
    TICK_ERROR( "Error in server ticking...", MODE_ERROR ),
    REQUEST_LOG( "Request: [Address: %s | User Agent: %s | Request String: %s | Host: %s | Query: %s, Post: %s]" ),
    CONNECTION_ACCEPTED( "Connection accepted from '%s' - Handling the data!", MODE_DEBUG ),
    DEBUG( ">> Debug - Ignore <<", MODE_DEBUG ),
    CANNOT_LOAD_TRANSLATIONS( "Cannot load the translation file", MODE_ERROR ),
    APPLICATION_CANNOT_FIND( "Couldn't find application '%s'" ),
    APPLICATION_CANNOT_INITIATE( "Couldn't initiate application '%s'" ),
    STANDALONE_NOT_LOADING_PLUGINS( "Running as standalone, not loading plugins!", MODE_INFO ),
    MYSQL_INIT( "Initalizing MySQL Connection", MODE_INFO ),
    LOADING_VIEWS( "Loading views...", MODE_INFO ),
    INTERNAL_REDIRECT( "Redirecting request to \"/%s\"", MODE_DEBUG ),
    VIEWS_DISABLED( "Skipped view loading (Disabled)", MODE_INFO ),
    STARTUP_STEP( "Calling Startup Step: '%s'", MODE_DEBUG ),
    CACHING_DISABLED( "Caching is not enabled, this can reduce load times on bigger files!", MODE_WARNING ),
    CACHING_ENABLED( "Caching is enabled, beware that this increases memory usage - So keep an eye on it", MODE_WARNING ),
    APPLICATION_DOES_NOT_EXTEND( "Application '%s' does not extend ApplicationStructure.class", MODE_WARNING ),
    INITIALIZING_LOCATION_SERVICES( "Initializing location services", MODE_INFO ),
    CLEARED_VIEWS( "Cleared views ( %s )", MODE_INFO ),
    REQUEST_HANDLER_DUMP( "> RequestHandler - Class '%s', Pattern: '%s'" );

    private final String message;
    private final int mode;

    Message(final String message)
    {
        this( message, MODE_INFO );
    }

    Message(final String message, int mode)
    {
        this.message = message;
        this.mode = mode;
    }

    public int getMode()
    {
        return this.mode;
    }

    @Override
    public String toString()
    {
        if ( ServerImplementation.getImplementation() != null && ( ServerImplementation
                .getImplementation().getTranslations() != null ) )
        {
            String nameSpace;
            switch ( this.getMode() )
            {
                case MODE_DEBUG:
                    nameSpace = "debug";
                    break;
                case MODE_INFO:
                    nameSpace = "info";
                    break;
                case MODE_ERROR:
                    nameSpace = "error";
                    break;
                case MODE_WARNING:
                    nameSpace = "warning";
                    break;
                default:
                    nameSpace = "info";
                    break;
            }
            if ( ServerImplementation.getImplementation().getTranslations().contains( nameSpace + "." + this.name().toLowerCase()
            ) )
            {
                return ( ServerImplementation.getImplementation().getTranslations().get( nameSpace + "." + this.name
                        ().toLowerCase() ) );
            }
        }
        return this.message;
    }

    public void log(Object... args)
    {
        ServerImplementation.getImplementation().log( this, args );
    }
}
