/*
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
import com.intellectualsites.configurable.ConfigurationFactory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import sun.misc.Signal;
import xyz.kvantum.crush.CrushEngine;
import xyz.kvantum.files.Extension;
import xyz.kvantum.files.FileSystem;
import xyz.kvantum.files.Path;
import xyz.kvantum.server.api.account.IAccountManager;
import xyz.kvantum.server.api.cache.ICacheManager;
import xyz.kvantum.server.api.config.ConfigVariableProvider;
import xyz.kvantum.server.api.config.ConfigurationFile;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.config.Message;
import xyz.kvantum.server.api.config.YamlConfiguration;
import xyz.kvantum.server.api.core.Kvantum;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.core.WorkerProcedure;
import xyz.kvantum.server.api.events.Event;
import xyz.kvantum.server.api.events.EventCaller;
import xyz.kvantum.server.api.events.EventManager;
import xyz.kvantum.server.api.events.defaultevents.ServerReadyEvent;
import xyz.kvantum.server.api.events.defaultevents.ShutdownEvent;
import xyz.kvantum.server.api.events.defaultevents.StartupEvent;
import xyz.kvantum.server.api.events.defaultevents.ViewsInitializedEvent;
import xyz.kvantum.server.api.fileupload.KvantumFileUpload;
import xyz.kvantum.server.api.jtwig.JTwigEngine;
import xyz.kvantum.server.api.logging.LogContext;
import xyz.kvantum.server.api.logging.LogFormatted;
import xyz.kvantum.server.api.logging.LogModes;
import xyz.kvantum.server.api.logging.LogProvider;
import xyz.kvantum.server.api.logging.LogWrapper;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.matching.Router;
import xyz.kvantum.server.api.plugin.PluginLoader;
import xyz.kvantum.server.api.plugin.PluginManager;
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
import xyz.kvantum.server.api.util.InstanceFactory;
import xyz.kvantum.server.api.util.LambdaUtil;
import xyz.kvantum.server.api.util.MapBuilder;
import xyz.kvantum.server.api.util.MetaProvider;
import xyz.kvantum.server.api.util.Metrics;
import xyz.kvantum.server.api.util.TimeUtil;
import xyz.kvantum.server.api.views.CSSView;
import xyz.kvantum.server.api.views.DownloadView;
import xyz.kvantum.server.api.views.HTMLView;
import xyz.kvantum.server.api.views.ImgView;
import xyz.kvantum.server.api.views.JSView;
import xyz.kvantum.server.api.views.LessView;
import xyz.kvantum.server.api.views.RequestHandler;
import xyz.kvantum.server.api.views.StandardView;
import xyz.kvantum.server.api.views.View;
import xyz.kvantum.server.api.views.requesthandler.SimpleRequestHandler;
import xyz.kvantum.server.implementation.commands.AccountCommand;
import xyz.kvantum.server.implementation.config.TranslationFile;
import xyz.kvantum.server.implementation.error.KvantumException;
import xyz.kvantum.server.implementation.error.KvantumInitializationException;
import xyz.kvantum.server.implementation.error.KvantumStartException;
import xyz.kvantum.server.implementation.mongo.MongoAccountManager;
import xyz.kvantum.server.implementation.mongo.MongoSessionDatabase;
import xyz.kvantum.server.implementation.sqlite.SQLiteAccountManager;
import xyz.kvantum.server.implementation.sqlite.SQLiteSessionDatabase;
import xyz.kvantum.server.implementation.tempfiles.TempFileManagerFactory;
import xyz.kvantum.velocity.VelocityEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Main {@link Kvantum} implementation.
 * <p>
 * Use {@link ServerImplementation#getImplementation()} to get an instance, rather
 * than {@link #getInstance()}.
 * </p>
 */
public final class Server implements Kvantum
{

    static AbstractPool<GzipHandler> gzipHandlerPool;
    static AbstractPool<Md5Handler> md5HandlerPool;

    @SuppressWarnings("ALL")
    @Getter(AccessLevel.PACKAGE)
    private static Server instance;
    @Getter
    private final LogWrapper logWrapper;
    @Getter
    private final boolean standalone;
    @Getter
    private final Map<String, Class<? extends View>> viewBindings = new HashMap<>();
    @Getter
    private final WorkerProcedure procedure = new WorkerProcedure();
    @Getter
    private final SocketHandler socketHandler;
    @Getter
    private final Metrics metrics = new Metrics();
    @Getter
    private final ITempFileManagerFactory tempFileManagerFactory = new TempFileManagerFactory();
    @Getter
    private final KvantumFileUpload globalFileUpload = new KvantumFileUpload();
    @Getter
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter( Account.class, new AccountSerializer() ).create();
    PrintStream logStream;
    @Getter
    private volatile ICacheManager cacheManager;
    @Getter
    private boolean silent = false;
    @Getter
    private Router router;
    @Getter
    private SessionManager sessionManager;
    @Getter
    private ConfigurationFile translations;
    @Getter
    private File coreFolder;
    @Getter
    private boolean paused = false;
    @Getter
    private boolean stopping;
    @Getter
    private boolean started;
    private HTTPThread httpThread;
    private HTTPSThread httpsThread;
    private ConfigurationFile configViews;
    @Setter
    @Getter
    private EventCaller eventCaller;
    @Getter
    private FileSystem fileSystem;
    @Getter
    private CommandManager commandManager;
    @Getter
    private ApplicationStructure applicationStructure;

