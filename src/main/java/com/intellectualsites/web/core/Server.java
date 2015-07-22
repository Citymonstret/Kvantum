//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//     IntellectualServer is a web server, written entirely in the Java language.                            /
//     Copyright (C) 2015 IntellectualSites                                                                  /
//                                                                                                           /
//     This program is free software; you can redistribute it and/or modify                                  /
//     it under the terms of the GNU General Public License as published by                                  /
//     the Free Software Foundation; either version 2 of the License, or                                     /
//     (at your option) any later version.                                                                   /
//                                                                                                           /
//     This program is distributed in the hope that it will be useful,                                       /
//     but WITHOUT ANY WARRANTY; without even the implied warranty of                                        /
//     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                         /
//     GNU General Public License for more details.                                                          /
//                                                                                                           /
//     You should have received a copy of the GNU General Public License along                               /
//     with this program; if not, write to the Free Software Foundation, Inc.,                               /
//     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.                                           /
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.intellectualsites.web.core;

import com.intellectualsites.web.config.ConfigVariableProvider;
import com.intellectualsites.web.config.ConfigurationFile;
import com.intellectualsites.web.config.Message;
import com.intellectualsites.web.config.YamlConfiguration;
import com.intellectualsites.web.events.Event;
import com.intellectualsites.web.events.EventCaller;
import com.intellectualsites.web.events.EventManager;
import com.intellectualsites.web.events.defaultEvents.ShutdownEvent;
import com.intellectualsites.web.events.defaultEvents.StartupEvent;
import com.intellectualsites.web.logging.LogProvider;
import com.intellectualsites.web.object.*;
import com.intellectualsites.web.object.cache.CacheApplicable;
import com.intellectualsites.web.object.cache.CachedResponse;
import com.intellectualsites.web.object.error.IntellectualServerInitializationException;
import com.intellectualsites.web.object.error.IntellectualServerStartException;
import com.intellectualsites.web.object.syntax.*;
import com.intellectualsites.web.plugin.PluginLoader;
import com.intellectualsites.web.plugin.PluginManager;
import com.intellectualsites.web.util.*;
import com.intellectualsites.web.views.*;
import com.intellectualsites.web.views.staticviews.StaticViewManager;
import com.sun.istack.internal.NotNull;
import org.apache.commons.io.output.TeeOutputStream;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.*;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static com.intellectualsites.web.logging.LogModes.*;

/**
 * The core server
 * <p>
 *
 * @author Citymonstret
 */
public class Server extends Thread implements IntellectualServer {

    //
    // public static
    //
    public static final String PREFIX = "Web";

    //
    // private static
    //
    private static Server instance;

    //
    // public
    //
    public ConfigurationFile translations;
    public boolean stopping, enableCaching;
    public Set<Syntax> syntaxes;
    public File coreFolder;

    //
    // public final
    //
    public final boolean verbose;

    //
    // public volatile
    //
    public volatile CacheManager cacheManager;

    //
    // protected
    //
    protected ViewManager viewManager;

    //
    // protected final
    //
    protected final Collection<ProviderFactory> providers;

    //
    // private
    //
    private boolean started, ipv4, mysqlEnabled;
    private ServerSocket socket;
    private SessionManager sessionManager;
    private String hostName;
    private int bufferIn, bufferOut;
    private ConfigurationFile configViews;
    private MySQLConnManager mysqlConnManager;
    private EventCaller eventCaller;
    private PluginLoader pluginLoader;

    //
    // private final
    //
    private final boolean standalone;
    private final int port;
    private final Map<String, Class<? extends View>> viewBindings;
    public final LogWrapper logWrapper;

    {
        viewBindings = new HashMap<>();
        providers = new ArrayList<>();
    }

    @Override
    public boolean isMysqlEnabled() {
        return this.mysqlEnabled;
    }

