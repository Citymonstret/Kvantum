/*
 * Kvantum is a web server, written entirely in the Java language.
 * Copyright (C) 2017 IntellectualSites
 *
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.github.intellectualsites.kvantum.implementation;

import com.github.intellectualsites.kvantum.api.views.RequestHandler;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Duration;

import static org.junit.Assert.*;
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
        assertEquals( new File( temporaryFolder, ".kvantum" ).getAbsolutePath(), serverInstance.getCoreFolder()
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
