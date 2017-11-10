# Kvantum

<p align="center">
<img alt="Kvantum" src="https://i.imgur.com/C8QKaSK.png" width="458"/>
</p>

**NOTE: The project is currently still in development. Beware - Things *will* break.** 

## Progress
You can see the current progress [here](https://github.com/IntellectualSites/Kvantum/blob/master/PROGRESS.md) along with *W.I.P* and *TODO*'s. 

## Description
Kvantum is a lightweight, portable and (if you so desire) embeddable (HTTP/HTTPS) web server, written 
entirely in java. It is meant to be easy to use, yet offer a wide variety of tools and utilities to simplify the 
development of your website. It comes shipped with tools such as account management, database connectors and template
engine support. All to remove the need for repeated boilerplate. 

Kvantum can be used both as a standalone application (serving static content), as a library to create your
own applications (by integrating Kvantum or by extending the server using plugins). It allows views to be
configured both through configuration files and through code (and even a mixture of both!).

It supports a wide variety of static file types out of the box, and it even offers support for Apache Velocity, JTwig
and Crush (our own template engine) templates! 

## Wiki/Information
More information can be found in our [wiki](https://github.com/IntellectualSites/Kvantum/wiki)

## Development

### Maven
[![](https://jitpack.io/v/IntellectualSites/Kvantum.svg)](https://jitpack.io/#IntellectualSites/Kvantum)

We are using JitPack as our maven repo
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.IntellectualSites.Kvantum</groupId>
    <artifactId>Implementation</artifactId>
    <version>BETA-0.0.4</version>
</dependency>
```