    /**
     * @param serverContext ServerContext that will be used to initialize the server
     * @throws KvantumException If anything was to fail
     */
    Server(final ServerContext serverContext)
            throws KvantumException
    {
        this.coreFolder = serverContext.getCoreFolder();
        this.logWrapper = serverContext.getLogWrapper();
        this.standalone = serverContext.isStandalone();
        this.router = serverContext.getRouter();

        //
        // Setup singleton references
        //
        InstanceFactory.setupInstanceAutomagic( this );
        ServerImplementation.registerServerImplementation( this );

        //
        // Setup and initialize the file system
        //
        coreFolder = new File( coreFolder, "kvantum" ); // Makes everything more portable
        if ( !coreFolder.exists() && !coreFolder.mkdirs() )
        {
            throw new KvantumInitializationException( "Failed to create the core folder: " + coreFolder );
        }
        this.fileSystem = new IntellectualFileSystem( coreFolder.toPath() );
        final File logFolder = FileUtils.attemptFolderCreation( new File( coreFolder, "log" ) );
        try
        {
            FileUtils.addToZip( new File( logFolder, "old.zip" ),
                    logFolder.listFiles( (dir, name) -> name.endsWith( ".txt" ) ) );
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }

        //
        // Setup the log-to-file system and the error stream
        //
        try
        {
            this.logStream = new LogStream( logFolder );
            System.setErr( new PrintStream( new ErrorOutputStream( logWrapper ), true ) );
        } catch ( FileNotFoundException e )
        {
            e.printStackTrace();
        }

        //
        // Print license information
        //
        this.printLicenseInfo();

        //
        // Add default view bindings
        //
        this.addDefaultViewBindings();

        //
        // Setup the translation configuration file
        //
        try
        {
            translations = new TranslationFile( new File( coreFolder, "config" ) );
        } catch ( final Exception e )
        {
            log( Message.CANNOT_LOAD_TRANSLATIONS );
            e.printStackTrace();
        }

        //
        // Load the configuration file
        //
        if ( !CoreConfig.isPreConfigured() )
        {
            ConfigurationFactory.load( CoreConfig.class, new File( coreFolder, "config" ) ).get();
        }

        this.logWrapper.setFormat( CoreConfig.Logging.logFormat );

        //
        // Check through the configuration file and make sure that the values
        // are not weird
        //
        validateConfiguration();

        //
        // Enable the custom security manager
        //
        if ( CoreConfig.enableSecurityManager )
        {
            try
            {
                System.setOut( ServerSecurityManager.SecurePrintStream.construct( System.out ) );
            } catch ( Exception e )
            {
                e.printStackTrace();
            }
        }

        //
        // Send a sample debug message
        //
        if ( CoreConfig.debug )
        {
            log( Message.DEBUG );
        }

        //
        // Setup the internal engine
        //
        this.socketHandler = new SocketHandler();
        if ( CoreConfig.gzip )
        {
            gzipHandlerPool = new AbstractPool<>( CoreConfig.Pools.gzipHandlers, GzipHandler::new );
        }
        if ( CoreConfig.contentMd5 )
        {
            md5HandlerPool = new AbstractPool<>( CoreConfig.Pools.md5Handlers, Md5Handler::new );
        }

        //
        // Setup default flags
        //
        this.started = false;
        this.stopping = false;

        //
        // Setup the cache manager
        //
        this.cacheManager = new CacheManager();

        //
        // Load view configuration
        //
        if ( !CoreConfig.disableViews )
        {
            this.loadViewConfiguration();
        }

        //
        // Setup the internal application
        //
        if ( standalone )
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
            switch ( CoreConfig.Application.databaseImplementation.toLowerCase() )
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
    }

    private void validateConfiguration()
    {
        if ( CoreConfig.Buffer.in < 1000 || CoreConfig.Buffer.in > 100000 )
        {
            Logger.warn( "It is recommended to keep 'buffer.in' in server.yml between 0 and 100000" );
            Logger.warn( "Any other values may cause the server to run slower than expected" );
        }
        if ( CoreConfig.Buffer.out < 1000 || CoreConfig.Buffer.out > 100000 )
        {
            Logger.warn( "It is recommended to keep 'buffer.out' in server.yml between 0 and 100000" );
            Logger.warn( "Any other values may cause the server to run slower than expected" );
        }
    }

