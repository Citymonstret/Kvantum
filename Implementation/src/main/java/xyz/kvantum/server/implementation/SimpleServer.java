/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2019 Alexander Söderberg
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

import com.github.sauilitired.loggbok.ColorLogger;
import com.github.sauilitired.loggbok.ColorStripper;
import com.github.sauilitired.loggbok.ErrorDigest;
import com.github.sauilitired.loggbok.FileLogger;
import com.github.sauilitired.loggbok.LevelSplitLogger;
import com.github.sauilitired.loggbok.LogFormatter;
import com.github.sauilitired.loggbok.LogLevels;
import com.github.sauilitired.loggbok.Logger;
import com.github.sauilitired.loggbok.PositionFormatter;
import com.github.sauilitired.loggbok.PrintStreamLogger;
import com.github.sauilitired.loggbok.SimpleLogger;
import com.github.sauilitired.loggbok.SplitLogger;
import com.github.sauilitired.loggbok.ThreadedQueueLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.configurable.ConfigurationFactory;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.logging.InternalLoggerFactory;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.Synchronized;
import xyz.kvantum.files.FileSystem;
import xyz.kvantum.files.FileWatcher;
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
import xyz.kvantum.server.api.logging.LogProvider;
import xyz.kvantum.server.api.matching.Router;
import xyz.kvantum.server.api.memguard.MemoryGuard;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.request.PostProviderFactory;
import xyz.kvantum.server.api.response.Response;
import xyz.kvantum.server.api.session.ISessionDatabase;
import xyz.kvantum.server.api.session.SessionManager;
import xyz.kvantum.server.api.templates.TemplateManager;
import xyz.kvantum.server.api.util.ApplicationStructure;
import xyz.kvantum.server.api.util.Assert;
import xyz.kvantum.server.api.util.AutoCloseable;
import xyz.kvantum.server.api.util.FileUtils;
import xyz.kvantum.server.api.util.ITempFileManagerFactory;
import xyz.kvantum.server.api.util.MetaProvider;
import xyz.kvantum.server.api.util.Metrics;
import xyz.kvantum.server.api.util.TimeUtil;
import xyz.kvantum.server.api.views.RequestHandler;
import xyz.kvantum.server.api.views.requesthandler.SimpleRequestHandler;
import xyz.kvantum.server.implementation.config.TranslationFile;
import xyz.kvantum.server.implementation.error.KvantumException;
import xyz.kvantum.server.implementation.error.KvantumInitializationException;
import xyz.kvantum.server.implementation.error.KvantumStartException;
import xyz.kvantum.server.implementation.mongo.MongoSessionDatabase;
import xyz.kvantum.server.implementation.mysql.MySQLSessionDatabase;
import xyz.kvantum.server.implementation.netty.NettyLoggerFactory;
import xyz.kvantum.server.implementation.sqlite.SQLiteSessionDatabase;
import xyz.kvantum.server.implementation.tempfiles.TempFileManagerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

/**
 * Main {@link Kvantum} implementation.
 */
public class SimpleServer implements Kvantum {

    static ObjectPool<GzipHandler> gzipHandlerPool;
    // static ObjectPool<Md5Handler> md5HandlerPool;

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
    AccessLogStream accessLogStream;
    @Getter private InputThread inputThread;
    @Getter private boolean silent = false;
    @Getter private ExecutorService executorService;
    @Getter private SessionManager sessionManager;
    @Getter private boolean paused = false;
    @Getter private boolean stopping;
    @Getter private boolean started;
    @Getter private boolean stopped;
    private HTTPThread httpThread;
    private HTTPSThread httpsThread;
    @Getter private FileSystem fileSystem;
    @Getter private FileWatcher fileWatcher;
    @Getter private CommandManager commandManager;
    @Getter private ApplicationStructure applicationStructure;
    @Getter private ITranslationManager translationManager;
    @Getter private EventBus eventBus;
    @Getter private Logger logger;
    @Getter private ErrorDigest errorDigest;
    //endregion

