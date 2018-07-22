package xyz.kvantum.server.implementation;

import java.io.File;
import java.util.Collection;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import xyz.kvantum.server.api.core.Kvantum;
import xyz.kvantum.server.api.util.RequestManager;
import xyz.kvantum.server.api.views.RequestHandler;

@SuppressWarnings({ "unused", "WeakerAccess" }) @UtilityClass public final class QuickStart
{

	/**
	 * Utility method that creates and starts a new standalone server. It will register any request handlers passed as
	 * arguments, and scan for annotations in the case that non request handler objects are passed
	 *
	 * @param classes Request handlers to register in the router
	 * @return Created instance
	 */
	public static Kvantum newStandaloneServer(final Object... classes) throws ServerStartFailureException
	{
		final ServerContext kvantumContext = ServerContext.builder().coreFolder( new File( "./kvantum" ) )
				.logWrapper( new DefaultLogWrapper() ).router( RequestManager.builder().build() ).standalone( true )
				.serverSupplier( StandaloneServer::new ).build();
		final Optional<Kvantum> kvantumOptional = kvantumContext.create();
		if ( !kvantumOptional.isPresent() )
		{
			throw new ServerStartFailureException( new IllegalStateException( "Failed to create server instance" ) );
		}
		final Kvantum kvantum = kvantumOptional.get();
		for ( final Object object : classes )
		{
			if ( object == null )
			{
				throw new NullPointerException( "Passed object is null. Not suitable for routing." );
			}
			if ( object instanceof RequestHandler )
			{
				kvantum.getRouter().add( ( RequestHandler ) object );
			} else
			{
				final Collection<? extends RequestHandler> added = kvantum.getRouter().scanAndAdd( object );
				if ( added.isEmpty() )
				{
					throw new IllegalArgumentException( "No views declarations found in " + object );
				}
			}
		}
		final boolean status;
		try
		{
			status = kvantum.start();
		} catch ( final Exception e )
		{
			throw new ServerStartFailureException( e );
		}
		if ( !status )
		{
			throw new ServerStartFailureException();
		}
		return kvantum;
	}

	public static final class ServerStartFailureException extends RuntimeException
	{

		ServerStartFailureException(final Exception e)
		{
			super( e );
		}

		ServerStartFailureException()
		{
			super();
		}

	}

}
