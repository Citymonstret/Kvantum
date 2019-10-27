/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2019 Alexander Söderberg
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
package xyz.kvantum.server.api.event;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SimpleEventBusTest {

    private static boolean indicateSuccess = false;

    private SimpleEventBus createEventBus() {
        return new SimpleEventBus();
    }

    @Test void createListeners() {
        final SimpleEventBus eventBus = createEventBus();
        eventBus.registerListeners(new TestListenerClass());
        Assertions.assertEquals(1, eventBus.getMethods(String.class.getName()).size());
    }

    @Test void throwAsync() {
        try {
            final EventBus bus = createEventBus();
            bus.registerListeners(new TestListenerClass());
            Assertions.assertDoesNotThrow(() -> {
                bus.throwAsync("Hello World").get();
            });
            Assertions.assertTrue(indicateSuccess);
        } finally {
            indicateSuccess = false;
        }
    }

    @Test void throwSync() {
        try {
            final EventBus bus = createEventBus();
            bus.registerListeners(new TestListenerClass());
            final String event = "Hello World!";
            Assertions.assertEquals(event, bus.throwSync(event));
            Assertions.assertTrue(indicateSuccess);
        } finally {
            indicateSuccess = false;
        }
    }


    public static final class TestListenerClass {
        @Listener public void onString(final String string) {
            indicateSuccess = true;
        }

    }
}
