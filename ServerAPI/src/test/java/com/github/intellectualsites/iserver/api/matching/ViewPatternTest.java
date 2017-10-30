package com.github.intellectualsites.iserver.api.matching;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

class ViewPatternTest
{

    @Test
    void matches()
    {
        final ViewPattern pattern1 = new ViewPattern( "user/<username>" );
        final ViewPattern pattern2 = new ViewPattern( "news/[page]" );
        final ViewPattern pattern3 = new ViewPattern( "user/<username>/posts/[page]" );

        Assert.assertNotNull( pattern1.matches( "user/Username" ) );
        Assert.assertNull( pattern1.matches( "user/Username/other" ) );

        Assert.assertNotNull( pattern2.matches("news") );
        Assert.assertNotNull( pattern2.matches( "news/foo" ) );
        Assert.assertNull( pattern2.matches( "news/foo/bar" ) );

        Assert.assertNotNull( pattern3.matches( "user/Username/posts" ) );
        Assert.assertNotNull( pattern3.matches("user/Username/posts/10") );
        Assert.assertNull( pattern3.matches( "user/" ) );
        Assert.assertNull( pattern3.matches( "user/Username/posts/foo/bar" ) );
    }

}
