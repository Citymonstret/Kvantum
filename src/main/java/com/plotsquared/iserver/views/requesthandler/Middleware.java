package com.plotsquared.iserver.views.requesthandler;

import com.plotsquared.iserver.object.Request;

/**
 * <p>
 * Middleware is a class responsible for filtering out, and acting on requests,
 * before they are handled by the appropriate views. They can be used to redirect
 * non-authenticated users, or make sure a requested object exists in the database
 * (and much, much more)
 * </p>
 * <p>
 * Middleware is lined up in a sort of chain, by using a special queue.
 * If a middleware breaks the queue, the request will not be served by the view.
 * ( Middleware can redirect requests to other views without continuing the chain )
 * </p>
 * <p>
 * <h1>Creation</h1></br>
 * You just have to extend {@link Middleware}. Your class must have a public no-args
 * constructor
 * </br>
 * <pre>
 * public class ExampleMiddleware extends Middleware
 * {
 *     {@literal @}Override
 *      public void handle(Request request, MiddlewareQueue queue)
 *      {
 *          if ( Foo.Bar( request ) )
 *          {
 *              // Here we choose to break the chain, and redirect to another view
 *              request.internalRedirect( "other/path" );
 *          } else
 *          {
 *              // This passes the request to the rest of the middleware chain
 *              queue.handle( request );
 *          }
 *      }
 * }
 * </pre>
 * </p>
 * <p>
 * <h1>Registration</h1></br>
 * You can register your Middleware class to a RequestHandler (such as {@link SimpleRequestHandler} or
 * ({@link com.plotsquared.iserver.views.View}) by doing:
 * <pre>
 * requestHandler.getMiddlewareQueuePopulator().add( ExampleMiddleware.class );
 * </pre>
 * Or add it to your {@literal @}{@link com.plotsquared.iserver.views.decl.ViewMatcher} like this:
 * <pre>
 * {@literal @}ViewMatcher(filter = "your/filter", cache = false, name = "Identifier", middlewares = { YourMiddleware.class }
 * </pre>
 * </p>
 * <p>
 * <h1>Redirects</h1></br>
 * Middlewares can redirect a request to another view
 * <pre>
 * {@literal @}Override
 *  public void handle(Request request, MiddlewareQueue queue)
 *  {
 *     request.internalRedirect("new/request/query");
 *  }
 * </pre>
 * </p>
 * <p>
 * <h1>Change response generator</h1></br>
 * RequestHandlers has a special system called Alternate outcomes, which means that they can
 * specify other methods than the ordinary handling methods. Your middleware can tell
 * IntellectualServer to use those methods
 * <pre>
 * // Inside your RequestHandler constructor
 * registerAlternateOutcome( "identifier", "methodName" );
 *
 * // Method declaration
 * public void methodName(Request request, Response response)
 * {
 *      // Handle the request
 * }
 *
 *  // Your middleware
 * {@literal @}Override
 *  public void handle(Request request, MiddlewareQueue queue)
 *  {
 *      request.useAlternateOutcome( "methodName" );
 *      queue.handle( request );
 *  }
 * </pre>
 * </p>
 *
 * @see AuthenticationRequiredMiddleware
 * @see DebugMiddleware
 */
public abstract class Middleware
{

    public abstract void handle(final Request request, final MiddlewareQueue queue);

}
