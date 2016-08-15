import com.plotsquared.iserver.files.FileSystem;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class FileSystemTest
{

    @Test(expected = FileSystem.IllegalPathException.class)
    public void testIllegalPath()
    {
        final FileSystem system = new FileSystem( new File( "./" ) );
        system.getPath( ".." );
    }

    @Test
    public void testListFiles()
    {
        final FileSystem system = new FileSystem( new File( "./" ) );
        Assert.assertTrue( system.getPath( "example" ).getSubPaths().length > 0 );
    }

}
