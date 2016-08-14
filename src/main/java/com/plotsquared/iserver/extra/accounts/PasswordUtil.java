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
