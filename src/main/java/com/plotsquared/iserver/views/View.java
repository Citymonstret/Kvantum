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

package com.plotsquared.iserver.views;

import com.plotsquared.iserver.core.Server;
import com.plotsquared.iserver.matching.ViewPattern;
import com.plotsquared.iserver.object.Request;
import com.plotsquared.iserver.object.Response;
import com.plotsquared.iserver.util.Assert;
import com.plotsquared.iserver.util.Context;
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
                this.folder = new File( getOption( "folder" ).toString() );
            } else if ( relatedFolderPath != null )
            {
                this.folder = new File( Context.coreFolder, relatedFolderPath );
            } else
            {
                this.folder = new File( Context.coreFolder, "/" + internalName );
            }
            if ( !folder.exists() )
            {
                if ( !folder.mkdirs() )
                {
                    System.out.println( "Couldn't create the " + internalName + " folder..." );
                }
            }
        }
        return this.folder;
    }

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
            String t = variables.get( "file" );
            n = n.replace( "{1}", variables.get( "folder" ) );
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
            n = n.replace( "{2}", variables.get( "file" ) );
            if ( variables.containsKey( "extension" ) )
            {
                n = n.replace( "{3}", variables.get( "extension" ).replace( ".", "" ) );
            } else
            {
                n = n.replace( "{3}", "" );
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
            if ( containsOption( "buffer" ) )
            {
                this.buffer = getOption( "buffer" );
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
            request.addMeta( "variables", map );
        }
        Server.getInstance().log( "Didn't match: " + viewPattern + " | " + ( map == null ) );
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
