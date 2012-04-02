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
* Publish persistent classes (class annotated with @Entity, @Embedded, etc.)

MyDomainModule.java:

```java
package sample.domain;

import org.knowhow.mwa.jpa.JpaConfigurer;
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
   * Publish persistent classes required by {@link JpaModule}.
   *
   * @return All persistent classes required by {@link JpaModule}.
   * @throws Exception If the persisten classes cannot be detected.
   */
  @Bean
  public JpaConfigurer jpaConfigurer() throws Exception {
    // Publish the demo.domain package as source of persistent classes.
    return new JpaConfigurer(getClass().getPackage());
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
## Export your DDL
  TODO:

## Advanced configuration
  TODO: Complete this section.

## External dependencies
* Spring 3.1+: orm, jdbc and tx.
* Hibernate 4+
* BoneCP for high performance connection pools.
* H2 for embedded/development database.