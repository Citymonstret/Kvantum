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
package com.plotsquared.iserver.core;

import com.intellectualsites.commands.Command;
import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.callers.CommandCaller;
import com.intellectualsites.commands.parser.Parserable;
import com.plotsquared.iserver.account.AccountManager;
import com.plotsquared.iserver.config.ConfigurationFile;
import com.plotsquared.iserver.config.Message;
import com.plotsquared.iserver.events.Event;
import com.plotsquared.iserver.events.EventCaller;
import com.plotsquared.iserver.files.FileSystem;
import com.plotsquared.iserver.internal.SocketHandler;
import com.plotsquared.iserver.logging.LogProvider;
import com.plotsquared.iserver.logging.LogWrapper;
import com.plotsquared.iserver.matching.Router;
import com.plotsquared.iserver.object.Request;
import com.plotsquared.iserver.object.Response;
import com.plotsquared.iserver.util.CacheManager;
import com.plotsquared.iserver.util.Metrics;
import com.plotsquared.iserver.util.SessionManager;
import com.plotsquared.iserver.views.RequestHandler;
import com.plotsquared.iserver.views.View;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Core server interface, contains
 * all methods that are required
 * for the server to work
 */
@SuppressWarnings("unused")
public interface IntellectualServer extends CommandCaller<IntellectualServer>
{

    @Override
    default void message(String s)
    {
        log( s );
    }

    @Override
    default IntellectualServer getSuperCaller()
    {
        return this;
    }

    @Override
    default boolean hasAttachment(String s)
    {
        return true;
    }

    @Override
    default void sendRequiredArgumentsList(CommandManager commandManager, Command command, Collection<Parserable> collection, String s)
    {
        message( "Argument List Not Implemented!" );
    }

    boolean isMysqlEnabled();

    /**
     * Add a view binding to the engine
     *
     * @param key Binding Key
     * @param c   The View Class
     * @see #validateViews()
     */
    void addViewBinding(String key, Class<? extends View> c);

    Optional<AccountManager> getAccountManager();

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

    FileSystem getFileSystem();

    abstract RequestHandler createSimpleRequestHandler(String filter, BiConsumer<Request, Response> generator);

    /**
     * Get the metric manager
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

    @SuppressWarnings("ALL")
    void start();

    WorkerProcedure getProcedure();

    /**
     * Send a message (Replaces %s with arg#toString)
     * @param message Message
     * @param args Arguments
     */
    void log(Message message, Object... args);

    /**
     * Send a message (Replaces %s with arg#toString)
     * @param message Message
     * @param mode Log Mode {@link com.plotsquared.iserver.logging.LogModes}
     * @param args Arguments
     */
    void log(String message, int mode, Object... args);

    CacheManager getCacheManager();

    LogWrapper getLogWrapper();

    ConfigurationFile getTranslations();

    File getCoreFolder();

    boolean isStandalone();

    Map<String, Class<? extends View>> getViewBindings();

    WorkerProcedure getWorkerProcedure();

    SocketHandler getSocketHandler();

    boolean isSilent();

    boolean isPause();

    boolean isStarted();

    AccountManager getGlobalAccountManager();

    abstract void log(String message, Object... args);

    abstract void log(LogProvider provider, String message, Object... args);

    abstract void stopServer();

    abstract SessionManager getSessionManager();

    abstract Router getRouter();

    abstract boolean isStopping();

    abstract boolean isPaused();
}
