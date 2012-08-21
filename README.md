# A Modern Web Application Architecture

## Introduction
MWA is a development platform and practices that let you build new applications using:
* Servlet 3.0
* Spring 3.1.x
* Maven 3.x

### What is MWA?
* A set of (best) practices for building web applications using Servlet 3.0, Spring 3.1.x with Maven 3.x.
* It applies sensible defaults to Spring application.
* RESTFUL programming is doing by Spring MVC with JSON serialization.
* You don't have to learn anything specific about MWA.
* It reduces startup and configuration time of new applications.
* It integrate other technologies with Spring.
* Promotes the use of reusable piece of software.
* It is an effort for making Java web applications less verbose and fun.

### What is NOT MWA?
* It's NOT a new framework.
* If you know Maven and Spring, you're ready to go.

### Technology Stack
* Servlet 3.0
* Dependency Injection (Spring 3.1.x)
* REST (Spring MVC 3.1x)
* JSON (Jackson 2.x)
* JPA 2.x (Hibernate 4.x)
* Bean Validation API (Hibernate Validator)
* QueryDSL
* Mongo
* SLF4J and Logback
* Maven 3.x

## Fundamentals
### No web.xml
Thanks to the Servlet 3.0 API and servlet container like Jetty 8.x and Tomcat 7.x

