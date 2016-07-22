CASDA VO Tools - Developer Documentation
==============


This web application provides access to CASDA data using Virtual Observatory protocols. It is designed to be reusable 
both within CSIRO and by other data centers. As a result there should be no 
CSIRO specific code or functionality included in the application. Any customisation should also be accessible using 
configuration rather than via code.  

Setting up
----------

This project assumes that Eclipse / STS is being used. Having checked out the project from git into the standard project location (ie: 'C:\Projects\Casda'), you can import it into Eclipse as a Gradle project. You will then need to right-click on the project and do a Gradle -> Refresh All. 

Please then follow the instructions at [https://wiki.csiro.au/display/CASDA/CASDA+Application+setup+-+common](https://wiki.csiro.au/display/CASDA/CASDA+Application+setup+-+common) to ensure you're using the standard code formatting and templates.

This project relies on the same Postgres instance managed by the `casda_deposit_manager` service. Please see that project for instructions on setting-up and configuring the database.

Running the Tests Locally
-------------------------

> `gradle clean test`

Note: Some tests may only be run if there is a local database available. This means that local builds may run much longer than CI builds, because CI builds will skip these tests. 


Running the Tests within Eclipse
--------------------------------

You should just be able to run a test as a JUnit test in Eclipse (ie: there is no special configuration required).


Building and Installing Locally
-------------------------------

> `gradle clean deployToLocal`

This will build and deploy the war to tomcat (along with an application context file.)


Running Locally
---------------

You can access the tools by going to:

> `http://localhost:8080/casda_vo_tools/tap`

The UI you see at this address can be used to execute some ADQL queries.  eg:

> `SELECT * FROM tap_schema.tables`

> `SELECT obs_id, count(*) FROM ObsCore GROUP BY obs_id`

> `SELECT obs_id, dataproduct_type, calib_level, dataproduct_subtype, obs_collection FROM ObsCore WHERE obs_id = 140112`

> `SELECT TOP 2 * FROM ObsCore WHERE 1=CONTAINS(POINT('ICRS', s_ra, s_dec),  CIRCLE('ICRS', 189.2, 62.21, 0.05 ))`

The tomcat server can be stopped and started with the following gradle commands:

> `gradle localTomcatStop`

> `gradle localTomcatStart`

To use the SIAP/Datalink tools locally you may need to change the `datalink.base.url` URL in the properties file. by default this is set to point to a proxy service for authentication. by changing this to point to your vo tools local URL e.g. `http://localhost:8080/casda_vo_tools/` you can work on this component without the need for authentication.


Running Within Eclipse
----------------------

* In Eclipse, make sure the 'Servers' view is visible somewhere (Window -> Show View -> Servers).  
* In that view, right-click and select New -> Server.
* In the dialog that opens, expand the Apache folder and select Tomcat v7.0 Server, then click Finish.  You should now 
see a server in the Servers view called "Tomcat v7.0 Server at localhost".
* Right click on that server and choose 'Add and Remove'.
* In the dialog that opens you should see `casda_vo_tools`.  Make sure it is in the Configured pane, then click 
Finish.  
* Now double-click on the server in the Servers view and then in the editor pane that opens click on the 'Open launch 
configuration' link.
* Click on the Arguments tab.  Under Working Directory click the Other radio button and then select your Apache's bin 
directory. Click OK to save and close the dialog. (This step is necessary so that the local application properties
file is found and used.)
* Now select the server in the Servers view and click on the 'play' button.  You should now be able to access the 
server using the URL above.


Configuration
-------------

### Application config
Two configuration systems are in use in the CASDA VO Tools application.

* Spring Boot's [externalized configuration](http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html) which 
uses `application.properties` and `config/application-casda_vo_tools.properties`. These can be within 
the war file or placed in the runtime working directory. See `src/main/resources/application.properties` for 
further details.  
* A custom `config/configuration.yaml` file in the runtime working directory. This is the main means by which an 
external data center would configure their VO Tools instance.

Either config system can be used in a particular deployment. If present, the yaml will override the spring boot config. 
Note that any new keys need to be added to the code for both systems. See the entries in 
`/src/main/java/au/csiro/casda/votools/config/ConfigValueKeys.java` for examples of how to do this.

The configuration can be interactively managed using the endpoint `/configure/home`. This is documented in 
[/src/docs/Deploying CASDA VO Tools.docx](https://stash.csiro.au/projects/CASDA/repos/casda_vo_tools/browse/src/docs/Deploying%20CASDA%20VO%20Tools.docx)

When running under Eclipse, the `src/test/resources/config` folder is copied into the local Eclipse-managed server's WEB-INF/classes directory (as it's in the classpath by dint of being located under src/test/resources).

Test cases always load application.properties files manually.

###Logging

The log4j configuration file is detected using default behaviour of log4j, by being named according to convention (log4j2.xml) and provided on the classpath. 
This configuration file is currently bundled with the application. When running under Eclipse, the log configuration file is copied into the local Eclipse-managed server's WEB-INF/classes directory, because it's in the src/test/resources folder, and loaded via the classpath.

When starting up, you will see log4j configuration errors as the logging system tries to talk to syslog (e.g. Error writing to TCP:localhost:514 ). All the logs will be written to:

    /CASDA/application/tomcat/logs/casda_vo_tools.log


Update the license header of the current project source files
--------------------------------------------------------------
Change the relevant value in pom.xml then run this command in command prompt to ensure all files carry the correct license header:

$ mvn license:update-file-header

