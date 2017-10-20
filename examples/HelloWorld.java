package your.package;

import com.plotsquared.iserver.DefaultLogWrapper;
import com.plotsquared.iserver.IntellectualServerMain;
import com.plotsquared.iserver.api.core.IntellectualServer;
import com.plotsquared.iserver.api.util.RequestManager;

import java.io.File;
import java.util.Optional;

public class HelloWorld
{

    public static void main(final String[] args)
    {
        final Optional<IntellectualServer> serverOptional = IntellectualServerMain.create(
                /* standalone application? */ true,
                /* working folder */ new File( "./HelloWorld" ),
                /* log manager */ new DefaultLogWrapper(),
                /* request router */ new RequestManager()
        );
        if ( !serverOptional.isPresent() )
        {
            System.out.println( "Failed to start the server... Check terminal for information!" );
            System.exit( -1 );
        }
        else
        {
            final IntellectualServer server = serverOptional.get();
            server.createSimpleRequestHandler( "[file]", (request, response) -> response.setContent( "Hello World!" ) );
            server.start();
        }
    }

}
