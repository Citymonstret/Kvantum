# Progress

This document aims to provide information about the current progress of the development of IntellectualServer. It is also meant to provide a list of planned features ("to do" for not yet started implementations and "work in progress" for features that have had \some\ progress). 

**Contributions:**

You are highly encouraged to aid the development of IntellectualServer. This document provides a good entry into contributions. It is recommended to focus on "todo"-marked entries, rather than "w.i.p". This is to prevent disruptions in the workflow, and to make sure that there aren't overlaps in development. 

**This document is *not* complete. All implemented features may not be listed here yet.**

## 1. Protocol

#### 1.1 HTTP/1.1 Protocol Support
**Note**: IntellectualServer only *officially* supports HTTP/1.1, though other versions may still work.

##### 1.1.1 HTTP Methods
* GET - Serve content through GET requests. Parameters are fully read.
* HEAD - Serve headers through HEAD requests. HTTP message is not provided.
* **[W.I.P]** POST - Send (and serve) content through POST requests
	* Basic HTTP request messages are supported
	* **[TODO]** multipart/form-data message format
	* **[TODO]** JSON message format
* Other methods such as PUT, PATCH, DELETE etc are read into requests, but not handled by the default IntellectualServer implementation. This is intended behavior, and these methods are to be handled by the individual application implementations instead (per-project implementations are recommened).

#### 1.2 HTTPS Support
* ... see HTTP/1.1 Protocol Support
* IntellectualServer has a built in SSL handler that is able to read local certificates through java keyservers. 
* Requests may be forced through HTTPS, which allows for *secure* applications.

## 2 View/Request Handling
#### 2.1 All views
* ViewPatterns - URL request mapping with variables (with optional default values)
* Server-side response caching

#### 2.2 Static Resources
IntellectualServer comes with default views for serving of static files (or pseudo-dynamic through static template rendering). These views makes sure that the appropritate headers are sent alongside with the response, according to the HTTP/1.1 specifications.

##### 2.2.1 Default views
* HTML - Serves html files and templates (currently: .vm and .twig)
* JavaScript - Serves javascript files
* CSS - Serves cascading stylesheets
* LESS - Compiles LESS files into CSS and serves as CSS
* Download - Sends binary representations of specified files to the client
* Image - Serves images (of common formats)
* Standard - Automatically detect and serve: HTML, JavaScript, CSS, LESS and Images.

###### 2.2.2 Configuration
The default views provide differenet configuration options to make sure that the content is served according to the requirements of the users:
* forceHTTPS - Force content through the HTTPS handler (redirects requests)
* extensionRewrite - Rewrite the requested file extension.
* filter - URL request mapping with variables (with optional default values)
* filePattern - Match URL requests to files (supports variables)
* headers - Allows the configuration of headers on a per-view basis
* templates - (Not a view configuration per se, although configured per-view) allow templates to be served only for specific views (defaults to ALL)

#### 2.3 API
IntellectualServer allows views/request handlers to be defined through:
* (@)Annotations on inline methods
* Fluent builder-patterns (IntellectualServer#createSimpleRequestHandler and SimpleRequestHandler#builder)
* OOP (by extending View, or other more specific implementations for different levels of abstraction)

#### 2.4 Template Engine Support
IntellectualServer provides support for the following templating engines:
* JTWig
* Apache Velocity
* Crush (Custom engine developed alongside IntellectualServer)

#### 2.5 Sessions
IntellectualServer provides a session system. Sessions can either be loaded automatically and stored in client cookies, or be loaded on requests.
* SQLite and MongoDB Session database support (for persistent and inter-application sessions)

## 3 Commands
IntellectualServer has a built in console command system that can be extended programmatically.

**Current commands:**
* /dump - Dump request handler info to the console
* /metrics - Dump metric info to the console
* /help - Show a list of supported console commands
* /account - Account management
* /show - Show license and warranty information

## 4 (Misc) API
### 4.1 Account System
IntellectualServer comes with an account system, that is meant to remove the need for account management boilerplate.
* SQLite Account database implementation
* MongoDB Account database implemenation
* Account server-cache
* Account session binding
* Account data management
* Account creation
* Password management

### 4.2 Plugin System
IntellectualServer has a plugin system implementation that allows plugins to be loaded by the server at runtime

### 4.3 Application Structure System
Plugins and integrating applications can replace the internal application structure to provide server-wide alternate implementations for views and account management.

# Changelog
Changes to this document are outlined below, using the format:

**[Date of changes]**
* \+ Added entries
* \- Removed entries
* \/ Changed entries