    /**
     * Constructor
     *
     * @param standalone Should the server run async?
     */
    protected Server(boolean standalone, File coreFolder, LogWrapper logWrapper) throws IntellectualServerInitializationException {
        instance = this;

        Assert.notNull(coreFolder, logWrapper);

        {
            logWrapper.log("");
            logWrapper.log(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            logWrapper.log("> GNU GENERAL PUBLIC LICENSE NOTICE:");
            logWrapper.log("> ");
            logWrapper.log("> IntellectualServer, Copyright (C) 2015 IntellectualSites");
            logWrapper.log("> IntellectualSites comes with ABSOLUTELY NO WARRANTY; for details type `/show w`");
            logWrapper.log("> This is free software, and you are welcome to redistribute it");
            logWrapper.log("> under certain conditions; type `/show c` for details.");
            logWrapper.log(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            logWrapper.log("");
        }

        this.logWrapper = logWrapper;
        this.standalone = standalone;
        addViewBinding("html", HTMLView.class);
        addViewBinding("css", CSSView.class);
        addViewBinding("javascript", JSView.class);
        addViewBinding("less", LessView.class);
        addViewBinding("img", ImgView.class);
        addViewBinding("download", DownloadView.class);
        addViewBinding("redirect", RedirectView.class);

        if (standalone) {
            Signal.handle(new Signal("INT"), new SignalHandler() {
                @Override
                public void handle(Signal signal) {
                    if (signal.toString().equals("SIGINT")) {
                        stopServer();
                    }
                }
            });
            new InputThread(this).start();
        }

        File logFolder = new File(coreFolder, "log");
        if (!logFolder.exists()) {
            if (!logFolder.mkdirs()) {
                log(Message.COULD_NOT_CREATE_FOLDER, "log");
            }
        }
        try {
            FileUtils.addToZip(new File(logFolder, "old.zip"), logFolder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".txt");
                }
            }), true);
            System.setOut(new PrintStream(new TeeOutputStream(System.out, new FileOutputStream(new File(logFolder, TimeUtil.getTimeStamp(TimeUtil.LogFileFormat) + ".txt")))));
        } catch (final Exception e) {
            e.printStackTrace();
        }

        try {
            this.translations = new YamlConfiguration("translations", new File(new File(coreFolder, "config"), "translations.yml"));
            this.translations.loadFile();
            for (final Message message : Message.values()) {
                String nameSpace;
                switch (message.getMode()) {
                    case MODE_DEBUG:
                        nameSpace = "debug";
                        break;
                    case MODE_INFO:
                        nameSpace = "info";
                        break;
                    case MODE_ERROR:
                        nameSpace = "error";
                        break;
                    case MODE_WARNING:
                        nameSpace = "warning";
                        break;
                    default:
                        nameSpace = "info";
                        break;
                }
                this.translations.setIfNotExists(nameSpace + "." + message.name().toLowerCase(), message.toString());
            }
            this.translations.saveFile();
        } catch (final Exception e) {
            log("Cannot load the translations file");
            e.printStackTrace();
        }

        log(Message.DEBUG);

        ConfigurationFile configServer;
        try {
            configServer = new YamlConfiguration("server", new File(new File(coreFolder, "config"), "server.yml"));
            configServer.loadFile();
            configServer.setIfNotExists("port", 80);
            configServer.setIfNotExists("hostname", "localhost");
            configServer.setIfNotExists("buffer.in", 1024 * 1024); // 16 mb
            configServer.setIfNotExists("buffer.out", 1024 * 1024);
            configServer.setIfNotExists("verbose", false);
            configServer.setIfNotExists("ipv4", false);
            configServer.setIfNotExists("cache.enabled", true);
            configServer.setIfNotExists("mysql.enabled", false);
            configServer.saveFile();
        } catch (final Exception e) {
            throw new IntellectualServerInitializationException("Couldn't load in the configuration file", e);
        }

        this.port = configServer.get("port");
        this.hostName = configServer.get("hostname");
        this.bufferIn = configServer.get("buffer.in");
        this.bufferOut = configServer.get("buffer.out");
        this.ipv4 = configServer.get("ipv4");
        this.verbose = configServer.get("verbose");
        this.enableCaching = configServer.get("cache.enabled");
        this.mysqlEnabled = configServer.get("mysql.enabled");

        this.started = false;
        this.stopping = false;

        this.viewManager = new ViewManager();
        this.sessionManager = new SessionManager(this);
        this.cacheManager = new CacheManager();

        if (mysqlEnabled) {
            this.mysqlConnManager = new MySQLConnManager();
        }

        try {
            configViews = new YamlConfiguration("views", new File(new File(coreFolder, "config"), "views.yml"));
            configViews.loadFile();
            // These are the default views
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

        // Setup the provider factories
        this.providers.add(this.sessionManager);
        this.providers.add(new ServerProvider());
        this.providers.add(ConfigVariableProvider.getInstance());
        this.providers.add(new PostProviderFactory());
        this.providers.add(new MetaProvider());

        // Setup the crush syntax-particles
        this.syntaxes = new LinkedHashSet<>();
        syntaxes.add(new Include());
        syntaxes.add(new Comment());
        syntaxes.add(new MetaBlock());
        syntaxes.add(new IfStatement());
        syntaxes.add(new ForEachBlock());
        syntaxes.add(new Variable());
    }

    /**
     * Get THE instance of the server
     *
     * @return this, literally... this!
     */
    public static Server getInstance() {
        return instance;
    }

    @Override
    public void addViewBinding(@NotNull final String key, @NotNull final Class<? extends View> c) {
        Assert.notNull(c);
        Assert.notEmpty(key);
        viewBindings.put(key, c);
    }

    @Override
    public void validateViews() {
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, Class<? extends View>> e : viewBindings.entrySet()) {
            Class<? extends View> vc = e.getValue();
            try {
                vc.getDeclaredConstructor(String.class, Map.class);
            } catch (final Exception ex) {
                log(Message.INVALID_VIEW, e.getKey());
                toRemove.add(e.getKey());
            }
        }
        for (String s : toRemove) {
            viewBindings.remove(s);
        }
    }

    @Override
    public void handleEvent(@NotNull final Event event) {
        Assert.notNull(event);
        if (standalone) {
            EventManager.getInstance().handle(event);
        } else {
            if (eventCaller != null) {
                eventCaller.callEvent(event);
            } else {
                log(Message.STANDALONE_NO_EVENT_CALLER);
            }
        }
    }

    @Override
    public void setEventCaller(@NotNull final EventCaller caller) {
        Assert.notNull(caller);
        this.eventCaller = caller;
    }

    @Override
    public void addProviderFactory(@NotNull final ProviderFactory factory) {
        Assert.notNull(factory);
        this.providers.add(factory);
    }

    private void loadPlugins() {
        if (standalone) {
            File file = new File(coreFolder, "plugins");
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    log(Message.COULD_NOT_CREATE_PLUGIN_FOLDER, file);
                    return;
                }
            }
            pluginLoader = new PluginLoader(new PluginManager());
            pluginLoader.loadAllPlugins(file);
            pluginLoader.enableAllPlugins();
        } else {
            log("Running as standalone, not loading plugins!");
        }
    }

    @SuppressWarnings("ALL")
    @Override
    public void start() {
        try {
            Assert.equals(this.started, false, new IntellectualServerStartException("Cannot start the server, it is already started", new RuntimeException("Cannot restart server singleton")));
        } catch (IntellectualServerStartException e) {
            e.printStackTrace();
            return;
        }
        //
        if (standalone) {
            loadPlugins();
            EventManager.getInstance().bake();
        }
        //
        log(Message.CALLING_EVENT, "startup");
        handleEvent(new StartupEvent(this));
        //
        if (mysqlEnabled) {
            log("Initalizing MySQL Connection");
            mysqlConnManager.init();
        }
        log(Message.VALIDATING_VIEWS);
        validateViews();
        //
        log("Loading views...");
        Map<String, Map<String, Object>> views = configViews.get("views");
        for (final Map.Entry<String, Map<String, Object>> entry : views.entrySet()) {
            Map<String, Object> view = entry.getValue();
            String type = "html", filter = view.get("filter").toString();
            if (view.containsKey("type")) {
                type = view.get("type").toString();
            }
            Map<String, Object> options;
            if (view.containsKey("options")) {
                options = (HashMap<String, Object>) view.get("options");
            } else {
                options = new HashMap<>();
            }

            if (viewBindings.containsKey(type.toLowerCase())) {
                Class<? extends View> vc = viewBindings.get(type.toLowerCase());
                try {
                    View vv = vc.getDeclaredConstructor(String.class, Map.class).newInstance(filter, options);
                    this.viewManager.add(vv);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (standalone) {
            try {
                StaticViewManager.generate(new SystemView());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        viewManager.dump(this);
        //
        if (this.ipv4) {
            log("ipv4 is true - Using IPv4 stack");
            System.setProperty("java.net.preferIPv4Stack", "true");
        }
        if (!this.enableCaching) {
            log("Caching is not enabled, this can reduce load times on bigger files!");
        } else {
            log("Caching is enabled, beware that this increases memory usage - So keep an eye on it");
        }
        //
        log(Message.STARTING_ON_PORT, this.port);
        this.started = true;
        try {
            socket = new ServerSocket(this.port);
            log(Message.SERVER_STARTED);
        } catch (final Exception e) {
            // throw new RuntimeException("Couldn't start the server...", e);
            boolean run = true;

            int port = this.port + 1;
            while (run) {
                try {
                    socket = new ServerSocket(port++);
                    run = false;
                    log("Specified port was occupied, running on " + port + " instead");

                    Field portField = getClass().getDeclaredField("port");
                    portField.setAccessible(true);
                    portField.set(this, port);
                } catch(final Exception ex) {
                    continue;
                }
            }
        }
        //
        log(Message.ACCEPTING_CONNECTIONS_ON, hostName + (this.port == 80 ? "" : ":" + port) + "/'");
        log(Message.OUTPUT_BUFFER_INFO, bufferOut / 1024, bufferIn / 1024);
        // Main Loop

        long lastExecution = System.currentTimeMillis();
        long placeholder = System.currentTimeMillis();
        int loops = 0;

        for (; ; ) {
            if (this.stopping) {
                log(Message.SHUTTING_DOWN);
                break;
            }
            try {
                tick();
            } catch (final Exception e) {
                log(Message.TICK_ERROR);
                e.printStackTrace();
            }
        }
    }

    private void runAsync(@NotNull final Socket remote) {
        new Thread() {
            @Override
            public void run() {
                if (verbose) {
                    log(Message.CONNECTION_ACCEPTED, remote.getInetAddress());
                }
                StringBuilder rRaw = new StringBuilder();
                BufferedOutputStream out;
                BufferedReader input;
                Request r;
                try {
                    // Let's read from the socket :D
                    input = new BufferedReader(new InputStreamReader(remote.getInputStream()), bufferIn);
                    // And... write!
                    out = new BufferedOutputStream(remote.getOutputStream(), bufferOut);
                    String str;
                    while ((str = input.readLine()) != null && !str.equals("")) {
                        rRaw.append(str).append("|");
                    }
                    r = new Request(rRaw.toString(), remote);
                    if (r.getQuery().getMethod() == Method.POST) {
                        StringBuilder pR = new StringBuilder();
                        int cl = Integer.parseInt(r.getHeader("Content-Length").substring(1));
                        for (int i = 0; i < cl; i++) {
                            pR.append((char) input.read());
                        }
                        r.setPostRequest(new PostRequest(pR.toString()));
                    }
                    Session session = sessionManager.getSession(r, out);
                    if (session != null) {
                        r.setSession(session);
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    return;
                }
                log(r.buildLog());
                // Get the view
                View view = viewManager.match(r);

                boolean isText;
                String content = "";
                byte[] bytes = null;

                if (enableCaching && view instanceof CacheApplicable && ((CacheApplicable) view).isApplicable(r)) {
                    if (cacheManager.hasCache(view)) {
                        CachedResponse response = cacheManager.getCache(view);
                        if ((isText = response.isText)) {
                            content = new String(response.bodyBytes);
                        } else {
                            bytes = response.bodyBytes;
                        }
                        try {
                            out.write(response.headerBytes);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Response response = view.generate(r);
                        response.getHeader().apply(out);
                        cacheManager.setCache(view, response);
                        if ((isText = response.isText())) {
                            content = response.getContent();
                        } else {
                            bytes = response.getBytes();
                        }
                    }
                } else {
                    Response response = view.generate(r);
                    response.getHeader().apply(out);
                    if ((isText = response.isText())) {
                        content = response.getContent();
                    } else {
                        bytes = response.getBytes();
                    }
                }

                if (isText) {
                    // Make sure to not use Crush when
                    // told not to
                    if (!(view instanceof IgnoreSyntax)) {
                        // Provider factories are fun, and so is the
                        // global map. But we also need the view
                        // specific ones!
                        Map<String, ProviderFactory> factories = new HashMap<>();
                        for (final ProviderFactory factory : providers) {
                            factories.put(factory.providerName().toLowerCase(), factory);
                        }
                        // Now make use of the view specific ProviderFactory
                        ProviderFactory z = view.getFactory(r);
                        if (z != null) {
                            factories.put(z.providerName().toLowerCase(), z);
                        }
                        // This is how the crush engine works.
                        // Quite simple, yet powerful!
                        for (Syntax syntax : syntaxes) {
                            if (syntax.matches(content)) {
                                content = syntax.handle(content, r, factories);
                            }
                        }
                    }
                    // Now, finally, let's get the bytes.
                    bytes = content.getBytes();
                }

                try {
                    out.write(bytes);
                    out.flush();
                } catch (final Exception e) {
                    e.printStackTrace();
                }

                try {
                    input.close();
                } catch (final Exception e) {
                    e.printStackTrace();
                }

                log("Request was served by '%s', with the type '%s'. The total lenght of the content was '%s'",
                        view.getName(), isText ? "text" : "bytes", bytes.length
                        );
            }
        }.start();
    }

    private void tick() {
        try {
            runAsync(socket.accept());
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void log(@NotNull Message message, @NotNull final Object... args) {
        this.log(message.toString(), message.getMode(), args);
    }

    private synchronized void log(@NotNull String message, @NotNull int mode, @NotNull final Object... args) {
        // This allows us to customize what messages are
        // sent to the logging screen, and thus we're able
        // to limit to only error messages or such
        if (mode < lowestLevel || mode > highestLevel) {
            return;
        }
        String prefix;
        switch (mode) {
            case MODE_DEBUG:
                prefix = "Debug";
                break;
            case MODE_INFO:
                prefix = "Info";
                break;
            case MODE_ERROR:
                prefix = "Error";
                break;
            case MODE_WARNING:
                prefix = "Warning";
                break;
            default:
                prefix = "Info";
                break;
        }
        for (final Object a : args) {
            message = message.replaceFirst("%s", a.toString());
        }
        logWrapper.log(PREFIX, prefix, TimeUtil.getTimeStamp(), message);
        // logWrapper.log(String.format("[%s][%s][%s] %s", PREFIX, prefix, TimeUtil.getTimeStamp(), message));
        // System.out.printf("[%s][%s][%s] %s%s", PREFIX, prefix, TimeUtil.getTimeStamp(), message, System.lineSeparator());
    }

    @Override
    public synchronized void log(@NotNull String message, @NotNull final Object... args) {
        this.log(message, MODE_INFO, args);
    }

    @Override
    public void log(@NotNull LogProvider provider, @NotNull String message, @NotNull final Object... args) {
        for (final Object a : args) {
            message = message.replaceFirst("%s", a.toString());
        }
        logWrapper.log(PREFIX, provider.getLogIdentifier(), TimeUtil.getTimeStamp(), message);
        // void log(String prefix, String prefix1, String timeStamp, String message);
        // logWrapper.log(String.format("[%s][%s] %s", provider.getLogIdentifier(), TimeUtil.getTimeStamp(), message));
        // System.out.printf("[%s][%s] %s\n", provider.getLogIdentifier(), TimeUtil.getTimeStamp(), message);
    }

    @Override
    public synchronized void stopServer() {
        log(Message.SHUTTING_DOWN);
        EventManager.getInstance().handle(new ShutdownEvent(this));
        if (pluginLoader != null) {
            pluginLoader.disableAllPlugins();
        }
        if (standalone) {
            System.exit(0);
        }
    }

    @Override
    public SessionManager getSessionManager() {
        return this.sessionManager;
    }

    @Override
    public ViewManager getViewManager() {
        return viewManager;
    }
}
