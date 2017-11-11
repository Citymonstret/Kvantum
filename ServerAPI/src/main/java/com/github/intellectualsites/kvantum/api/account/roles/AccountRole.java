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
