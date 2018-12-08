/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2018 Alexander Söderberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.kvantum.server.implementation;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.configurable.ConfigurationFactory;
import io.netty.util.internal.logging.InternalLoggerFactory;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Synchronized;
import xyz.kvantum.files.FileSystem;
import xyz.kvantum.files.FileWatcher;
import xyz.kvantum.server.api.account.IAccount;
import xyz.kvantum.server.api.account.IAccountManager;
import xyz.kvantum.server.api.cache.ICacheManager;
import xyz.kvantum.server.api.config.ConfigVariableProvider;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.config.ITranslationManager;
import xyz.kvantum.server.api.config.Message;
import xyz.kvantum.server.api.core.Kvantum;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.core.WorkerProcedure;
import xyz.kvantum.server.api.event.EventBus;
import xyz.kvantum.server.api.event.Listener;
import xyz.kvantum.server.api.event.SimpleEventBus;
import xyz.kvantum.server.api.events.ConnectionEstablishedEvent;
import xyz.kvantum.server.api.events.ServerShutdownEvent;
import xyz.kvantum.server.api.events.ServerStartedEvent;
import xyz.kvantum.server.api.fileupload.KvantumFileUpload;
import xyz.kvantum.server.api.logging.*;
import xyz.kvantum.server.api.matching.Router;
import xyz.kvantum.server.api.memguard.MemoryGuard;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.request.PostProviderFactory;
import xyz.kvantum.server.api.response.Response;
import xyz.kvantum.server.api.session.ISessionDatabase;
import xyz.kvantum.server.api.session.SessionManager;
import xyz.kvantum.server.api.templates.TemplateManager;
import xyz.kvantum.server.api.util.AutoCloseable;
import xyz.kvantum.server.api.util.*;
import xyz.kvantum.server.api.views.RequestHandler;
import xyz.kvantum.server.api.views.requesthandler.SimpleRequestHandler;
import xyz.kvantum.server.implementation.commands.AccountCommand;
import xyz.kvantum.server.implementation.config.TranslationFile;
import xyz.kvantum.server.implementation.error.KvantumException;
import xyz.kvantum.server.implementation.error.KvantumInitializationException;
import xyz.kvantum.server.implementation.error.KvantumStartException;
import xyz.kvantum.server.implementation.mongo.MongoAccountManager;
import xyz.kvantum.server.implementation.mongo.MongoSessionDatabase;
import xyz.kvantum.server.implementation.mysql.MySQLAccountManager;
import xyz.kvantum.server.implementation.mysql.MySQLSessionDatabase;
import xyz.kvantum.server.implementation.netty.NettyLoggerFactory;
import xyz.kvantum.server.implementation.sqlite.SQLiteAccountManager;
import xyz.kvantum.server.implementation.sqlite.SQLiteSessionDatabase;
import xyz.kvantum.server.implementation.tempfiles.TempFileManagerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main {@link Kvantum} implementation. Extended by {@link StandaloneServer}
 */
public class SimpleServer implements Kvantum {

    private static final Pattern LOG_ARG_PATTERN = Pattern.compile("\\{(?<num>([0-9])+)}");

    static ObjectPool<GzipHandler> gzipHandlerPool;
    static ObjectPool<Md5Handler> md5HandlerPool;

    //region Instance fields
    @Getter private final WorkerProcedure procedure = new WorkerProcedure();
    @Getter private final ITempFileManagerFactory tempFileManagerFactory =
        new TempFileManagerFactory();
    @Getter private final KvantumFileUpload globalFileUpload = new KvantumFileUpload();
    @Getter private final Metrics metrics = new Metrics();
    @Getter private final Gson gson =
        new GsonBuilder().registerTypeAdapter(Account.class, new AccountSerializer()).create();
    private final ServerContext serverContext;
    @Getter protected ICacheManager cacheManager;
    PrintStream logStream;
    @Getter private InputThread inputThread;
    @Getter private boolean silent = false;
    @Getter private ExecutorService executorService;
    @Getter private SessionManager sessionManager;
    @Getter private boolean paused = false;
    @Getter private boolean stopping;
    @Getter private boolean started;
    private HTTPThread httpThread;
    private HTTPSThread httpsThread;
    @Getter private FileSystem fileSystem;
    @Getter private FileWatcher fileWatcher;
    @Getter private CommandManager commandManager;
    @Getter private ApplicationStructure applicationStructure;
    @Getter private ITranslationManager translationManager;
    @Getter private EventBus eventBus;
    //endregion