    private void loadViewConfiguration() throws KvantumException
    {
        try
        {
            configViews = new YamlConfiguration( "views", new File( new File( coreFolder, "config" ),
                    "views.yml" ) );
            configViews.loadFile();

            // These are the default views

            if ( !configViews.contains( "views" ) )
            {
                final YamlConfiguration standardFile = new YamlConfiguration( "views",
                        new File( new File( coreFolder, "config" ), "standardViews.yml" ) );
                standardFile.loadFile();

                {
                    Map<String, Object> views = new HashMap<>();
                    final Map<String, Object> defaultView = MapBuilder.<String, Object>newHashMap()
                            .put( "filter", "[file=index].[extension=html]" )
                            .put( "type", "std" )
                            .put( "options", MapBuilder.<String, Object>newHashMap()
                                    .put( "folder", "./public" )
                                    .put( "excludeExtensions", Collections.singletonList( "txt" ) )
                                    .put( "filePattern", "${file}.${extension}" ).get()
                            ).get();
                    views.put( "std", defaultView );
                    standardFile.setIfNotExists( "views", views );
                    standardFile.saveFile();
                }

                {
                    Map<String, Object> views = new HashMap<>();
                    views.put( "standard", "standardViews.yml" );
                    configViews.setIfNotExists( "views", views );
                    configViews.saveFile();
                }

                Logger.info( "If you want to import a folder structure, see /generate" );
                Logger.info( "Instructions for /generate: https://github.com/IntellectualSites/Kvantum/wiki/generate" );
            }

            final Path path = getFileSystem().getPath( "public" );
            if ( !path.exists() )
            {
                path.create();
                //
                // Only create the default files if the /public folder doesn't exist
                //
                if ( !path.getPath( "favicon.ico" ).exists() )
                {
                    Message.CREATING.log( "public/favicon.ico" );
                    try ( OutputStream out = new FileOutputStream( new File( path.getJavaPath().toFile(),
                            "favicon.ico" )
                    ) )
                    {
                        FileUtils.copyFile( getClass().getResourceAsStream( "/template/favicon.ico" ), out, 1024 * 16 );
                    } catch ( final Exception e )
                    {
                        e.printStackTrace();
                    }
                }

                if ( !path.getPath( "index", Extension.HTML ).exists() )
                {
                    Message.CREATING.log( "public/index.html" );
                    try ( OutputStream out = new FileOutputStream( new File( path.getJavaPath().toFile(), "index.html" )
                    ) )
                    {
                        FileUtils.copyFile( getClass().getResourceAsStream( "/template/index.html" ), out, 1024 * 16 );
                    } catch ( final Exception e )
                    {
                        e.printStackTrace();
                    }
                }
            }
        } catch ( final Exception e )
        {
            throw new KvantumInitializationException( "Couldn't load in views", e );
        }
    }

    private void addDefaultViewBindings()
    {
        addViewBinding( "html", HTMLView.class );
        addViewBinding( "css", CSSView.class );
        addViewBinding( "javascript", JSView.class );
        addViewBinding( "less", LessView.class );
        addViewBinding( "img", ImgView.class );
        addViewBinding( "download", DownloadView.class );
        addViewBinding( "std", StandardView.class );
    }

