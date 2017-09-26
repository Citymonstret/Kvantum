/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
 *
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.plotsquared.iserver;

import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.configurable.ConfigurationFactory;
import com.plotsquared.iserver.api.account.AccountManager;
import com.plotsquared.iserver.api.cache.CacheManager;
import com.plotsquared.iserver.api.config.ConfigurationFile;
import com.plotsquared.iserver.api.config.CoreConfig;
import com.plotsquared.iserver.api.config.Message;
import com.plotsquared.iserver.api.config.YamlConfiguration;
import com.plotsquared.iserver.api.core.IntellectualServer;
import com.plotsquared.iserver.api.core.ServerImplementation;
import com.plotsquared.iserver.api.core.WorkerProcedure;
import com.plotsquared.iserver.api.events.Event;
import com.plotsquared.iserver.api.events.EventCaller;
import com.plotsquared.iserver.api.events.EventManager;
import com.plotsquared.iserver.api.events.defaultEvents.ServerReadyEvent;
import com.plotsquared.iserver.api.events.defaultEvents.ShutdownEvent;
import com.plotsquared.iserver.api.events.defaultEvents.StartupEvent;
import com.plotsquared.iserver.api.logging.LogModes;
import com.plotsquared.iserver.api.logging.LogProvider;
import com.plotsquared.iserver.api.logging.LogWrapper;
import com.plotsquared.iserver.api.logging.Logger;
import com.plotsquared.iserver.api.matching.Router;
import com.plotsquared.iserver.api.plugin.PluginLoader;
import com.plotsquared.iserver.api.plugin.PluginManager;
import com.plotsquared.iserver.api.request.Request;
import com.plotsquared.iserver.api.response.Response;
import com.plotsquared.iserver.api.session.SessionManager;
import com.plotsquared.iserver.api.util.*;
import com.plotsquared.iserver.api.util.AutoCloseable;
import com.plotsquared.iserver.api.views.*;
import com.plotsquared.iserver.api.views.requesthandler.SimpleRequestHandler;
import com.plotsquared.iserver.commands.AccountCommand;
import com.plotsquared.iserver.crush.CrushEngine;
import com.plotsquared.iserver.error.IntellectualServerInitializationException;
import com.plotsquared.iserver.error.IntellectualServerStartException;
import com.plotsquared.iserver.files.FileCacheManager;
import com.plotsquared.iserver.files.FileSystem;
import com.plotsquared.iserver.files.Path;
import sun.misc.Signal;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.*;
import java.net.ServerSocket;
import java.util.*;
import java.util.function.BiConsumer;

public final class Server implements IntellectualServer
{

    // Private Static
    @SuppressWarnings( "ALL" )
    private static Server instance;
    // Private Final
    private final LogWrapper logWrapper;
    private final boolean standalone;
    private final Map<String, Class<? extends View>> viewBindings;
    private final WorkerProcedure workerProcedure = new WorkerProcedure();
    private final SimpleSocketHandler socketHandler;
    private final Metrics metrics = new Metrics();
    // Private
    PrintStream logStream;
    // Public
    private volatile CacheManager cacheManager;
    private boolean silent = false;
    private Router router;
    private SessionManager sessionManager;
    // Package-Protected
    private ConfigurationFile translations;
    private File coreFolder;
    private boolean pause = false;
    private boolean stopping;
    private boolean started;
    private ServerSocket serverSocket;
    private SSLServerSocket sslServerSocket;
    private ConfigurationFile configViews;
    private MySQLConnManager mysqlConnManager;
    private EventCaller eventCaller;
    private ApplicationStructure mainApplicationStructure;
    private AccountManager globalAccountManager;
    private FileSystem fileSystem;
    private CommandManager commandManager;

    {
        viewBindings = new HashMap<>();
    }

