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

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Synchronized;
import xyz.kvantum.files.Extension;
import xyz.kvantum.files.FileSystem;
import xyz.kvantum.files.Path;
import xyz.kvantum.server.api.addon.AddOnManager;
import xyz.kvantum.server.api.config.ConfigurationFile;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.config.Message;
import xyz.kvantum.server.api.config.YamlConfiguration;
import xyz.kvantum.server.api.events.ServerInitializedEvent;
import xyz.kvantum.server.api.logging.LogWrapper;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.scripts.ScriptView;
import xyz.kvantum.server.api.util.Assert;
import xyz.kvantum.server.api.util.FileUtils;
import xyz.kvantum.server.api.util.LambdaUtil;
import xyz.kvantum.server.api.util.MapBuilder;
import xyz.kvantum.server.api.views.CSSView;
import xyz.kvantum.server.api.views.DownloadView;
import xyz.kvantum.server.api.views.HTMLView;
import xyz.kvantum.server.api.views.ImgView;
import xyz.kvantum.server.api.views.JSView;
import xyz.kvantum.server.api.views.StandardView;
import xyz.kvantum.server.api.views.View;
import xyz.kvantum.server.api.views.ViewDetector;
import xyz.kvantum.server.api.views.decorators.CacheDecorator;
import xyz.kvantum.server.implementation.error.KvantumException;
import xyz.kvantum.server.implementation.error.KvantumInitializationException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class StandaloneServer extends SimpleServer
{

    @Getter
    private final Map<String, Class<? extends View>> viewBindings = new HashMap<>();
    private ConfigurationFile configViews;

    /**
     * @param serverContext ServerContext that will be used to initialize the server
     */
    @SneakyThrows
    @SuppressWarnings("WeakerAccess")
    public StandaloneServer(final ServerContext serverContext)
    {
        super( serverContext );

        this.printLicenseInfo();
        this.addDefaultViewBindings();

        //
        // Check through the configuration file and make sure that the values
        // are not weird
        //
        this.validateConfiguration();

        //
        // File Watcher that will invalidate cache entries if files are updated
        //
        ( (IntellectualFileSystem) getFileSystem() ).registerFileWatcher();

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
        // Load view configuration
        //
        if ( !CoreConfig.disableViews )
        {
            this.loadViewConfiguration();
        }
    }

    @SuppressWarnings("WeakerAccess")
    @Synchronized
    public void addViewBinding(final String key, final Class<? extends View> c)
    {
        Assert.notNull( c );
        Assert.notEmpty( key );

        viewBindings.put( key, c );
    }

    @SuppressWarnings("ALL")
    private void validateViews()
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

    private void loadViewConfiguration() throws KvantumException
    {
        try
        {
            this.configViews = new YamlConfiguration( "views", new File( new File( getCoreFolder(), "config" ),
                    "views.yml" ) );
            configViews.loadFile();

            // These are the default views

            if ( !configViews.contains( "views" ) )
            {
                final YamlConfiguration standardFile = new YamlConfiguration( "views",
                        new File( new File( getCoreFolder(), "config" ), "standardViews.yml" ) );
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
                Logger.info( "If you'd rather have the views detected automatically, " +
                        "set `autoDetect: true` in `server.yml`!" );
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
        this.getEventBus().emit( new ServerInitializedEvent( this ) );
    }

    @Override
    protected void onStart()
    {
        //
        // Load standard plugins
        //
        {
            try
            {
                @NonNull final URL url = StandaloneServer.class.getResource( "/addons" );
                if ( CoreConfig.debug )
                {
                    Logger.debug( "Trying to load addons from {}", url );
                }
                final java.nio.file.Path addonFolderPath;
                if ( url.getFile().contains( "!" ) )
                {
                    // inside of jar
                    // MyClass.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()
                    final java.nio.file.Path jarPath = Paths.get( StandaloneServer.class.getProtectionDomain()
                            .getCodeSource().getLocation().getPath() );
                    final java.nio.file.FileSystem fileSystem = FileSystems.newFileSystem( jarPath, null );
                    addonFolderPath = fileSystem.getPath( "/addons" );
                } else
                {
                    // loading from IDE
                    @NonNull final File file = new File( url.getFile() );
                    addonFolderPath = file.toPath();
                }


                Logger.info( "Loading internal addons..." );
                Logger.info( "Skipping: " );
                final Collection<String> skipping = CoreConfig.InternalAddons.disabled;
                //
                // Prevent template engines from loading when they're
                // not enabled
                //
                if ( !CoreConfig.Templates.engine.equals( "CRUSH" ) )
                {
                    skipping.add( "Crush.zip" );
                }
                if ( !CoreConfig.Templates.engine.equals( "JTWIG" ) )
                {
                    skipping.add( "JTwig.zip" );
                }
                if ( !CoreConfig.Templates.engine.equals( "VELOCITY" ) )
                {
                    skipping.add( "Velocity.zip" );
                }
                for ( final String disabled : skipping )
                {
                    Logger.info( "- {}", disabled );
                }

                Files.list( addonFolderPath ).filter( file -> !skipping.contains( file.getFileName().toString() ) )
                        .forEach( file -> {
                            try
                            {
                                Files.copy( file,
                                        new File( new File( getCoreFolder(), "plugins" ), file.getFileName().toString() )
                                                .toPath(), StandardCopyOption.REPLACE_EXISTING );
                            } catch ( IOException e )
                            {
                                e.printStackTrace();
                            }
                        } );
            } catch ( final Exception e )
            {
                e.printStackTrace();
            }
        }

        this.loadPlugins();

        // Validating views
        this.log( Message.VALIDATING_VIEWS );
        this.validateViews();
        loadWebJars:
        if ( CoreConfig.loadWebJars )
        {
            try
            {
                @NonNull final URL url = StandaloneServer.class.getResource( "/META-INF/resources/webjars" );
                final java.nio.file.Path webjarsFolderPath;
                if ( url.getFile().contains( "!" ) )
                {
                    // inside of jar
                    // MyClass.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()
                    final java.nio.file.Path jarPath = Paths.get( StandaloneServer.class.getProtectionDomain()
                            .getCodeSource().getLocation().getPath() );
                    final java.nio.file.FileSystem fileSystem = FileSystems.newFileSystem( jarPath, null );
                    webjarsFolderPath = fileSystem.getPath( "/META-INF/resources/webjars" );
                } else
                {
                    // loading from IDE
                    @NonNull final File file = new File( url.getFile() );
                    webjarsFolderPath = file.toPath();
                }
                if ( !Files.exists( webjarsFolderPath ) )
                {
                    Logger.warn( "`loadWebJars`= true, but no webjars resources present..." );
                    break loadWebJars;
                }
                final FileSystem webjarsFileSystem = new FileSystem( webjarsFolderPath, getFileSystem()
                        .getFileCacheManager() );
                if ( CoreConfig.debug )
                {
                    webjarsFileSystem.getPath( "" ).getSubPaths().forEach( path ->
                            Logger.info( "Found path: " + path ) );
                }
                if ( !CoreConfig.autoDetectViews )
                {
                    final ViewDetector viewDetector = new ViewDetector( "webjars/",
                            webjarsFileSystem.getPath( "" ), Collections.emptyList() );
                    final int loaded = viewDetector.loadPaths();
                    Logger.info( "Found {} folders", loaded );
                    viewDetector.getPaths().forEach( p -> Logger.info( "- {}", p.toString() ) );
                    viewDetector.generateViewEntries();
                    Logger.info( "Loaded {} webjars resource(s)", viewDetector.getViewEntries().size() );
                    final CacheDecorator decorator = CacheDecorator.builder()
                            .maxAge( TimeUnit.DAYS.toMillis( 365 ) ).cachePublic( true ).build();
                    new ViewLoader( () -> webjarsFileSystem, Collections.singleton( decorator.getDecorator() ),
                            viewDetector.getViewEntries(), this.viewBindings );
                }
            } catch ( final Exception e )
            {
                e.printStackTrace();
            }
        }
        if ( CoreConfig.autoDetectViews )
        {
            Logger.info( "Auto-Detecting views..." );
            final Collection<String> ignore = Arrays.asList(
                    "config",
                    "log",
                    "plugins",
                    "storage",
                    "templates" );
            final ViewDetector viewDetector = new ViewDetector( "",
                    getFileSystem().getPath( "" ), ignore );
            final int loaded = viewDetector.loadPaths();
            Logger.info( "Found {} folders", loaded );
            viewDetector.getPaths().forEach( p -> Logger.info( "- {}", p.toString() ) );
            viewDetector.generateViewEntries();
            new ViewLoader( this::getFileSystem, Collections.emptyList(),
                    viewDetector.getViewEntries(), this.viewBindings );
        } else if ( !CoreConfig.disableViews )
        {
            this.log( Message.LOADING_VIEWS );
            this.log( "" );
            new ViewLoader( configViews, this::getFileSystem, Collections.emptyList(), this.viewBindings );
        } else
        {
            Message.VIEWS_DISABLED.log();
        }
    }

    @SneakyThrows
    @SuppressWarnings("unused")
    private <T> void emit(@NonNull final T event)
    {
        this.getEventBus().emit( event );
    }

    private void loadPlugins()
    {
        if ( isStandalone() )
        {
            final File file = new File( getCoreFolder(), "plugins" );
            if ( !file.exists() && !file.mkdirs() )
            {
                log( Message.COULD_NOT_CREATE_PLUGIN_FOLDER, file );
                return;
            }
            final AddOnManager addOnManager = new AddOnManager( file, Collections.emptyList() );
            addOnManager.load();
            addOnManager.enableAddOns();
        } else
        {
            log( Message.STANDALONE_NOT_LOADING_PLUGINS );
        }
    }

    private void addDefaultViewBindings()
    {
        addViewBinding( "html", HTMLView.class );
        addViewBinding( "css", CSSView.class );
        addViewBinding( "javascript", JSView.class );
        addViewBinding( "img", ImgView.class );
        addViewBinding( "download", DownloadView.class );
        addViewBinding( "std", StandardView.class );
        addViewBinding( "script", ScriptView.class );
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

    private void printLicenseInfo()
    {
        final LogWrapper.LogEntryFormatter prefix = msg -> "> " + msg;
        getLogWrapper().log( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" );
        LambdaUtil.arrayForeach( string -> getLogWrapper().log( prefix, string ),
                "APACHE LICENSE VERSION 2.0:",
                "",
                "Kvantum, Copyright (C) 2017 IntellectualSites",
                "Kvantum comes with ABSOLUTELY NO WARRANTY; for details type `/show w`",
                "This is free software, and you are welcome to redistribute it",
                "under certain conditions; type `/show c` for details.",
                ""
        );
        getLogWrapper().log( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" );
        getLogWrapper().log();
    }

}
