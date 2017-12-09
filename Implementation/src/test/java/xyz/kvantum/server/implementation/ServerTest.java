/*
 *
 *    Copyright (C) 2017 IntellectualSites
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

import org.junit.jupiter.api.Test;
import xyz.kvantum.server.api.views.RequestHandler;

import java.io.File;
import java.time.Duration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeout;

class ServerTest extends GenericServerTest
{

    @Test
    void stop()
    {
        assertTimeout( Duration.ofSeconds( 10 ), () -> serverInstance.stopServer() );
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
        assertEquals( new File( temporaryFolder, "kvantum" ).getAbsolutePath(), serverInstance.getCoreFolder()
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
