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
package xyz.kvantum.server.api.account.roles.defaults;

import xyz.kvantum.server.api.account.roles.SimpleAccountRole;

/**
 * Default account role that is permitted to do everything
 * {@inheritDoc}
 */
@SuppressWarnings("ALL") final public class Administrator extends SimpleAccountRole {

    public static final String ADMIN_IDENTIFIER = "Administrator";
    public static final Administrator instance = new Administrator();

    protected Administrator() {
        super(ADMIN_IDENTIFIER);
    }

    @Override public boolean hasPermission(final String permissionKey) {
        return true;
    }

    @Override public boolean addPermission(final String permissionKey) {
        // Cannot add administrator permission
        return false;
    }

    @Override public boolean removePermission(final String permissionKey) {
        // Cannot remove administrator permission
        return false;
    }

}
