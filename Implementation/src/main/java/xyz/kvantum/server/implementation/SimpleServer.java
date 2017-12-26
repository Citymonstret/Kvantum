/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2017 IntellectualSites
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellectualsites.commands.CommandManager;
import io.netty.util.internal.logging.InternalLoggerFactory;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Synchronized;
import pw.stamina.causam.EventBus;
import pw.stamina.causam.event.EventEmitter;
import pw.stamina.causam.publish.Publisher;
import pw.stamina.causam.registry.SetBasedSubscriptionRegistry;
import pw.stamina.causam.registry.SubscriptionRegistry;
import pw.stamina.causam.select.CachingSubscriptionSelectorServiceDecorator;
import pw.stamina.causam.select.SubscriptionSelectorService;
import sun.misc.Signal;
import xyz.kvantum.crush.CrushEngine;
import xyz.kvantum.files.FileSystem;
import xyz.kvantum.files.FileWatcher;
import xyz.kvantum.server.api.account.IAccountManager;
import xyz.kvantum.server.api.cache.ICacheManager;
import xyz.kvantum.server.api.config.ConfigVariableProvider;
import xyz.kvantum.server.api.config.ConfigurationFile;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.config.Message;
import xyz.kvantum.server.api.core.Kvantum;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.core.WorkerProcedure;
import xyz.kvantum.server.api.events.ServerShutdownEvent;
import xyz.kvantum.server.api.events.ServerStartedEvent;
import xyz.kvantum.server.api.fileupload.KvantumFileUpload;
import xyz.kvantum.server.api.jtwig.JTwigEngine;
import xyz.kvantum.server.api.logging.LogContext;
import xyz.kvantum.server.api.logging.LogFormatted;
import xyz.kvantum.server.api.logging.LogModes;
import xyz.kvantum.server.api.logging.LogProvider;
import xyz.kvantum.server.api.logging.LogWrapper;
import xyz.kvantum.server.api.logging.Logger;
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
import xyz.kvantum.server.api.util.ErrorOutputStream;
import xyz.kvantum.server.api.util.FileUtils;
import xyz.kvantum.server.api.util.ITempFileManagerFactory;
import xyz.kvantum.server.api.util.MetaProvider;
import xyz.kvantum.server.api.util.TimeUtil;
import xyz.kvantum.server.api.views.RequestHandler;
import xyz.kvantum.server.api.views.requesthandler.SimpleRequestHandler;
import xyz.kvantum.server.implementation.commands.AccountCommand;
import xyz.kvantum.server.implementation.config.TranslationFile;
import xyz.kvantum.server.implementation.error.KvantumException;
import xyz.kvantum.server.implementation.error.KvantumInitializationException;
import xyz.kvantum.server.implementation.error.KvantumStartException;
import xyz.kvantum.server.implementation.mongo.MongoAccountManager;
import xyz.kvantum.server.implementation.mongo.MongoSessionDatabase;
import xyz.kvantum.server.implementation.netty.NettyLoggerFactory;
import xyz.kvantum.server.implementation.sqlite.SQLiteAccountManager;
import xyz.kvantum.server.implementation.sqlite.SQLiteSessionDatabase;
import xyz.kvantum.server.implementation.tempfiles.TempFileManagerFactory;
import xyz.kvantum.velocity.VelocityEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main {@link Kvantum} implementation.
 * Extended by {@link StandaloneServer}
 */
public class SimpleServer implements Kvantum
{

    private static final Pattern LOG_ARG_PATTERN = Pattern.compile( "\\{(?<num>([0-9])+)}" );

    static ObjectPool<GzipHandler> gzipHandlerPool;
    static ObjectPool<Md5Handler> md5HandlerPool;

    @Getter
    private final WorkerProcedure procedure = new WorkerProcedure();
    @Getter
    private final ITempFileManagerFactory tempFileManagerFactory = new TempFileManagerFactory();
    @Getter
    private final KvantumFileUpload globalFileUpload = new KvantumFileUpload();
    @Getter
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter( Account.class, new AccountSerializer() ).create();
    private final ServerContext serverContext;
    PrintStream logStream;
    @Getter
    protected ICacheManager cacheManager;
    @Getter
    private boolean silent = false;
    @Getter
    private SessionManager sessionManager;
    @Getter
    private boolean paused = false;
    @Getter
    private boolean stopping;
    @Getter
    private boolean started;
    private HTTPThread httpThread;
    private HTTPSThread httpsThread;
    @Getter
    private FileSystem fileSystem;
    @Getter
    private FileWatcher fileWatcher;
    @Getter
    private CommandManager commandManager;
    @Getter
    private ApplicationStructure applicationStructure;
    @Getter
    private ConfigurationFile translations;
    @Getter
    private EventBus eventBus;

