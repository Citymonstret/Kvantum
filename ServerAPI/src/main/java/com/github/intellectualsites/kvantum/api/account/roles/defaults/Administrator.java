package com.github.intellectualsites.kvantum.api.account.roles.defaults;

import com.github.intellectualsites.kvantum.api.account.roles.SimpleAccountRole;

@SuppressWarnings( "ALL" )
final public class Administrator extends SimpleAccountRole
{

    public static final String ADMIN_IDENTIFIER = "Administrator";
    public static final Administrator instance = new Administrator();

    protected Administrator()
    {
        super( ADMIN_IDENTIFIER );
    }

    @Override
    public boolean hasPermission(String permissionKey)
    {
        return true;
    }

    @Override
    public void addPermission(String permissionKey)
    {
        // Cannot add administrator permission
    }

    @Override
    public void removePermission(String permissionKey)
    {
        // Cannot remove administrator permission
    }
}
