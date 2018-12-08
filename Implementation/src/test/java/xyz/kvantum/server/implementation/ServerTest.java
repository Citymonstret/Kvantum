/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2018 Alexander Söderberg
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
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.views.RequestHandler;

import java.time.Duration;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeout;

class ServerTest extends GenericServerTest {

    @Test void stop() {
        assertTimeout(Duration.ofSeconds(10), () -> serverInstance.stopServer());
    }

    @Test void createSimpleRequestHandler() {
        RequestHandler created =
            serverInstance.createSimpleRequestHandler("/", (request, response) -> {
            });
        assertNotNull(created);
    }

    @Test void getInstance() {
        assertNotNull(ServerImplementation.getImplementation());
        assertEquals(serverInstance, ServerImplementation.getImplementation());
    }

    @Test void getLogWrapper() {
        assertNotNull(serverInstance.getLogWrapper());
        assertTrue(serverInstance.getLogWrapper() instanceof DefaultLogWrapper);
    }

    @Test void isStandalone() {
        assertTrue(serverInstance.isStandalone());
    }

    @Test void getCacheManager() {
        assertNotNull(serverInstance.getCacheManager());
    }

    @Test void isSilent() {
        assertFalse(serverInstance.isSilent());
    }

    @Test void getRouter() {
        assertNotNull(serverInstance.getRouter());
    }

    @Test void getSessionManager() {
        assertNotNull(serverInstance.getSessionManager());
    }

    @Test void getTranslations() {
        assertNotNull(serverInstance.getTranslationManager());
    }

    @Test void isPaused() {
        assertFalse(serverInstance.isPaused());
    }

    @Test void isStopping() {
        assertFalse(serverInstance.isStopping());
    }

    @Test void isStarted() {
        assertFalse(serverInstance.isStarted());
    }

    @Test void getFileSystem() {
        assertNotNull(serverInstance.getFileSystem());
    }

    @Test void getCommandManager() {
        assertNotNull(serverInstance.getCommandManager());
    }

}
