//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//     IntellectualServer is a web server, written entirely in the Java language.                            /
//     Copyright (C) 2015 IntellectualSites                                                                  /
//                                                                                                           /
//     This program is free software; you can redistribute it and/or modify                                  /
//     it under the terms of the GNU General Public License as published by                                  /
//     the Free Software Foundation; either version 2 of the License, or                                     /
//     (at your option) any later version.                                                                   /
//                                                                                                           /
//     This program is distributed in the hope that it will be useful,                                       /
//     but WITHOUT ANY WARRANTY; without even the implied warranty of                                        /
//     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                         /
//     GNU General Public License for more details.                                                          /
//                                                                                                           /
//     You should have received a copy of the GNU General Public License along                               /
//     with this program; if not, write to the Free Software Foundation, Inc.,                               /
//     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.                                           /
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.plotsquared.iserver.core;

import com.plotsquared.iserver.config.ConfigurationFile;
import com.plotsquared.iserver.config.Message;
import com.plotsquared.iserver.events.Event;
import com.plotsquared.iserver.events.EventCaller;
import com.plotsquared.iserver.extra.accounts.AccountManager;
import com.plotsquared.iserver.files.FileSystem;
import com.plotsquared.iserver.logging.LogProvider;
import com.plotsquared.iserver.object.LogWrapper;
import com.plotsquared.iserver.util.CacheManager;
import com.plotsquared.iserver.util.RequestManager;
import com.plotsquared.iserver.util.SessionManager;
import com.plotsquared.iserver.views.View;

import java.io.File;
import java.util.Optional;

/**
 * Core server interface, contains
 * all methods that are required
 * for the server to work
 */
@SuppressWarnings("unused")
public interface IntellectualServer
{

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

    @SuppressWarnings("ALL")
    void start();

    void tick();

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

    /**
     * Send a message (Replaces %s with arg#toString)
     * @param message Message
     * @param args Arguments
     */
    void log(String message, Object... args);

    /**
     * Send a message (Replaces %s with arg#toString)
     * @param provider Log Provider
     * @param message Message
     * @param args Arguments
     */
    void log(LogProvider provider, String message, Object... args);

    /**
     * Stop the web server
     */
    void stopServer();

    /**
     * Get the session manager
     *
     * @return Session Manager;
     */
    SessionManager getSessionManager();

    /**
     * Get the view manager
     *
     * @return view manager
     */
    RequestManager getRequestManager();
}
