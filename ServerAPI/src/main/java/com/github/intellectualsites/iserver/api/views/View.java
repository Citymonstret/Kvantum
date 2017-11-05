/*
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2017 IntellectualSites
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
package com.github.intellectualsites.iserver.api.views;

import com.github.intellectualsites.iserver.api.config.CoreConfig;
import com.github.intellectualsites.iserver.api.config.Message;
import com.github.intellectualsites.iserver.api.core.ServerImplementation;
import com.github.intellectualsites.iserver.api.exceptions.IntellectualServerException;
import com.github.intellectualsites.iserver.api.logging.Logger;
import com.github.intellectualsites.iserver.api.matching.FilePattern;
import com.github.intellectualsites.iserver.api.matching.Router;
import com.github.intellectualsites.iserver.api.matching.ViewPattern;
import com.github.intellectualsites.iserver.api.request.Request;
import com.github.intellectualsites.iserver.api.response.Response;
import com.github.intellectualsites.iserver.api.util.Assert;
import com.github.intellectualsites.iserver.files.Path;
import lombok.EqualsAndHashCode;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * These are the view management classes.
 * Essentially, these are how the server
 * is able to output anything at all!
 *
 * @author Citymonstret
 */
@SuppressWarnings("ALL")
@EqualsAndHashCode(of = "internalName", callSuper = false)
public class View extends RequestHandler
{

    private static final String DEFAULT_RESPONSE = "<h1>Hello World!</h1>";

    /**
     * Variable corrensponding to {@link #PATTERN_VARIABLE_FILE}
     */
    public static final String VARIABLE_FILE = "file";
    /**
     * Variable corrensponding to {@link #PATTERN_VARIABLE_FOLDER}
     */
    public static final String VARIABLE_FOLDER = "folder";
    /**
     * Variable corrensponding to {@link #PATTERN_VARIABLE_EXTENSION}
     */
    public static final String VARIABLE_EXTENSION = "extension";

    /**
     * Pattern corrensponding to {@link #VARIABLE_FILE}
     */
    public static final String PATTERN_VARIABLE_FILE = "{file}";
    /**
     * Pattern corrensponding to {@link #VARIABLE_FOLDER}
     */
    public static final String PATTERN_VARIABLE_FOLDER = "{folder}";
    /**
     * Pattern corrensponding to {@link #VARIABLE_EXTENSION}
     */
    public static final String PATTERN_VARIABLE_EXTENSION = "{extension}";

    public static final String CONSTANT_BUFFER = "buffer";
    public static final String CONSTANT_VARIABLES = "variables";

    private final Map<String, Object> options;
    private final String internalName;
    private final UUID uuid;

    private final ViewPattern viewPattern;
    protected String defaultFilePattern = "${file}.${extension}";

    public String relatedFolderPath;
    private Path folder;

    private int buffer = -1;
    private ViewReturn viewReturn;
    private FilePattern filePattern;

    /**
     * The constructor (Without prestored options)
     *
     * @param pattern used to decide whether or not to use this view
     * @see View(String, Map) - This is an alternate constructor
     */
    public View(final String pattern, final String internalName)
    {
        this( pattern, internalName, null );
    }

    /**
     * Constructor with prestored options
     *
     * @param pattern Regex pattern that will decide whether or not to use this view
     * @param options Pre Stored options
     */
    public View(final String pattern, final String internalName, final Map<String, Object> options)
    {
        this( pattern, internalName, options, null );
    }

    public View(final String pattern, final String internalName, final Map<String, Object> options,
                final ViewReturn viewReturn)
    {
        if ( options == null )
        {
            this.options = new HashMap<>();
        } else
        {
            this.options = options;
        }
        if ( options.containsKey( "internalName" ) )
        {
            this.internalName = options.get( "internalName" ).toString();
        } else
        {
            this.internalName = internalName;
        }
        this.viewPattern = new ViewPattern( pattern );
        this.viewReturn = viewReturn;
        this.uuid = UUID.randomUUID();
    }

    protected FilePattern getFilePattern()
    {
        if ( this.filePattern == null )
        {
            if ( this.options.containsKey( "filePattern" ) )
            {
                this.filePattern = FilePattern.compile( this.options.get( "filePattern" ).toString() );
            } else
            {
                this.filePattern = FilePattern.compile( defaultFilePattern );
            }
        }
        return this.filePattern;
    }


    /**
     * Get a stored option
     *
     * @param <T> Type
     * @param s   Key
     * @return (Type Casted) Value
     * @see #containsOption(String) Check if the option exists before getting it
     */
    @SuppressWarnings("ALL")
    final public <T> T getOption(final String s)
    {
        Assert.notNull( s );

        return ( (T) options.get( s ) );
    }

    final public <T> Optional<T> getOptionSafe(final String s)
    {
        Assert.notNull( s );

        if ( options.containsKey( s ) )
        {
            return Optional.of( (T) options.get( s ) );
        }

        return Optional.empty();
    }

    /**
     * Get all options as a string
     *
     * @return options as string
     */
    final public String getOptionString()
    {
        final StringBuilder b = new StringBuilder();
        for ( final Map.Entry<String, Object> e : options.entrySet() )
        {
            b.append( ";" ).append( e.getKey() ).append( "=" ).append( e.getValue().toString() );
        }
        return b.toString();
    }

    /**
     * Check if the option is stored
     *
     * @param s Key
     * @return True if the option is stored, False if it isn't
     */
    final public boolean containsOption(final String s)
    {
        Assert.notNull( s );

        return options.containsKey( s );
    }