    private void printLicenseInfo()
    {
        final LogWrapper.LogEntryFormatter prefix = msg -> "> " + msg;
        logWrapper.log( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" );
        LambdaUtil.arrayForeach( string -> logWrapper.log( prefix, string ),
                "APACHE LICENSE VERSION 2.0:",
                "",
                "Kvantum, Copyright (C) 2017 IntellectualSites",
                "Kvantum comes with ABSOLUTELY NO WARRANTY; for details type `/show w`",
                "This is free software, and you are welcome to redistribute it",
                "under certain conditions; type `/show c` for details.",
                ""
        );
        logWrapper.log( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" );
        logWrapper.log();
    }

    @Synchronized
    @Override
    public void addViewBinding(final String key, final Class<? extends View> c)
    {
        Assert.notNull( c );
        Assert.notEmpty( key );

        viewBindings.put( key, c );
    }

    @SuppressWarnings("ALL")
    @Override
    public void validateViews()
    {
        final List<String> toRemove = new ArrayList<>();
        for ( final Map.Entry<String, Class<? extends View>> e : viewBindings.entrySet() )
        {
            final Class<? extends View> vc = e.getValue();
            try
            {
                vc.getDeclaredConstructor( String.class, Map.class );
            } catch ( final Exception ex )
            {
                log( Message.INVALID_VIEW, e.getKey() );
                toRemove.add( e.getKey() );
            }
        }
        toRemove.forEach( viewBindings::remove );
    }

    @Synchronized
    @Override
    public void handleEvent(final Event event)
    {
        Assert.notNull( event );

        if ( standalone || eventCaller == null )
        {
            EventManager.getInstance().handle( event );
        } else
        {
            eventCaller.callEvent( event );
        }
    }

    @Override
    public void loadPlugins()
    {
        if ( standalone )
        {
            final File file = new File( coreFolder, "plugins" );
            if ( !file.exists() && !file.mkdirs() )
            {
                log( Message.COULD_NOT_CREATE_PLUGIN_FOLDER, file );
                return;
            }
            PluginLoader pluginLoader = new PluginLoader( new PluginManager() );
            pluginLoader.loadAllPlugins( file );
            pluginLoader.enableAllPlugins();
        } else
        {
            log( Message.STANDALONE_NOT_LOADING_PLUGINS );
        }
    }

    @SuppressWarnings("ALL")
    @Override
    @Synchronized
    public void start()
    {
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

        // Load Plugins
        this.loadPlugins();
        EventManager.getInstance().bake();

        this.log( Message.CALLING_EVENT, "startup" );
        this.handleEvent( new StartupEvent( this ) );

        // Validating views
        this.log( Message.VALIDATING_VIEWS );
        this.validateViews();

        if ( !CoreConfig.disableViews )
        {
            this.log( Message.LOADING_VIEWS );
            this.log( "" );
            new ViewLoader( configViews );
        } else
        {
            Message.VIEWS_DISABLED.log();
        }

        this.applicationStructure.registerViews( this );
        this.handleEvent( new ViewsInitializedEvent( this ) );

        router.dump( this );

        if ( !CoreConfig.Cache.enabled )
        {
            log( Message.CACHING_DISABLED );
        } else
        {
            log( Message.CACHING_ENABLED );
        }

        log( "" );

        log( Message.STARTING_ON_PORT, CoreConfig.port );

        if ( standalone && CoreConfig.enableInputThread )
        {
            new InputThread().start();
        }

        this.started = true;

        log( "" );

        if ( CoreConfig.SSL.enable )
        {
            log( Message.STARTING_SSL_ON_PORT, CoreConfig.SSL.port );
            try
            {
                System.setProperty( "javax.net.ssl.keyStore", CoreConfig.SSL.keyStore );
                System.setProperty( "javax.net.ssl.keyStorePassword", CoreConfig.SSL.keyStorePassword );

                this.httpsThread = new HTTPSThread( socketHandler );
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
            this.httpThread = new HTTPThread( new ServerSocketFactory(), socketHandler );
        } catch ( KvantumInitializationException e )
        {
            Logger.error( "Failed to start server..." );
            ServerImplementation.getImplementation().stopServer();
            return;
        }
        this.httpThread.start();

        this.handleEvent( new ServerReadyEvent( this ) );
    }

    @Override
    public void log(final Message message, final Object... args)
    {
        this.log( message.toString(), message.getMode(), args );
    }

    @Override
    public void log(final String message, final int mode, final Object... args)
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

    @Synchronized
    private void log(final String prefix, final String message, final Object... args)
    {
        String msg = message;
        for ( final Object a : args )
        {
            String objectString;
            if ( a == null )
            {
                objectString = "null";
            } else if ( a instanceof LogFormatted )
            {
                objectString = ( (LogFormatted) a ).getLogFormatted();
            } else
            {
                objectString = a.toString();
            }
            msg = msg.replaceFirst( "%s", objectString );
        }

        logWrapper.log( LogContext.builder().applicationPrefix( CoreConfig.logPrefix ).logPrefix( prefix ).timeStamp(
                TimeUtil.getTimeStamp() ).message( msg ).thread( Thread.currentThread().getName() ).build() );
    }

    @Override
    public void log(final String message, final Object... args)
    {
        this.log( message, LogModes.MODE_INFO, args );
    }

    @Override
    public void log(final LogProvider provider, final String message, final Object... args)
    {
        this.log( provider.getLogIdentifier(), message, args );
    }

    @Synchronized
    @Override
    public void stopServer()
    {
        Message.SHUTTING_DOWN.log();
        EventManager.getInstance().handle( new ShutdownEvent( this ) );

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

        socketHandler.handleShutdown();

        AutoCloseable.closeAll();

        try
        {
            this.logStream.close();
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }

        if ( standalone && CoreConfig.exitOnStop )
        {
            System.exit( 0 );
        }
    }

    @Override
    public RequestHandler createSimpleRequestHandler(final String filter, final BiConsumer<AbstractRequest, Response> generator)
    {
        return SimpleRequestHandler.builder().setPattern( filter ).setGenerator( generator )
                .build().addToRouter( router );
    }

}
