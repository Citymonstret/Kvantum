/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
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
package com.plotsquared.iserver.extra.accounts;

import com.plotsquared.iserver.crush.syntax.VariableProvider;
import com.plotsquared.iserver.util.Assert;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.UUID;

public class Account implements VariableProvider
{

    private int id;
    private UUID uuid;
    private String username;
    private byte[] password;
    private byte[] salt;

    public Account(final int id, final String username, final byte[] password, final byte[] salt)
    {
        Assert.notEmpty( username );
        Assert.notEmpty( password );
        Assert.notEmpty( salt );
        Assert.isPositive( id );

        this.id = id;
        this.username = username;
        this.password = password;
        this.salt = salt;
        this.uuid = UUID.randomUUID();
    }

    public String getUsername()
    {
        return username;
    }

    public byte[] getPassword()
    {
        return password;
    }

    public boolean passwordMatches(final String password)
    {
        Assert.notEmpty( password );

        try
        {
            final byte[] encryptedPassword = PasswordUtil.encryptPassword( password, salt );
            return Arrays.equals( encryptedPassword, this.password );
        } catch ( final NoSuchAlgorithmException | InvalidKeySpecException e )
        {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return this.id;
    }

    public int getID()
    {
        return this.id;
    }

    private UUID getUUID()
    {
        return this.uuid;
    }

    @Override
    public boolean equals(Object o)
    {
        return ( o instanceof Account ) && ( (Account) o ).getUUID().equals( getUUID() );
    }

    @Override
    public boolean contains(String variable)
    {
        return Arrays.asList( "username", "id" ).contains( variable );
    }

    @Override
    public Object get(String variable)
    {
        switch ( variable )
        {
            case "username":
                return getUsername();
            case "id":
                return getID();
            default:
                return null;
        }
    }

    byte[] getSalt()
    {
        return salt;
    }
}