    /**
     * @param serverContext ServerContext that will be used to initialize the server
     */
    @SneakyThrows @SuppressWarnings("WeakerAccess") public SimpleServer(
        @NonNull final ServerContext serverContext) {
        this.serverContext = serverContext;

        //
        // Setup singleton references
        //
        ServerImplementation.registerServerImplementation(this);

        //
        // Setup and initialize the file system
        //
        if (!serverContext.getCoreFolder().exists() && !serverContext.getCoreFolder().mkdirs()) {
            throw new KvantumInitializationException(
                "Failed to create the core folder: " + getCoreFolder());
        }

        //
        // Initialize the executor service
        //
        this.executorService = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder().setDaemon(false).setNameFormat("kvantum-%d").build());

        //
        // Watches for file updates and invalidates cache
        //
        this.fileWatcher = new FileWatcher();

        //
        // Virtual filesystem
        //
        this.fileSystem = new IntellectualFileSystem(serverContext.getCoreFolder().toPath());

        //
        // Log->File
        //
        final File logFolder = FileUtils.attemptFolderCreation(new File(getCoreFolder(), "log"));

        //
        // Compress old zip files
        //
        try {
            FileUtils.addToZip(new File(logFolder, "old.zip"), logFolder
                .listFiles((dir, name) -> name.endsWith(".log") && !name.contains("access")));
        } catch (final Exception e) {
            e.printStackTrace();
        }

        //
        // Setup the translation configuration file
        //
        try {
            translationManager = new TranslationFile(new File(getCoreFolder(), "config"));
        } catch (final Exception e) {
            log(Message.CANNOT_LOAD_TRANSLATIONS);
            e.printStackTrace();
        }

