package com.intellectualsites.web.views.requesthandler;

import com.intellectualsites.web.core.CoreConfig;
import com.intellectualsites.web.core.Server;
import com.intellectualsites.web.matching.ViewPattern;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.util.Final;
import com.intellectualsites.web.views.RequestHandler;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

@RequiredArgsConstructor
public class SimpleRequestHandler extends RequestHandler {

    private static AtomicInteger identifier = new AtomicInteger(0);

    @NonNull
    private final String pattern;
    @NonNull
    private final BiConsumer<Request, Response> generator;
    @Setter
    private String internalName = "simpleRequestHandler::" + identifier.getAndIncrement();

    {
        if (CoreConfig.debug) {
            Server.getInstance().log("Adding DebugMiddleware to SimpleRequestHandler");
            this.middlewareQueuePopulator.add(DebugMiddleware.class);
        }
    }

    private ViewPattern compiledPattern;

    protected ViewPattern getPattern() {
        if (compiledPattern == null) {
            compiledPattern = new ViewPattern(pattern);
        }
        return compiledPattern;
    }

    @Override
    public boolean matches(@NonNull final Request request) {
        if (CoreConfig.debug) {
            request.addMeta("zmetakey", UUID.randomUUID().toString());
        }
        final Map<String, String> map = getPattern().matches(request.getQuery().getFullRequest());
        if (map != null) {
            request.addMeta("variables", map);
        }
        return map != null;
    }

    @Override
    public final Response generate(@NonNull final Request r) {
        final Response response = new Response(this);
        generator.accept(r, response);
        return response;
    }

    @Override
    public String getName() {
        return this.internalName;
    }

    @Final
    final public void register() {
        Server.getInstance().getRequestManager().add(this);
    }

}
