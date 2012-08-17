# MWA Core Module

## Features
* No web.xml
* The ```com.github.jknack.Startup``` class
* The application's environment
* The application's mode
* The application's namespace
* Spring profile activation

## No web.xml
Since Servlet 3.0 API the web.xml file is optional. The platform replace the web.xml with the ```com.github.jknack.Startup``` class.

The ```Startup``` class configure the Spring Root Context and the Dispatcher Servlet. For example:

```java
```