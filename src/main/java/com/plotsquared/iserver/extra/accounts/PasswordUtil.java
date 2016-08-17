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

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public final class PasswordUtil
{

    private static final String ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final int DERIVED_KEY_LENGTH = 160;
    private static final int ITERATIONS = 5000;

    public static byte[] encryptPassword(final String password, final byte[] salt) throws NoSuchAlgorithmException,
            InvalidKeySpecException
    {
        final KeySpec spec = new PBEKeySpec( password.toCharArray(), salt, ITERATIONS, DERIVED_KEY_LENGTH );
        return SecretKeyFactory.getInstance( ALGORITHM ).generateSecret( spec ).getEncoded();
    }

    public static byte[] getSalt() throws NoSuchAlgorithmException
    {
        final SecureRandom random = SecureRandom.getInstance( "SHA1PRNG" );
        final byte[] salt = new byte[8];
        random.nextBytes( salt);
        return salt;
    }

}