    /**
     * @param serverContext ServerContext that will be used to initialize the server
     */
    @SneakyThrows
    @SuppressWarnings("WeakerAccess")
    public SimpleServer(@NonNull final ServerContext serverContext)
    {
        this.serverContext = serverContext;

        //
        // Setup singleton references
        //
        ServerImplementation.registerServerImplementation( this );

        //
        // Setup and initialize the file system
        //
        if ( !serverContext.getCoreFolder().exists() && !serverContext.getCoreFolder().mkdirs() )
        {
            throw new KvantumInitializationException( "Failed to create the core folder: " + getCoreFolder() );
        }
        this.fileWatcher = new FileWatcher();
        this.fileSystem = new IntellectualFileSystem( serverContext.getCoreFolder().toPath() );
        final File logFolder = FileUtils.attemptFolderCreation( new File( getCoreFolder(), "log" ) );
        try
        {
            FileUtils.addToZip( new File( logFolder, "old.zip" ),
                    logFolder.listFiles( (dir, name) -> name.endsWith( ".log" ) && !name.contains( "access" ) ) );
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }

        //
        // Setup the translation configuration file
        //
        try
        {
            translations = new TranslationFile( new File( getCoreFolder(), "config" ) );
        } catch ( final Exception e )
        {
            log( Message.CANNOT_LOAD_TRANSLATIONS );
            e.printStackTrace();
        }

        //
        // Setup the log-to-file system and the error stream
        //
        try
        {
            this.logStream = new LogStream( logFolder );
            System.setErr( new PrintStream( new ErrorOutputStream( serverContext.getLogWrapper() ), true ) );
        } catch ( FileNotFoundException e )
        {
            e.printStackTrace();
        }

        InternalLoggerFactory.setDefaultFactory( new NettyLoggerFactory() );

        //
        // Setup default flags
        //
        this.started = false;
        this.stopping = false;

        //
        // Setup memory guard
        //
        Logger.info( "Starting memory guard!" );
        MemoryGuard.getInstance().start();

        //
        // Setup the cache manager
        //
        this.cacheManager = new CacheManager();

        //
        // Setup the internal application
        //
        if ( serverContext.isStandalone() )
        {
            // Makes the application closable in ze terminal
            Signal.handle( new Signal( "INT" ), new ExitSignalHandler() );

            this.commandManager = new CommandManager( '/' );
            this.commandManager.getManagerOptions().getFindCloseMatches( false );
            this.commandManager.getManagerOptions().setRequirePrefix( false );

            switch ( CoreConfig.Application.databaseImplementation )
            {
                case "sqlite":
                    applicationStructure = new SQLiteApplicationStructure( "core" )
                    {
                        @Override
                        public IAccountManager createNewAccountManager()
                        {
                            return new SQLiteAccountManager( this );
                        }
                    };
                    break;
                case "mongo":
                    applicationStructure = new MongoApplicationStructure( "core" )
                    {
                        @Override
                        public IAccountManager createNewAccountManager()
                        {
                            return new MongoAccountManager( this );
                        }
                    };
                    break;
                default:
                    Message.DATABASE_UNKNOWN.log( CoreConfig.Application.databaseImplementation );
                    throw new KvantumInitializationException( "Cannot load - Invalid session database " +
                            "implementation provided" );
            }
        } else if ( !CoreConfig.Application.main.isEmpty() )
        {
            try
            {
                Class temp = Class.forName( CoreConfig.Application.main );
                if ( temp.getSuperclass().equals( ApplicationStructure.class ) )
                {
                    this.applicationStructure = (ApplicationStructure) temp.newInstance();
                } else
                {
                    log( Message.APPLICATION_DOES_NOT_EXTEND, CoreConfig.Application.main );
                }
            } catch ( ClassNotFoundException e )
            {
                log( Message.APPLICATION_CANNOT_FIND, CoreConfig.Application.main );
                e.printStackTrace();
            } catch ( InstantiationException | IllegalAccessException e )
            {
                log( Message.APPLICATION_CANNOT_INITIATE, CoreConfig.Application.main );
                e.printStackTrace();
            }
        }

        try
        {
            this.getApplicationStructure().getAccountManager().setup();
            if ( this.getCommandManager() != null )
            {
                getCommandManager().createCommand( new AccountCommand( applicationStructure ) );
            }
        } catch ( Exception e )
        {
            e.printStackTrace();
        }

        //
        // Load the session database
        //
        final ISessionDatabase sessionDatabase;
        if ( CoreConfig.Sessions.enableDb )
        {
            switch ( CoreConfig.Application.databaseImplementation.toLowerCase( Locale.ENGLISH ) )
            {
                case "sqlite":
                    sessionDatabase = new SQLiteSessionDatabase( (SQLiteApplicationStructure) this
                            .getApplicationStructure().getAccountManager()
                            .getApplicationStructure() );
                    break;
                case "mongo":
                    sessionDatabase = new MongoSessionDatabase( (MongoApplicationStructure) this
                            .getApplicationStructure().getAccountManager()
                            .getApplicationStructure() );
                    break;
                default:
                    Message.DATABASE_SESSION_UNKNOWN.log( CoreConfig.Application.databaseImplementation );
                    sessionDatabase = new DumbSessionDatabase();
                    break;
            }
            try
            {
                sessionDatabase.setup();
            } catch ( Exception e )
            {
                e.printStackTrace();
            }
        } else
        {
            sessionDatabase = new DumbSessionDatabase();
        }

        //
        // Setup the session manager implementation
        //
        this.sessionManager = new SessionManager( new SessionFactory(), sessionDatabase );

        //
        // Setup causam (EventBus)
        //
        final SubscriptionSelectorService selectorService =
                CachingSubscriptionSelectorServiceDecorator.concurrent( SubscriptionSelectorService.simple() );
        final SubscriptionRegistry registry = SetBasedSubscriptionRegistry.hash( selectorService );
        final Publisher publisher = Publisher.immediate();
        final EventEmitter emitter = EventEmitter.standard( registry, publisher );
        this.eventBus = EventBus.standard( registry, emitter );

        //
        // Initialize access.log logger
        //
        this.eventBus.register( new AccessLogStream( logFolder ) );

        //
        // Setup the connection throttler
        //
        ConnectionThrottle.initialize();
    }

