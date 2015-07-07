package com.intellectualsites.web.config;

import com.intellectualsites.web.core.Server;

import static com.intellectualsites.web.object.LogModes.*;

public enum Message {
    COULD_NOT_CREATE_FOLDER("Couldn't create the %s folder", MODE_WARNING),
    INVALID_VIEW("Invalid view ('%s') - - Constructor has to be #(String.class, Map.class)", MODE_WARNING),
    STANDALONE_NO_EVENT_CALLER("STANDALONE = TRUE; but there is no alternate event caller set", MODE_ERROR),
    COULD_NOT_CREATE_PLUGIN_FOLDER("Couldn't create %s - No plugins were loaded", MODE_ERROR),
    CALLING_EVENT("Calling <%s> event", MODE_INFO),
    VALIDATING_VIEWS("Validating Views...", MODE_INFO),
    OUTPUT_BUFFER_INFO("Output buffer size: %skb | Input buffer size: %skb", MODE_INFO),
    ACCEPTING_CONNECTIONS_ON("Accepting connections on 'http://%s", MODE_INFO),
    SHUTTING_DOWN("Shutting down", MODE_INFO),
    STARTING_ON_PORT("Starting the web server on port %s", MODE_INFO),
    SERVER_STARTED("The server is started", MODE_INFO),
    TICK_ERROR("Error in server ticking...", MODE_ERROR),
    CONNECTION_ACCEPTED("Connection accepted from '%s' - Handling the data!", MODE_DEBUG),
    DEBUG(">> Debug - Ignore <<", MODE_DEBUG);

    private final String message;
    private final int mode;

    Message(final String message) {
        this(message, MODE_INFO);
    }

    Message(final String message, int mode) {
        this.message = message;
        this.mode = mode;
    }

    public int getMode() {
        return this.mode;
    }

    @Override
    public String toString() {
        if (Server.getInstance() != null && Server.getInstance().translations != null) {
            String nameSpace;
            switch (this.getMode()) {
                case MODE_DEBUG:
                    nameSpace = "debug";
                    break;
                case MODE_INFO:
                    nameSpace = "info";
                    break;
                case MODE_ERROR:
                    nameSpace = "error";
                    break;
                case MODE_WARNING:
                    nameSpace = "warning";
                    break;
                default:
                    nameSpace = "info";
                    break;
            }
            if (Server.getInstance().translations.contains(nameSpace + "." + this.name().toLowerCase())) {
                return Server.getInstance().translations.get(nameSpace + "." + this.name().toLowerCase());
            }
        }
        return this.message;
    }
}
