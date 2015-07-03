package com.intellectualsites.web.object;

import com.intellectualsites.web.events.Event;
import com.intellectualsites.web.util.SessionManager;
import com.sun.istack.internal.NotNull;

public interface IntellectualServer {

    /**
     * Add a view binding to the engine
     *
     * @param key Binding Key
     * @param c   The View Class
     * @see #validateViews()
     */
    void addViewBinding(@NotNull String key, @NotNull Class<? extends View> c);

    void validateViews();

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
     * Start the web server
     *
     * @throws RuntimeException If anything goes wrong
     */
    void start() throws Throwable;

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
    void stop();

    /**
     * Get the session manager
     *
     * @return Session Manager;
     */
    SessionManager getSessionManager();
}
