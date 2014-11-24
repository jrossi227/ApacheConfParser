ApacheConfParser
=====================
The ApacheConfParser provides a java library that is used to read the Apache http server configuration.

Development Environment
----------------------
The ApacheConfParser source code is currently structured as an Eclipse java project. It can be imported to Eclipse using the EGit Eclipse plugin.

Building
-------------------

####Build Dependancies

- Java 1.6+
- Ant 

####Building a new jar

The following steps can be used to generate a new jar file.

1. Navigate to the *build* directory.
2. Open *build.properties* and update the version.
3. Run *ant jar*

A new jar file should be generated under *dist*

####Generating Java Doc

The following steps can be used to generate JavaDoc for the library.

1. Navigate to the *build* directory.
2. Run *ant javadoc*

javadoc should be generated under *doc*