    @Override
    public final String getName()
    {
        return this.internalName;
    }

    /**
     * Register the view to the implemented {@link Router}
     */
    final public void register()
    {
        ServerImplementation.getImplementation().getRouter().add( this );
    }

    /**
     * Get the folder used
     * by this view, doesn't
     * have to be used
     *
     * @return File
     */
    protected Path getFolder()
    {
        if ( this.folder == null )
        {
            if ( containsOption( "folder" ) )
            {
                this.folder = ServerImplementation.getImplementation().getFileSystem().getPath( getOption( "folder" ).toString() );
            } else if ( relatedFolderPath != null )
            {
                this.folder = ServerImplementation.getImplementation().getFileSystem().getPath( relatedFolderPath );
            } else
            {
                this.folder = ServerImplementation.getImplementation().getFileSystem().getPath( "/" + internalName );
            }
            if ( !folder.exists() && !folder.create() )
            {
                Message.COULD_NOT_CREATE_FOLDER.log( folder );
            }
        }
        return this.folder;
    }

    /**
     * <p>
     * Get a file from the {@link #getFolder()} folder, based on request
     * variables and the {@link #fileName} or {@link #getOption(String)} "filepattern", pattern
     * </p>
     * <p>
     * This uses {@literal {pattern}}'s: {@literal {file}}, {@literal {folder}} and {@literal {extension}}
     * <br>
     * For example: <pre>/{folder}/{filename}}.{extension}</pre>
     * </p>
     *
     * @param request
     * @return The file (use {@link File#exists()}!)
     * @see #PATTERN_VARIABLE_EXTENSION
     * @see #PATTERN_VARIABLE_FILE
     * @see #PATTERN_VARIABLE_FOLDER
     * @see #VARIABLE_EXTENSION
     * @see #VARIABLE_FILE
     * @see #VARIABLE_FOLDER
     * @see #getFolder()
     */
    protected Path getFile(final Request request)
    {
        Assert.isValid( request );

        if ( request.getMeta( "fileMatcher" ) == null )
        {
            throw new IntellectualServerException( "fileMatcher isn't set" );
        }

        final FilePattern.FileMatcher fileMatcher = (FilePattern.FileMatcher) request.getMeta( "fileMatcher" );

        if ( !fileMatcher.matches() )
        {
            throw new IntellectualServerException( "getFile called when matches = false" );
        }

        if ( CoreConfig.debug )
        {
            ServerImplementation.getImplementation().log( "Translated file name: '%s'", fileMatcher.getFileName() );
        }

        String fileName = fileMatcher.getFileName();

        if ( containsOption( "extensionRewrite" ) )
        {
            if ( CoreConfig.debug )
            {
                Logger.debug( "Rewrite found for : " + toString() );
            }

            final String variableExtension = request.getVariables().get( VARIABLE_EXTENSION );

            final Map<String, Object> rewrite = getOption( "extensionRewrite" );
            if ( rewrite.containsKey( variableExtension ) )
            {
                final String rewritten = rewrite.get( variableExtension ).toString();
                if ( CoreConfig.debug )
                {
                    Logger.debug( "Rewrote %s to %s",
                            variableExtension,
                            rewritten
                    );
                }
                fileName = fileName.replace( variableExtension, rewritten );
            }

        }

        if ( CoreConfig.debug )
        {
            Logger.debug( "Final file name: " + fileName );
        }

        return getFolder().getPath( fileName );
    }

    /**
     * Get the file buffer (if needed)
     *
     * @return file buffer
     */
    final protected int getBuffer()
    {
        if ( this.buffer == -1 )
        {
            if ( containsOption( CONSTANT_BUFFER ) )
            {
                this.buffer = getOption( CONSTANT_BUFFER );
            } else
            {
                this.buffer = 65536; // 64kb
            }
        }
        return this.buffer;
    }

    /**
     * Check if the request URL matches the regex pattern
     *
     * @param request Request, from which the URL should be checked
     * @return True if the request Matches, False if not
     * @see #passes(Request) - This is called!
     */
    @Override
    final public boolean matches(final Request request)
    {
        Assert.isValid( request );

        final Map<String, String> map = viewPattern.matches( request.getQuery().getFullRequest().toLowerCase() );
        if ( map != null )
        {
            request.addMeta( CONSTANT_VARIABLES, map );
        }

        if ( CoreConfig.debug && map == null )
        {
            ServerImplementation.getImplementation().log( "Request: '%s' failed to " +
                            "pass '%s'", request.getQuery().getFullRequest(),
                    viewPattern.toString() );
        }

        return map != null && passes( request );
    }

    /**
     * This is for further testing (... further than regex...)
     * For example, check if a file exists etc.
     *
     * @param request The request from which the URL is fetches
     * @return True if the request matches, false if not
     */
    protected boolean passes(final Request request)
    {
        return true;
    }

    @Override
    public String toString()
    {
        return internalName + "@" + uuid.toString();
    }

    /**
     * Generate a response
     *
     * @param r Incoming request
     * @return Either the view generated by the configured view return, or a generated response.
     */
    @Override
    public Response generate(final Request r)
    {
        if ( viewReturn != null )
        {
            return viewReturn.get( r );
        } else
        {
            return new Response( this ).setContent( DEFAULT_RESPONSE );
        }
    }

    /**
     * Set an internal option
     *
     * @param key   Option key
     * @param value Option value
     */
    public void setOption(final String key, final Object value)
    {
        Assert.notNull( key );
        Assert.notNull( value );

        this.options.put( key, value );
    }
}
