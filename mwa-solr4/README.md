# MWA Solr 4.x Module

This module let you embedded a Solr Server inside your Spring Application.

## Configure the Solr home
The following property need to be present in the environment:

```properties
 solr.home=
```

The 'solr.home' property can be a any valid Spring {@link Resource} expression.
Per each Solr 'core' a {@link SolrServer} is created and published into the Spring Application Context.
The bean's name matches the name of the 'core', so at any time you can inject a SolrServer by doing:

```java
public MyService(@Named("core") SolrServer) {
 ...
 }
```

Of course if there is just one core you don't need to add the Named annotation.

## Configure Solr Data Directory

It is recommended to configure a Solr data directoy.

```
solr.dataDir=
```

If <code>solr.dataDir</code> isn't set a temporary directory will be selected.

##Configure Solr URI mapping

By default the Solr will be mounted at <code>/search</code>.
You can change that by:

```
 solr.uri=/query
```

## Fixtures
You can add test or init data by creating a 'fixtures' directory under a Solr core.
Test files are described using the Solr XML format for documents.

### Fixtures properties

* solr.fixtures: enabled or disabled the loading of test files. Default is: true.
* solr.fixtures.async: if true, a new thread will be created for loading the fixtures. Default is: true.
