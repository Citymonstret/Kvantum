package com.intellectualsites.web.views.requesthandler;

import com.intellectualsites.web.core.Server;
import com.intellectualsites.web.logging.LogModes;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collection;

public final class MiddlewareQueuePopulator {

    private final Collection<Class<? extends Middleware>> middlewares = new ArrayList<>();

    public void add(@NonNull final Class<? extends Middleware> middleware) {
        try  {
            middleware.getConstructor();
        } catch (final Exception e) {
            Server.getInstance().log("Middleware '" + middleware + "' doesn't have a default constructor, skipping it!",
                    LogModes.MODE_WARNING);
            return;
        }
        this.middlewares.add(middleware);
    }

    public MiddlewareQueue generateQueue() {
        final MiddlewareQueue queue = new MiddlewareQueue();
        middlewares.forEach(clazz -> {
            try {
                queue.add(clazz.newInstance());
            } catch (final Exception e) {
                e.printStackTrace();
            }
        });
        return queue;
    }

}
