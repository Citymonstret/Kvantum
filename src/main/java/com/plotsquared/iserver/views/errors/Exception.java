package com.plotsquared.iserver.views.errors;

import com.plotsquared.iserver.object.Request;
import com.plotsquared.iserver.object.Response;
import com.plotsquared.iserver.util.FileUtils;
import com.plotsquared.iserver.views.View;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;

public class Exception extends View
{

    private static String template;

    static {
        final File file;
        try
        {
            file = new File( Exception.class.getClassLoader().getResource( "template" + File.separator +
                    "exception" +
                    ".html" ).toURI() );
            template = FileUtils.getDocument( file, 1024 * 1024 );
        } catch ( URISyntaxException e )
        {
            e.printStackTrace();
            template = "ERROR??";
        }
    }

    private final java.lang.Exception in;

    public Exception(final java.lang.Exception in)
    {
        super( "", "exception" );
        this.in = in;
    }

    @Override
    public Response generate(Request request)
    {
        StringWriter sw = new StringWriter();
        in.printStackTrace( new PrintWriter( sw ) );
        return new Response().setContent( template.replace( "{{path}}", request.getQuery().getResource() ).replace(
                        "{{exception}}", in.toString() ).replace( "{{cause}}", sw.toString().replace(System.getProperty
                        ("line.separator"), "<br/>\n") ).replace( "{{message}}", in.getMessage() ) );
    }
}
