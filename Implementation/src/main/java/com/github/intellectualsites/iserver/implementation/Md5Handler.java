package com.github.intellectualsites.iserver.implementation;

import com.github.intellectualsites.iserver.api.config.Message;
import com.github.intellectualsites.iserver.api.util.Assert;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

final class Md5Handler
{

    private final Base64.Encoder encoder;
    private final MessageDigest digest;

    Md5Handler()
    {
        this.encoder = Base64.getMimeEncoder();
        MessageDigest temporary = null;
        try
        {
            temporary = MessageDigest.getInstance( "MD5" );
        } catch ( final NoSuchAlgorithmException e )
        {
            Message.MD5_DIGEST_NOT_FOUND.log( e.getMessage() );
        }
        digest = temporary;
    }

    /**
     * MD5-ify the input
     *
     * @param input Input text to be digested
     * @return md5-ified digested text
     */
    String generateChecksum(final byte[] input)
    {
        Assert.notNull( input );

        // Make sure that the buffer is clean
        digest.reset();
        // Update the digest with the current input
        digest.update( input );
        // Now encode it, yay
        return new String( encoder.encode( digest.digest() ) );
    }

}
