#IntellectualServer

## Table Of Content
* [Description](#description)
* [Wiki/Information](#wikiinformation)
* [Features](#features)
* [Crush / Templating Engine](#templates)
* [Views](#views)
* [Configuration](https://github.com/IntellectualSites/IntellectualServer/wiki/config-server.yml)
* [**Development**](#development)
* [Maven](#maven)
* [Building](#building)
* [Example](#example)
* [Middleware](#middleware)
* [Filters](#filters)

##Description
IntellectualServer is a lightweight, innovative, portable and embeddable web server - Written entirely in java.
It is the perfect choice, if you need a an embedded web server to ship with your application; as it can be
run as both an integrated and standalone application. It supports both programmed views, and configuration of 
pre-made ones - To use it just like you'd use any other web server. It's almost magic.

##Wiki/Information
( **OUTDATED** ) More information can be found in our [wiki](https://github.com/IntellectualSites/IntellectualServer/wiki)

##Features
Major:
- Supports both HTTP and HTTPS
- Easy to use API (including Middlewares, Annotation based view declaration, Functional View Declaration, Object 
oriented View Declaration)
- Easy configuration of views (see `Views`)
- Plugin System
- LOTS MORE (... to be continued ...)

Minor:
- Response Caching
- GZIP Compression
- MD5 Checksum

##Templates
CrushTemplate is a templating engine that is shipped with the core of the server. It allows for things that you just cannot do with normal webservers.

Current Crush Features:

 - Variables `{{provider.variable}}`
 - Variable filters `{{provider.variable || FILTER}}`
 - Meta data `{{: [author: Citymonstret] :}}`
 - File inclusion `{{include:header.html}}`
 - Array looping `{#foreach system.filters -> filter} A Filter {filter} {/forach}`
 - If Statements `{#if provider.variable} {/if}`
 - Inverted If Statements `{#if !provider.variable} {/if}`

##Views
IntellectualServer officially supports:
- CSS
- LESS
- JavaScript
- Images
- Downloads
- Redirect
- HTML (with templating)
- Mixed (CSS/LESS, JavaScript, Images, HTML) - [WIKI](https://github.com/IntellectualSites/IntellectualServer/wiki/view-std.yml)

You can also expand upon this yourself, with the easy to use API!

##SSL
The server supports SSL. You just have to enable it in `.iserver/config/server.yml`
```yml
ssl:
  port: 443 # The SSL port
  keyStorePassword: password # The keystore password
  enable: true # Set to true to enable the SSL socket
  keyStore: './path/to/your/keystore.jks' # Full path to the keystore
```

##Development

###Maven
[![](https://jitpack.io/v/IntellectualSites/IntellectualServer.svg)](https://jitpack.io/#IntellectualSites/IntellectualServer)


We are using JitPack as our maven repo
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.IntellectualSites</groupId>
    <artifactId>IntellectualServer</artifactId>
    <version>2.2.0</version>
</dependency>
```

###Building
You build IntellectualServer using gradle. Use `gradlew build` to download all dependencies and build the compiled 
output. To build a jar containing all dependencies, use `gradlew shadowJar`. And finally, to run the tests, use 
`gradlew test`

###Example
More examples can be found in the "example" folder

####Initialization
```java
public static void main(final String[] args)
{
    final Server server = IntellectualServerMain.createServer(
            false, // Indicates that are are not running the server as a standalone app
            new File( "." ), // The core folder
            new DefaultLogWrapper() // Default log handling
    );
        
    // A very simple "/" handler    
    new SimpleRequestHandler( "", ( request, response ) ->
     response.setContent( "<h1>Hello World</h1>" ) ).register();
      
    server.start();  
}
```

####@Annotation Views
```java
// Scan the file for views
try
{
    StaticViewManager.generate( yourClassInstance );
} catch ( final Exception e )
{
    e.printStackTrace();
}


// Declare a view
@ViewMatcher( filter = "random/uuid/", cache = false, name = "randomUUID" )
public Response randomUUID(final Request request)
{
    return new Response().setContent( "<h1>" + UUID.randomUUID() + "</h1>" );
}

// OR
@ViewMatcher( filter = "random/uuid/", cache = false, name = "randomUUID" )
public void randomUUID(final Request request, final Response response)
{
    response.setContent( "<h1>" + UUID.randomUUID() + "</h1>" );
}
```

###Middleware
Middleware is a class responsible for filtering out, and acting on requests, before they are handled by the appropriate views. They can be used to redirect non-authenticated users, or make sure that a requested user exists in a database.

Middleware is lined up in a sort of chain, by using a special queue. If a middleware breaks the que, the request is not served by the view ( Middleware can redirect requests to other views without continuing the chain ).

#### Creation
You just have to extend `com.plotsquared.iserver.api.views.requesthandler.Middleware`
Your class must have a public no-args constructor.

```java
public class ExampleMiddleware extends Middleware
{

   @Override
   public void handle(Request request, MiddlewareQueue queue)
   {
      if ( Foo.bar( request) )
      {
         // Here we choose to break the chain, and redirect to another view
         request.internalRedirect( "other/path" );
      } else 
      {  
         // This passes the request to the rest of the middleware chain
         queue.handle( request );
      }
   }
}
```

##### Registration
You can register your Middleware class to a RequestHandler (such as SimpleRequestHandler or View) by doing:
```java
requestHandler.getMiddlewareQueuePopulator().add( ExampleMiddleware.class );
```

Or add it to your `@ViewMatcher` like this:
```java
@ViewMatcher(filter = "user/<username>", cache = false, name = "User", middlewares = { UserMiddleware.class } )
```

##### Example(s)
[**AuthenticationRequiredMiddleware**](https://github.com/IntellectualSites/IntellectualServer/blob/master/src/main/java/com/plotsquared/iserver/views/requesthandler/AuthenticationRequiredMiddleware.java)

[**Example of request filtering**](https://github.com/IntellectualSites/IntellectualServer/blob/master/example/Embedded/src/main/java/com/plotsquared/iserver/example/APITest.java#L58)

###Filters
Filters are used to determine which view gets to serve the incoming requests. Each filter is made up of different parts, there are four types of parts:
* Separator - `/` - Used like a path separator
* Static - Example: `user` - A static string
* Required Variable - Example: `<username>`
* Optional Variable - Example: `[page]`

##### Examples
**`user/<username>`** - Serves `user/Citymonstret`, but not `user/` or `user/Citymonstret/other`

**`news/[page]`** - Serves `news`, `news/1`, `news/foo` but not `news/foo/bar`

**`user/<username>/posts/[page]`** - Serves `user/Citymonstret/posts` and `user/Citymonstrst/posts/10`

##### Filtering / Validation
There is no filtering of variable. That is done using Middleware
