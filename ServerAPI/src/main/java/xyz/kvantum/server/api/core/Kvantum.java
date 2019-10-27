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
package xyz.kvantum.server.api.core;

import com.github.sauilitired.loggbok.ErrorDigest;
import com.google.gson.Gson;
import com.intellectualsites.commands.Command;
import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.callers.CommandCaller;
import com.intellectualsites.commands.parser.Parserable;
import xyz.kvantum.files.FileSystem;
import xyz.kvantum.files.FileWatcher;
import xyz.kvantum.server.api.cache.ICacheManager;
import xyz.kvantum.server.api.config.ITranslationManager;
import xyz.kvantum.server.api.config.Message;
import xyz.kvantum.server.api.event.EventBus;
import xyz.kvantum.server.api.fileupload.KvantumFileUpload;
import xyz.kvantum.server.api.logging.LogProvider;
import xyz.kvantum.server.api.matching.Router;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.response.Response;
import xyz.kvantum.server.api.session.SessionManager;
import xyz.kvantum.server.api.util.ApplicationStructure;
import xyz.kvantum.server.api.util.ApplicationStructureFactory;
import xyz.kvantum.server.api.util.Assert;
import xyz.kvantum.server.api.util.CollectionUtil;
import xyz.kvantum.server.api.util.Generator;
import xyz.kvantum.server.api.util.ITempFileManagerFactory;
import xyz.kvantum.server.api.util.Metrics;
import xyz.kvantum.server.api.views.RequestHandler;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;

/**
 * Core server interface, contains all methods that are required for the server to work
 */
@SuppressWarnings("unused") public interface Kvantum extends CommandCaller<Kvantum> {

    /**
     * The same as {@link #log(String, Object...)}
     *
     * @param s Message to be logged
     */
    @Override default void message(String s) {
        log(Assert.notNull(s));
    }

    /**
     * Returns itself
     *
     * @return this.
     */
    @Override default Kvantum getSuperCaller() {
        return this;
    }

    /**
     * Get the event bus for the implementation
     */
    EventBus getEventBus();

    /**
     * Ignore this.
     */
    @Override default boolean hasAttachment(String s) {
        return true;
    }

    @Override default void sendRequiredArgumentsList(CommandManager commandManager, Command command,
        Collection<Parserable> collection, String s) {
        final Generator<Parserable, String> parserableStringGenerator =
            input -> "[name: " + input.getName() + ", " + "desc: " + input.getDesc() + ", parser: "
                + input.getParser().getName() + ", example: " + input.getParser().getExample()
                + "]";
        message("Command '" + s + "' requires following arguments: " + CollectionUtil
            .smartJoin(collection, parserableStringGenerator, ", "));
    }

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

    ITranslationManager getTranslationManager();

    /**
     * Create a simple request handler
     *
     * @param filter    Filter to use for the handler
     * @param generator Response generator
     * @return The created request handler
     */
    RequestHandler createSimpleRequestHandler(String filter,
        BiConsumer<AbstractRequest, Response> generator);

    CommandManager getCommandManager();

    /**
     * Get the executor service registered in the {@link Kvantum}
     * implementation. This can safely be used by extensions to
     * Kvantum, and is the preferred method of implementing new exectuors
     *
     * @return Executor service
     */
    ExecutorService getExecutorService();

    /**
     * Start the server instance
     *
     * @return Status: true if started, false if not
     */
    boolean start();

    /**
     * Get the worker procedure instance
     *
     * @return Worker procedure instance
     */
    WorkerProcedure getProcedure();

    /**
     * Replaces string arguments using the pattern {num} from an array of objects, starting from index 0, as such: 0
     * &le; num &lt; args.length. If num &ge; args.length, then the pattern will be replaced by an empty string. An
     * argument can also be passed as "{}" or "{}", in which case the number will be implied.
     *
     * @param message message to be logged
     * @param args    Replacements
     */
    void log(Message message, Object... args);

    /**
     * Replaces string arguments using the pattern {num} from an array of objects, starting from index 0, as such: 0
     * &le; num &lt; args.length. If num &ge; args.length, then the pattern will be replaced by an empty string. An
     * argument can also be passed as "{}" or "{}", in which case the number will be implied.
     *
     * @param message message to be logged
     * @param mode    log mode ({@link com.github.sauilitired.loggbok.LogLevels})
     * @param args    Replacements
     */
    void log(String message, int mode, Object... args);

    /**
     * Get the metric manager
     *
     * @return Metric manager
     */
    Metrics getMetrics();

    /**
     * Get the cache manager instance
     *
     * @return Cache manager instance
     */
    ICacheManager getCacheManager();

    /**
     * Get the main folder (configured_folder/.kvantum/)
     *
     * @return main folder
     */
    File getCoreFolder();

    /**
     * Check to see if the server is in standalone mode
     *
     * @return boolean indicating whether or not the server is in standalone mode
     */
    boolean isStandalone();

    /**
     * Check to see if the server is in silent mode
     *
     * @return boolean indicating whether or not the server is in silent mode
     */
    boolean isSilent();

    /**
     * Check if the server is started
     *
     * @return boolean indicating whether or not the server has started
     */
    boolean isStarted();

    /**
     * Get the application structure that is currently backing Kvantum
     *
     * @return Application structure
     */
    ApplicationStructure getApplicationStructure();

    /**
     * Get the error digest used by the Kvantum instance
     *
     * @return ErrorDigest instance
     */
    ErrorDigest getErrorDigest();

    /**
     * Log a message
     *
     * @param message Message
     * @param args    Arguments, will replace "{}" in the order provided, uses #toString
     */
    void log(String message, Object... args);

    /**
     * Log a message
     *
     * @param provider Message provider
     * @param message  Message
     * @param args     Arguments, will replace "{}" in the order provided, uses #toString
     */
    void log(LogProvider provider, String message, Object... args);

    /**
     * Shut down the server
     */
    void stopServer();

    /**
     * Get the session manager instance
     *
     * @return Session manager
     */
    SessionManager getSessionManager();

    /**
     * Get the current router instance
     *
     * @return Current router
     */
    Router getRouter();

    /**
     * Is the server currently shutting down?
     *
     * @return true if the server is shutting down
     */
    boolean isStopping();

    /**
     * Is the server currently paused? This could be when the server is waiting for required input, etc.
     *
     * @return true if the server is paused
     */
    boolean isPaused();

    /**
     * Get a GSON implementation with parsers for implementations
     *
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

    /**
     * Attempt to retrieve an ApplicationStructureFactory
     * from a given key
     *
     * @param key The key
     * @return The factory, if it could be found, or null
     */
    ApplicationStructureFactory<?> getApplicationStructureFactory(String key);

}