    @Override
    public final File getCoreFolder()
    {
        return this.serverContext.getCoreFolder();
    }

    @Override
    public final LogWrapper getLogWrapper()
    {
        return this.serverContext.getLogWrapper();
    }

    @Override
    public final boolean isStandalone()
    {
        return this.serverContext.isStandalone();
    }

    @Override
    public final Router getRouter()
    {
        return this.serverContext.getRouter();
    }

    protected void onStart()
    {
    }

    @SuppressWarnings("ALL")
    @Override
    @Synchronized
    public final void start()
    {
        if ( CoreConfig.gzip )
        {
            gzipHandlerPool = new ObjectPool<>( CoreConfig.Pools.gzipHandlers, GzipHandler::new );
        }
        if ( CoreConfig.contentMd5 )
        {
            md5HandlerPool = new ObjectPool<>( CoreConfig.Pools.md5Handlers, Md5Handler::new );
        }

        try
        {
            Assert.equals( this.started, false,
                    new KvantumStartException( "Cannot start the server, it is already started",
                            new KvantumException( "Cannot restart server singleton" ) ) );
        } catch ( KvantumStartException e )
        {
            e.printStackTrace();
            return;
        }

        TemplateManager.get().addProviderFactory( ServerImplementation.getImplementation().getSessionManager() );
        TemplateManager.get().addProviderFactory( ConfigVariableProvider.getInstance() );
        TemplateManager.get().addProviderFactory( new PostProviderFactory() );
        TemplateManager.get().addProviderFactory( new MetaProvider() );

        Logger.info( "Checking templating engines:" );
        CrushEngine.getInstance().load();
        VelocityEngine.getInstance().load();
        JTwigEngine.getInstance().load();
        Logger.info( "" );

        this.onStart();

        this.applicationStructure.registerViews( this );

        getRouter().dump( this );

        log( "" );

        log( Message.STARTING_ON_PORT, CoreConfig.port );

        if ( isStandalone() && CoreConfig.enableInputThread )
        {
            new InputThread().start();
        }

        this.started = true;

        log( "" );

        final NioClassResolver classResolver = new NioClassResolver();

        if ( CoreConfig.SSL.enable )
        {
            log( Message.STARTING_SSL_ON_PORT, CoreConfig.SSL.port );
            try
            {
                System.setProperty( "javax.net.ssl.keyStore", CoreConfig.SSL.keyStore );
                System.setProperty( "javax.net.ssl.keyStorePassword", CoreConfig.SSL.keyStorePassword );

                this.httpsThread = new HTTPSThread( classResolver );
                this.httpsThread.start();
            } catch ( final Exception e )
            {
                new KvantumException( "Failed to start HTTPS server", e ).printStackTrace();
            }
        }

        log( Message.ACCEPTING_CONNECTIONS_ON, CoreConfig.webAddress +
                ( CoreConfig.port == 80 ? "" : ":" + CoreConfig.port ) + "/'" );
        log( Message.OUTPUT_BUFFER_INFO, CoreConfig.Buffer.out / 1024, CoreConfig.Buffer.in / 1024 );

        try
        {
            this.httpThread = new HTTPThread( new ServerSocketFactory(), classResolver );
        } catch ( KvantumInitializationException e )
        {
            Logger.error( "Failed to start server..." );
            ServerImplementation.getImplementation().stopServer();
            return;
        }
        this.httpThread.start();
        this.getEventBus().emit( new ServerStartedEvent( this ) );
    }