    /**
     * @param serverContext ServerContext that will be used to initialize the server
     */
    @SneakyThrows public SimpleServer(final ServerContext serverContext) {
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
            new DefaultThreadFactory("kvantum-pool"));

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
            e.printStackTrace(); // Can't use ErrorDigest
        }

        //
        // Setup the translation configuration file
        //
        try {
            translationManager = new TranslationFile(new File(getCoreFolder(), "config"));
        } catch (final Exception e) {
            log(Message.CANNOT_LOAD_TRANSLATIONS);
            e.printStackTrace(); // Can't use ErrorDigest
        }

        //
        // Load the configuration file
        //
        if (!CoreConfig.isPreConfigured()) {
            ConfigurationFactory.load(CoreConfig.class, new File(getCoreFolder(), "config")).get();
        }

        //
        // Setup the log-to-file system and the error stream
        //
        try {
            /*
            new FileOutputStream(new File(logFolder,
            TimeUtil.getTimeStamp(TimeUtil.logFileFormat, new Date()) + ".log")))
             */
            final Path path = new File(logFolder, TimeUtil
                .getTimeStamp(TimeUtil.logFileFormat, new Date()) + ".log").toPath();
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
            final LogLevels logLevels = new LogLevels();
            logLevels.addLevel("ACCESS"); // will have level value 0x10
            logLevels.setEnabled(LogLevels.LEVEL_DEBUG, CoreConfig.debug);
            final String format = CoreConfig.Logging.logFormat;
            // We need to do two things:
            // the first is to create a threaded queue logger
            // we then need to split the logging into two different loggers:
            // - file logging
            // - standard output logging
            final LogFormatter logFormatter = new PositionFormatter();
            final SimpleLogger defaultLogger = new PrintStreamLogger(System.out, format, logLevels);
            defaultLogger.setLogFormatter(logFormatter);
            final SimpleLogger errorLogger = new PrintStreamLogger(System.err, format, logLevels);
            errorLogger.setLogFormatter(logFormatter);
            final Logger standardLogger = new LevelSplitLogger(new ColorLogger(defaultLogger), logLevels)
                .split(LogLevels.LEVEL_ERROR, new ColorStripper(errorLogger));
            final SimpleLogger fileLogger = new FileLogger(path, format, logLevels);
            fileLogger.setLogFormatter(logFormatter);
            this.logger = new ThreadedQueueLogger(new SplitLogger(standardLogger, new ColorStripper(fileLogger)));
            this.errorDigest = new ErrorDigest(logger);
        } catch (FileNotFoundException e) {
            e.printStackTrace(); // Can't use ErrorDigest
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
        log("Starting memory guard!");
        MemoryGuard.getInstance().start();

        //
        // Setup the cache manager
        //
        this.cacheManager = new CacheManager();

        //
        // Setup the internal application
        //
        if (serverContext.isStandalone()) {
            // Makes the application closable in the terminal
            // Removed post Java 8: Signal.handle( new Signal( "INT" ), new ExitSignalHandler() );
            final Thread exitThread =
                new Thread(() -> ServerImplementation.getImplementation().stopServer());
            exitThread.setName("Shutdown Handler");
            exitThread.setDaemon(true);
            Runtime.getRuntime().addShutdownHook(exitThread);
            this.commandManager = new CommandManager('/');
            this.commandManager.getManagerOptions().getFindCloseMatches(false);
            this.commandManager.getManagerOptions().setRequirePrefix(false);

            // TODO: Find a way to NOT hard code this, as it's silly.
            switch (CoreConfig.Application.databaseImplementation) {
                case "sqlite":
                    applicationStructure = new SQLiteApplicationStructure("core");
                    break;
                case "mongo":
                    applicationStructure = new MongoApplicationStructure("core");
                    break;
                case "mysql":
                    applicationStructure = new MySQLApplicationStructure("core");
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
                ServerImplementation.getImplementation().getErrorDigest().digest(e);
            } catch (InstantiationException | IllegalAccessException e) {
                log(Message.APPLICATION_CANNOT_INITIATE, CoreConfig.Application.main);
                ServerImplementation.getImplementation().getErrorDigest().digest(e);
            }
        }

        //
        // Load the session database
        //
        final ISessionDatabase sessionDatabase;
        if (CoreConfig.Sessions.enableDb) {
            switch (CoreConfig.Application.databaseImplementation.toLowerCase(Locale.ENGLISH)) {
                case "sqlite":
                    sessionDatabase = new SQLiteSessionDatabase(
                        (SQLiteApplicationStructure) this.getApplicationStructure());
                    break;
                case "mongo":
                    sessionDatabase = new MongoSessionDatabase(
                        (MongoApplicationStructure) this.getApplicationStructure());
                    break;
                case "mysql":
                    sessionDatabase = new MySQLSessionDatabase(
                        (MySQLApplicationStructure) this.getApplicationStructure());
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
                ServerImplementation.getImplementation().getErrorDigest().digest(e);
            }
        } else {
            sessionDatabase = new DumbSessionDatabase();
        }

        //
        // Setup the session manager implementation
        //
        this.sessionManager = new SessionManager(new SessionFactory(), sessionDatabase);

        //
        // Setup (EventBus)
        //
        this.eventBus = new SimpleEventBus();

        //
        // Initialize access.log logger
        //
        log("Creating access.log handler...");
        this.eventBus.registerListeners((this.accessLogStream = new AccessLogStream(logFolder)));

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

    @Override public final boolean isStandalone() {
        return this.serverContext.isStandalone();
    }

    @Override public final Router getRouter() {
        return this.serverContext.getRouter();
    }

    protected void onStart() {
    }

    @Override @Synchronized public final boolean start() {
        if (CoreConfig.gzip) {
            gzipHandlerPool = new ObjectPool<>(CoreConfig.Pools.gzipHandlers, GzipHandler::new);
        }
        // md5HandlerPool = new ObjectPool<>(CoreConfig.Pools.md5Handlers, Md5Handler::new);

        try {
            Assert.equals(this.started, false,
                new KvantumStartException("Cannot start the server, it is already started",
                    new KvantumException("Cannot restart server singleton")));
        } catch (KvantumStartException e) {
            ServerImplementation.getImplementation().getErrorDigest().digest(e);
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
                ServerImplementation.getImplementation().getErrorDigest()
                    .digest(new KvantumException("Failed to start HTTPS server", e));
            }
        }

        log(Message.ACCEPTING_CONNECTIONS_ON,
            CoreConfig.webAddress + (CoreConfig.port == 80 ? "" : ":" + CoreConfig.port));
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

    @Override public final void log(final String message, final Object... args) {
        this.log(message, LogLevels.LEVEL_INFO, args);
    }

    @Override public final void log(final String message, final int mode, final Object... args) {
        this.logger.log(mode, message, args);
    }

    @Override
    public final void log(final LogProvider provider, final String message, final Object... args) {
        this.log(provider.getLogIdentifier(), message, args);
    }

    @Synchronized @Override public void stopServer() {
        if (isStopped()) {
            return;
        }

        Message.SHUTTING_DOWN.log();

        //
        // Emit the shut down event
        //
        if (getEventBus() != null) {
            getEventBus().throwEvent(new ServerShutdownEvent(this), false);
        }

        //
        // Gracefully shutdown the file watcher
        //
        this.fileWatcher.getStopSignal().stop();

        //
        // Shutdown the netty servers
        //
        try {
            if (httpThread != null) {
                httpThread.close();
            }
            if (CoreConfig.SSL.enable && httpsThread != null) {
                httpsThread.close();
            }
        } catch (final Exception e) {
            ServerImplementation.getImplementation().getErrorDigest().digest(e);
        }

        //
        // Shutdown utilities
        //
        AutoCloseable.closeAll();

        //
        // Close the log stream
        //
        try {
            this.logger.close();
            this.accessLogStream.close();
        } catch (final Exception e) {
            e.printStackTrace(); // Can't use ErrorDigest
        }

        if (isStandalone() && CoreConfig.exitOnStop) {
            log("Shutting down the JVM.");

            //
            // Find all threads that are currently running
            //
            boolean destroyThreadExists = false;

            final Set<Thread> threads = Thread.getAllStackTraces().keySet();
            for (final Thread thread : threads) {
                log("Running thread found: \"{0}\" daemon: {1}", thread.getName(),
                    thread.isDaemon());
                if (thread.getName().equalsIgnoreCase("DestroyJavaVM")) {
                    destroyThreadExists = true;
                }
                if (!thread.isDaemon()) {
                    for (final StackTraceElement stackTraceElement : thread.getStackTrace()) {
                        log("- {}", stackTraceElement.toString());
                    }
                }
            }

            if (!destroyThreadExists) {
                System.exit(0);
            }
        } else {
            log("Not set to shutdown on exit. Waiting for parent application to close.");
        }

        this.stopped = true;
    }

    @Override public final RequestHandler createSimpleRequestHandler(final String filter,
        final BiConsumer<AbstractRequest, Response> generator) {
        return SimpleRequestHandler.builder().pattern(filter).generator(generator).build()
            .addToRouter(getRouter());
    }

    @Listener @SuppressWarnings("unused")
    public void listenForConnections(final ConnectionEstablishedEvent establishedEvent) {
        log("Checking for external connection {}", establishedEvent.getIp());
        boolean shouldCancel = true;
        final InetAddress address;
        try {
            address = InetAddress.getByName(establishedEvent.getIp());
        } catch (final UnknownHostException e) {
            log("Failed to get InetAddress... ");
            getErrorDigest().digest(e);
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
            log("Cancelling connection because it isn't local...", LogLevels.LEVEL_DEBUG);
            establishedEvent.setCancelled(true);
        }
    }

}
