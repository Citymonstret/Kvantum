# Kvantum

<p align="center">
<img alt="Kvantum" src="https://i.imgur.com/C8QKaSK.png" width="458"/>
</p>

**NOTE: The project is currently still in development. Beware - Things *will* break.** 

## Progress
You can see the current progress [here](https://github.com/IntellectualSites/Kvantum/blob/master/PROGRESS.md) along with *W.I.P* and *TODO*'s. 

[![Project Stats](https://www.openhub.net/p/Kvantum/widgets/project_thin_badge.gif)](https://www.openhub.net/p/Kvantum)

You can join the developer chat at discord: 
[![Discord](https://img.shields.io/discord/380323926959587338.svg?style=flat-square)](https://discord.gg/Gb2TDk9)

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

### Code Style

If you are planning to commit any changes to the project,
it would be highly appreciated if you were to follow the 
project code style conventions. To make this easier we have
provided settings that can be imported into your IDE.

**Eclipse:**
`Window > Preferences > Java > Code Style > Formatter`
Press `Import` and select `...path/to/project/code_style_eclipse.xml`

**IntelliJ:**
`File > Settings > Editor > Code Style`. Next to "Scheme" there is a cog wheel, press that and then
`Import Scheme > IntelliJ IDEA Code Style XML` and then select `..path/to/project/code_style_intellij.xml`

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
