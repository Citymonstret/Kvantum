package com.intellectualsites.web.views.requesthandler;

import com.intellectualsites.web.core.Server;
import com.intellectualsites.web.logging.LogModes;
import com.intellectualsites.web.object.Request;

public final class DebugMiddleware extends Middleware {

    @Override
    public void handle(Request request, MiddlewareQueue queue) {
        if (request.getMeta("zmetakey") != null) {
            queue.handle(request);
        } else {
            Server.getInstance().log("DebugMiddleware :> Request doesn't have a stored 'zmetakey', dropping!",
                    LogModes.MODE_WARNING);
        }
    }

}
