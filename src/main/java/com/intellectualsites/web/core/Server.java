package com.intellectualsites.web.core;

import com.intellectualsites.web.config.ConfigVariableProvider;
import com.intellectualsites.web.config.ConfigurationFile;
import com.intellectualsites.web.config.YamlConfiguration;
import com.intellectualsites.web.events.Event;
import com.intellectualsites.web.events.EventManager;
import com.intellectualsites.web.events.defaultEvents.ShutdownEvent;
import com.intellectualsites.web.events.defaultEvents.StartupEvent;
import com.intellectualsites.web.object.*;
import com.intellectualsites.web.object.syntax.*;
import com.intellectualsites.web.plugin.PluginLoader;
import com.intellectualsites.web.plugin.PluginManager;
import com.intellectualsites.web.util.*;
import com.intellectualsites.web.views.*;
import com.sun.istack.internal.NotNull;
import org.apache.commons.io.output.TeeOutputStream;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * The core server
 * <p>
 *
 * @author Citymonstret
 */
public class Server implements IntellectualServer {

    /**
     * The logging prefix
     */
    public static final String PREFIX = "Web";
    private static Server instance;
    private final int port;
    private final boolean verbose;

    /**
     * Is the server stopping?
     */
    public boolean stopping;
    /**
     * The Crush syntax particles
     */
    public Set<Syntax> syntaxes;
    /**
     * The folder from which everything is based
     */
    public File coreFolder;
    protected ViewManager viewManager;
    protected Collection<ProviderFactory> providers;
    private boolean started, standalone;
    private ServerSocket socket;
    private SessionManager sessionManager;
    private String hostName;
    private boolean ipv4;
    private int bufferIn, bufferOut;
    private ConfigurationFile configViews;
    private Map<String, Class<? extends View>> viewBindings;
    private EventCaller eventCaller;
    private PluginLoader pluginLoader;
    CacheManager cacheManager;

    {
        viewBindings = new HashMap<>();
        providers = new ArrayList<>();
    }

    /**
     * Constructor
     *
     * @param standalone Should the server run async?
     */
    protected Server(boolean standalone, File coreFolder) throws IntellectualServerInitializationException {
        instance = this;

        this.standalone = standalone;
        addViewBinding("html", HTMLView.class);
        addViewBinding("css", CSSView.class);
        addViewBinding("javascript", JSView.class);
        addViewBinding("less", LessView.class);
        addViewBinding("img", ImgView.class);
        addViewBinding("download", DownloadView.class);
        addViewBinding("redirect", RedirectView.class);

        {
            Signal.handle(new Signal("INT"), new SignalHandler() {
                @Override
                public void handle(Signal signal) {
                    if (signal.toString().equals("SIGINT")) {
                        stop();
                    }
                }
            });

            new InputThread(this).start();
        }

        File logFolder = new File(coreFolder, "log");
        if (!logFolder.exists()) {
            if (!logFolder.mkdirs()) {
                log("Couldn't create the log folder");
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

        this.started = false;
        this.stopping = false;

        this.viewManager = new ViewManager();
        this.sessionManager = new SessionManager(this);
        this.cacheManager = new CacheManager();

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
                log("Invalid view '%s' - Constructor has to be #(String.class, Map.class)", e.getKey());
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
                log("STANDALONE = TRUE; but there is no alternate event caller set");
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

    /**
     * Load the plugins
     */
    private void loadPlugins() {
        File file = new File(coreFolder, "plugins");
        if (!file.exists()) {
            if (!file.mkdirs()) {
                log("Couldn't create %s - No plugins were loaded.", file);
                return;
            }
        }
        pluginLoader = new PluginLoader(new PluginManager());
        pluginLoader.loadAllPlugins(file);
        pluginLoader.enableAllPlugins();
    }

    @SuppressWarnings("ALL")
    @Override
    public void start() throws IntellectualServerStartException {
        Assert.equals(this.started, false, new IntellectualServerStartException("Cannot start the server, it is already started", new RuntimeException("Cannot restart server singleton")));
        //
        if (standalone) {
            loadPlugins();
            EventManager.getInstance().bake();
        }
        //
        log("Calling the startup event...");
        handleEvent(new StartupEvent(this));
        //
        log("Validating views...");
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
        viewManager.dump(this);
        //
        if (this.ipv4) {
            log("ipv4 is true - Using IPv4 stack");
            System.setProperty("java.net.preferIPv4Stack", "true");
        }
        //
        log("Starting the web server on port %s", this.port);
        this.started = true;
        try {
            socket = new ServerSocket(this.port);
            log("Server started");
        } catch (final Exception e) {
            throw new RuntimeException("Couldn't start the server...", e);
        }
        //
        log("Accepting connections on 'http://%s" + (this.port == 80 ? "" : ":" + port) + "/'", hostName);
        log("Output buffer size: %skb | Input buffer size: %skb", bufferOut / 1024, bufferIn / 1024);
        // Main Loop
        for (; ; ) {
            if (this.stopping) {
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

    /**
     * Accept the socket and the information async
     *
     * @param remote Socket to send and read from
     */
    private void runAsync(@NotNull final Socket remote) {
        new Thread() {
            @Override
            public void run() {
                if (verbose) {
                    log("Connection accepted from '%s' - Handling the data!", remote.getInetAddress());
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
                Response response;
                if (view instanceof CacheApplicable && ((CacheApplicable) view).isApplicable(r)) {
                    if (cacheManager.hasCache(view)) {
                        response = cacheManager.getCache(view);
                    } else {
                        response = view.generate(r);
                        cacheManager.setCache(view, response);
                    }
                } else {
                    response = view.generate(r);
                }
                // Response headers
                response.getHeader().apply(out);
                byte[] bytes;
                if (response.isText()) {
                    // Let's make a copy of the content before
                    // getting the bytes
                    String content = response.getContent();
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
                } else {
                    bytes = response.getBytes();
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
            }
        }.start();
    }

    /**
     * The core tick method
     * <p>
     * (runs the async socket accept)
     *
     * @see #runAsync(Socket)
     */
    private void tick() {
        try {
            runAsync(socket.accept());
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void log(@NotNull String message, @NotNull final Object... args) {
        for (final Object a : args) {
            message = message.replaceFirst("%s", a.toString());
        }
        System.out.printf("[%s][%s] %s\n", PREFIX, TimeUtil.getTimeStamp(), message);
    }

    @Override
    public void log(@NotNull LogProvider provider, @NotNull String message, @NotNull final Object... args) {
        for (final Object a : args) {
            message = message.replaceFirst("%s", a.toString());
        }
        System.out.printf("[%s][%s] %s\n", provider.getLogIdentifier(), TimeUtil.getTimeStamp(), message);
    }

    @Override
    public synchronized void stop() {
        log("Shutting down!");
        EventManager.getInstance().handle(new ShutdownEvent(this));
        pluginLoader.disableAllPlugins();
        System.exit(0);
    }

    @Override
    public SessionManager getSessionManager() {
        return this.sessionManager;
    }
}
