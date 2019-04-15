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

Kvantum is a web server and framework written in Java, on top of the Netty application framework.
Kvantum is written with two main goals in mind: a) to be able to run as a standalone web server, with
no extra programming needed, and b) to offer an extensive API, allowing developers to extend Kvantum
to fit their needs exactly. The user can choose to either run Kvantum on its own, 
to create Kvantum extensions (addons) or to ship their application with Kvantum embedded in it. 

Kvantum is written for HTTP/HTTPS 1.1 and aims to implement the entire protocol. The web server is 
written to be non-blocking, and to greatly favour caching over I/O operations.

The Kvantum framework includes many specially written components, that will drastically reduce
end user boilerplate code. This includes (but is not limited to): account management, database connections 
(MySQL, SQLite, MongoDB), template engines (Apache Velocity, JTwig, Crush), REST utilities, search service utilities, etc.

Kvantum can be used as a standalone server for static content, natively serving:
* HTML
* CSS 
* JavaScript
* (Downloading of...) PDF, TXT &amp; ZIP
* Images (PNG, ICO, SVG)
* Webjars resources as static files
* ... and the system can also be extended programmatically, both by extending the application itself, by scripts and by plugins. 

### Examples
Code examples can be found at [this](https://github.com/IntellectualSites/Kvantum/tree/master/Examples) link. There are also some sample projects (may not be up to date): [Forum](https://github.com/Sauilitired/Forum), [KvantumBlog](https://github.com/IntellectualSites/KvantumBlog), [Foton](https://github.com/IntellectualSites/Foton) and [KvantumBukkit](https://github.com/Sauilitired/KvantumBukkit).

### Prerequisites
Kvantum only requires Java 10 (or later versions) to be installed on your system.

### How to run

**Note:** These are the instructions to run Kvantum as a standalone web server.
If you instead want to use the framework, please refer to the [wiki](https://github.com/IntellectualSites/Kvantum/wiki)

The easiest way to run Kvantum as a standalone web server is to use a pre-compiled Implementation jar file. These
can be found in [releases](https://github.com/IntellectualSites/Kvantum/releases). Simply download the jar file into
a directory, and then run it using:

(older versions):
```bash
java -jar /path/to/Implementation-all.jar
```

(newer versions):
```bash
java -jar /path/to/Standalone-all.jar
```

However, if you'd rather call the main method directly, then it is
located in [KvantumMain](https://github.com/IntellectualSites/Kvantum/blob/master/Standalone/src/main/java/xyz/kvantum/server/implementation/KvantumMain.java)

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
<!-- CURRENTLY DOWN! NEW REPOSITORY COMING SOON -->

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
