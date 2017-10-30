package com.github.intellectualsites.iserver.implementation;

import com.github.intellectualsites.iserver.api.config.CoreConfig;
import com.github.intellectualsites.iserver.api.core.IntellectualServer;
import com.github.intellectualsites.iserver.api.util.RequestManager;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@TestInstance( TestInstance.Lifecycle.PER_CLASS )
public class GenericServerTest
{

    protected File temporaryFolder;
    protected IntellectualServer serverInstance;

    private File getFileIfNotExists(final String name)
    {
        final File file = new File( name );
        if ( file.exists() )
        {
            return getFileIfNotExists( name + ( 'a' + ( char )( Math.random() * 26 ) ) );
        }
        return file;
    }

    @BeforeAll
    void initAll()
    {
        this.temporaryFolder = getFileIfNotExists( "temporaryFolder" );
        if ( !this.temporaryFolder.mkdir() )
        {
            System.out.println( "ERROR: Failed to create temporary folder: " + temporaryFolder );
            System.exit( -1 );
        }

        CoreConfig.setPreConfigured( true );
        CoreConfig.exitOnStop = false;

        final ServerContext.ServerContextBuilder serverContextBuilder = ServerContext.builder();
        assertNotNull( serverContextBuilder );

        final RequestManager.RequestManagerBuilder requestManagerBuilder = RequestManager.builder();
        assertNotNull( requestManagerBuilder );

        final RequestManager requestManager = requestManagerBuilder.build();
        assertNotNull( requestManager );
        assertNotNull( requestManager.getError404Generator() );

        final ServerContext serverContext = serverContextBuilder.standalone( true ).coreFolder( temporaryFolder )
                .logWrapper( new DefaultLogWrapper() ).router( requestManager ).build();
        assertNotNull( serverContext );

        final Optional<IntellectualServer> serverOptional = serverContext.create();
        assertTrue( serverOptional.isPresent() );

        this.serverInstance = serverOptional.get();
    }

    @AfterAll
    void tearDownAll()
    {
        try
        {

            FileUtils.deleteDirectory( temporaryFolder );
        } catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

}
