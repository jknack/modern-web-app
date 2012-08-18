# MWA Core Module

## Features
* No web.xml
* The application's environment
* The application's mode
* The application's namespace

## No web.xml
Since Servlet 3.0 API the web.xml file is optional. The platform replace the web.xml with the ```com.github.jknack.Startup``` class.

The ```Startup``` class configure the Spring Root Context and the Dispatcher Servlet. For example:

```java
package com.myapp;

import com.github.jknack.mwa.Startup;

public class MyApp extends Startup {
}
```

## The application's environment
The application's environment consist of:
 * The application properties files
 * The Java system properties
 * The environment variables

By default, the platform looks for the ```application.properties``` file in the root of classpath. The next example show you how to override the name of the property file.

```java
package com.myapp;

import com.github.jknack.mwa.Startup;

public class MyApp extends Startup {
  
  @Override
  public String propertySource() {
    return "myapp.properties";
  }
}
```

Or you can specify multiples property files by:

```java
package com.myapp;

import com.github.jknack.mwa.Startup;

public class MyApp extends Startup {
  
  @Override
  public String[] propertySources() {
    return new String[] {"myapp.properties", "more.properties"};
  }
}
```

### Injecting properties
The platform configure all the Spring infrastructure to let you inject properties in your beans using: ```@Named```. For example

```properties
my.int=123
my.string=Hello
```

```java
package com.myapp.beans;

@Component
public class MyBean {

  public MyBean(@Named("my.int") int myInt, @Named("my.string") int myString) {
  }
}
```

Please note you can access to the Java system properties or even to system variables.

Alernative you can use ```@Value("${my.int}")``` from Spring.

## The application's mode
A special property MUST to be declared in one of your property sources:

```properties
application.mode=dev
```

The default is: ```dev``` and it has a special meaning for some components of the platform. You can type there whatever you want, just remember ```dev``` is special.

### Activating Spring profiles
The ```application.mode``` configure a Spring Profile. For example:

```java
@Profile("dev")
public class MyDevBean {}
```

If you set the application's mode to: ```foo``` then:

```java
@Profile("foo")
public class MyFooBean {}
```
If you want to learn more about Spring profiles, have a look at [Introducing Profile](http://blog.springsource.org/2011/02/14/spring-3-1-m1-introducing-profile/)

## The application's namespace
The platform set sensible defaults in order to increase application configuration time and reduce complexity.

The application's namespace is defined by your startup class: ```com.myapp.MyApp```.
Spring will scan and detect all the beans under ```com.myapp``` package/sub-package.

### Overriding the default namespace
You can override or extend the application's namespace by overriding the method: ```Startup#namespace```

```java
public class MyApp extends Startup {
  ...
  protected Package[] namespace() {
    return new Package[] {Package.getPackage("external.namespace")};
  }
}
```

### Injecting the namespace
In case you need it, you can access to the application's namespace by doing:

```java
public class MyBean {
 
  @Inject
  public MyBean(List<Package> namespace) {
    ...
  }
}
```
