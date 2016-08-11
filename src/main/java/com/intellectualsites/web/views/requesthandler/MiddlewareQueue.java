package com.intellectualsites.web.views.requesthandler;


import com.intellectualsites.web.object.Request;
import lombok.NonNull;

import java.util.ArrayDeque;
import java.util.Queue;

public final class MiddlewareQueue {

    private final Queue<Middleware> queue = new ArrayDeque<>();

    private boolean finished = false;

    public void add(@NonNull final Middleware middleware) {
        this.queue.add(middleware);
    }

    public void handle(@NonNull final Request request) {
        final Middleware next = this.queue.poll();
        if (next != null) {
            next.handle(request, this);
        } else {
            finished = true;
        }
    }

    public final boolean finished() {
        return finished;
    }

}
