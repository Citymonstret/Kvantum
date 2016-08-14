import com.plotsquared.iserver.core.DefaultLogWrapper;
import com.plotsquared.iserver.core.IntellectualServerMain;
import com.plotsquared.iserver.core.Server;
import org.junit.Test;

import java.io.File;
import java.util.Optional;

import static org.junit.Assert.assertTrue;

public class BootstrapTest
{

    @Test
    public void serverCreation()
    {
        Optional<Server> serverOptional = IntellectualServerMain.create( true, new File( "." ), new DefaultLogWrapper
                () );

        assertTrue( "Server is present", serverOptional.isPresent() );
    }

}
