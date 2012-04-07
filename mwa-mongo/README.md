# The Mongo Module
The MongoModule configures your application with MongoDB Support.

## Features
* A Mongo uri: mongodb://[username:password@]host1[:port1][,host2[:port2],...[,hostN[:portN]]][/[database][?options]]
* A Mongo database connection.
* A MongoDbFactory service from Spring Data.
* A MongoTemplate service from Spring Data.

## Configuration
* Add maven dependency:

pom.xml:

```xml
    <!-- MWA Mongo -->
    <dependency>
      <groupId>org.knowhow</groupId>
      <artifactId>mwa-mongo</artifactId>
      <version>${mwa-version}</version>
    </dependency>
```

* Add the "db" property

application.properties:

```properties
###############################################################################
#                             Mongo Database setup
###############################################################################
# mongodb://[username:password@]host1[:port1][,host2[:port2],...[,hostN[:portN]]][/[database][?options]]
db=mongodb://localhost/mydb

```

* Register the MongoModule:

Main.java:

```java
package ar.jug;

import org.knowhow.mwa.Startup;
import org.knowhow.mwa.mongo.MongoModule;
/**
 * Startup the web-app.
 *
 * @author edgar.espina
 * @since 0.1
 */
public class Main extends Startup {

  /**
   * Publish all the modules.
   *
   * @return All the modules.
   */
  @Override
  protected Class<?>[] modules() {
    return new Class<?>[] {MongoModule.class, ...};
  }
}
```

## Usage
MyService.java:

```java
  public class MyService {

    /**
     * The mongo db template
     */
    private MongoTemplate template;

    @Inject
    public MyService(MongoTemplate template) {
      this.template = template;
    }

    public MyEntity create(MyEntity entity) {
       this.template.save(entity);
    }
  }
```

## External dependencies
* Spring 3.1+: data
* Mongo Java Driver
* MongoDB Server