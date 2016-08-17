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
package com.plotsquared.iserver.views;

import com.plotsquared.iserver.config.Message;
import com.plotsquared.iserver.core.Server;
import com.plotsquared.iserver.matching.ViewPattern;
import com.plotsquared.iserver.object.Request;
import com.plotsquared.iserver.object.Response;
import com.plotsquared.iserver.util.Assert;
import com.plotsquared.iserver.util.Final;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * These are the view management classes.
 * Essentially, these are how the server
 * is able to output anything at all!
 *
 * @author Citymonstret
 */
@SuppressWarnings("ALL")
public class View extends RequestHandler
{

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
    private final ViewPattern viewPattern;
    protected String relatedFolderPath, fileName, defaultFile;
    private int buffer = -1;
    private String internalFileName, internalDefaultFile;
    private File folder;
    private ViewReturn viewReturn;

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
        this.internalName = internalName;
        this.viewPattern = new ViewPattern( pattern );
        this.viewReturn = viewReturn;
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
    @Final
    final public <T> T getOption(final String s)
    {
        Assert.notNull( s );

        return ( (T) options.get( s ) );
    }

    /**
     * Get all options as a string
     *
     * @return options as string
     */
    @Final
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
    @Final
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

    @Final
    final public void register()
    {
        Server.getInstance().getRequestManager().add( this );
    }

    /**
     * Get the folder used
     * by this view, doesn't
     * have to be used
     *
     * @return File
     */
    protected File getFolder()
    {
        if ( this.folder == null )
        {
            if ( containsOption( "folder" ) )
            {
                this.folder = new File( Server.getInstance().getCoreFolder(), getOption( "folder" ).toString() );
            } else if ( relatedFolderPath != null )
            {
                this.folder = new File( Server.getInstance().getCoreFolder(), relatedFolderPath );
            } else
            {
                this.folder = new File( Server.getInstance().getCoreFolder(), "/" + internalName );
            }
            if ( !folder.exists() )
            {
                if ( !folder.mkdirs() )
                {
                    Message.COULD_NOT_CREATE_FOLDER.log( folder );
                }
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
    protected File getFile(final Request request)
    {
        Assert.isValid( request );

        final Map<String, String> variables = (Map<String, String>) request.getMeta( "viewV" );
        if ( internalFileName == null )
        {
            if ( containsOption( "filepattern" ) )
            {
                this.internalFileName = getOption( "filepattern" );
            } else if ( fileName != null )
            {
                this.internalFileName = fileName;
            } else
            {
                throw new RuntimeException( "getFile called without a filename set!" );
            }
        }
        String n = internalFileName;
        {
            String t = variables.get( VARIABLE_FILE );
            n = n.replace( PATTERN_VARIABLE_FOLDER, variables.get( VARIABLE_FOLDER ) );
            if ( t == null || t.equals( "" ) )
            {
                if ( internalDefaultFile == null )
                {
                    if ( containsOption( "defaultfile" ) )
                    {
                        this.internalDefaultFile = getOption( "defaultfile" );
                    } else if ( defaultFile != null )
                    {
                        this.internalDefaultFile = defaultFile;
                    } else
                    {
                        throw new RuntimeException( "getFile called with empty file path, and no default file set!" );
                    }
                }
                t = internalDefaultFile;
            }
            n = n.replace( PATTERN_VARIABLE_FILE, variables.get( VARIABLE_FILE ) );
            if ( variables.containsKey( VARIABLE_EXTENSION ) )
            {
                n = n.replace( PATTERN_VARIABLE_EXTENSION, variables.get( VARIABLE_EXTENSION ).replace( ".", "" ) );
            } else
            {
                n = n.replace( PATTERN_VARIABLE_EXTENSION, "" );
            }
        }
        return new File( getFolder(), n );
    }

    /**
     * Get the file buffer (if needed)
     *
     * @return file buffer
     */
    @Final
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
    @Final
    @Override
    final public boolean matches(final Request request)
    {
        Assert.isValid( request );

        final Map<String, String> map = viewPattern.matches( request.getQuery().getFullRequest() );
        if ( map != null )
        {
            request.addMeta( CONSTANT_VARIABLES, map );
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
    protected boolean passes(Request request)
    {
        return true;
    }

    @Override
    public String toString()
    {
        return this.viewPattern.toString();
    }

    @Override
    public Response generate(final Request r)
    {
        if ( viewReturn != null )
        {
            return viewReturn.get( r );
        } else
        {
            Response response = new Response( this );
            response.setContent( "<h1>Content!</h1>" );
            return response;
        }
    }

}
