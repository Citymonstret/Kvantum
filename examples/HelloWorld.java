package your.package;

import com.github.intellectualsites.iserver.api.core.IntellectualServer;
import com.github.intellectualsites.iserver.api.util.RequestManager;

import java.io.File;
import java.util.Optional;


public class HelloWorld
{

    public static void main(final String[] args)
    {
        final ServerContext serverContext = ServerContext.builder()
                .standalone( true )
                .coreFolder( new File( "./HelloWorld" ) )
                .logWrapper( new DefaultLogWrapper() )
                .router( RequestManager.builder().build() ).build();

        final Optional<IntellectualServer> serverOptional = serverContext.create();

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
