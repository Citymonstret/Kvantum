package com.intellectualsites.web.core;

import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.util.CacheManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * The thread which handles command inputs, when ran as a standalone
 * applications.
 *
 * @author Citymonstret
 */
public class InputThread extends Thread {

    private Server server;

    protected InputThread(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String line;
            boolean stop = false;
            while (!(line = in.readLine()).equalsIgnoreCase("quit") && !stop) {
                if (line.startsWith("/")) {
                    // This is a command :D
                    switch(line.replace("/", "").toLowerCase()) {
                        case "stop":
                            stop = true;
                            server.stop();
                            break;
                        case "cachedump":
                            CacheManager cacheManager = server.cacheManager;
                            StringBuilder output = new StringBuilder("Currently Cached: ");
                            for (Map.Entry<String, Response> e : cacheManager.getAll().entrySet()) {
                                output.append(e.getKey()).append(" = ").append(e.getValue().isText() ? "text" : "bytes").append(", ");
                            }
                            server.log(output.toString());
                            break;
                        default:
                            server.log("Unknown command '%s'", line);
                            break;
                    }
                }
            }
            in.close();
        } catch (final Exception ignored) {
        }
    }
}
