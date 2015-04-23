package com.intellectualsites.web.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created 2015-04-23 for IntellectualServer
 *
 * @author Citymonstret
 */
public class InputThread extends Thread {

    private Server server;
    public InputThread(Server server) {
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
                            server.stop();
                            break;
                        default:
                            server.log("Unknown command '%s'", line);
                            break;
                    }
                }
            }
            in.close();
        } catch(final Exception e) {
        }
    }
}
