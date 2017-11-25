/*
 *
 *    Copyright (C) 2017 IntellectualSites
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.kvantum.server.api.config;

import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.logging.LogModes;

/**
 * These are logging messages
 */
public enum Message
{
    CREATING( "Creating %s...", LogModes.MODE_INFO ),
    WAITING_FOR_EXECUTOR_SERVICE( "Waiting for the executor service to shutdown", LogModes.MODE_INFO ),
    TEMPLATING_ENGINE_STATUS( "Templating language '%s': %s", LogModes.MODE_INFO ),
    TEMPLATING_ENGINE_REACTING( "%s is reacting to: %s", LogModes.MODE_DEBUG ),
    TEMPLATING_ENGINE_DEBUG_NOT_ENABLED( "Config does not enable templates for: %s", LogModes.MODE_DEBUG ),
    DISABLING_PLUGINS( "Disabling all plugins", LogModes.MODE_INFO ),
    DISABLED_PLUGIN( "Disabled plugin '%s'", LogModes.MODE_INFO ),
    MD5_DIGEST_NOT_FOUND( "Could not load the MD5 MessageDigest Instance %s", LogModes.MODE_ERROR ),
    CLIENT_NOT_ACCEPTING_GZIP( "The client does not accept GZIP encoding %s", LogModes.MODE_DEBUG ),
    SSL_NOT_ENOUGH_WORKERS( "SSL is enabled. It is recommended to use at least 3 worker threads ( Not Required )", LogModes.MODE_WARNING ),
    COULD_NOT_CREATE_FOLDER( "Couldn't create the %s folder", LogModes.MODE_WARNING ),
    INVALID_VIEW( "Invalid view ('%s') - - Constructor has to be #(String.class, Map.class)", LogModes.MODE_WARNING ),
    STANDALONE_NO_EVENT_CALLER( "STANDALONE = TRUE; but there is no alternate event caller set", LogModes.MODE_ERROR ),
    COULD_NOT_CREATE_PLUGIN_FOLDER( "Couldn't create %s - No plugins were loaded", LogModes.MODE_ERROR ),
    CALLING_EVENT( "Calling <%s> event", LogModes.MODE_DEBUG ),
    VALIDATING_VIEWS( "Validating Views...", LogModes.MODE_INFO ),
    OUTPUT_BUFFER_INFO( "Output buffer size: %skb | Input buffer size: %skb", LogModes.MODE_DEBUG ),
    ACCEPTING_CONNECTIONS_ON( "Accepting connections on 'http://%s", LogModes.MODE_INFO ),
    ACCEPTING_SSL_CONNECTIONS_ON( "Accepting SSL connections on 'https://%s'", LogModes.MODE_INFO ),
    SHUTTING_DOWN( "Shutting down", LogModes.MODE_INFO ),
    STARTING_ON_PORT( "Starting the HTTP server on port %s", LogModes.MODE_INFO ),
    PORT_OCCUPIED( "Specified port was occupied, running on %s instead!", LogModes.MODE_INFO ),
    PORT_SWITCHED( "Specified port (%s) was not available, using %s instead", LogModes.MODE_WARNING ),
    STARTING_SSL_ON_PORT( "Starting the HTTPS server on port %s", LogModes.MODE_INFO ),
    SERVER_STARTED( "The server is started", LogModes.MODE_INFO ),
    TICK_ERROR( "Error in server ticking...", LogModes.MODE_ERROR ),
    REQUEST_LOG( "Request: [Address: %s | User Agent: %s | Request String: %s | Host: %s | Query: %s ]" ),
    REQUEST_SERVED( "Request was served by '%s', with the type '%s'. The total length of the content was '%skB'",
            LogModes.MODE_DEBUG ),
    REQUEST_SERVED_STATUS( "Request was served with HTTP status: %s", LogModes.MODE_DEBUG ),
    CONNECTION_ACCEPTED( "Connection accepted from '%s' - Handling the data!", LogModes.MODE_DEBUG ),
    DEBUG( ">> Debug - Ignore <<", LogModes.MODE_DEBUG ),
    CANNOT_LOAD_TRANSLATIONS( "Cannot load the translation file", LogModes.MODE_ERROR ),
    APPLICATION_CANNOT_FIND( "Couldn't find application '%s'" ),
    APPLICATION_CANNOT_INITIATE( "Couldn't initiate application '%s'" ),
    DATABASE_SESSION_UNKNOWN( "Unknown database implementation (%s). Using a DumbSessionDatabase instead.", LogModes.MODE_ERROR ),
    DATABASE_UNKNOWN( "Unknown database implementation (%s)", LogModes.MODE_ERROR ),
    STANDALONE_NOT_LOADING_PLUGINS( "Running as standalone, not loading plugins!", LogModes.MODE_INFO ),
    MYSQL_INIT( "Initalizing MySQL Connection", LogModes.MODE_INFO ),
    LOADING_VIEWS( "Loading views...", LogModes.MODE_INFO ),
    INTERNAL_REDIRECT( "Redirecting request to \"/%s\"", LogModes.MODE_DEBUG ),
    VIEWS_DISABLED( "Skipped view loading (Disabled)", LogModes.MODE_INFO ),
    STARTUP_STEP( "Calling Startup Step: '%s'", LogModes.MODE_DEBUG ),
    CACHING_DISABLED( "Caching is not enabled, this can reduce load times on bigger files!", LogModes.MODE_WARNING ),
    CACHING_ENABLED( "Caching is enabled, beware that this increases memory usage - So keep an eye on it", LogModes.MODE_WARNING ),
    APPLICATION_DOES_NOT_EXTEND( "Application '%s' does not extend ApplicationStructure.class", LogModes.MODE_WARNING ),
    INITIALIZING_LOCATION_SERVICES( "Initializing location services", LogModes.MODE_INFO ),
    CLEARED_VIEWS( "Cleared views ( %s )", LogModes.MODE_INFO ),
    SESSION_SET( "Set session (%s=%s)", LogModes.MODE_DEBUG ),
    SESSION_FOUND( "Found session (%s=%s) for request %s", LogModes.MODE_DEBUG ),
    CMD_HELP_HEADER( "# Available Commands ( Page %s/%s ) " ),
    CMD_HELP_ITEM( "> Command: /%s | Usage: %s | Description: %s " ),
    CMD_HELP_FOOTER( "# Type '/help %s' to see the next page" ),
    CACHE_REQUEST_ACCESS( "Accessing cached body: %s", LogModes.MODE_DEBUG ),
    CACHE_FILE_ACCESS( "Accessing cached file: %s", LogModes.MODE_DEBUG ),
    ACCOUNT_ADMIN_FAILED( "Failed to create admin account", LogModes.MODE_ERROR ),
    ACCOUNT_ADMIN_CREATED( "Created admin account with password: \"%s\"", LogModes.MODE_INFO ),
    WORKER_FAILED_HANDLING( "Error when handling request: %s", LogModes.MODE_ERROR ),
    WORKER_AVAILABLE( "Available workers: %s", LogModes.MODE_DEBUG ),
    REQUEST_HANDLER_DUMP( "> RequestHandler - Class '%s', Pattern: '%s'", LogModes.MODE_DEBUG );

    private final String message;
    private final int mode;

    Message(final String message)
    {
        this( message, LogModes.MODE_INFO );
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
                case LogModes.MODE_DEBUG:
                    nameSpace = "debug";
                    break;
                case LogModes.MODE_INFO:
                    nameSpace = "info";
                    break;
                case LogModes.MODE_ERROR:
                    nameSpace = "error";
                    break;
                case LogModes.MODE_WARNING:
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
