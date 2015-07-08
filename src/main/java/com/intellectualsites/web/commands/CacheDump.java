package com.intellectualsites.web.commands;

import com.intellectualsites.web.core.Server;
import com.intellectualsites.web.object.CachedResponse;
import com.intellectualsites.web.util.CacheManager;

import java.util.Map;

/**
 * Created 2015-07-08 for IntellectualServer
 *
 * @author Citymonstret
 */
public class CacheDump extends Command {

    @Override
    public void handle(String[] args) {
        CacheManager cacheManager = Server.getInstance().cacheManager;
        StringBuilder output = new StringBuilder("Currently Cached: ");
        for (Map.Entry<String, CachedResponse> e : cacheManager.getAll().entrySet()) {
            output.append(e.getKey()).append(" = ").append(e.getValue().isText ? "text" : "bytes").append(", ");
        }
        Server.getInstance().log(output.toString());
    }

}
