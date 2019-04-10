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
package xyz.kvantum.server.implementation;

import lombok.Getter;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.util.ApplicationStructure;
import xyz.kvantum.server.implementation.mysql.MySQLAccountManager;
import xyz.kvantum.server.implementation.mysql.MySQLManager;

public final class MySQLApplicationStructure extends ApplicationStructure {

    @Getter private final MySQLManager databaseManager;

    MySQLApplicationStructure(final String applicationName) {
        super(applicationName);
        this.databaseManager = new MySQLManager(this.applicationName);
        new MySQLAccountManager(this); // Initialize
        Logger.info("Initialized MySQLApplicationStructure: {}", this.applicationName);
    }
}
