package xyz.kvantum.server.api.views.staticviews.converters;

import xyz.kvantum.server.api.response.Header;
import xyz.kvantum.server.api.response.Response;
import xyz.kvantum.server.api.views.staticviews.OutputConverter;

public class XmlConverter extends OutputConverter<String>
{

    protected XmlConverter()
    {
        super( "xml", String.class );
    }

    @Override
    protected Response generateResponse(final String input)
    {
        final Response response = new Response();
        response.getHeader().set( Header.HEADER_CONTENT_TYPE, Header.CONTENT_TYPE_XML );
        response.getHeader().set( Header.X_CONTENT_TYPE_OPTIONS, "nosniff" );
        response.getHeader().set( Header.X_FRAME_OPTIONS, "deny" );
        response.setContent( input );
        return response;
    }
}
