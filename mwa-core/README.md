# MWA Core

## Introduction
MWA let you startup for application with minimal setup.

## Create a Maven Project
* mkdir your-project-name
* cd your-project-name
* create a new file: pom.xml  
* open the file: pom.xml and put this content:


     <?xml version="1.0" encoding="UTF-8"?>
     <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
       <parent>
         <groupId>org.knowhow</groupId>
         <artifactId>modern-web-app</artifactId>
         <version>0.1.0-SNAPSHOT</version>
       </parent>
       <modelVersion>4.0.0</modelVersion>
       <groupId>your group id</groupId>
       <artifactId>your project name</artifactId>
       <version>1.0</version>
       <packaging>war</packaging>
     </project>

* Create some folders
  * src/main/java
  * src/main/resources
  * src/main/webapp
  * src/test/java
  * src/test/resources

* run **mvn eclipse:clean eclipse:eclipse**

## Use mwa-core
Edit pom.xml file and add the mwa-core dependencies.


    <dependencies>
      <!-- Servlet API -->
      <dependency>
        <groupId>javax.servlet</groupId>
        <artifactId>javax.servlet-api</artifactId>
        <scope>provided</scope>
      </dependency>

      <!-- MWA Boot -->
      <dependency>
        <groupId>org.knowhow</groupId>
        <artifactId>mwa-core</artifactId>
        <version>0.1.0-SNAPSHOT</version>
      </dependency>
    </dependencies>

## Configure logging and application's properties
* Create the file: **application.properties** in src/test/resources


     #############################################################  
     #         Application environment  
     #############################################################  
     # The profile to use: dev or anything else. Default is: dev.  
     application.mode=dev  
     application.name=${project.artifactId}  
     application.version=${project.version}  
     

* Create a file: *logback-test.xml* in src/test/resources  


     <configuration scanPeriod="1 seconds" scan="true">
       <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
         <encoder>
           <pattern>%d{HH:mm:ss.SSS} [%thread] %-4level %logger - %msg%n</pattern>
         </encoder>
       </appender>
       <root level="INFO">
         <appender-ref ref="STDOUT" />
       </root>
     </configuration>

## Create Main.java
    package your.package;
    
    import org.knowhow.mwa.Startup;
    
    public class Main extends Startup {
       public Class<?>[] modules() {
         return new Class<?>[] {};
       }
    }

## Run
* mvn jetty:run
