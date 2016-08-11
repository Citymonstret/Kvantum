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

import com.intellectualsites.configurable.ConfigurationFactory;
import com.intellectualsites.web.config.ConfigVariableProvider;
import com.intellectualsites.web.config.ConfigurationFile;
import com.intellectualsites.web.config.Message;
import com.intellectualsites.web.config.YamlConfiguration;
import com.intellectualsites.web.events.Event;
import com.intellectualsites.web.events.EventCaller;
import com.intellectualsites.web.events.EventManager;
import com.intellectualsites.web.events.defaultEvents.ServerReadyEvent;
import com.intellectualsites.web.events.defaultEvents.ShutdownEvent;
import com.intellectualsites.web.events.defaultEvents.StartupEvent;
import com.intellectualsites.web.extra.ApplicationStructure;
import com.intellectualsites.web.extra.accounts.Account;
import com.intellectualsites.web.extra.accounts.AccountCommand;
import com.intellectualsites.web.extra.accounts.AccountManager;
import com.intellectualsites.web.logging.LogProvider;
import com.intellectualsites.web.object.LogWrapper;
import com.intellectualsites.web.object.error.IntellectualServerInitializationException;
import com.intellectualsites.web.object.error.IntellectualServerStartException;
import com.intellectualsites.web.object.syntax.*;
import com.intellectualsites.web.plugin.PluginLoader;
import com.intellectualsites.web.plugin.PluginManager;
import com.intellectualsites.web.util.*;
import com.intellectualsites.web.views.*;
import lombok.NonNull;
import org.apache.commons.io.output.TeeOutputStream;
import sun.misc.Signal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.*;

import static com.intellectualsites.web.logging.LogModes.*;

public class Server extends Thread implements IntellectualServer {

    /**
     * Running in silent mode means that the server
     * wont output anything, anywhere - not recommended
     * for obvious reasons
     */
    boolean silent = false;

    private boolean pause = false;

    /**
     * This is the logging prefix
     */
    public static final String PREFIX = "Web";

    /**
     * This is the configuration file
     * used for translations of logging messages
     */
    public ConfigurationFile translations;

    /**
     * Set to true to stop the server
     */
    private boolean stopping;

    /**
     * Whether or not to cache view responses
     */
    public boolean enableCaching;

    /**
     * All the syntaxtes used by the Crush Engine
     */
    Set<Syntax> syntaxes;

    /**
     * The core folder for the server
     */
    public File coreFolder;

    /**
     * The thread handing user input
     */
    private InputThread inputThread;

    /**
     * Verbose ouputs?
     */
    public final boolean verbose;

    /**
     * The cache manager
     */
    public volatile CacheManager cacheManager;
    final Queue<Socket> queue = new LinkedList<>();
    //
    // protected
    //
    RequestManager requestManager;

    //
    // protected final
    //
    final Collection<ProviderFactory> providers;

    //
    // private
    //
    private boolean started;
    private boolean ipv4;
    private boolean mysqlEnabled;
    private ServerSocket socket;
    SessionManager sessionManager;
    private String hostName;
    int bufferIn, bufferOut;
    private ConfigurationFile configViews;
    private MySQLConnManager mysqlConnManager;
    private EventCaller eventCaller;
    private PluginLoader pluginLoader;
    private static Server instance;
    private final boolean standalone;
    private final int port;
    private final Map<String, Class<? extends View>> viewBindings;
    public final LogWrapper logWrapper;
    private final AccountManager globalAccountManager;
    private ApplicationStructure mainApplicationStructure;
    private Worker[] workerThreads;

    {
        viewBindings = new HashMap<>();
        providers = new ArrayList<>();
    }

    @Override
    public boolean isMysqlEnabled() {
        return this.mysqlEnabled;
    }

