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
package xyz.kvantum.server.api.account.roles;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * HashSet implementation of {@link AccountRole}
 */
public class SimpleAccountRole extends AccountRole
{

    private final Set<String> permissionSet = new HashSet<>();

    /**
     * Construct a new account role
     *
     * @param roleIdentifier Unique role identifier
     */
    protected SimpleAccountRole(final String roleIdentifier)
    {
        super( roleIdentifier );
    }

    @Override
    public boolean hasPermission(final String permissionKey)
    {
        return this.permissionSet.contains( permissionKey.toLowerCase( Locale.ENGLISH ) );
    }

    @Override
    public boolean addPermission(final String permissionKey)
    {
        return !this.hasPermission( permissionKey ) && this.permissionSet.add( permissionKey );
    }

    @Override
    public boolean removePermission(final String permissionKey)
    {
        return this.hasPermission( permissionKey ) && this.permissionSet.remove( permissionKey );
    }
}
