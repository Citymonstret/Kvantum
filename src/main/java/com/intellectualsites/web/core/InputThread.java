package com.intellectualsites.web.core;

import com.intellectualsites.web.commands.CacheDump;
import com.intellectualsites.web.commands.Command;
import com.intellectualsites.web.commands.Stop;
import com.intellectualsites.web.object.CachedResponse;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.util.CacheManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * The thread which handles command inputs, when ran as a standalone
 * applications.
 *
 * @author Citymonstret
 */
public class InputThread extends Thread {

    private Server server;
    private Map<String, Command> commands;


    protected InputThread(Server server) {
        this.server = server;
        this.commands = new HashMap<>();

        this.commands.put("stop", new Stop());
        this.commands.put("cachedump", new CacheDump());
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String line;
            boolean stop = false;
            while (!(line = in.readLine()).equalsIgnoreCase("quit") && !stop) {
                if (line.startsWith("/")) {
                    line = line.replace("/", "").toLowerCase();
                    String[] strings = line.split(" ");
                    String[] args;
                    if (strings.length > 1) {
                        args = new String[strings.length - 1];
                        System.arraycopy(strings, 1, args, 0, strings.length - 1);
                    } else {
                        args = new String[0];
                    }
                    String command = strings[0];
                    if (commands.containsKey(command)) {
                        commands.get(command).handle(args);
                    } else {
                        server.log("Unknown command '%s'", line);
                    }
                }
            }
            in.close();
        } catch (final Exception ignored) {
        }
    }
}
