CASDA VO Tools
==============


This web application provides access to astronomical data using the following Virtual Observatory protocols:

| Protocol |Version |Endpoint Base |
|---------|--------|---------|
| [Table Access Protocol](http://www.ivoa.net/Documents/TAP/) | v1.0 | /tap |
| [Simple Cone Search](http://www.ivoa.net/Documents/latest/ConeSearch.html) | v1.03 | /scs |
| [Simple Spectral Access](http://www.ivoa.net/documents/SSA/) | v1.1  | /ssa |
| [Simple Image Access](http://www.ivoa.net/documents/SIA/) | v2.0  | /sia2 |
| [Datalink](http://www.ivoa.net/documents/DataLink/index.html) | v1.0 | /datalink |
 
Each protocol has an `/availability` and `/capabilities` endpoint (e.g. `/tap/capabilities` ) which comply 
with the [IVOA Support Interfaces (VOSI) v1.0](http://www.ivoa.net/documents/VOSI/) specification. The `/capabilities` endpoint 
lists all other endpoints and is the means clients will use 
for auto discovery of services. The `/capabilities` will also be included in the IVOA registry entries for each VO Tools instance.

The application is designed to be reusable both within CSIRO and by other data centers. As a result there should be no 
CSIRO specific code or functionality included in the application. All customisation is also accessible using 
configuration rather than via code.  

Release History
---------------

Current Release: v1.13

Notes for each release are available at  [release_notes.md](./release_notes.md)


Installation
------------

### System Requirements

* Java v8 SDK
* Web container: tested with Tomcat 8
* Postgres database running PostgreSQL 9.4 or later and (optionally) pgShere v1.1 or later

### Accessing the WAR file

CASDA VO Tools is distributed as a ready to install war file (web archive). This can be directly deployed in a Java web container such as Tomcat.

The casda\_vo\_tools.war file can either be downloaded from the files tab at [CASDA VO Tools v1.13 on CSIRO Data Access Portal](https://doi.org/10.25919/zy6w-h884)
or you can build it using the instructions below.

### Deployment

1. Tomcat should be configured to expand war files (for ease of configuration).
2. The casda_vo_tools.war file should be added to the webapps folder in Tomcat.
3. Start Tomcat (or restart Tomcat if it is already running)
4. Open `http://localhost:8080/casda_vo_tools/` to see a welcome message. 
5. Then open `http://localhost:8080/casda_vo_tools/configure/home` to access the initial configuration screen.
 
Note that the server name and port may vary depending on your deployment server and how tomcat is configured.
 

Configuration
-------------

A full guide to interactive configuration is provided at 
[Deploying_CASDA_VO_Tools.pdf](https://data.csiro.au/dap/ws/v2/collections/47697/support/3735) however a short intro is provided here.

### Application config
Two configuration systems are available for use in the CASDA VO Tools application.

* Spring Boot's [externalized configuration](http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html) which 
uses `application.properties` and `config/application-casda_vo_tools.properties`. These can be within 
the war file or placed in the runtime working directory. See `src/main/resources/application.properties` for 
further details.  
* A custom `config/configuration.yaml` file in the runtime working directory. This is the main means by which an 
external data center would configure their VO Tools instance.

Either config system can be used in a particular deployment. If present, the yaml will override the spring boot config. 

The configuration can be interactively managed using the endpoint `/configure/home`. This is documented in 
[/src/docs/Deploying CASDA VO Tools.docx](https://github.com/csiro-rds/casda_vo_tools/raw/master/src/docs/Deploying%20CASDA%20VO%20Tools.docx)

For an initial installation, you should follow these basic steps:

1. Go to `http://localhost:8080/casda_vo_tools/configure/home`
2. If a database connection is detected then a login will be required. The default login details are User: `voadmin` Password: `password` 
3. Click `Current` to see the current configuration
4. Change the values for `connection.url`, `connection.username`, and `connection.password` to connect to your database.
5. Select `Update` as the allowed access level and click `Explore` to read in the details of your database and create the TAP metadata tables 
6. Add schema and table entries e.g.

    schemas:  
       ivoa: !au.csiro.casda.votools.config.SchemaConfig {}  
    tables:  
       ivoa.obscore: {}  
  
7. Select `Update` as the allowed access level and click `Apply` to save the configuration and update the TAP metadata tables 
8. Edit `config/authz` to change the password to one of your choosing.
9. Go to `http://localhost:8080/casda_vo_tools/tap`
10. The UI you see at this address can be used to execute some ADQL queries.  eg:

> `SELECT * FROM tap_schema.tables`

### Logging

The log4j configuration file is detected using default behaviour of log4j, by being named according to convention (log4j2.xml) and provided on the classpath. When running locally or in the application environments, the log4j configuration file is bundled with the application. When running under Eclipse, the log configuration file is copied into the local Eclipse-managed server's WEB-INF/classes directory, because it's in the src/test/resources folder, and loaded via the classpath. A custom logging config can be placed at config/CasdaVoTools-log4j2.xml under the Tomcat working directory. 

Logs will be written by default to the following location, relative to the working directory of the Tomcat process:

    logs/casda_vo_tools.log

Building
--------

CASDA VO Tools is a Java web application. It has a fairly standard layout and the build is managed using the [Gradle Build Tool](http://gradle.org/getting-started-gradle-java) .

The Gradle build is configured to download all dependencies, compile the code, run the unit tests and build the war file. 

On Windows:

> `gradlew clean build`

On Unix or Mac:

> `./gradlew clean build`

 


Development
-----------

For information on developing CASDA VO Tools, see [DEVELOPERS.md](./DEVELOPERS.md)

