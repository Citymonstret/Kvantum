# Kvantum

<p align="center">
<img alt="Kvantum" src="https://i.imgur.com/C8QKaSK.png" width="458"/>
</p>

**NOTE: The project is currently still in development. Beware - Things *will* break.** 

[Kvantum](http://kvantum.xyz/) is a OSS project maintained by Alexander Söderberg. Kvantum is a web server and web 
framework written in Java. For support, submit a new 
[issue](https://github.com/IntellectualSites/Kvantum/issues) or email 
[sauilitired@gmail.com](mailto:sauilitired@gmail.com)
 

## Progress
You can see the current progress [here](https://github.com/IntellectualSites/Kvantum/blob/master/PROGRESS.md) along with *W.I.P* and *TODO*'s. 

[![Project Stats](https://www.openhub.net/p/Kvantum/widgets/project_thin_badge.gif)](https://www.openhub.net/p/Kvantum)

Travis CI:
[![Build Status](https://travis-ci.org/Sauilitired/Kvantum.svg?branch=master)](https://travis-ci.org/Sauilitired/Kvantum)

## Description
Kvantum is a lightweight, portable and (if you so desire) embeddable (HTTP/HTTPS) web server, written entirely in java. It is meant to be easy to use, yet offer a wide variety of tools and utilities to simplify the development of your website. It comes shipped with tools such as account management, database connectors and template engine support. All to remove the need for repeated boilerplate. 

Kvantum can be used both as a standalone web server for static content, natively serving:
* HTML
* CSS 
* JavaScript
* (Downloading of...) PDF, TXT &amp; ZIP
* Images (PNG, ICO, SVG)
* Webjars resources as static files
* ... and the system can also be extended programmatically, both by extending the application itself, by scripts and by plugins. 

and as a web framework with a powerful API. Everything served by Kvantum, regardless if it's dynamic or static, can be managed by templates. Kvantum supports Crush, JTwig and Apache Velocity out of the box!

### Examples
Code examples can be found at [this](https://github.com/IntellectualSites/Kvantum/tree/master/Examples) link. There are also some sample projects (may not be up to date): [Forum](https://github.com/Sauilitired/Forum), [KvantumBlog](https://github.com/IntellectualSites/KvantumBlog), [Foton](https://github.com/IntellectualSites/Foton) and [KvantumBukkit](https://github.com/Sauilitired/KvantumBukkit).

### Prerequisites
Kvantum only requires Java 8 (or later versions) to be installed on your system.

### How to run

**Note:** These are the instructions to run Kvantum as a standalone web server.
If you instead want to use the framework, please refer to the [wiki](https://github.com/IntellectualSites/Kvantum/wiki)

The easiest way to run Kvantum as a standalone web server is to use a pre-compiled Implementation jar file. These
can be found in [releases](https://github.com/IntellectualSites/Kvantum/releases). Simply download the jar file into
a directory, and then run it using:
```bash
java -jar /path/to/Implementation-all.jar
```

However, if you'd rather call the main method directly, then it is
located in [KvantumMain](https://github.com/IntellectualSites/Kvantum/blob/master/Implementation/src/main/java/xyz/kvantum/server/implementation/KvantumMain.java)

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
Press `Import` and select `...path/to/project/code_style.xml`

**IntelliJ:**
`File > Settings > Editor > Code Style`. Next to "Scheme" there is a cog wheel, press that and then
`Import Scheme > Eclipse XML Profile` and then select `..path/to/project/code_style.xml`

### Maven

```xml
<repositories>
    <repository>
        <id>Incendo</id>
        <url>https://incendo.org/mvn/repository/maven-releases/</url>
    </repository>
</repositories>

<dependency>
    <groupId>xyz.kvantum</groupId>
    <artifactId>Implementation</artifactId>
    <version>1.3</version>
</dependency>
```

### Building from scratch

On *nix:
```bash
$ git clone git://github.com/IntellectualSites/Kvantum.git
$ cd Kvantum
$ chmod a+x ./gradlew
$ ./gradlew :build
```

Windows:
```batch
$ git clone git://github.com/IntellectualSites/Kvantum.git
$ cd Kvantum
$ ./gradlew.bat :build
```
