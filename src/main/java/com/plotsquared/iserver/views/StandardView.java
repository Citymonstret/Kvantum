package com.plotsquared.iserver.views;

import com.plotsquared.iserver.object.Request;
import com.plotsquared.iserver.object.Response;
import com.plotsquared.iserver.object.cache.CacheApplicable;
import com.plotsquared.iserver.util.GenericViewUtil;

import java.io.File;
import java.util.Map;

public class StandardView extends View implements CacheApplicable
{

    public StandardView(String filter, Map<String, Object> options)
    {
        super( filter, "STANDARD", options );
    }

    @Override
    public boolean passes(Request request)
    {
        String fileName, extension;
        final Map<String, String> variables = request.getVariables();

        fileName = variables.get( "file" );
        extension = variables.get( "extension" ).replace( ".", "" );

        if ( extension.isEmpty() )
        {
            if ( containsOption( "defaultExt" ) )
            {
                extension = getOption( "defaultExt" );
            } else
            {
                extension = "html";
            }
        }
        File file = new File( getFolder(), fileName + "." + extension );
        if ( !file.exists() )
        {
            return false;
        }
        request.addMeta( "stdfile", file );
        request.addMeta( "stdext", extension.toLowerCase() );
        return true;
    }

    @Override
    public boolean isApplicable(Request r)
    {
        return true;
    }

    @Override
    public Response generate(final Request r)
    {
        final File file = (File) r.getMeta( "stdfile" );
        final Response response = new Response( this );
        final String extension = r.getMeta( "stdext" ).toString();
        return GenericViewUtil.getGenericResponse( file, r, response, extension, getBuffer() );
    }
}
