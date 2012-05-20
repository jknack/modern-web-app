# MWA Wro4j

## Introduction
This module integrates the excellent library [wro4j](http://code.google.com/p/wro4j/) in MWA.
The wro4j library brings together almost all the modern web tools: JsHint, CssLint, JsMin, Google Closure compressor, YUI Compressor, UglifyJs, Dojo Shrinksafe, Css Variables Support, JSON Compression, Less, Sass, CoffeeScript and much more.

## Configuration
### Maven

```xml
<dependency>
  <groupId>com.github.edgarespina</groupId>
  <artifactId>mwa-wro4j</artifactId>
  <version>${mwa-version}</version>
</dependency>
```

#### Optional configuration
* wro.cacheGzippedContent: When this flag is enabled, the raw processed content
will be gzipped only the first time and all subsequent requests will use the
cached gzipped content. Otherwise, the gzip operation will be performed for
each request. This flag allow to control the memory vs processing power trade-off.
In "dev" this property is set to **false** by default.

* wro.cacheUpdatePeriod: How often to run a thread responsible for refreshing
the cache. In "dev" this property is set to **0** by default.

* wro.connectionTimeout: Timeout (seconds) of the url connection for external
resources. This is used to ensure that locator doesn't spend too much time on
slow end-point. Default is: 2000.

* wro.disableCache:  Flag which will force no caching of the processed content.
In "dev" this property is set to **false**.

* wro.encoding: Encoding to use when reading resources. Default is: "UTF-8"

* wro.gzipEnabled: Gzip enable flag. In "dev" this property is set to **false**.

* wro.header: The parameter used to specify headers to put into the response,
used mainly for caching.

* wro.ignoreEmptyGroups: When a group is empty and this flag is false, the
processing will fail. This is useful for runtime solution to allow filter
chaining when there is nothing to process for a given request. Default is: "true".

* wro.ignoreMissingResources: If true, missing resources are ignored.
By default this value is true.

* wro.jmxEnabled: Allow to turn jmx on or off. By default this value is false.

* wro.mbeanName: A preferred name of the MBean object.

* wro.modelUpdatePeriod: How often in seconds to run a thread responsible for
refreshing the model. In "dev" this property is set to **1**.

* wro.parallelPreprocessing: When true, will run in parallel preprocessing of
multiple resources. In theory this should improve the performance. By default
this flag is false, because this feature is experimental.

## Activating the module

```java
import com.github.edgarespina.mwa.wro4j.WroModule;
...
public class Main extends Startup {
  ...
  protected Class<?>[] modules() {
    return new Class<?>[] {..., WroModule.class };
  }
  ...
} 
```

## Configuring the Web Resource Optimizer
### Directory structure

```text
 /src
     /main
          /webapp
                 /js
                    home.js
                    /libs
                         jquery.js
                         underscore.js
                         backbone.js
                 /css
                     home.css
                 home.html
 pom.xml
```

### Creating groups in: wro.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<groups xmlns="http://www.isdc.ro/wro" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://www.isdc.ro/wro wro.xsd">

  <!-- The home's page -->
  <group name="home">
    <group-ref>defaults</group-ref>
    <js>/js/home.js</js>
    <css>/css/home.css</css>
  </group>

  <group name="defaults">
    <js>/js/libs/jquery.js</js>
    <js>/js/libs/underscore.js</js>
    <js>/js/libs/backbone.js</js>
  </group>
</groups>

```

### Selecting some processors

The goal of a wro processor is to alter a css or js file in some way. There are
two kind of processor: pre and post. A pre-processor works over a single file
(like home.js), on the other hand a post-processor works over multiples files.

```java
import static import static com.github.edgarespina.mwa.wro4j.Processors.*;

@Configuration
public class ViewModule {
  @Bean
  public ProcessorsFactory processorsFactory() {
    return new SimpleProcessorsFactory()
        .addPreProcessor(
            excludes(jsHint(), "/**/libs/*.js"))
        .addPreProcessor(
            excludes(cssLint(), "/**/libs/*.js"))
        .addPostProcessor(yuiCssCompressor())
        .addPostProcessor(googleClosureSimple());
  }
}
```

Here we added a **jsHint** pre-processor that will print errors found in *.js
files excluding all the *.js files under the "libs" directory.

The **cssLint** processor does exactly the same.

These two processors are allowed in "dev", in "no-dev" environment they are off.

Also, you see here the yuiCssCompressor and the googleClosureSimple. These two
works as post-processor and are in a "no-dev" environment.

### Using wro resources in your views
One clear disadvantage of defining your js/css resources in the wro file is that
you have to duplicate the same definitions in your views.

MWA go one step further and publish those resources under two model variables:
**cssLinks** and **scripts**.

For example:

```java
...
@Controller
public class Views {

  @RequestMapping
  public String home() {
    return "home";
  }
}
```

MWA takes the view's name: **home** and look for a group in the wro file with
the same name. If that group isn't found to fallback to the group named as:
**defaults**.

Later, in your views you can include the js/css by doing:

```html
...
<head>
...
${cssLinks}
${scripts}
...
</head>
...
```

Please note that the ${...} expression depends on the view resolver you have
configured. If you're using Mustache it will be: {{{cssLinks}}} or {{{scripts}}}

### HTML problem reporter for jsHint, jsLint, cssLint and lessCss processors

TODO: add some screenshots here

### Minimize, compress and merge js and css

Changing the application's mode to a **no-dev** environment you will get js/css
optimized resources.