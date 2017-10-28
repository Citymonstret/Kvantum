package com.github.intellectualsites.iserver.implementation;

import com.github.intellectualsites.iserver.api.core.IntellectualServer;
import com.github.intellectualsites.iserver.api.logging.LogWrapper;
import com.github.intellectualsites.iserver.api.matching.Router;
import com.github.intellectualsites.iserver.api.util.Assert;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.io.File;
import java.util.Optional;

/**
 * <p>
 * This class is used when initializing a new {@link ServerContext} instance.
 * </p>
 * <p>
 * ServerContext instances are created using {@link ServerContextBuilder}. A new
 * {@link ServerContextBuilder} can be initialized using either {@link ServerContext#builder()}
 * or by using {@code new ServerContextBuilder()}
 * </p>
 */
@Getter
@Builder
public class ServerContext
{

    @Builder.Default
    private boolean standalone = false;
    @NonNull
    private File coreFolder;
    @NonNull
    private LogWrapper logWrapper;
    @NonNull
    private Router router;

    /**
     * Creates a server instance using this context.
     * Will print any exceptions, but if the server
     * cannot be initialized a null optional will
     * be returned.
     */
    public final Optional<IntellectualServer> create()
    {
        Assert.equals( coreFolder.getAbsolutePath().indexOf( '!' ) == -1, true,
                "Cannot use a folder with '!' path as core folder" );
        Server server = null;
        try
        {
            server = new Server( standalone, coreFolder, logWrapper, router );
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }
        return Optional.ofNullable( server );
    }

}
