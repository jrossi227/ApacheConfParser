ApacheConfParser
=====================
The ApacheConfParser provides a java library that is used to parse and read the Apache http server configuration. The library is currently used to power http://apachegui.net. Questions and comments can be sent to apachegui.net@gmail.com

The followng functionality is included in the library:

1. Search for directives
2. Search for enclosures
3. Search for directives inside enclosures
4. Grab the active file list 
5. Grab currently loaded modules
6. Various parsing utilties
7. Filters lines inside of IfModule declarations with modules that are not loaded
8. Supports Multi-line configuration lines
9. Much more...

Current Jar
---------------------
The current jar files can be found under the *dist* directory of this project. You may also download a released jar file from the releases page of this project. The current jar file has two formats:

1. Java library - This is a jar file with the ApacheConfParser code packaged into it. It does not contain any project dependencies. If you wish to run ApacheConfParser solely as a java library then this file is your best option.
2. Runnable - This is a jar file with all dependencies packaged into it. This jar file has a Main class and is a valid runnable jar file. You can view details on running the jar [here](https://github.com/jrossi227/ApacheConfParser/wiki/Runnable-Jar-Usage). 

Development Environment
----------------------
The ApacheConfParser source code is currently structured as a Maven enabled java project. It can be imported to any Java IDE as a Maven project.

Maven Dependency
----------------------
If you wish to add ApacheConfParser to your project as a Maven dependency then you must add the snippets below to your pom.xml. Version 1.0.10 and above support Maven dependencies.

```xml
<repositories>
  ...
  <repository>
      <id>ApacheConfParser-mvn-repo</id>
      <url>https://raw.githubusercontent.com/jrossi227/ApacheConfParser/mvn-repo/</url>
      <snapshots>
          <enabled>true</enabled>
          <updatePolicy>always</updatePolicy>
      </snapshots>
  </repository>
  ...
</repositories>

<dependencies>
  ...
  <dependency>
      <groupId>net.apachegui</groupId>
      <artifactId>ApacheConfParser</artifactId>
      <version>1.0.10</version>
  </dependency>
  ...
</dependencies>  
```

Building
-------------------

#### Build Dependancies

- Java 1.6+
- Maven

#### Building javadoc and new jar files

The following steps can be used to generate javadoc and new jar files.

1. Navigate to the project root directory (This is the directory that contains pom.xml).
2. Run ```mvn clean package```

The target directory should contain the following after running the package build phase:

1. A new java library jar file. A java library jar file can be found with the following naming convention ApacheConfParser-{version}.jar.
2. A new runnable jar file. A runnable jar file can be found with the following naming convention ApacheConfParser-{version}-jar-with-dependencies.jar.
3. New javadoc. Javadoc can be found exploded in the apidocs folder or bundled as jar file with the naming convention ApacheConfParser-{version}-javadoc.jar.

Code Samples
------------------------

You can view code samples [here](https://github.com/jrossi227/ApacheConfParser/wiki/Code-Samples)

API Docs
------------------------

API Docs are available [here](https://github.com/jrossi227/ApacheConfParser/wiki/API-Docs)

Jar Usage
------------------------

The generated jar is a Runnable jar file. You can view details on running the jar [here](https://github.com/jrossi227/ApacheConfParser/wiki/Runnable-Jar-Usage). 

