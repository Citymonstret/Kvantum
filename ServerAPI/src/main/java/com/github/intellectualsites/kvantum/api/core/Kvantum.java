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
package com.github.intellectualsites.kvantum.api.core;

import com.github.intellectualsites.kvantum.api.cache.ICacheManager;
import com.github.intellectualsites.kvantum.api.config.ConfigurationFile;
import com.github.intellectualsites.kvantum.api.config.Message;
import com.github.intellectualsites.kvantum.api.events.Event;
import com.github.intellectualsites.kvantum.api.events.EventCaller;
import com.github.intellectualsites.kvantum.api.fileupload.KvantumFileUpload;
import com.github.intellectualsites.kvantum.api.logging.LogModes;
import com.github.intellectualsites.kvantum.api.logging.LogProvider;
import com.github.intellectualsites.kvantum.api.logging.LogWrapper;
import com.github.intellectualsites.kvantum.api.matching.Router;
import com.github.intellectualsites.kvantum.api.request.Request;
import com.github.intellectualsites.kvantum.api.response.Response;
import com.github.intellectualsites.kvantum.api.session.SessionManager;
import com.github.intellectualsites.kvantum.api.socket.ISocketHandler;
import com.github.intellectualsites.kvantum.api.util.*;
import com.github.intellectualsites.kvantum.api.views.RequestHandler;
import com.github.intellectualsites.kvantum.api.views.View;
import com.github.intellectualsites.kvantum.files.FileSystem;
import com.google.gson.Gson;
import com.intellectualsites.commands.Command;
import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.callers.CommandCaller;
import com.intellectualsites.commands.parser.Parserable;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Core server interface, contains
 * all methods that are required
 * for the server to work
 */
@SuppressWarnings("unused")
public interface Kvantum extends CommandCaller<Kvantum>
{

    /**
     * The same as {@link #log(String, Object...)}
     *
     * @param s Message to be logged
     */
    @Override
    default void message(String s)
    {
        log( Assert.notNull( s ) );
    }

    /**
     * Returns itself
     * @return this.
     */
    @Override
    default Kvantum getSuperCaller()
    {
        return this;
    }

    /**
     * Ignore this.
     */
    @Override
    default boolean hasAttachment(String s)
    {
        return true;
    }

    @Override
    default void sendRequiredArgumentsList(CommandManager commandManager, Command command, Collection<Parserable> collection, String s)
    {
        final Generator<Parserable, String> parserableStringGenerator = input -> "[name: " + input.getName() + ", " +
                "desc: " +
                input.getDesc() + ", parser: " + input.getParser().getName() + ", example: " + input.getParser()
                .getExample() + "]";
        message( "Command '" + s + "' requires following arguments: " + CollectionUtil.smartJoin( collection,
                parserableStringGenerator, ", " ) );
    }

    /**
     * Add a view binding to the engine
     *
     * @param key Binding Key
     * @param c   The View Class
     * @see #validateViews()
     */
    void addViewBinding(String key, Class<? extends View> c);

    /**
     * Validate the views, and make sure they
     * contain the right constructor
     */
    void validateViews();

    /**
     * Handle an event
     *
     * @param event Event to handle
     */
    void handleEvent(Event event);

    /**
     * Get the file system used in the server implementation
     *
     * @return File system (defaults to .kvantum)
     */
    FileSystem getFileSystem();

    /**
     * Create a simple request handler
     *
     * @param filter    Filter to use for the handler
     * @param generator Response generator
     * @return The created request handler
     */
    RequestHandler createSimpleRequestHandler(String filter, BiConsumer<Request, Response> generator);

    /**
     * Get the metric manager
     *
     * @return Metric manager
     */
    Metrics getMetrics();

    /**
     * Set the engine event caller
     *
     * @param caller New Event Caller
     */
    void setEventCaller(EventCaller caller);

    /**
     * Load all plugins
     */
    void loadPlugins();

    CommandManager getCommandManager();

    /**
     * Start the server instance
     */
    @SuppressWarnings("ALL")
    void start();

    WorkerProcedure getProcedure();

    /**
     * Send a message (Replaces %s with arg#toString)
     *
     * @param message Message
     * @param args    Arguments
     */
    void log(Message message, Object... args);

    /**
     * Send a message (Replaces %s with arg#toString)
     *
     * @param message Message
     * @param mode    Log Mode {@link LogModes}
     * @param args    Arguments
     */
    void log(String message, int mode, Object... args);

    ICacheManager getCacheManager();

    LogWrapper getLogWrapper();

    ConfigurationFile getTranslations();

    /**
     * Get the main folder (configured_folder/.kvantum/)
     * @return main folder
     */
    File getCoreFolder();

    /**
     * Check to see if the server is in standalone mode
     * @return boolean indicating whether or not the server is in standalone mode
     */
    boolean isStandalone();

    /**
     * Get all view bindings
     * @return map containing all view bindings
     */
    Map<String, Class<? extends View>> getViewBindings();

    /**
     * Get the socket handler
     * @return socket handler
     */
    ISocketHandler getSocketHandler();

    /**
     * Check to see if the server is in silent mode
     * @return boolean indicating whether or not the server is in silent mode
     */
    boolean isSilent();

    /**
     * Check if the server is started
     * @return boolean indicating whether or not the server has started
     */
    boolean isStarted();

    /**
     * Get the application structure that is currently backing Kvantum
     * @return Application structure
     */
    ApplicationStructure getApplicationStructure();

    /**
     * Log a message
     * @param message Message
     * @param args Arguments, will replace "%s" in the order provided, uses
     *             #toString
     */
    void log(String message, Object... args);

    /**
     * Log a message
     * @param provider Message provider
     * @param message Message
     * @param args Arguments, will replace "%s" in the order provided, uses
     *             #toString
     */
    void log(LogProvider provider, String message, Object... args);

    /**
     * Shut down the server
     */
    void stopServer();

    /**
     * Get the session manager instance
     * @return Session manager
     */
    SessionManager getSessionManager();

    /**
     * Get the current router instance
     * @return Current router
     */
    Router getRouter();

    /**
     * Is the server currently shutting down?
     * @return true if the server is shutting down
     */
    boolean isStopping();

    /**
     * Is the server currently paused? This could be when the server is waiting for required input, etc.
     * @return true if the server is paused
     */
    boolean isPaused();

    /**
     * Get a GSON implementation with
     * parsers for implementations
     * @return GSON implementation
     */
    Gson getGson();

    /**
     * Get the temporary file manager factory implementation
     *
     * @return ITempFileManagerFactory Implementation
     */
    ITempFileManagerFactory getTempFileManagerFactory();

    /**
     * Get the global file upload instance
     *
     * @return global file upload instance
     */
    KvantumFileUpload getGlobalFileUpload();
}
