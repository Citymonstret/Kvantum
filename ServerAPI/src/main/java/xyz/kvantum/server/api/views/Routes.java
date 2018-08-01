package xyz.kvantum.server.api.views;

import java.util.function.BiConsumer;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.request.HttpMethod;
import xyz.kvantum.server.api.response.Response;
import xyz.kvantum.server.api.views.requesthandler.SimpleRequestHandler;

@UtilityClass public final class Routes
{

	/**
	 * Register a GET route
	 *
	 * @param filter Route filter
	 * @param function Route generator
	 */
	public static void get(final String filter, final BiConsumer<AbstractRequest, Response> function)
	{
		handle( filter, HttpMethod.GET, function );
	}

	/**
	 * Register a POST route
	 *
	 * @param filter Route filter
	 * @param function Route generator
	 */
	public static void post(final String filter, final BiConsumer<AbstractRequest, Response> function)
	{
		handle( filter, HttpMethod.POST, function );
	}

	private static void handle(@NonNull final String filter, @NonNull final HttpMethod method,
			@NonNull final BiConsumer<AbstractRequest, Response> function)
	{
		ServerImplementation.getImplementation().getRouter()
				.add( SimpleRequestHandler.builder().generator( function ).pattern( filter ).httpMethod( method )
						.build() );
	}

}
