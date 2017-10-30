package com.github.intellectualsites.iserver.implementation;

import com.github.intellectualsites.iserver.api.config.CoreConfig;
import com.github.intellectualsites.iserver.api.core.IntellectualServer;
import com.github.intellectualsites.iserver.api.util.RequestManager;
import com.github.intellectualsites.iserver.api.views.RequestHandler;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeout;

class ServerTest extends com.github.intellectualsites.iserver.implementation.GenericServerTest
{

    @Test
    void stop()
    {
        assertTimeout( Duration.ofSeconds( 10 ), () -> serverInstance.stopServer() );
    }

    @Test
    void getAccountManager()
    {
        assertTrue( serverInstance.getAccountManager().isPresent() );
    }

    @Test
    void createSimpleRequestHandler()
    {
        RequestHandler created = serverInstance.createSimpleRequestHandler( "/", (request, response ) -> {} );
        assertNotNull( created );
    }

    @Test
    void getInstance()
    {
        assertNotNull( Server.getInstance() );
        assertEquals( serverInstance, Server.getInstance() );
    }

    @Test
    void getLogWrapper()
    {
        assertNotNull( serverInstance.getLogWrapper() );
        assertTrue( serverInstance.getLogWrapper() instanceof DefaultLogWrapper );
    }

    @Test
    void isStandalone()
    {
        assertTrue( serverInstance.isStandalone() );
    }

    @Test
    void getSocketHandler()
    {
        assertNotNull( serverInstance.getSocketHandler() );
    }

    @Test
    void getMetrics()
    {
        assertNotNull( serverInstance.getMetrics() );
    }

    @Test
    void getCacheManager()
    {
        assertNotNull( serverInstance.getCacheManager() );
    }

    @Test
    void isSilent()
    {
        assertFalse( serverInstance.isSilent() );
    }

    @Test
    void getRouter()
    {
        assertNotNull( serverInstance.getRouter() );
    }

    @Test
    void getSessionManager()
    {
        assertNotNull( serverInstance.getSessionManager() );
    }

    @Test
    void getTranslations()
    {
        assertNotNull( serverInstance.getTranslations() );
    }

    @Test
    void getCoreFolder()
    {
        assertEquals( new File( temporaryFolder, ".iserver" ).getAbsolutePath(), serverInstance.getCoreFolder()
                .getAbsolutePath() );
    }

    @Test
    void isPaused()
    {
        assertFalse( serverInstance.isPaused() );
    }

    @Test
    void isStopping()
    {
        assertFalse( serverInstance.isStopping() );
    }

    @Test
    void isStarted()
    {
        assertFalse( serverInstance.isStarted() );
    }

    @Test
    void getGlobalAccountManager()
    {
        if ( serverInstance.isStandalone() )
        {
            assertNotNull( serverInstance.getAccountManager() );
        }
    }

    @Test
    void getFileSystem()
    {
        assertNotNull( serverInstance.getFileSystem() );
    }

    @Test
    void getCommandManager()
    {
        assertNotNull( serverInstance.getCommandManager() );
    }

}
