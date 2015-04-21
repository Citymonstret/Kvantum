package com.intellectualsites.web.core;

import com.intellectualsites.web.object.*;
import com.intellectualsites.web.util.ServerProvider;
import com.intellectualsites.web.util.SessionManager;
import com.intellectualsites.web.util.TimeUtil;
import com.intellectualsites.web.util.ViewManager;
import com.intellectualsites.web.views.HTMLView;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
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

    public static Pattern variable, comment, include;

    private Collection<ProviderFactory> providers;

    private File coreFolder;

    static {
        variable = Pattern.compile("\\{\\{([a-zA-Z0-9]*)\\.([A-Za-z0-9\\_\\-]*)( [|]{2} [A-Z]*)?\\}\\}");
        comment = Pattern.compile("(\\/\\*[\\S\\s]*?\\*\\/)");
        include = Pattern.compile("\\{\\{include:([\\/A-Za-z\\.\\-]*)\\}\\}");
    }
    public Server() {
        this.started = false;
        this.stopping = false;
        this.port = 80; // TODO Make configurable
        this.viewManager = new ViewManager();
        this.sessionManager = new SessionManager(this);
        this.coreFolder = new File("./");

        // Allow .html Files!
        this.viewManager.add(new HTMLView());

        this.providers = new ArrayList<ProviderFactory>();
        this.providers.add(this.sessionManager);
        this.providers.add(new ServerProvider());
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
            } catch(final Exception e) {
                log("Error in server ticking...");
                e.printStackTrace();
            }
        }
    }

    private void runAsync(final Socket remote) {
        new Thread() {
            @Override
            public void run() {
                log("Connection Accepted! Sending data...");
                StringBuilder rRaw = new StringBuilder();
                PrintWriter out = null;
                BufferedReader input;
                try {
                    input = new BufferedReader(new InputStreamReader(remote.getInputStream()));
                    out = new PrintWriter(remote.getOutputStream());
                    String str;
                    while ((str = input.readLine()) != null && !str.equals("")) {
                        rRaw.append(str).append("|");
                    }
                } catch(final Exception e) {
                    e.printStackTrace();
                    return;
                }
                Request r = new Request(rRaw.toString());
                log(r.buildLog());
                View view = viewManager.match(r);
                Response response = view.generate(r);
                // Response headers
                response.getHeader().apply(out);
                Session session = sessionManager.getSession(r, out);
                // Empty line indicates that the header response is finished, send content!
                out.println();
                String content = response.getContent();
                Matcher matcher = Server.comment.matcher(content);

                // First replace all comments
                while (matcher.find()) {
                    content = content.replace(matcher.group(1), "");
                }
                // Find includes
                matcher = Server.include.matcher(content);
                while(matcher.find()) {
                    File file = new File(coreFolder, matcher.group(1));
                    if (file.exists()) {
                        StringBuilder c = new StringBuilder();
                        String line;
                        try {
                            BufferedReader reader = new BufferedReader(new FileReader(file));
                            while ((line = reader.readLine()) != null)
                                c.append(line).append("\n");
                            reader.close();
                        } catch(final Exception e) {
                            e.printStackTrace();
                        }
                        if (file.getName().endsWith(".css")) {
                            content = content.replace(matcher.group(), "<style>\n" + c + "</style>");
                        } else {
                            content = content.replace(matcher.group(), c.toString());
                        }
                    } else {
                        log("Couldn't find file for '%s'", matcher.group());
                    }
                }
                // Replace all variables
                matcher = Server.variable.matcher(content);
                while (matcher.find()) {
                    String provider = matcher.group(1);
                    String variable = matcher.group(2);

                    String filter = "";
                    if (matcher.group().contains(" || ")) {
                        filter = matcher.group().split(" \\|\\| ")[1].replace("}}", "");
                    }

                    boolean found = false;

                    for (final ProviderFactory factory : providers) {
                        if (factory.providerName().equalsIgnoreCase(provider)) {
                            VariableProvider p = factory.get(r);
                            if (p != null) {
                                if (p.contains(variable)) {
                                    Object o = p.get(variable);
                                    if (!filter.equals("")) {
                                        switch(filter) {
                                            case "UPPERCASE":
                                                o = o.toString().toUpperCase();
                                                break;
                                            case "LOWERCASE":
                                                o = o.toString().toLowerCase();
                                                break;
                                            case "LIST": {
                                                StringBuilder s = new StringBuilder();
                                                s.append("<ul>");
                                                if (o instanceof Object[]) {
                                                    for (Object oo : (Object[]) o) {
                                                        s.append("<li>").append(oo).append("</li>");
                                                    }
                                                } else if(o instanceof Collection) {
                                                    for (Object oo : (Collection) o) {
                                                        s.append("<li>").append(oo).append("</li>");
                                                    }
                                                }
                                                s.append("</ul>");
                                                o = s.toString();
                                            }
                                            break;
                                            default:
                                                break;
                                        }
                                    }
                                    content = content.replace(matcher.group(), o.toString());
                                    found = true;
                                }
                            }
                            break;
                        }
                    }
                    if (!found) {
                        content = content.replace(matcher.group(),  "");
                    }
                }
                for (String s : content.split("\n")) {
                    out.println(s);
                }
                // Flush<3
                out.flush();
                try {
                    input.close();
                } catch(final Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    protected void tick() {
        try {
            runAsync(socket.accept());
        } catch(final Exception e) {
            e.printStackTrace();
        }
    }

    public void log(String message, final Object... args) {
        for (final Object a : args) {
            message = message.replaceFirst("%s", a.toString());
        }
        System.out.printf("[%s][%s] %s\n", PREFIX, TimeUtil.getTimeStamp(), message);
    }
}
