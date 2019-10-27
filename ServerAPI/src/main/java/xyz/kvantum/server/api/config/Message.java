/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2019 Alexander Söderberg
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

import com.github.sauilitired.loggbok.LogLevels;
import xyz.kvantum.server.api.core.ServerImplementation;

import java.util.Locale;

/**
 * These are logging messages
 */
public enum Message {
    CREATING("Creating {}...", LogLevels.LEVEL_INFO), WAITING_FOR_EXECUTOR_SERVICE(
        "Waiting for the executor service to shutdown",
        LogLevels.LEVEL_INFO), TEMPLATING_ENGINE_STATUS("Templating language '{0}': {1}",
        LogLevels.LEVEL_INFO), TEMPLATING_ENGINE_REACTING("{0} is reacting to: {1}",
        LogLevels.LEVEL_DEBUG), TEMPLATING_ENGINE_DEBUG_NOT_ENABLED(
        "Config does not enable templates for: {0}", LogLevels.LEVEL_DEBUG), DISABLING_PLUGINS(
        "Disabling all plugins", LogLevels.LEVEL_INFO), DISABLED_PLUGIN("Disabled plugin '{}'",
        LogLevels.LEVEL_INFO), MD5_DIGEST_NOT_FOUND(
        "Could not load the MD5 MessageDigest Instance {}",
        LogLevels.LEVEL_ERROR), CLIENT_NOT_ACCEPTING_GZIP(
        "The client does not accept GZIP encoding {}",
        LogLevels.LEVEL_DEBUG), SSL_NOT_ENOUGH_WORKERS(
        "SSL is enabled. It is recommended to use at least 3 worker threads ( Not Required )",
        LogLevels.LEVEL_WARNING), COULD_NOT_CREATE_FOLDER("Couldn't create the {} folder",
        LogLevels.LEVEL_WARNING), INVALID_VIEW(
        "Invalid view ('{}') - - Constructor has to be #(String.class, Map.class)",
        LogLevels.LEVEL_WARNING), STANDALONE_NO_EVENT_CALLER(
        "STANDALONE = TRUE; but there is no alternate event caller set",
        LogLevels.LEVEL_ERROR), COULD_NOT_CREATE_PLUGIN_FOLDER(
        "Couldn't create {} - No plugins were loaded", LogLevels.LEVEL_ERROR), CALLING_EVENT(
        "Calling <{}> event", LogLevels.LEVEL_DEBUG), VALIDATING_VIEWS("Validating Views...",
        LogLevels.LEVEL_INFO), OUTPUT_BUFFER_INFO(
        "Output buffer size: {0}kb | Input buffer size: {1}kb",
        LogLevels.LEVEL_DEBUG), ACCEPTING_CONNECTIONS_ON("Accepting connections on 'http://{}/'",
        LogLevels.LEVEL_INFO), ACCEPTING_SSL_CONNECTIONS_ON(
        "Accepting SSL connections on 'https://{}'", LogLevels.LEVEL_INFO), SHUTTING_DOWN(
        "Shutting down", LogLevels.LEVEL_INFO), STARTING_ON_PORT(
        "Starting the HTTP server on port {}", LogLevels.LEVEL_INFO), PORT_OCCUPIED(
        "Specified port was occupied, running on {} instead!", LogLevels.LEVEL_INFO), PORT_SWITCHED(
        "Specified port ({0}) was not available, using {1} instead",
        LogLevels.LEVEL_WARNING), STARTING_SSL_ON_PORT("Starting the HTTPS server on port {}",
        LogLevels.LEVEL_INFO), SERVER_STARTED("The server is started",
        LogLevels.LEVEL_INFO), TICK_ERROR("Error in server ticking...",
        LogLevels.LEVEL_ERROR), REQUEST_LOG(
        "Request: [Address: {} | User Agent: {} | Request String: {} | Host: {} | Query: {} ]"), REQUEST_SERVED(
        "Request was served by '{0}', with the type '{1}'. The total length of the content was '{3}kB'",
        LogLevels.LEVEL_DEBUG), REQUEST_SERVED_STATUS("Request was served with HTTP status: {}",
        LogLevels.LEVEL_DEBUG), CONNECTION_ACCEPTED(
        "Connection accepted from '{}' - Handling the data!", LogLevels.LEVEL_DEBUG), DEBUG(
        ">> Debug - Ignore <<", LogLevels.LEVEL_DEBUG), CANNOT_LOAD_TRANSLATIONS(
        "Cannot load the translation file", LogLevels.LEVEL_ERROR), APPLICATION_CANNOT_FIND(
        "Couldn't find application '{}'"), APPLICATION_CANNOT_INITIATE(
        "Couldn't initiate application '{}'"), DATABASE_SESSION_UNKNOWN(
        "Unknown database implementation ({}). Using a DumbSessionDatabase instead.",
        LogLevels.LEVEL_ERROR), DATABASE_UNKNOWN("Unknown database implementation ({})",
        LogLevels.LEVEL_ERROR), STANDALONE_NOT_LOADING_PLUGINS(
        "Running as standalone, not loading plugins!", LogLevels.LEVEL_INFO), LOADING_VIEWS(
        "Loading views...", LogLevels.LEVEL_INFO), INTERNAL_REDIRECT(
        "Redirecting request to \"/{}\"", LogLevels.LEVEL_DEBUG), VIEWS_DISABLED(
        "Skipped view loading (Disabled)", LogLevels.LEVEL_INFO), STARTUP_STEP(
        "Calling Startup Step: '{}'", LogLevels.LEVEL_DEBUG), CACHING_DISABLED(
        "Caching is not enabled, this can reduce load times on bigger files!",
        LogLevels.LEVEL_WARNING), CACHING_ENABLED(
        "Caching is enabled, beware that this increases memory usage - So keep an eye on it",
        LogLevels.LEVEL_WARNING), APPLICATION_DOES_NOT_EXTEND(
        "Application '{}' does not extend ApplicationStructure.class",
        LogLevels.LEVEL_WARNING), INITIALIZING_LOCATION_SERVICES("Initializing location services",
        LogLevels.LEVEL_INFO), CLEARED_VIEWS("Cleared views ( {} )",
        LogLevels.LEVEL_INFO), SESSION_SET("Set session ({0}={1})",
        LogLevels.LEVEL_DEBUG), SESSION_FOUND("Found session ({0}={1}) for request {2}",
        LogLevels.LEVEL_DEBUG), SESSION_DELETED_OUTDATED("Deleted outdated session: {}",
        LogLevels.LEVEL_DEBUG), SESSION_DELETED_OTHER("Deleted session: {} (Cause: {})",
        LogLevels.LEVEL_DEBUG), SESSION_KEY_INVALID("Wrong session key",
        LogLevels.LEVEL_INFO), CMD_HELP_HEADER(
        "# Available Commands ( Page {0}/{1} ) "), CMD_HELP_ITEM(
        "> Command: /{0} | Usage: {1} | Description: {2} "), CMD_HELP_FOOTER(
        "# Type '/help {0}' to see the next page"), ACCOUNT_ADMIN_FAILED(
        "Failed to create admin account", LogLevels.LEVEL_ERROR), ACCOUNT_ADMIN_CREATED(
        "Created admin account with password: \"{}\"",
        LogLevels.LEVEL_INFO), WORKER_FAILED_HANDLING("Error when handling request: {}",
        LogLevels.LEVEL_ERROR), SERVER_START_FAILED(
        "Failed to start server..."), SERVER_START_PORT_CHANGED_PRIVILEGED(
        "Failed to bind to privileged port, trying 1024 instead",
        LogLevels.LEVEL_ERROR), SERVER_START_PORT_CHANGED_OCCUPIED(
        "Port {0} is occupied. Trying {1}...", LogLevels.LEVEL_ERROR), REQUEST_HANDLER_DUMP(
        "> RequestHandler - Class '{0}', Pattern: '{1}'", LogLevels.LEVEL_DEBUG);

