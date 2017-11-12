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
package com.github.intellectualsites.kvantum.api.account.roles;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Roles are attached to accounts and may be used to check
 * whether or not an user is permitted to perform certain actions
 */
@EqualsAndHashCode( of = "roleIdentifier" )
@RequiredArgsConstructor( access = AccessLevel.PROTECTED )
@SuppressWarnings( { "unused", "WeakerAccess" } )
public abstract class AccountRole
{

    @Getter
    private final String roleIdentifier;

    /**
     * Check if the role is permitted to perform an action
     * @param permissionKey Permission key
     * @return boolean indicating whether or not the account has a certain
     *         permission
     */
    public abstract boolean hasPermission(String permissionKey);

    /**
     * Add a permission to the role
     * @param permissionKey Permission key
     */
    public abstract void addPermission(String permissionKey);

    /**
     * Remove a permission from the role
     * @param permissionKey Permission key
     */
    public abstract void removePermission(String permissionKey);

}