    /**
     * @param standalone Whether or not the server should run as a standalone application,
     *                   or as an integrated application
     * @param coreFolder The main folder (in which configuration files and alike are stored)
     * @param logWrapper The log implementation
     * @throws IntellectualServerInitializationException If anything was to fail
     */
    Server(final boolean standalone, File coreFolder, final LogWrapper logWrapper, final Router
            router)
            throws IntellectualServerInitializationException
    {
        Assert.notNull( coreFolder, logWrapper );

        // This setups the "instance" instance
        InstanceFactory.setupInstanceAutomagic( this );
        ServerImplementation.registerServerImplementation( this );

        // Make sure that the main folder is created
        coreFolder = new File( coreFolder, ".iserver" ); // Makes everything more portable
        if ( !coreFolder.exists() )
        {
            if ( !coreFolder.mkdirs() )
            {
                throw new IntellectualServerInitializationException( "Failed to create the core folder: " + coreFolder );
            }
        }

        this.coreFolder = coreFolder;
        this.logWrapper = logWrapper;
        this.standalone = standalone;

        // Setup the proprietary file system
        // TODO: Move to separate class
        this.fileSystem = new FileSystem( coreFolder.toPath(), new FileCacheManager()
        {
            @Override
            public Optional<String> readCachedFile(final String string)
            {
                return getCacheManager().getCachedFile( string );
            }

            @Override
            public void writeCachedFile(final String file, final String content)
            {
                getCacheManager().setCachedFile( file, content );
            }
        } );

        final File logFolder = new File( coreFolder, "log" );
        if ( !logFolder.exists() )
        {
            if ( !logFolder.mkdirs() )
            {
                log( Message.COULD_NOT_CREATE_FOLDER, "log" );
            }
        }

        // Setup log-to-file
        try
        {
            this.logStream = new PrintStream( new FileOutputStream( new File( logFolder,
                    TimeUtil.getTimeStamp( TimeUtil.LogFileFormat ) + ".txt" ) ) );
            System.setErr( new PrintStream( new ErrorOutputStream( logWrapper ), true ) );
        } catch ( FileNotFoundException e )
        {
            e.printStackTrace();
        }

        printLicenseInfo();

        // This adds the default view bindings
        // TODO: Make configurable
        addViewBinding( "html", HTMLView.class );
        addViewBinding( "css", CSSView.class );
        addViewBinding( "javascript", JSView.class );
        addViewBinding( "less", LessView.class );
        addViewBinding( "img", ImgView.class );
        addViewBinding( "download", DownloadView.class );
        addViewBinding( "std", StandardView.class );

        if ( standalone )
        {
            // Makes the application closable in ze terminal
            Signal.handle( new Signal( "INT" ), new ExitSignalHandler() );

            this.commandManager = new CommandManager( '/' );
            this.commandManager.getManagerOptions().getFindCloseMatches( false );
            this.commandManager.getManagerOptions().setRequirePrefix( false );
            new InputThread().start();
        }

        try
        {
            FileUtils.addToZip( new File( logFolder, "old.zip" ),
                    logFolder.listFiles( (dir, name) -> name.endsWith( ".txt" ) ), true );
            // System.setOut( new PrintStream( new TeeOutputStream( System.out,
            //         new FileOutputStream( new File( logFolder, TimeUtil.getTimeStamp( TimeUtil.LogFileFormat ) + ".txt" ) ) ) ) );
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }

        try
        {
            /*
      This is the configuration file
      used for translations of logging messages
     */
            translations = new YamlConfiguration( "translations",
                    new File( new File( coreFolder, "config" ), "translations.yml" ) );
            translations.loadFile();
            for ( final Message message : Message.values() )
            {
                final String nameSpace;
                switch ( message.getMode() )
                {
                    case LogModes.MODE_DEBUG:
                        nameSpace = "debug";
                        break;
                    case LogModes.MODE_INFO:
                        nameSpace = "info";
                        break;
                    case LogModes.MODE_ERROR:
                        nameSpace = "error";
                        break;
                    case LogModes.MODE_WARNING:
                        nameSpace = "warning";
                        break;
                    default:
                        nameSpace = "info";
                        break;
                }
                translations.setIfNotExists( nameSpace + "." + message.name().toLowerCase(), message.toString() );
            }
            translations.saveFile();
        } catch ( final Exception e )
        {
            log( Message.CANNOT_LOAD_TRANSLATIONS );
            e.printStackTrace();
        }

        if ( !CoreConfig.isPreConfigured() )
        {
            ConfigurationFactory.load( CoreConfig.class, new File( coreFolder, "config" ) ).get();
        }

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

        if ( CoreConfig.debug )
        {
            log( Message.DEBUG );
        }

        this.socketHandler = new SimpleSocketHandler();
        Worker.setup( CoreConfig.workers );

        this.started = false;
        this.stopping = false;

        this.router = router;
        this.sessionManager = new SimpleSessionManager();
        this.cacheManager = new CacheManager();

        if ( CoreConfig.MySQL.enabled )
        {
            this.mysqlConnManager = new MySQLConnManager();
        }

        if ( !CoreConfig.disableViews )
        {
            try
            {
                configViews = new YamlConfiguration( "views", new File( new File( coreFolder, "config" ), "views.yml" ) );
                configViews.loadFile();
                // These are the default views
                Map<String, Object> views = new HashMap<>();
                // HTML View
                Map<String, Object> view = new HashMap<>();
                view.put( "filter", "[file][extension]" );
                view.put( "type", "std" );
                Map<String, Object> opts = new HashMap<>();
                opts.put( "folder", "./public" );
                view.put( "options", opts );
                views.put( "std", view );
                configViews.setIfNotExists( "views", views );
                final Path path = getFileSystem().getPath( "public" );
                if ( !path.exists() )
                {
                    path.create();
                }
                if ( !path.getPath( "favicon.ico" ).exists() )
                {
                    Logger.info( "Creating public/favicon.ico" );
                    try ( final OutputStream out = new FileOutputStream( new File( path.getJavaPath().toFile(),
                            "favicon.ico" )
                    ) )
                    {
                        FileUtils.copyFile( getClass().getResourceAsStream( "/template/favicon.ico" ), out, 1024 * 16 );
                    } catch ( final Exception e )
                    {
                        e.printStackTrace();
                    }
                }
                if ( !path.getPath( "index.html" ).exists() )
                {
                    Logger.info( "Creating public/index.html!" );
                    try ( final OutputStream out = new FileOutputStream( new File( path.getJavaPath().toFile(), "index.html" )
                    ) )
                    {
                        FileUtils.copyFile( getClass().getResourceAsStream( "/template/index.html" ), out, 1024 * 16 );
                    } catch ( final Exception e )
                    {
                        e.printStackTrace();
                    }
                }
                configViews.saveFile();
            } catch ( final Exception e )
            {
                throw new RuntimeException( "Couldn't load in views", e );
            }
        }

        if ( standalone )
        {
            ApplicationStructure applicationStructure = new ApplicationStructure( "core" )
            {
                @Override
                public AccountManager createNewAccountManager()
                {
                    return new SimpleAccountManager( this );
                }
            };
            this.globalAccountManager = applicationStructure.getAccountManager();
            try
            {
                this.globalAccountManager.setup();
            } catch ( Exception e )
            {
                e.printStackTrace();
            }
            // TODO: Remake
            getCommandManager().createCommand( new AccountCommand( applicationStructure ) );
        }

        if ( !CoreConfig.Application.main.isEmpty() )
        {
            try
            {
                Class temp = Class.forName( CoreConfig.Application.main );
                if ( temp.getSuperclass().equals( ApplicationStructure.class ) )
                {
                    this.mainApplicationStructure = (ApplicationStructure) temp.newInstance();
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
    }

    /**
     * Get THE instance of the server
     *
     * @return this, literally... this!
     */
    static IntellectualServer getInstance()
    {
        return instance;
    }

    @Override
    public Metrics getMetrics()
    {
        return metrics;
    }

    private void printLicenseInfo()
    {
        final LogWrapper.LogEntryFormatter prefix = msg -> "> " + msg;
        logWrapper.log( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" );
        LambdaUtil.arrayForeach( string -> logWrapper.log( prefix, string ),
                "GNU GENERAL PUBLIC LICENSE NOTICE:",
                "",
                "IntellectualServer, Copyright (C) 2015 IntellectualSites",
                "IntellectualSites comes with ABSOLUTELY NO WARRANTY; for details type `/show w`",
                "This is free software, and you are welcome to redistribute it",
                "under certain conditions; type `/show c` for details.",
                ""
        );
        logWrapper.log( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" );
        logWrapper.log();
    }

    @Override
    public boolean isMysqlEnabled()
    {
        return CoreConfig.MySQL.enabled;
    }

    @Override
    public void addViewBinding(final String key, final Class<? extends View> c)
    {
        Assert.notNull( c );
        Assert.notEmpty( key );

        viewBindings.put( key, c );
    }

    @Override
    public Optional<AccountManager> getAccountManager()
    {
        return Optional.ofNullable( this.globalAccountManager );
    }

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
    public FileSystem getFileSystem()
    {
        return fileSystem;
    }

    @Override
    public void setEventCaller(final EventCaller caller)
    {
        Assert.notNull( caller );

        this.eventCaller = caller;
    }

    @Override
    public void loadPlugins()
    {
        if ( standalone )
        {
            final File file = new File( coreFolder, "plugins" );
            if ( !file.exists() )
            {
                if ( !file.mkdirs() )
                {
                    log( Message.COULD_NOT_CREATE_PLUGIN_FOLDER, file );
                    return;
                }
            }
            PluginLoader pluginLoader = new PluginLoader( new PluginManager() );
            pluginLoader.loadAllPlugins( file );
            pluginLoader.enableAllPlugins();
        } else
        {
            log( Message.STANDALONE_NOT_LOADING_PLUGINS );
        }
    }

    @Override
    public CommandManager getCommandManager()
    {
        return this.commandManager;
    }

    @SuppressWarnings("ALL")
    @Override
    public void start()
    {
        try
        {
            Assert.equals( this.started, false,
                    new IntellectualServerStartException( "Cannot start the server, it is already started",
                            new RuntimeException( "Cannot restart server singleton" ) ) );
        } catch ( IntellectualServerStartException e )
        {
            e.printStackTrace();
            return;
        }

        CrushEngine.getInstance().load();

        // Load Plugins
        this.loadPlugins();
        EventManager.getInstance().bake();

        this.log( Message.CALLING_EVENT, "startup" );
        this.handleEvent( new StartupEvent( this ) );

        if ( CoreConfig.MySQL.enabled )
        {
            this.log( Message.MYSQL_INIT );
            this.mysqlConnManager.init();
        }

        // Validating views
        this.log( Message.VALIDATING_VIEWS );
        this.validateViews();

        if ( !CoreConfig.disableViews )
        {
            this.log( Message.LOADING_VIEWS );
            final Map<String, Map<String, Object>> views = configViews.get( "views" );
            Assert.notNull( views );
            views.entrySet().forEach( entry ->
            {
                final Map<String, Object> view = entry.getValue();
                String type = "html", filter = view.get( "filter" ).toString();
                if ( view.containsKey( "type" ) )
                {
                    type = view.get( "type" ).toString();
                }
                final Map<String, Object> options;
                if ( view.containsKey( "options" ) )
                {
                    options = (HashMap<String, Object>) view.get( "options" );
                } else
                {
                    options = new HashMap<>();
                }

                if ( viewBindings.containsKey( type.toLowerCase() ) )
                {
                    final Class<? extends View> vc = viewBindings.get( type.toLowerCase() );
                    try
                    {
                        final View vv = vc.getDeclaredConstructor( String.class, Map.class ).newInstance( filter, options );
                        router.add( vv );
                    } catch ( final Exception e )
                    {
                        e.printStackTrace();
                    }
                }
            } );
        } else
        {
            Message.VIEWS_DISABLED.log();
        }

        if ( mainApplicationStructure != null )
        {
            mainApplicationStructure.registerViews( this );
        }

        router.dump( this );

        if ( !CoreConfig.Cache.enabled )
        {
            log( Message.CACHING_DISABLED );
        } else
        {
            log( Message.CACHING_ENABLED );
        }

        log( Message.STARTING_ON_PORT, CoreConfig.port );
        this.started = true;
        try
        {
            serverSocket = new ServerSocket( CoreConfig.port );
            log( Message.SERVER_STARTED );
        } catch ( final Exception e )
        {
            // throw new RuntimeException("Couldn't start the server...", e);
            boolean run = true;

            int port = CoreConfig.port + 1;
            while ( run )
            {
                try
                {
                    serverSocket = new ServerSocket( port++ );
                    run = false;
                    log( "Specified port was occupied, running on " + ( port - 1 ) + " instead" );

                    CoreConfig.port = port;
                } catch ( final Exception ex )
                {
                    continue;
                }
            }
        }

        if ( CoreConfig.SSL.enable )
        {
            log( Message.STARTING_SSL_ON_PORT, CoreConfig.SSL.port );
            try
            {
                System.setProperty( "javax.net.ssl.keyStore", CoreConfig.SSL.keyStore );
                System.setProperty( "javax.net.ssl.keyStorePassword", CoreConfig.SSL.keyStorePassword );

                final SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
                sslServerSocket = (SSLServerSocket) factory.createServerSocket( CoreConfig.SSL.port );
            } catch ( final Exception e )
            {
                new RuntimeException( "Failed to start HTTPS server", e ).printStackTrace();
            }
        }

        log( Message.ACCEPTING_CONNECTIONS_ON, CoreConfig.hostname + ( CoreConfig.port == 80 ? "" : ":" + CoreConfig.port ) +
                "/'" );
        log( Message.OUTPUT_BUFFER_INFO, CoreConfig.Buffer.out / 1024, CoreConfig.Buffer.in / 1024 );

        this.handleEvent( new ServerReadyEvent( this ) );

        if ( CoreConfig.SSL.enable && sslServerSocket != null )
        {
            new HTTPSThread( sslServerSocket, socketHandler ).start();
        }

        // Main Loop
        for ( ; ; )
        {
            if ( this.stopping )
            {
                log( Message.SHUTTING_DOWN );
                break;
            }
            if ( pause )
            {
                continue;
            }
            try
            {
                socketHandler.acceptSocket( serverSocket.accept() );
            } catch ( final Exception e )
            {
                log( Message.TICK_ERROR );
                e.printStackTrace();
            }
        }
    }

    @Override
    public WorkerProcedure getProcedure()
    {
        return this.workerProcedure;
    }

    @Override
    public void log(Message message, final Object... args)
    {
        this.log( message.toString(), message.getMode(), args );
    }

    @Override
    public synchronized void log(String message, int mode, final Object... args)
    {
        // This allows us to customize what messages are
        // sent to the logging screen, and thus we're able
        // to limit to only error messages or such
        if ( (mode == LogModes.MODE_DEBUG && !CoreConfig.debug) || mode < LogModes.lowestLevel || mode > LogModes
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
        for ( final Object a : args )
        {
            message = message.replaceFirst( "%s", a.toString() );
        }
        logWrapper.log( CoreConfig.logPrefix, prefix, TimeUtil.getTimeStamp(), message,
                Thread.currentThread().getName() );
    }

    @Override
    public CacheManager getCacheManager()
    {
        return cacheManager;
    }

    @Override
    public LogWrapper getLogWrapper()
    {

        return logWrapper;
    }

    @Override
    public ConfigurationFile getTranslations()
    {

        return translations;
    }

    @Override
    public File getCoreFolder()
    {
        return coreFolder;
    }

    @Override
    public boolean isStandalone()
    {
        return standalone;
    }

    @Override
    public Map<String, Class<? extends View>> getViewBindings()
    {
        return viewBindings;
    }

    @Override
    public WorkerProcedure getWorkerProcedure()
    {
        return workerProcedure;
    }

    @Override
    public SimpleSocketHandler getSocketHandler()
    {
        return socketHandler;
    }

    @Override
    public boolean isSilent()
    {
        return silent;
    }

    @Override
    public boolean isPause()
    {
        return pause;
    }

    @Override
    public boolean isStarted()
    {
        return started;
    }

    @Override
    public AccountManager getGlobalAccountManager()
    {
        return globalAccountManager;
    }

    @Override

    public synchronized void log(String message, final Object... args)
    {
        this.log( message, LogModes.MODE_INFO, args );
    }

    @Override
    public void log(final LogProvider provider, String message, final Object... args)
    {
        for ( final Object a : args )
        {
            message = message.replaceFirst( "%s", a.toString() );
        }

        logWrapper.log( CoreConfig.logPrefix, provider.getLogIdentifier(), TimeUtil.getTimeStamp(),
                message, Thread.currentThread().getName() );
    }

    @Override
    public synchronized void stopServer()
    {
        Message.SHUTTING_DOWN.log();
        EventManager.getInstance().handle( new ShutdownEvent( this ) );

        try
        {
            serverSocket.close();
            if ( CoreConfig.SSL.enable )
            {
                sslServerSocket.close();
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

        if ( standalone )
        {
            System.exit( 0 );
        }
    }

    @Override
    public SessionManager getSessionManager()
    {
        return this.sessionManager;
    }

    @Override
    public Router getRouter()
    {
        return router;
    }

    @Override
    public boolean isStopping()
    {
        return this.stopping;
    }

    @Override
    public boolean isPaused()
    {
        return this.pause;
    }

    @Override
    public RequestHandler createSimpleRequestHandler(final String filter, final BiConsumer<Request, Response> generator)
    {
        return this.getRouter().add( new SimpleRequestHandler( filter, generator ) );
    }
}
