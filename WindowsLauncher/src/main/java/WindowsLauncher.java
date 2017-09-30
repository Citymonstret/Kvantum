import com.plotsquared.iserver.IntellectualServerMain;
import com.plotsquared.iserver.api.util.AutoCloseable;
import dorkbox.systemTray.SystemTray;

public class WindowsLauncher
{

    public static void main(final String[] args)
    {
        final SystemTray systemTray = SystemTray.get();
        if ( systemTray != null )
        {
            try
            {
                new SystemTrayIcon( systemTray ).setup();
            } catch ( Throwable throwable )
            {
                throwable.printStackTrace();
            }
        }
        IntellectualServerMain.main( args );
    }

    private static class SystemTrayIcon extends AutoCloseable
    {

        private final SystemTray systemTray;

        private SystemTrayIcon(final SystemTray systemTray)
        {
            this.systemTray = systemTray;
        }

        private void setup() throws Throwable
        {
            systemTray.setImage( WindowsLauncher.class.getResourceAsStream( "logo.png" ) );
            systemTray.setStatus( "Running" );
            systemTray.setTooltip( "IntellectualServer" );
            systemTray.setEnabled( true );
        }

        @Override
        protected void handleClose()
        {
            systemTray.shutdown();
        }
    }

}
