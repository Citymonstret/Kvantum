package com.intellectualsites.web.core;

import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Session;
import com.intellectualsites.web.object.View;
import com.intellectualsites.web.util.SessionManager;
import com.intellectualsites.web.util.TimeUtil;
import com.intellectualsites.web.util.ViewManager;
import com.intellectualsites.web.views.HTMLView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created 2015-04-19 for IntellectualServer
 *
 * @author Citymonstret
 */
public class Server {

    private boolean started, stopping;
    private ServerSocket socket;

    private final int port;

    private ViewManager viewManager;
    private SessionManager sessionManager;

    public static final String PREFIX = "Web";

    public static Pattern variable;

    static {
        variable = Pattern.compile("\\{\\{([a-zA-Z0-9]*)\\.([a-zA-Z0-9]*)\\}\\}");
    }
    public Server() {
        this.started = false;
        this.stopping = false;
        this.port = 80; // TODO Make configurable
        this.viewManager = new ViewManager();
        this.sessionManager = new SessionManager(this);

        // Allow .html Files!
        this.viewManager.add(new HTMLView());
    }

    public void start() throws RuntimeException {
        if (this.started) {
            throw new RuntimeException("Cannot start the server when it's already started...");
        }
        this.started = true;
        log("Starting the web server on port %s", this.port);
        try {
            socket = new ServerSocket(this.port);
            log("Server started");
        } catch(final Exception e) {
            throw new RuntimeException("Couldn't start the server...", e);
        }
        log("Accepting connections");
        for (;;) {
            if (this.stopping) {
                // Stop the server gracefully :D
                log("Shutting down...");
                break;
            }
            try {
                tick();
            } catch(final RuntimeException e) {
                log("Error in server ticking...");
                e.printStackTrace();
            }
        }
    }

    protected void tick() throws RuntimeException {
        try {
            final Socket remote = socket.accept();
            log("Connection Accepted! Sending data...");
            StringBuilder rRaw = new StringBuilder();
            BufferedReader input = new BufferedReader(new InputStreamReader(remote.getInputStream()));
            PrintWriter out = new PrintWriter(remote.getOutputStream());
            String str;
            while ((str = input.readLine()) != null && !str.equals("")) {
                rRaw.append(str).append("|");
            }
            Request r = new Request(rRaw.toString());
            log(r.buildLog());
            View view = viewManager.match(r);
            // Response headers
            view.headers(out, r);
            Session session = sessionManager.getSession(r, out);
            // Empty line indicates that the header response is finished, send content!
            out.println();
            // TODO Actual content
            String content = view.content(r);
            Matcher matcher = Server.variable.matcher(content);
            while (matcher.find()) {
                String provider = matcher.group(1);
                String variable = matcher.group(2);
                content = content.replace(matcher.group(), "Provider=" + provider + ";Variable=" + variable + ";");
            }
            for (String s : content.split("\n")) {
                out.println(s);
            }
            // Flush<3
            out.flush();
            input.close();
        } catch(final Exception e) {
            throw new RuntimeException("Ticking error!", e);
        }
    }

    public void log(String message, final Object... args) {
        for (final Object a : args) {
            message = message.replaceFirst("%s", a.toString());
        }
        System.out.printf("[%s][%s] %s\n", PREFIX, TimeUtil.getTimeStamp(), message);
    }
}
