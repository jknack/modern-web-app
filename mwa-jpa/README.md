# The JPA Module
This module add JPA 2.x features using Hibernate 4.x

## Features
* A datasource for development or production environments.
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
If you set the ```db``` property to ```fs```, a file database will be ready for use

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
Fixtures can be provided using YAML. Example:

Todo.java:

```java
@Entity
public class Todo {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  private String title;

  private boolean completed;

  public Todo(final int id) {
    this.id = id;
  }

  public Todo() {
  }

  public int getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(final String title) {
    this.title = notEmpty(title, "The title is required.");
  }

  public boolean isCompleted() {
    return completed;
  }

  public void setCompleted(final boolean completed) {
    this.completed = completed;
  }
}

```

Sprint.java:

```java
@Entity
public class Sprint {

  @Id
  private String name;

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }
}

```

fixtures/Model.yml:

```yml
# YAML.
# EBNF Format: '!!' SimpleClassName ['&' anchor]

- !!Sprint &1112
  name: Nov 2012

- !!Sprint &1212
  name: Dec 2012

# Compact Object Notation. http://code.google.com/p/snakeyaml/wiki/CompactObjectNotation
# EBNF Format: ['&' anchor] SimpleClassName '(' (value || id '=' value)* ')'

- &T1 Todo(1):
    title: Maven 3.x
    completed: true
    sprint: *1112

- Todo(2):
    title: Servlet 3.x
    dependsOn: *T1
    sprint: *1212

- Todo(3):
    title: Spring / Spring MVC 3.1

- Todo(4):
    title: JPA 2.x

- Todo(5):
    title: Solr 3.x / Solr 4.x

- Todo(6):
    title: Apache Camel

- Todo(7):
    title: MongoDB

- Todo(8):
    title: Wro4j. JS and CSS management

- Todo(9):
    title: jQuery

- Todo(10):
    title: Handlebars.java and Handlebars.js

- Todo(11):
    title: BackBone.js

```
###### See http://code.google.com/p/snakeyaml/wiki/CompactObjectNotation for more information.

At startup time the JpaModule will load all the entities from ```fixtures/Model.yml``` and persist all them
in the database.

Only transient entities will be persisted. Detached (or already existing entities) wont be affected at all.

By default, fixtures are loaded from a classpath location: ```/fixtures```. You can specify a different location
throw the use of ```db.fixtures``` environment property.

Finally, all the ```*.yml``` files under the ```fixtures``` directory will be automatically detected by the JpaModule.

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
