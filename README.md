#IntellectualServer

##Description
IntellectualServer is a lightweight, innovative and totally portable web server - Written entirely in java.
It is the perfect choice, if you need a an embedded webserver to ship with your application; as it can be
run as both an intergrated and standalone application.

##Wiki/Information
More information can be found in our [wiki](https://github.com/IntellectualSites/IntellectualServer/wiki)

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
You build IntellectualServer using maven. Use `mvn package` to download all dependencies and build the compiled output.

##Views
IntellectualServer uses a special view setup, so you can freely design the regex pattern that you want for your website. For example: `(\/assets\/img\/)([A-Za-z0-9]*)(.png|.jpg|.jpeg|.ico|.gif)?`
