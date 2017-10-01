/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
 * <p>
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

import com.github.intellectualsites.iserver.api.matching.ViewPattern;
import org.junit.Assert;
import org.junit.Test;

public class ViewMatcherTest
{

    @Test
    public void testMatches()
    {
        final ViewPattern pattern1 = new ViewPattern( "<file>[extension]" );
        final ViewPattern pattern2 = new ViewPattern( "public/<required>/[optional]" );

        final String in1 = "test.html";
        final String in2 = "/public/foo/";
        final String in3 = "/public/foo/bar";
        final String in4 = "/public/foo/bar.html";
        final String in5 = "test";
        final String in6 = "";

        Assert.assertNotNull( pattern1 + ": " + in1, pattern1.matches( in1 ) );
        Assert.assertNotNull( pattern2 + ": " + in2, pattern2.matches( in2 ) );
        Assert.assertNotNull( pattern2 + ": " + in3, pattern2.matches( in3 ) );
        Assert.assertNotNull( pattern1 + ": " + in5, pattern1.matches( in5 ) );

        Assert.assertNull( pattern2 + ": " + in4, pattern2.matches( in4 ) );
        // Assert.assertNull( pattern1 + ": " + in6, pattern1.matches( in6 ) );
    }

}
