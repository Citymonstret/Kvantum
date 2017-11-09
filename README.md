# IntellectualServer
[![](https://jitpack.io/v/IntellectualSites/IntellectualServer.svg)](https://jitpack.io/#IntellectualSites/IntellectualServer)

**NOTE: The project is currently still in development. Beware - Things *will* break.** 

## Progress
You can see the current progress [here](https://github.com/IntellectualSites/IntellectualServer/blob/master/PROGRESS.md) along with *W.I.P* and *TODO*'s. 

## Description
IntellectualServer is a lightweight, portable and (if you so desire) embeddable (HTTP/HTTPS) web server, written 
entirely in java. It is meant to be easy to use, yet offer a wide variety of tools and utilities to simplify the 
development of your website. It comes shipped with tools such as account management, database connectors and template
engine support. All to remove the need for repeated boilerplate. 

IntellectualServer can be used both as a standalone application (serving static content), as a library to create your
own applications (by integrating IntellectualServer or by extending the server using plugins). It allows views to be
configured both through configuration files and through code (and even a mixture of both!).

It supports a wide variety of static file types out of the box, and it even offers support for Apache Velocity, JTwig
and Crush (our own template engine) templates! 

## Wiki/Information
More information can be found in our [wiki](https://github.com/IntellectualSites/IntellectualServer/wiki)

## Development

### Maven

We are using JitPack as our maven repo
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.IntellectualSites.IntellectualServer</groupId>
    <artifactId>Implementation</artifactId>
    <version>BETA-0.0.4</version>
</dependency>
```