    @Override
    public final void log(final Message message, final Object... args)
    {
        this.log( message.toString(), message.getMode(), args );
    }

    @Override
    public final void log(final String message, final int mode, final Object... args)
    {
        // This allows us to customize what messages are
        // sent to the logging screen, and thus we're able
        // to limit to only error messages or such
        if ( ( mode == LogModes.MODE_DEBUG && !CoreConfig.debug ) || mode < LogModes.lowestLevel || mode > LogModes
                .highestLevel )
        {
            return;
        }
        String prefix;
        switch ( mode )
        {
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
            default:
                prefix = "Info";
                break;
        }

        this.log( prefix, message, args );
    }

    /**
     * Replaces string arguments using the pattern {num} from
     * an array of objects, starting from index 0, as such:
     * 0 &le; num &lt; args.length. If num &ge; args.length, then
     * the pattern will be replaced by an empty string. An argument
     * can also be passed as "{}", in which case the
     * number will be implied.
     *
     * @param prefix  Log prefix
     * @param message String to be replaced. Cannot be null.
     * @param args    Replacements
     */
    @Synchronized
    private void log(@NonNull final String prefix,
                     @NonNull final String message, final Object... args)
    {
        String msg = message;

        final Matcher matcher = LOG_ARG_PATTERN.matcher( msg );

        int index = 0;
        boolean containsEmptyBrackets;
        while ( ( containsEmptyBrackets = msg.contains( "{}" ) ) ||
                matcher.find() )
        {
            final int argumentNum;
            final String toReplace;
            if ( containsEmptyBrackets )
            {
                argumentNum = index++;
                toReplace = "{}";
            } else
            {
                toReplace = matcher.group();
                argumentNum = Integer.parseInt( matcher.group( "num" ) );
            }
            if ( argumentNum < args.length )
            {
                final Object object = args[ argumentNum ];
                String objectString;
                if ( object == null )
                {
                    objectString = "null";
                } else if ( object instanceof LogFormatted )
                {
                    objectString = ( (LogFormatted) object ).getLogFormatted();
                } else
                {
                    objectString = object.toString();
                }
                msg = msg.replace( toReplace, objectString );
            } else
            {
                msg = msg.replace( toReplace, "" );
            }
        }
        getLogWrapper().log( LogContext.builder().applicationPrefix( CoreConfig.logPrefix ).logPrefix( prefix ).timeStamp(
                TimeUtil.getTimeStamp() ).message( msg ).thread( Thread.currentThread().getName() ).build() );
    }

    @Override
    public final void log(final String message, final Object... args)
    {
        this.log( message, LogModes.MODE_INFO, args );
    }

    @Override
    public final void log(final LogProvider provider, final String message, final Object... args)
    {
        this.log( provider.getLogIdentifier(), message, args );
    }

    @Synchronized
    @Override
    public void stopServer()
    {
        Message.SHUTTING_DOWN.log();

        //
        // Emit the shut down event
        //
        getEventBus().emit( new ServerShutdownEvent( this ) );

        //
        // Gracefully shutdown the file watcher
        //
        this.fileWatcher.getStopSignal().stop();

        //
        // Shutdown the netty servers
        //
        try
        {
            httpThread.close();
            if ( CoreConfig.SSL.enable )
            {
                httpsThread.close();
            }
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }

        //
        // Shutdown utilities
        //
        AutoCloseable.closeAll();

        //
        // Close the log stream
        //
        try
        {
            this.logStream.close();
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }

        if ( isStandalone() && CoreConfig.exitOnStop )
        {
            System.exit( 0 );
        }
    }

    @Override
    public final RequestHandler createSimpleRequestHandler(final String filter,
                                                           final BiConsumer<AbstractRequest, Response> generator)
    {
        return SimpleRequestHandler.builder().pattern( filter ).generator( generator ).build()
                .addToRouter( getRouter() );
    }

}
