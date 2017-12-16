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
package xyz.kvantum.server.api.core;

import com.google.gson.Gson;
import com.intellectualsites.commands.Command;
import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.callers.CommandCaller;
import com.intellectualsites.commands.parser.Parserable;
import xyz.kvantum.files.FileSystem;
import xyz.kvantum.files.FileWatcher;
import xyz.kvantum.server.api.cache.ICacheManager;
import xyz.kvantum.server.api.config.ConfigurationFile;
import xyz.kvantum.server.api.config.Message;
import xyz.kvantum.server.api.fileupload.KvantumFileUpload;
import xyz.kvantum.server.api.logging.LogModes;
import xyz.kvantum.server.api.logging.LogProvider;
import xyz.kvantum.server.api.logging.LogWrapper;
import xyz.kvantum.server.api.matching.Router;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.response.Response;
import xyz.kvantum.server.api.scripts.ScriptManager;
import xyz.kvantum.server.api.session.SessionManager;
import xyz.kvantum.server.api.util.ApplicationStructure;
import xyz.kvantum.server.api.util.Assert;
import xyz.kvantum.server.api.util.CollectionUtil;
import xyz.kvantum.server.api.util.Generator;
import xyz.kvantum.server.api.util.ITempFileManagerFactory;
import xyz.kvantum.server.api.views.RequestHandler;
import xyz.kvantum.server.api.views.View;

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
     * Get the file system used in the server implementation
     *
     * @return File system (defaults to .kvantum)
     */
    FileSystem getFileSystem();

    /**
     * Get the file watcher used in the server implementation
     *
     * @return Implementation specific file watcher
     */
    FileWatcher getFileWatcher();

    /**
     * Get the implementation specific script manager
     *
     * @return Script manager
     */
    ScriptManager getScriptManager();

    /**
     * Create a simple request handler
     *
     * @param filter    Filter to use for the handler
     * @param generator Response generator
     * @return The created request handler
     */
    RequestHandler createSimpleRequestHandler(String filter, BiConsumer<AbstractRequest, Response> generator);

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

    /**
     * Get the worker procedure instance
     *
     * @return Worker procedure instance
     */
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

    /**
     * Get the cache manager instance
     * @return Cache manager instance
     */
    ICacheManager getCacheManager();

    /**
     * Get the currently used log wrapper instance
     * @return Log wrapper
     */
    LogWrapper getLogWrapper();

    /**
     * Get the translation configuration file instance
     * @return Translation file
     */
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
