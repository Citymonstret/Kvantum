#IntellectualServer

##Description
IntellectualServer is a lightweight, innovative, portable and embeddable web server - Written entirely in java.
It is the perfect choice, if you need a an embedded web server to ship with your application; as it can be
run as both an integrated and standalone application. It supports both programmed views, and configuration of 
pre-made ones - To use it just like you'd use any other web server. It's almost magic.

##Wiki/Information
( **OUTDATED** ) More information can be found in our [wiki](https://github.com/IntellectualSites/IntellectualServer/wiki)

##Features
Major:
- Easy to use API (including Middlewares, Annotation based view declaration, Functional View Declaration, Object 
oriented View Declaration)
- Easy configuration of views (see `Views`)
- LOTS MORE (... to be continued ...)

Minor:
- Response Caching
- GZIP Compression
- MD5 Checksum

##Crush
CrushTemplate is a templating engine that is shipped with the core of the server. It allows for things that you just cannot do with normal webservers.

Current Crush Features:

 - Variables `{{provider.variable}}`
 - Variable filters `{{provider.variable || FILTER}}`
 - Meta data `{{: [author: Citymonstret] :}}`
 - File inclusion `{{include:header.html}}`
 - Array looping `{#foreach system.filters -> filter} A Filter {filter} {/forach}`
 - If Statements `{#if provider.variable} {/if}`
 - Inverted If Statements `{#if !provider.variable} {/if}`

##Building
You build IntellectualServer using gradle. Use `gradlew build` to download all dependencies and build the compiled 
output.

##Views
IntellectualServer uses a special view setup, so you can freely design the pattern that you want for your website.
 For example: `user/<user>`, where `<user>` is a required parameter. Or, `user/<username>/posts/[page]`, where 
 `<username>` is a required parameter, and `[page]` is optional
 
##Official Views
IntellectualServer officially supports:
- CSS
- LESS
- JavaScript
- Images
- Downloads
- Redirect
- HTML (with templating)
- Mixed (CSS/LESS, JavaScript, Images, HTML)

You can also expand upon this yourself, with the easy to use API!

##Example
Examples can be found in the "example" folder