    private final String message;
    private final int mode;

    Message(final String message) {
        this(message, LogLevels.LEVEL_INFO);
    }

    Message(final String message, int mode) {
        this.message = message;
        this.mode = mode;
    }

    public int getMode() {
        return this.mode;
    }

    @Override public String toString() {
        if (ServerImplementation.getImplementation() != null && (
            ServerImplementation.getImplementation().getTranslationManager() != null)) {
            String nameSpace;
            switch (this.getMode()) {
                case LogLevels.LEVEL_DEBUG:
                    nameSpace = "debug";
                    break;
                case LogLevels.LEVEL_ERROR:
                    nameSpace = "error";
                    break;
                case LogLevels.LEVEL_WARNING:
                    nameSpace = "warning";
                    break;
                default:
                    nameSpace = "info";
                    break;
            }
            if (ServerImplementation.getImplementation().getTranslationManager()
                .containsTranslation(nameSpace + "." + this.name().toLowerCase(Locale.ENGLISH))) {
                return (ServerImplementation.getImplementation().getTranslationManager()
                    .getTranslation(nameSpace + "." + this.name().toLowerCase(Locale.ENGLISH)));
            }
        }
        return this.message;
    }

    public void log(Object... args) {
        ServerImplementation.getImplementation().log(this, args);
    }
}
