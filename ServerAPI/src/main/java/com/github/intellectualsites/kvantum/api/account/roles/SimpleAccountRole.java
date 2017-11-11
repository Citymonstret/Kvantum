package com.github.intellectualsites.kvantum.api.account.roles;

import java.util.HashSet;
import java.util.Set;

/**
 * HashSet implementation of {@link AccountRole}
 */
public class SimpleAccountRole extends AccountRole
{

    private final Set<String> permissionSet = new HashSet<>();

    protected SimpleAccountRole(final String roleIdentifier)
    {
        super( roleIdentifier );
    }

    @Override
    public boolean hasPermission(final String permissionKey)
    {
        return this.permissionSet.contains( permissionKey.toLowerCase() );
    }

    @Override
    public void addPermission(final String permissionKey)
    {
        if ( this.hasPermission( permissionKey ) )
        {
            return;
        }
        this.permissionSet.add( permissionKey );
    }

    @Override
    public void removePermission(final String permissionKey)
    {
        if ( this.hasPermission( permissionKey ) )
        {
            this.permissionSet.remove( permissionKey );
        }
    }
}
