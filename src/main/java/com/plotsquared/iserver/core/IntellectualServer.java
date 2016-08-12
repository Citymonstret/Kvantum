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
import com.plotsquared.iserver.logging.LogProvider;
import com.plotsquared.iserver.object.LogWrapper;
import com.plotsquared.iserver.object.syntax.ProviderFactory;
import com.plotsquared.iserver.util.CacheManager;
import com.plotsquared.iserver.util.RequestManager;
import com.plotsquared.iserver.util.SessionManager;
import com.plotsquared.iserver.views.View;

import java.io.File;

/**
 * Core server interface, contains
 * all methods that are required
 * for the server to work
 */
@SuppressWarnings("unused")
public interface IntellectualServer
{

    /**
     * Check if mysql is enabled
     *
     * @return true|false
     */
    boolean isMysqlEnabled();

    /**
     * Add a view binding to the engine
     *
     * @param key Binding Key
     * @param c   The View Class
     * @see #validateViews()
     */
    void addViewBinding(String key, Class<? extends View> c);

    AccountManager getAccountManager();

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
     * Set the engine event caller
     *
     * @param caller New Event Caller
     */
    void setEventCaller(EventCaller caller);

    /**
     * Add a provider factory to the core provider
     *
     * @param factory Factory to add
     */
    void addProviderFactory(final ProviderFactory factory);


    void loadPlugins();

    @SuppressWarnings("ALL")
    void start();

    void tick();

    void log(Message message, Object... args);

    void log(String message, int mode, Object... args);

    CacheManager getCacheManager();

    LogWrapper getLogWrapper();

    ConfigurationFile getTranslations();

    File getCoreFolder();

    /**
     * Log a message
     *
     * @param message String message to log
     * @param args    Arguments to be sent (replaces %s with arg#toString)
     */
    void log(String message, Object... args);

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
