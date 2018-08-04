/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2018 IntellectualSites
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

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.core.Kvantum;
import xyz.kvantum.server.api.util.RequestManager;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@TestInstance( TestInstance.Lifecycle.PER_CLASS )
public class GenericServerTest
{

    protected File temporaryFolder;
    protected Kvantum serverInstance;

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
                .logWrapper( new DefaultLogWrapper() ).router( requestManager ).serverSupplier( StandaloneServer::new )
                .build();
        assertNotNull( serverContext );

        final Optional<Kvantum> serverOptional = serverContext.create();
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
