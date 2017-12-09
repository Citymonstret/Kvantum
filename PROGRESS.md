# Progress

This document aims to provide information about the current progress of the development of Kvantum. It is also meant to provide a list of planned features ("to do" for not yet started implementations and "work in progress" for features that have had \some\ progress). 

**Contributions:**

You are highly encouraged to aid the development of Kvantum. This document provides a good entry into contributions. It is recommended to focus on "todo"-marked entries, rather than "w.i.p". This is to prevent disruptions in the workflow, and to make sure that there aren't overlaps in development. 

**This document is *not* complete. All implemented features may not be listed here yet.**

## 1. Protocol

#### 1.1 HTTP/1.1 Protocol Support
**Note**: Kvantum only *officially* supports HTTP/1.1, though other versions may still work.

##### 1.1.1 HTTP Methods
* GET - Serve content through GET requests. Parameters are fully read.
* HEAD - Serve headers through HEAD requests. HTTP message is not provided.
* **[W.I.P]** POST - Send (and serve) content through POST requests
	* Basic HTTP request messages are supported
	* multipart/form-data message format
	* JSON message format
	* **[TODO]** XML message format
* Other methods such as PUT, PATCH, DELETE etc are read into requests, but not handled by the default Kvantum implementation. This is intended behavior, and these methods are to be handled by the individual application implementations instead (per-project implementations are recommened).

#### 1.2 HTTPS Support
* ... see HTTP/1.1 Protocol Support
* Kvantum has a built in SSL handler that is able to read local certificates through java keyservers. 
* Requests may be forced through HTTPS, which allows for *secure* applications.

#### 1.3 Misc
* Connection Throttling

## 2 View/Request Handling
#### 2.1 All views
* ViewPatterns - URL request mapping with variables (with optional default values)
* Server-side response caching
* Scriptable views (JavaScript, through Nashorn)

#### 2.2 Static Resources
Kvantum comes with default views for serving of static files (or pseudo-dynamic through static template rendering). These views makes sure that the appropritate headers are sent alongside with the response, according to the HTTP/1.1 specifications.

##### 2.2.1 Default views
* HTML - Serves html files and templates (currently: .vm and .twig)
* JavaScript - Serves javascript files
* CSS - Serves cascading stylesheets
* LESS - Compiles LESS files into CSS and serves as CSS
* Download - Sends binary representations of specified files to the client
* Image - Serves images (of common formats)
* Standard - Automatically detect and serve: HTML, JavaScript, CSS, LESS and Images.
* **[TODO]** Precompiled Responses - Allow responses (including headers) to be compiled into raw bytes, which can then be sent to the client (Benefit: Fast!)

###### 2.2.2 Configuration
The default views provide different configuration options to make sure that the content is served according to the 
requirements of the users:
* forceHTTPS - Force content through the HTTPS handler (redirects requests)
* extensionRewrite - Rewrite the requested file extension.
* filter - URL request mapping with variables (with optional default values)
* filePattern - Match URL requests to files (supports variables)
* headers - Allows the configuration of headers on a per-view basis
* templates - (Not a view configuration per se, although configured per-view) allow templates to be served only for specific views (defaults to ALL)

#### 2.3 API
Kvantum allows views/request handlers to be defined through:
* (@)Annotations on inline methods
* Fluent builder-patterns (Kvantum#createSimpleRequestHandler and SimpleRequestHandler#builder)
* OOP (by extending View, or other more specific implementations for different levels of abstraction)

##### 2.3.1 REST API
Kvantum comes with some utilities and rest based views to allow the creation of REST based APIs
* Utility to autogenerate response for invalid requests (missing parameters)
* Auto-serving of JSON content through REST Handlers
* Utilities for static REST microservices using the Kvantum ORM system:
    * Automatic datastore search: Rest#createSearch

#### 2.4 Template Engine Support
Kvantum provides support for the following templating engines:
* JTWig
* Apache Velocity
* Crush (Custom engine developed alongside Kvantum)

#### 2.5 Sessions
Kvantum provides a session system. Sessions can either be loaded automatically and stored in client cookies, or be loaded on requests.
* SQLite and MongoDB Session database support (for persistent and inter-application sessions)

## 3 Commands
Kvantum has a built in console command system that can be extended programmatically.

**Current commands:**
* /dump - Dump request handler info to the console
* /metrics - Dump metric info to the console
* /help - Show a list of supported console commands
* /account - Account management
* /show - Show license and warranty information

## 4 (Misc) API
### 4.1 Account System
Kvantum comes with an account system, that is meant to remove the need for account management boilerplate.
* SQLite Account database implementation
* MongoDB Account database implementation
* Account server-cache
* Account session binding
* Account data management
* Account creation
* Password management
* Account role (permission) system with API and persistent role storage

### 4.2 Plugin System
Kvantum has a plugin system implementation that allows plugins to be loaded by the server at runtime

### 4.3 Application Structure System
Plugins and integrating applications can replace the internal application structure to provide server-wide alternate implementations for views and account management.

### 4.4 Middleware
Middleware can be injected into request handlers to filter out unwanted requests, and also redirect them to other methods in the request handlers (alternate outcomes)

### 4.5 Byte/String handlers
Byte and String handlers can be added to the worker, to change generated requests after they have been generated. (Used for the tempalte engine support).

### 4.7 Socket Filters
The socket handler can be extended to include filters, that can filter out requests before they are even read by the server.

### 4.8 Configuration API
Kvantum comes with some tools to simplify the creation of configuration files, and these files may also be read by the template systems (which provides configurable static variables).

### 4.9 Validation API & Request Requirements
Kvantum includes a validation system that can be implemented into request handlers to filter out unwanted requests based on POST and GET parameters. 

Kvantum also includes another more fluent system that is especially handy in REST based applications, called 
`RequestRequirements` that can be implemented per request, and even on demand. 

### 4.10 POST/GET mapped ORM
Kvantum includes a framework which allows you to map objects to <string,string> parameters from GET/POST requests and
automatically generate objects based on these parameters

* Currently supports: String, Boolean, Integer, Long, Short, Byte, Char, Double, Float
