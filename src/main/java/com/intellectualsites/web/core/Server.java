package com.intellectualsites.web.core;

import com.intellectualsites.web.config.ConfigVariableProvider;
import com.intellectualsites.web.config.ConfigurationFile;
import com.intellectualsites.web.config.YamlConfiguration;
import com.intellectualsites.web.object.*;
import com.intellectualsites.web.util.ServerProvider;
import com.intellectualsites.web.util.SessionManager;
import com.intellectualsites.web.util.TimeUtil;
import com.intellectualsites.web.util.ViewManager;
import com.intellectualsites.web.views.CSSView;
import com.intellectualsites.web.views.HTMLView;
import com.intellectualsites.web.views.JSView;
import com.intellectualsites.web.views.LessView;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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

    private String hostName;

    private ConfigurationFile configServer, configViews;

    static {
        variable = Pattern.compile("\\{\\{([a-zA-Z0-9]*)\\.([@A-Za-z0-9\\_\\-]*)( [|]{2} [A-Z]*)?\\}\\}");
        comment = Pattern.compile("(\\/\\*[\\S\\s]*?\\*\\/)");
        include = Pattern.compile("\\{\\{include:([\\/A-Za-z\\.\\-]*)\\}\\}");
    }

    public Server() {
        this.coreFolder = new File("./");

        try {
            configServer = new YamlConfiguration("server", new File(new File(coreFolder, "config"), "server.yml"));
            configServer.loadFile();
            configServer.setIfNotExists("port", 80);
            configServer.setIfNotExists("hostname", "localhost");
            configServer.saveFile();
        } catch (final Exception e) {
            throw new RuntimeException("Couldn't load in the config file...", e);
        }

        this.port = configServer.get("port");
        this.hostName = configServer.get("hostname");

        this.started = false;
        this.stopping = false;

        this.viewManager = new ViewManager();
        this.sessionManager = new SessionManager(this);

        try {
            configViews = new YamlConfiguration("views", new File(new File(coreFolder, "config"), "views.yml"));
            configViews.loadFile();

            Map<String, Object> views = new HashMap<>();
            // HTML View
            Map<String, Object> view = new HashMap<>();
            view.put("filter", "(\\/)([A-Za-z0-9]*)(.html)?");
            view.put("type", "html");
            views.put("html", view);
            // CSS View
            view = new HashMap<>();
            view.put("filter", "(\\/style\\/)([A-Za-z0-9]*)(.css)?");
            view.put("type", "css");
            views.put("css", view);

            configViews.setIfNotExists("views", views);
            configViews.saveFile();
        } catch (final Exception e) {
            throw new RuntimeException("Couldn't load in views");
        }

        Map<String, Map<String, Object>> views = configViews.get("views");
        for (final Map.Entry<String, Map<String, Object>> entry : views.entrySet()) {
            Map<String, Object> view = entry.getValue();
            String type = "html", filter = view.get("filter").toString();
            if (view.containsKey("type")) {
                type = view.get("type").toString();
            }
            switch (type.toLowerCase()) {
                case "html":
                    this.viewManager.add(new HTMLView(filter));
                    break;
                case "css":
                    this.viewManager.add(new CSSView(filter));
                    break;
                case "less":
                    this.viewManager.add(new LessView(filter));
                    break;
                case "javascript":
                    this.viewManager.add(new JSView(filter));
                    break;
                default:
                    break;
            }
        }

        viewManager.dump(this);

        // Allow .html Files!
        // this.viewManager.add(new CSSView());
        // this.viewManager.add(new HTMLView("(\\/)([A-Za-z0-9]*)(.html)?"));

        this.providers = new ArrayList<ProviderFactory>();
        this.providers.add(this.sessionManager);
        this.providers.add(new ServerProvider());
        this.providers.add(ConfigVariableProvider.getInstance());
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
        } catch (final Exception e) {
            throw new RuntimeException("Couldn't start the server...", e);
        }

        log("Accepting connections on 'http://%s/'", hostName);
        for (; ; ) {
            if (this.stopping) {
                // Stop the server gracefully :D
                log("Shutting down...");
                break;
            }
            try {
                tick();
            } catch (final Exception e) {
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
                } catch (final Exception e) {
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
                String content = response.getContent();
                Matcher matcher;

                // Find includes
                matcher = Server.include.matcher(content);
                while (matcher.find()) {
                    File file = new File(coreFolder, matcher.group(1));
                    if (file.exists()) {
                        StringBuilder c = new StringBuilder();
                        String line;
                        try {
                            BufferedReader reader = new BufferedReader(new FileReader(file));
                            while ((line = reader.readLine()) != null)
                                c.append(line).append("\n");
                            reader.close();
                        } catch (final Exception e) {
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
                matcher = Server.comment.matcher(content);
                while (matcher.find()) {
                    content = content.replace(matcher.group(1), "");
                }

                Map<String, ProviderFactory> factories = new HashMap<String, ProviderFactory>();
                for (final ProviderFactory factory : providers) {
                    factories.put(factory.providerName().toLowerCase(), factory);
                }
                ProviderFactory z = view.getFactory(r);
                if (z != null) {
                    factories.put(z.providerName().toLowerCase(), z);
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

                    if (factories.containsKey(provider.toLowerCase())) {
                        VariableProvider p = factories.get(provider.toLowerCase()).get(r);
                        if (p != null) {
                            if (p.contains(variable)) {
                                Object o = p.get(variable);
                                if (!filter.equals("")) {
                                    switch (filter) {
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
                                            } else if (o instanceof Collection) {
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
                            }
                        }
                    } else {
                        content = content.replace(matcher.group(), "");
                    }
                }
                for (String s : content.split("\n")) {
                    out.println(s);
                }
                // Flush<3
                out.flush();
                try {
                    input.close();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    protected void tick() {
        try {
            runAsync(socket.accept());
        } catch (final Exception e) {
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