        //
        // Setup the log-to-file system and the error stream
        //
        try {
            this.logStream = new LogStream(logFolder);
            System.setErr(
                new PrintStream(new ErrorOutputStream(serverContext.getLogWrapper()), true));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //
        // Replace the netty logger
        //
        InternalLoggerFactory.setDefaultFactory(new NettyLoggerFactory());

        //
        // Setup default flags
        //
        this.started = false;
        this.stopping = false;

        //
        // Setup memory guard
        //
        Logger.info("Starting memory guard!");
        MemoryGuard.getInstance().start();

        //
        // Setup the cache manager
        //
        this.cacheManager = new CacheManager();

        //
        // Load the configuration file
        //
        if (!CoreConfig.isPreConfigured()) {
            ConfigurationFactory.load(CoreConfig.class, new File(getCoreFolder(), "config")).get();
        }

        //
        // Setup the internal application
        //
        if (serverContext.isStandalone()) {
            // Makes the application closable in the terminal
            // Removed post Java 8: Signal.handle( new Signal( "INT" ), new ExitSignalHandler() );
            final Thread exitThread = new Thread(() -> {
                ServerImplementation.getImplementation().stopServer();
            });
            exitThread.setName("Shutdown Handler");
            exitThread.setDaemon(true);
            Runtime.getRuntime().addShutdownHook(exitThread);
            this.commandManager = new CommandManager('/');
            this.commandManager.getManagerOptions().getFindCloseMatches(false);
            this.commandManager.getManagerOptions().setRequirePrefix(false);

            switch (CoreConfig.Application.databaseImplementation) {
                case "sqlite":
                    applicationStructure = new SQLiteApplicationStructure("core") {
                        @Override public IAccountManager createNewAccountManager() {
                            return new SQLiteAccountManager(this);
                        }
                    };
                    break;
                case "mongo":
                    applicationStructure = new MongoApplicationStructure("core") {
                        @Override public IAccountManager createNewAccountManager() {
                            return new MongoAccountManager(this);
                        }
                    };
                    break;
                case "mysql":
                    applicationStructure = new MySQLApplicationStructure("core") {
                        @Override public IAccountManager createNewAccountManager() {
                            return new MySQLAccountManager(this);
                        }
                    };
                    break;
                default:
                    Message.DATABASE_UNKNOWN.log(CoreConfig.Application.databaseImplementation);
                    throw new KvantumInitializationException(
                        "Cannot load - Invalid session database " + "implementation provided");
            }
        } else if (!CoreConfig.Application.main.isEmpty()) {
            try {
                Class temp = Class.forName(CoreConfig.Application.main);
                if (temp.getSuperclass().equals(ApplicationStructure.class)) {
                    this.applicationStructure = (ApplicationStructure) temp.newInstance();
                } else {
                    log(Message.APPLICATION_DOES_NOT_EXTEND, CoreConfig.Application.main);
                }
            } catch (ClassNotFoundException e) {
                log(Message.APPLICATION_CANNOT_FIND, CoreConfig.Application.main);
                e.printStackTrace();
            } catch (InstantiationException | IllegalAccessException e) {
                log(Message.APPLICATION_CANNOT_INITIATE, CoreConfig.Application.main);
                e.printStackTrace();
            }
        }

        try {
            this.getApplicationStructure().getAccountManager().setup();
            if (this.getCommandManager() != null) {
                getCommandManager().createCommand(new AccountCommand(applicationStructure));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //
        // Load the session database
        //
        final ISessionDatabase sessionDatabase;
        if (CoreConfig.Sessions.enableDb) {
            switch (CoreConfig.Application.databaseImplementation.toLowerCase(Locale.ENGLISH)) {
                case "sqlite":
                    sessionDatabase = new SQLiteSessionDatabase(
                        (SQLiteApplicationStructure) this.getApplicationStructure()
                            .getAccountManager().getApplicationStructure());
                    break;
                case "mongo":
                    sessionDatabase = new MongoSessionDatabase(
                        (MongoApplicationStructure) this.getApplicationStructure()
                            .getAccountManager().getApplicationStructure());
                    break;
                case "mysql":
                    sessionDatabase = new MySQLSessionDatabase(
                        (MySQLApplicationStructure) this.getApplicationStructure()
                            .getAccountManager().getApplicationStructure());
                    break;
                default:
                    Message.DATABASE_SESSION_UNKNOWN
                        .log(CoreConfig.Application.databaseImplementation);
                    sessionDatabase = new DumbSessionDatabase();
                    break;
            }
            try {
                sessionDatabase.setup();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            sessionDatabase = new DumbSessionDatabase();
        }

        //
        // Setup the session manager implementation
        //
        this.sessionManager = new SessionManager(new SessionFactory(), sessionDatabase);

        //
        // Setup causam (EventBus)
        //
        this.eventBus = new SimpleEventBus();

        //
        // Initialize access.log logger
        //
        Logger.info("Creating access.log handler...");
        this.eventBus.registerListeners(new AccessLogStream(logFolder));

        //
        // Register connection denier
        //
        if (CoreConfig.debug) {
            this.getEventBus().registerListeners(this);
        }

        //
        // Setup the connection throttler
        //
        ConnectionThrottle.initialize();
    }

    @Override public final File getCoreFolder() {
        return this.serverContext.getCoreFolder();
    }

    @Override public final LogWrapper getLogWrapper() {
        return this.serverContext.getLogWrapper();
    }

    @Override public final boolean isStandalone() {
        return this.serverContext.isStandalone();
    }

    @Override public final Router getRouter() {
        return this.serverContext.getRouter();
    }

    protected void onStart() {
    }

    @SuppressWarnings("ALL") @Override @Synchronized public final boolean start() {
        if (CoreConfig.gzip) {
            gzipHandlerPool = new ObjectPool<>(CoreConfig.Pools.gzipHandlers, GzipHandler::new);
        }
        md5HandlerPool = new ObjectPool<>(CoreConfig.Pools.md5Handlers, Md5Handler::new);

        try {
            Assert.equals(this.started, false,
                new KvantumStartException("Cannot start the server, it is already started",
                    new KvantumException("Cannot restart server singleton")));
        } catch (KvantumStartException e) {
            e.printStackTrace();
            return false;
        }

        TemplateManager.get()
            .addProviderFactory(ServerImplementation.getImplementation().getSessionManager());
        TemplateManager.get().addProviderFactory(ConfigVariableProvider.getInstance());
        TemplateManager.get().addProviderFactory(new PostProviderFactory());
        TemplateManager.get().addProviderFactory(new MetaProvider());

        this.onStart();

        this.applicationStructure.registerViews(this);

        if (CoreConfig.debug) {
            getRouter().dump(this);
            log("");
        }

        log(Message.STARTING_ON_PORT, CoreConfig.port);

        if (isStandalone() && CoreConfig.enableInputThread) {
            this.inputThread = new InputThread();
            this.inputThread.start();
        }

        this.started = true;

        log("");

        final NioClassResolver classResolver = new NioClassResolver();

        if (CoreConfig.SSL.enable) {
            log(Message.STARTING_SSL_ON_PORT, CoreConfig.SSL.port);
            try {
                System.setProperty("javax.net.ssl.keyStore", CoreConfig.SSL.keyStore);
                System
                    .setProperty("javax.net.ssl.keyStorePassword", CoreConfig.SSL.keyStorePassword);

                this.httpsThread = new HTTPSThread(classResolver);
                this.httpsThread.start();
            } catch (final Exception e) {
                new KvantumException("Failed to start HTTPS server", e).printStackTrace();
            }
        }

        log(Message.ACCEPTING_CONNECTIONS_ON,
            CoreConfig.webAddress + (CoreConfig.port == 80 ? "" : ":" + CoreConfig.port) + "/'");
        log(Message.OUTPUT_BUFFER_INFO, CoreConfig.Buffer.out / 1024, CoreConfig.Buffer.in / 1024);

        try {
            this.httpThread = new HTTPThread(new ServerSocketFactory(), classResolver);
        } catch (KvantumInitializationException e) {
            Message.SERVER_START_FAILED.log();
            ServerImplementation.getImplementation().stopServer();
            return false;
        }
        this.httpThread.start();
        this.getEventBus().throwEvent(new ServerStartedEvent(this), false);
        return true;
    }

    @Override public final void log(final Message message, final Object... args) {
        this.log(message.toString(), message.getMode(), args);
    }

    @Override public final void log(final String message, final int mode, final Object... args) {
        // This allows us to customize what messages are
        // sent to the logging screen, and thus we're able
        // to limit to only error messages or such
        if ((mode == LogModes.MODE_DEBUG && !CoreConfig.debug) || mode < LogModes.lowestLevel
            || mode > LogModes.highestLevel) {
            return;
        }
        String prefix;
        switch (mode) {
            case LogModes.MODE_DEBUG:
                prefix = "Debug";
                break;
            case LogModes.MODE_INFO:
                prefix = "Info";
                break;
            case LogModes.MODE_ERROR:
                prefix = "Error";
                break;
            case LogModes.MODE_WARNING:
                prefix = "Warning";
                break;
            case LogModes.MODE_ACCESS:
                prefix = "Access";
                break;
            default:
                prefix = "Info";
                break;
        }

        this.log(prefix, message, args);
    }

    /**
     * Replaces string arguments using the pattern {num} from an array of objects, starting from index 0, as such: 0
     * &le; num &lt; args.length. If num &ge; args.length, then the pattern will be replaced by an empty string. An
     * argument can also be passed as "{}", in which case the number will be implied.
     *
     * @param prefix  Log prefix
     * @param message String to be replaced. Cannot be null.
     * @param args    Replacements
     */
    @Synchronized private void log(@NonNull final String prefix, @NonNull final String message,
        final Object... args) {
        String msg = message;

        final Matcher matcher = LOG_ARG_PATTERN.matcher(msg);

        int index = 0;
        boolean containsEmptyBrackets;
        while ((containsEmptyBrackets = msg.contains("{}")) || matcher.find()) {
            final int argumentNum;
            final String toReplace;
            if (containsEmptyBrackets) {
                argumentNum = index++;
                toReplace = "{}";
            } else {
                toReplace = matcher.group();
                argumentNum = Integer.parseInt(matcher.group("num"));
            }
            if (argumentNum < args.length) {
                final Object object = args[argumentNum];
                String objectString;
                if (object == null) {
                    objectString = "null";
                } else if (object instanceof LogFormatted) {
                    objectString = ((LogFormatted) object).getLogFormatted();
                } else {
                    objectString = object.toString();
                }
                msg = msg.replace(toReplace, objectString);
            } else {
                msg = msg.replace(toReplace, "");
            }
        }
        getLogWrapper().log(
            LogContext.builder().applicationPrefix(CoreConfig.logPrefix).logPrefix(prefix)
                .timeStamp(TimeUtil.getTimeStamp()).message(msg)
                .thread(Thread.currentThread().getName()).build());
    }

    @Override public final void log(final String message, final Object... args) {
        this.log(message, LogModes.MODE_INFO, args);
    }

    @Override
    public final void log(final LogProvider provider, final String message, final Object... args) {
        this.log(provider.getLogIdentifier(), message, args);
    }

    @Synchronized @Override public void stopServer() {
        Message.SHUTTING_DOWN.log();

        //
        // Emit the shut down event
        //
        if (getEventBus() != null) {
            getEventBus().throwEvent(new ServerShutdownEvent(this), false);
        }

        //
        // Save all stored account states on shutdown
        //
        this.cacheManager.getAllStoredAccounts().forEach(IAccount::saveState);

        //
        // Gracefully shutdown the file watcher
        //
        this.fileWatcher.getStopSignal().stop();

        //
        // Shutdown the netty servers
        //
        try {
            httpThread.close();
            if (CoreConfig.SSL.enable) {
                httpsThread.close();
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }

        //
        // Shutdown utilities
        //
        AutoCloseable.closeAll();

        //
        // Close the log stream
        //
        try {
            this.logStream.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        if (isStandalone() && CoreConfig.exitOnStop) {
            Logger.info("Shutting down the JVM.");

            //
            // Find all threads that are currently running
            //
            boolean destroyThreadExists = false;

            final Set<Thread> threads = Thread.getAllStackTraces().keySet();
            for (final Thread thread : threads) {
                Logger.info("Running thread found: \"{0}\" daemon: {1}", thread.getName(),
                    thread.isDaemon());
                if (thread.getName().equalsIgnoreCase("DestroyJavaVM")) {
                    destroyThreadExists = true;
                }
            }

            if (!destroyThreadExists) {
                System.exit(0);
            }
        } else {
            Logger.info("Not set to shutdown on exit. Waiting for parent application to close.");
        }
    }

    @Override public final RequestHandler createSimpleRequestHandler(@NonNull final String filter,
        @NonNull final BiConsumer<AbstractRequest, Response> generator) {
        return SimpleRequestHandler.builder().pattern(filter).generator(generator).build()
            .addToRouter(getRouter());
    }

    @Listener @SuppressWarnings("unused")
    private void listenForConnections(@NonNull final ConnectionEstablishedEvent establishedEvent) {
        Logger.debug("Checking for external connection {}", establishedEvent.getIp());
        boolean shouldCancel = true;
        final InetAddress address;
        try {
            address = InetAddress.getByName(establishedEvent.getIp());
        } catch (final UnknownHostException e) {
            Logger.error("Failed to get InetAddress... ");
            e.printStackTrace();
            return;
        }
        if (address.isAnyLocalAddress() || address.isLoopbackAddress()) {
            shouldCancel = false;
        } else {
            try {
                shouldCancel = NetworkInterface.getByInetAddress(address) == null;
            } catch (final Exception ignored) {
            }
        }
        if (shouldCancel) {
            Logger.debug("Cancelling connection because it isn't local...");
            establishedEvent.setCancelled(true);
        }
    }

}
