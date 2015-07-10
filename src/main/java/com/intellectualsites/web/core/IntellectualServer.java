package com.intellectualsites.web.core;

import com.intellectualsites.web.events.Event;
import com.intellectualsites.web.events.EventCaller;
import com.intellectualsites.web.logging.LogProvider;
import com.intellectualsites.web.object.syntax.ProviderFactory;
import com.intellectualsites.web.util.SessionManager;
import com.intellectualsites.web.util.ViewManager;
import com.intellectualsites.web.views.View;
import com.sun.istack.internal.NotNull;

/**
 * Core server interface, contains
 * all methods that are required
 * for the server to work
 */
public interface IntellectualServer {

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
    void addViewBinding(@NotNull String key, @NotNull Class<? extends View> c);

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
    void handleEvent(@NotNull Event event);

    /**
     * Set the engine event caller
     *
     * @param caller New Event Caller
     */
    void setEventCaller(@NotNull EventCaller caller);

    /**
     * Add a provider factory to the core provider
     *
     * @param factory Factory to add
     */
    void addProviderFactory(@NotNull final ProviderFactory factory);


    /**
     * Log a message
     *
     * @param message String message to log
     * @param args    Arguments to be sent (replaces %s with arg#toString)
     */
    void log(@NotNull String message, Object... args);

    void log(@NotNull LogProvider provider, @NotNull String message, Object... args);

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
    ViewManager getViewManager();
}