### No XML for dependency injection
[JSR-330](http://www.jcp.org/en/jsr/detail?id=330) annotations for Spring bean definitions and dependency injection.

### The Spring Application Context
In order to simplify the startup and configuration time, the platform creates a single/unique Spring Application Context (a.k.a as Root Context).  
The Spring Dispatcher Servlet is bound to the **root context**.

### Logging system
Base on slf4j with logback as native implementation. The platform deals with all the logging classpath issues of legacy logging facade and implementation. You don't have to worry about having commons-logging, log4j, jul issues in your application.

### The application environment
You can define all the properties files you need, by default the platform looks for a: ```application.properties``` at the root of the application classpath.

The [Environment](http://static.springsource.org/spring/docs/current/javadoc-api/org/springframework/core/env/Environment.html) contains all your properties files, the system environment and the Java system properties.

The application environment MUST define a special property:
application.properties:

```properties
######################################################################
#                       MyApp environment
######################################################################
# The application's mode: dev or anything else. Default is: dev.
application.mode=dev
```

Sensible defaults applies when the application is running in dev mode.

### Reusable Software
Reusable piece of software are delivered in one of the two formats:
* Maven profiles, activated by the presence of specific files
* Spring Configuration classes (a.k.a Module)

## Getting Started
* Create a Maven Web Project
```text
config
          application.properties
          logback-test.xml
src
       main
           java
           webapp
pom.xml
```

* Edit your ```pom.xml``` file with:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">

  <parent>
    <groupId>com.github.jknack</groupId>
    <artifactId>modern-web-app</artifactId>
    <version>${mwa-version}</version>
  </parent>

  <modelVersion>4.0.0</modelVersion>
  <groupId>mwa.example</groupId>
  <artifactId>getting-started</artifactId>
  <packaging>war</packaging>

  <dependencies>
    <!-- Servlet API -->
    <dependency>
      <groupId>javax.servlet</groupId>
      <artifactId>javax.servlet-api</artifactId>
      <scope>provided</scope>
    </dependency>

    <dependency>
      <groupId>com.github.jknack</groupId>
      <artifactId>mwa-core</artifactId>
      <version>${mwa-version}</version>
    </dependency>
  </dependencies>
```

* Edit the ```application.properties``` with:
```properties
application.mode=dev
```

* Create the file ```logback-test.xml``` under ```config```:

```xml
<configuration scanPeriod="1 seconds" scan="true">

  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%d{HH:mm:ss.SSS} [%thread] %-4level %logger - %msg%n
      </pattern>
    </encoder>
  </appender>

  <root level="info">
    <appender-ref ref="STDOUT" />
  </root>
</configuration>
```
This is a logack configuration file useful for dev, because:
  1. It scan for changes every second
  2. It write the output to the standard console

* Create a new class ```mwa.example.MyApp``` under ```src/main/java```

* Edit ```mwa.example.MyApp``` with:

```java
package mwa.example;

import com.github.jknack.Startup;

public class MyApp extends Startup {}
```

**NOTE**: Please note the namespace or base package: ```mwa.example```. The namespace is used by Spring, JPA and others.

* Using Eclipse: ```mvn eclipse:clean eclipse:eclipse```. The application can be deployed with Eclipse WTP in a Servlet 3.x container (like Tomcat 7).

* or you can use Maven: ```mvn jetty:run```

* That's all!!!

### JPA 2.x Features
In this section we're going to add JPA 2.x Support, so let's start:

* Edit the ```pom.xml``` file with:

```xml
 ...
    <dependency>
      <groupId>com.github.jknack</groupId>
      <artifactId>mwa-jpa</artifactId>
      <version>${mwa-version}</version>
    </dependency>
 ...
```

* Configure the database you want to connect in ```application.properties```:

```properties
db=mem
```

Because we want to go fast, we select an in-memory database.

* Re-generate Eclipse metadata ```mvn eclipse:clean eclipse:eclipse```:
* Import the JpaModule, ```MyApp.java```:

```java
...
import com.github.jknack.jpa.JpaModule;
...
public class MyApp extends Startup {
  public Class<?>[] imports() {
    return new Class<?>[] {
      JpaModule.class
    };
  }
}
```

* Adding a JPA entity, ```User.java```:

```java
package mwa.example.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class User {
  @Id
  private int id;

  private String name;

  ...
}
```

* The User entity belong to the namespace: ```mwa.example```, so, the JpaModule discover all the JPA entities automatically.
* Using the EntityManager. Create a new class ```mwa.example.domain.UserManager```:

```java
import javax.inject.Inject;

import javax.persistence.EntityManager;
import org.springframework.stereotype.Component;

@Component
public class UserManager {
  private EntityManager em;

  @Inject
  public UserManager(EntityManager em) {
    this.em = em;
  }

  public User find(int id) {
    return em.find(User.class, id);
  }
}
```

* Adding Transactions

```java
...
import org.springframework.transaction.annotation.Transactional;
...
@Transactional
...
public class UserManager {
...
}
```

* That's all!

### QueryDSL JPA Support
Optionally, you can enabled [Query DSL JPA Support](http://www.querydsl.com/static/querydsl/2.1.0/reference/html/ch02s02.html) for type safe queries. Let's see how easy is:
* Create a new folder: ```src/main/etc```
* Inside the folder ```src/main/etc``` create the file: ```querydsl-jpa.md```
* Run: ```mvn eclipse:clean eclipse:eclipse``` or ```mvn clean install```
* That's all!!!

So, how it works? The ```src/main/etc/query-dsl.md``` activate a Maven profile that does 3 things:
  1. Add the necessary dependencies to your project
  2. Re-generate the QueryDSL generated classes during a Maven build.
  3. Configure Eclipse for live editing and synchronization of QueryDSL generated classes.

### Want to learn more?
Check out the modules documentation
* [Core Module] (modern-web-app/tree/develop/mwa-core): applies sensible defaults to your application.
* [Mvc Module] (modern-web-app/tree/develop/mwa-mvc): useful additions to Spring MVC.
* [Logging Module] (modern-web-app/tree/develop/mwa-logging): classpath configuration of Java logging libraries.
* [JPA 2.0 Module] (modern-web-app/tree/develop/mwa-jpa): configuration of Spring beans required by JPA 2.x.
* [Mongo Module] (modern-web-app/tree/develop/mwa-mongo): configuration of Spring beans for MongoDB.
* [Morphia Module] (modern-web-app/tree/develop/mwa-morphia): configuration of Spring beans for Morphia MongoDB.
* [Web Resource Optimizer Module] (modern-web-app/tree/develop/mwa-wro4j): javascript and css compression, optimization and more.
* [Mail Module] (modern-web-app/tree/develop/mwa-mail): a simple and powerful mail facade.
