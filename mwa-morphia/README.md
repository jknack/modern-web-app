# The Morphia Module
Add Morphia Support to your application.

## Features
* Map entities with: @Entity, @Embedded, etc.

## Configuration
* It depends on **mwa-mongo**, make sure you follow the steps of configuring: [mwa-mongo](https://github.com/edgarespina/modern-web-app/tree/master/mwa-mongo)

* Add maven dependency:

pom.xml:

```xml
    <!-- MWA Morphia -->
    <dependency>
      <groupId>org.knowhow</groupId>
      <artifactId>mwa-morphia</artifactId>
      <version>${mwa-version}</version>
    </dependency>
```

* Register the MorphiaModule:

Main.java:

```java
package mwa.demo;

import org.knowhow.mwa.Startup;
import org.knowhow.mwa.morphia.MorphiaModule;
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
    return new Class<?>[] {MorphiaModule.class, ...};
  }
}
```
* Publish persistent classes (class annotated with @Entity, @Embedded, etc.)

MyDomainModule.java:

```java
package mwa.demo.domain;

import org.knowhow.mwa.morphia.MorphiaConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * My domain module.
 *
 * @author edgar.espina
 * @since 0.1
 */
@Configuration
public class MyDomainModule {

  /**
   * Publish persistent classes required by {@link MorphiaModule}.
   *
   * @return All persistent classes required by {@link MorphiaModule}.
   * @throws Exception If the persisten classes cannot be detected.
   */
  @Bean
  public MorphiaConfigurer morphiaConfigurer() throws Exception {
    // Publish the mwa.demo.domain package as source of persistent classes.
    return new MorphiaConfigurer(getClass().getPackage());
  }
}

```

## Usage
MyService.java:

```java
  public class MyService {

    /**
     * The morphia datastore
     */
    private Datastore template;

    @Inject
    public MyService(Datastore datastore) {
      this.datastore = datastore;
    }

    public MyEntity create(MyEntity entity) {
       this.datastore.save(entity);
       return entity;
    }
  }
```

## External dependencies
* Spring-Data for Mongo 2+
* Morphia
* Mongo Java Driver
* MongoDB Server