    /**
     * @param standalone Whether or not the server should run as a standalone application,
     *                   or as an integrated application
     * @param coreFolder The main folder (in which configuration files and alike are stored)
     * @param logWrapper The log implementation
     * @throws IntellectualServerInitializationException If anything was to fail
     */
    protected Server(boolean standalone, @NonNull File coreFolder, @NonNull final LogWrapper logWrapper)
            throws IntellectualServerInitializationException {
        instance = this;

        Assert.notNull(coreFolder, logWrapper);

        { // This is due to the licensing nature of the code
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

        coreFolder = new File(coreFolder, ".iserver"); // Makes everything more portable
        if (!coreFolder.exists()) {
            if (!coreFolder.mkdirs()) {
                throw new IntellectualServerInitializationException("Failed to create the core folder: " + coreFolder);
            }
        }

        this.coreFolder = coreFolder;
        this.logWrapper = logWrapper;
        this.standalone = standalone;

        // This adds the default view bindings
        addViewBinding("html", HTMLView.class);
        addViewBinding("css", CSSView.class);
        addViewBinding("javascript", JSView.class);
        addViewBinding("less", LessView.class);
        addViewBinding("img", ImgView.class);
        addViewBinding("download", DownloadView.class);
        addViewBinding("redirect", RedirectView.class);
        addViewBinding("std", StandardView.class);

        if (standalone) {
            // Makes the application closable in ze terminal
            Signal.handle(new Signal("INT"), signal -> {
                if (signal.toString().equals("SIGINT")) {
                    stopServer();
                }
            });
            // Handles incoming commands
            // TODO: Replace the command system
            (inputThread = new InputThread(this)).start();
        }

        final File logFolder = new File(coreFolder, "log");
        if (!logFolder.exists()) {
            if (!logFolder.mkdirs()) {
                log(Message.COULD_NOT_CREATE_FOLDER, "log");
            }
        }
        try {
            FileUtils.addToZip(new File(logFolder, "old.zip"),
                    logFolder.listFiles((dir, name) -> name.endsWith(".txt")), true);
            System.setOut(new PrintStream(new TeeOutputStream(System.out,
                    new FileOutputStream(new File(logFolder, TimeUtil.getTimeStamp(TimeUtil.LogFileFormat) + ".txt")))));
        } catch (final Exception e) {
            e.printStackTrace();
        }

        try {
            this.translations = new YamlConfiguration("translations",
                    new File(new File(coreFolder, "config"), "translations.yml"));
            this.translations.loadFile();
            for (final Message message : Message.values()) {
                final String nameSpace;
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
            log(Message.CANNOT_LOAD_TRANSLATIONS);
            e.printStackTrace();
        }

        ConfigurationFactory.load(CoreConfig.class, new File(coreFolder, "config")).get();

        if (CoreConfig.debug) {
            log(Message.DEBUG);
        }

        this.port = CoreConfig.port;
        this.hostName = CoreConfig.hostname;
        this.bufferIn = CoreConfig.Buffer.in;
        this.bufferOut = CoreConfig.Buffer.out;
        this.ipv4 = CoreConfig.ipv4;
        this.verbose = CoreConfig.verbose;
        this.enableCaching = CoreConfig.Cache.enabled;
        this.mysqlEnabled = CoreConfig.MySQL.enabled;
        String mainApplication = CoreConfig.Application.main;

        this.workerThreads = LambdaUtil.arrayAssign(new Worker[CoreConfig.workers], Worker::new);

        this.started = false;
        this.stopping = false;

        this.requestManager = new RequestManager();
        this.sessionManager = new SessionManager(this);
        this.cacheManager = new CacheManager();

        if (mysqlEnabled) {
            this.mysqlConnManager = new MySQLConnManager();
        }

        if (!CoreConfig.disableViews) {
            try {
                configViews = new YamlConfiguration("views", new File(new File(coreFolder, "config"), "views.yml"));
                configViews.loadFile();
                // These are the default views
                Map<String, Object> views = new HashMap<>();
                // HTML View
                Map<String, Object> view = new HashMap<>();
                view.put("filter", "(\\/?[A-Za-z]*)\\/([A-Za-z0-9]*)\\.?([A-Za-z]?)");
                view.put("type", "std");
                Map<String, Object> opts = new HashMap<>();
                opts.put("folder", "./public");
                view.put("options", opts);
                views.put("std", view);
                configViews.setIfNotExists("views", views);
                configViews.saveFile();
            } catch (final Exception e) {
                throw new RuntimeException("Couldn't load in views");
            }
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

        ApplicationStructure applicationStructure = new ApplicationStructure("core");
        this.globalAccountManager = applicationStructure.getAccountManager();
        this.globalAccountManager.load();
        this.inputThread.commands.put("account", new AccountCommand(applicationStructure));
        this.providers.add(this.globalAccountManager);

        if (!mainApplication.isEmpty()) {
            try {
                Class temp = Class.forName(mainApplication);
                if (temp.getSuperclass().equals(ApplicationStructure.class)) {
                    this.mainApplicationStructure = (ApplicationStructure) temp.newInstance();
                } else {
                    log(Message.APPLICATION_DOES_NOT_EXTEND, mainApplication);
                }
            } catch (ClassNotFoundException e) {
                log(Message.APPLICATION_CANNOT_FIND, mainApplication);
                e.printStackTrace();
            } catch (InstantiationException | IllegalAccessException e) {
                log(Message.APPLICATION_CANNOT_INITIATE, mainApplication);
                e.printStackTrace();
            }
        }
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
    public void addViewBinding(final String key, final Class<? extends View> c) {
        Assert.notNull(c);
        Assert.notEmpty(key);
        viewBindings.put(key, c);
    }

    @Override
    public AccountManager getAccountManager() {
        return this.globalAccountManager;
    }

    @Override
    public void validateViews() {
        final List<String> toRemove = new ArrayList<>();
        for (final Map.Entry<String, Class<? extends View>> e : viewBindings.entrySet()) {
            final Class<? extends View> vc = e.getValue();
            try {
                vc.getDeclaredConstructor(String.class, Map.class);
            } catch (final Exception ex) {
                log(Message.INVALID_VIEW, e.getKey());
                toRemove.add(e.getKey());
            }
        }
        toRemove.forEach(viewBindings::remove);
    }

    @Override
    public void handleEvent(@NonNull final Event event) {
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
    public void setEventCaller(@NonNull final EventCaller caller) {
        Assert.notNull(caller);
        this.eventCaller = caller;
    }

    @Override
    public void addProviderFactory(@NonNull final ProviderFactory factory) {
        Assert.notNull(factory);
        this.providers.add(factory);
    }

    @Override
    public void loadPlugins() {
        if (standalone) {
            final File file = new File(coreFolder, "plugins");
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
            log(Message.STANDALONE_NOT_LOADING_PLUGINS);
        }
    }

    @SuppressWarnings("ALL")
    @Override
    public void start() {
        try {
            Assert.equals(this.started, false,
                    new IntellectualServerStartException("Cannot start the server, it is already started",
                    new RuntimeException("Cannot restart server singleton")));
        } catch (IntellectualServerStartException e) {
            e.printStackTrace();
            return;
        }

        // Load Plugins
        this.loadPlugins();
        EventManager.getInstance().bake();

        this.log(Message.CALLING_EVENT, "startup");
        this.handleEvent(new StartupEvent(this));

        if (mysqlEnabled) {
            this.log(Message.MYSQL_INIT);
            this.mysqlConnManager.init();
        }

        // Validating views
        this.log(Message.VALIDATING_VIEWS);
        this.validateViews();

        if (!CoreConfig.disableViews) {
            this.log(Message.LOADING_VIEWS);
            final Map<String, Map<String, Object>> views = configViews.get("views");
            Assert.notNull(views);
            views.entrySet().forEach(entry -> {
                final Map<String, Object> view = entry.getValue();
                String type = "html", filter = view.get("filter").toString();
                if (view.containsKey("type")) {
                    type = view.get("type").toString();
                }
                final Map<String, Object> options;
                if (view.containsKey("options")) {
                    options = (HashMap<String, Object>) view.get("options");
                } else {
                    options = new HashMap<>();
                }

                if (viewBindings.containsKey(type.toLowerCase())) {
                    final Class<? extends View> vc = viewBindings.get(type.toLowerCase());
                    try {
                        final View vv = vc.getDeclaredConstructor(String.class, Map.class).newInstance(filter, options);
                        requestManager.add(vv);
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Message.VIEWS_DISABLED.log();
        }

        if (mainApplicationStructure != null) {
            mainApplicationStructure.registerViews(this);
        }

        requestManager.dump(this);

        if (this.ipv4) {
            log("ipv4 is true - Using IPv4 stack");
            System.setProperty("java.net.preferIPv4Stack", "true");
        }
        if (!this.enableCaching) {
            log(Message.CACHING_DISABLED);
        } else {
            log(Message.CACHING_ENABLED);
        }

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
                } catch (final Exception ex) {
                    continue;
                }
            }
        }

        // Start the workers
        LambdaUtil.arrayForeach(workerThreads, Worker::start);

        log(Message.ACCEPTING_CONNECTIONS_ON, hostName + (this.port == 80 ? "" : ":" + port) + "/'");
        log(Message.OUTPUT_BUFFER_INFO, bufferOut / 1024, bufferIn / 1024);

        // Pre-Steps
        {
            if (globalAccountManager.getAccount(new Object[]{null, "admin", null}) == null) {
                log("There is no admin account as of yet. So, well, let's create one! Please enter a password!");
                pause = true;
                EventManager.getInstance()
                        .addListener(new com.intellectualsites.web.events.EventListener<InputThread.TextEvent>() {
                    @Override
                    public void listen(InputThread.TextEvent event) {
                        String password = event.getText();
                        try {
                            globalAccountManager.createAccount(new Account(globalAccountManager.getNextId(), "admin", password.getBytes()));
                            log("Great, you've got yourself an admin account. The username is \"admin\".");
                            pause = false;
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                });
                EventManager.getInstance().bake();
            }
        }

        this.handleEvent(new ServerReadyEvent(this));

        // Main Loop
        for (; ; ) {
            if (this.stopping) {
                log(Message.SHUTTING_DOWN);
                break;
            }
            if (pause) {
                continue;
            }
            try {
                tick();
            } catch (final Exception e) {
                log(Message.TICK_ERROR);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void tick() {
        // Recently remade to use worker threads, rather than
        // creating a new thread for each request - this saves time
        // and makes sure to use all resources to their fullest potential
        try {
            final Socket s = socket.accept();
            queue.add(s);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void log(Message message, final Object... args) {
        this.log(message.toString(), message.getMode(), args);
    }

    @Override
    public synchronized void log(String message, int mode, final Object... args) {
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
        logWrapper.log(PREFIX, prefix, TimeUtil.getTimeStamp(), message, Thread.currentThread().getName());
        // logWrapper.log(String.format("[%s][%s][%s] %s", PREFIX, prefix, TimeUtil.getTimeStamp(), message));
        // System.out.printf("[%s][%s][%s] %s%s", PREFIX, prefix, TimeUtil.getTimeStamp(), message, System.lineSeparator());
    }

    @Override
    public synchronized void log(String message, final Object... args) {
        this.log(message, MODE_INFO, args);
    }

    @Override
    public void log(@NonNull final LogProvider provider, @NonNull String message, final Object... args) {
        for (final Object a : args) {
            message = message.replaceFirst("%s", a.toString());
        }
        logWrapper.log(PREFIX, provider.getLogIdentifier(), TimeUtil.getTimeStamp(), message, Thread.currentThread().getName());
        // void log(String prefix, String prefix1, String timeStamp, String message);
        // logWrapper.log(String.format("[%s][%s] %s", provider.getLogIdentifier(), TimeUtil.getTimeStamp(), message));
        // System.out.printf("[%s][%s] %s\n", provider.getLogIdentifier(), TimeUtil.getTimeStamp(), message);
    }

    @Override
    public synchronized void stopServer() {
        log(Message.SHUTTING_DOWN);

        EventManager.getInstance().handle(new ShutdownEvent(this));

        // Close all database connections
        SQLiteManager.sessions.forEach(SQLiteManager::close);

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
    public RequestManager getRequestManager() {
        return requestManager;
    }
}
