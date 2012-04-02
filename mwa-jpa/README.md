# The JPA Module
The JpaModule configures your application with JPA 2.0 Support.

## Features
* A datasource for development or production like environment.
* An EntityManagerFactory JPA 2.0 service.
* An EntityManager JPA 2.0 service.
* A JpaTransactionManager Spring service.
* Resource cleanup at application shutdown.

## Configuration
* Add maven dependency:

pom.xml:

```xml
    <!-- MWA JPA -->
    <dependency>
      <groupId>org.knowhow</groupId>
      <artifactId>mwa-jpa</artifactId>
      <version>${mwa-version}</version>
    </dependency>
```

* Add the "db" property

application.properties:

```properties
###############################################################################
#                             Database setup
###############################################################################
# Embedded/Development database: mem or fs
db=fs

# Production database
#db=jdbc:mysql://localhost/mydb
#db.user=
#db.password=
```

* Register the JpaModule:

Main.java:

```java
package ar.jug;

import org.knowhow.mwa.Startup;
import org.knowhow.mwa.jpa.JpaModule;
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
    return new Class<?>[] {JpaModule.class, ...};
  }
}
```

## Usage
MyService.java:

```java
  public class MyService {

    private EntityManager em;

    @Inject
    public MyService(EntityManager em) {
      this.em = em;
    }

    @Transactional
    public void doSomething() {
       // Use the EntityManager here.
    }
  }
```
## Advanced
  TODO: Complete this section.

## Dependencies
* Spring 3.1+: orm, jdbc and tx.
* Hibernate 4+
* BoneCP for high performance connection pools.
* H2 for embedded/development database.