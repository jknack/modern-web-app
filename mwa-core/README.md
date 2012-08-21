# MWA Core Module

## Features
* No web.xml
* The application's environment
* The application's mode
* The application's namespace
* Servlet Filters as Spring beans

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

The default is: ```dev``` and it has a special meaning inside the platform. You can type there whatever you want, just remember ```dev``` is special.

### Injecting the ```com.github.jknack.Mode```
You can access to the special object: ```Mode``` using Spring DI.

```java
public class MyBean {
 
  @Inject
  public MyBean(Mode mode) {
    ...
  }
}
```
Alternative, a ```com.github.jknack.ModeAware``` is available

```java
public class MyBean implements ModeAware {
 
  public void setMode(Mode mode) {
    ...
  }
}
```


### The Spring's  profile
The platform confgure a Spring's profile using the ```application.mode```.

```java
@Profile("dev")
public class MyDevBean {}
```

This bean will be enabled if and only if ```application.mode=dev```

If you want to learn more about Spring profiles, have a look at [Introducing Profile](http://blog.springsource.org/2011/02/14/spring-3-1-m1-introducing-profile/) blog.

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

### Servlet filters as Spring beans
A Servlet Filter can be configured as a Spring beans using the ```com.github.jknack.FilterMapping```

```java
import static com.github.jknack.FilterMapping.filter;

@Configuration
public class MyApp extends Startup {

  @Bean
  public FilterMapping myFilter(BeanA beanA, BeanB beanB) {
    return filter("/**").through(new MyFilter(beanA, beanB);
  }
}
```

Additionally, you can set the ```order``` property of the ```FilterMapping```:

```java
...
  return filter("/**").through(new MyFilter(beanA, beanB).order(Ordered.HIGHEST_PRECEDENCE);
...
```

The ```order``` is useful for security filters or similar.