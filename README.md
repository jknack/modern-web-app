# A Modern Web Application Architecture

## Introduction
MWA it's an effort for making Java Web Applications less verbose and more fun.

### What is MWA?
* A set of best and modern practices for building Java Web Applications using Maven 3, Servlet 3.0 and Spring 3.1+.
* It applies sensible defaults for Spring 3.1+ MVC applications.
* You don't have to learn anything specific about MWA.
* It reduces startup and configuration time of new applications.
* Organize your project and code by promoting the use of Modules.
* Finally, MWA it's an effort for making Java Web Applications less verbose and more fun.

### What is NOT MWA?
* It's NOT a framework.
* If you know Maven and Spring, you're ready to go.

## Fundamentals
### No web.xml
MWA promote the use of Servlet 3.0 for Java Web Applications (you'll need a Servlet 3.0 container: like Tomcat 7.x).

### No XML for dependency injection
MWA promote the use Java Annotation over XML for Spring bean definition and dependency injection. You wont find XML for configuring Spring in MWA.

### The Spring Application Context
In order to simpplify the startup and configuration time, MWA creates a single/unique Spring Application Context (a.k.a as Root Context).  
The same is true for the Spring Dispatcher Servlet, there is only **one** and it's binded to the **root context**.

### JSR-330 for DI
MWA promote the use of the JSR-330 API for doing DI, but you can use the custom Spring annotations if you want to.

### Environment API
MWA promote the use of the [Environment API](http://static.springsource.org/spring/docs/current/javadoc-api/org/springframework/core/env/Environment.html) for dealing with application configuration.

### Application's mode
The application's mode controls how some application components should behave at runtime. MWA, reconize one mode as special: "dev".  
Some modules depends on the application's mode and configure in one way or another depending if the app is running in "dev" or not.

### Modules
A module is a collection of reusable code and configuration for new and existing applications.

#### Characteristics
* Must to be marked with @Configuration Spring Annotation.
* Must be registered.
* The module's namespace or package is added as a source of bean definitions. What does it means? The module's package is scanned for discovering beans.

### Available modules
* [mwa-core] (https://github.com/edgarespina/modern-web-app/tree/master/mwa-core)
* [mwa-jpa] (https://github.com/edgarespina/modern-web-app/tree/master/mwa-jpa)

## Getting Started