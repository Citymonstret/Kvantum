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
package com.github.intellectualsites.kvantum.implementation;

import com.github.intellectualsites.kvantum.api.config.Message;
import com.github.intellectualsites.kvantum.api.util.Assert;

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
