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
package com.github.intellectualsites.kvantum.api.matching;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Map;

class ViewPatternTest
{

    private String getRandomString(final int size)
    {
        final char[] characters = new char[ size ];
        for ( int i = 0; i < size; i++ )
        {
            characters[ i ] = (char) ( 'a' + ( Math.random() * 26 ) );
        }
        return new String( characters );
    }


    @Test
    void matches()
    {
        final String username = getRandomString( 10 );

        final ViewPattern pattern1 = new ViewPattern( "user/<username>" );
        Map<String, String> map = pattern1.matches( "user/" + username );
        Assert.assertNotNull( map );
        Assert.assertTrue( map.containsKey( "username" ) );
        Assert.assertEquals( map.get( "username" ), username );

        Assert.assertNull( pattern1.matches( "user/Username/other" ) );

        final ViewPattern pattern2 = new ViewPattern( "news/[page=0]" );
        map = pattern2.matches( "news" );
        Assert.assertNotNull( map );
        Assert.assertTrue( map.containsKey( "page" ) );
        Assert.assertEquals( "0", map.get( "page" ) );

        map = pattern2.matches( "news/foo" );
        Assert.assertNotNull( map );
        Assert.assertTrue( map.containsKey( "page" ) );
        Assert.assertEquals( map.get( "page" ), "foo" );

        Assert.assertNull( pattern2.matches( "news/foo/bar" ) );

        final ViewPattern pattern3 = new ViewPattern( "user/<username>/posts/[page]" );
        map = pattern3.matches( "user/" + username + "/posts" );
        Assert.assertNotNull( map );
        Assert.assertTrue( map.containsKey( "username" ) );
        Assert.assertEquals( username, map.get( "username" ) );
        Assert.assertFalse( "\"" + map.get( "page" ) + "\" is not supposed to exist",
                map.containsKey( "page" ) );

        map = pattern3.matches( "user/" + username + "/posts/10" );
        Assert.assertNotNull( map );
        Assert.assertTrue( map.containsKey( "username" ) );
        Assert.assertEquals( username, map.get( "username" ) );
        Assert.assertTrue( map.containsKey( "page" ) );
        Assert.assertEquals( "10", map.get( "page" ) );

        Assert.assertNull( pattern3.matches( "user/" ) );
        Assert.assertNull( pattern3.matches( "user/Username/posts/foo/bar" ) );
    }

}
