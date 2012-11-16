# The JPA Module
This module add JPA 2.x features using Hibernate 4.x

## Features
* A datasource for development or production like environment.
* An EntityManagerFactory service.
* An EntityManager service.
* A JpaTransactionManager Spring service.
* Resource cleanup at application shutdown.

## Using the JpaModule
### Database configuration
The ```db``` property configure a database to use.

### Using a memory database
If you set the ```db``` property to ```mem```, a memory database will be ready for use

```properties
###############################################################################
#                             Database setup
###############################################################################
# Embedded/Development database: mem or fs
db=mem

```
**Please note you need the H2 driver in your classpath**

### Using a file system database
If you set the ```db``` property to ```fs```, a memory database will be ready for use

```properties
###############################################################################
#                             Database setup
###############################################################################
# Embedded/Development database: mem or fs
db=fs

```

The database is created in the temporary directory defined by your operating system.

**Please note you need the H2 driver in your classpath**

### Using a JDBC URI
When a JDBC URI is found, a ```dataSource pool``` is configured using [BoneCP](http://jolbox.com/)

```properties
###############################################################################
#                             Database setup
###############################################################################
# Production database
db=jdbc:mysql://localhost/mydb
db.user=user
db.password=pass

# Advanced option for JDBC URI
# See BoneCP: http://jolbox.com
# default values

#db.driver=com.mysql.jdbc.Driver
#db.iddleConnectionTestPeriod=14400
#db.iddleMaxAge=360
#db.maxConnectionsPerPartition=30
#db.minConnectionsPerPartition=10
#db.partitionCount=3
#db.acquireIncrement=5
#db.statementsCacheSize=100
#db.releaseThreads=3
```

### Enabling the JpaModule
Just imports the ```JpaModule``` in your application.

```java
package mwa.demo;

import com.github.jknack.mwa.Startup;
import com.github.jknack.mwa.jpa.JpaModule;

public class MyApp extends Startup {

  @Override
  protected Class<?>[] imports() {
    return new Class<?>[] {JpaModule.class, ...};
  }
}
```

The ``JpaModule``` automatically discover all the JPA entities in ```mwa.demo``` package or sub-packages.


### Injecting the EntityManager

```java
  @Component
  public class UserManager {

    private EntityManager em;

    @Inject
    public UserManager(EntityManager em) {
      this.em = em;
    }

    @Transactional
    public void doSomething() {
       // Use the EntityManager here.
    }
  }
```

### Loading fixtures

Fixtures will be loaded from ```classpath://fixtures```. For example, given the entity: ```User```
the ```/fixtures/User.json``` will be automatically loaded and persisted in the database.

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
