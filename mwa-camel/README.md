# The Apache Camel Module
This module offers the following functionality:

* A ```CamelContext```
* A ```ProducerTemplate```
* A ```ConsumerTemplate```
* The 'properties://' endpoint is binding to the {@link Environment}.

# The Camel Configurer
You can configure a ```CamelContext``` using the ```CamelConfigurer``` callback. Just add
them to the ```ApplicationContext```. Optionally, a ```CamelConfigurer``` can implement the
```Ordered``` interface.

# Registering ```RoutesBuilder```
You can add routes to the ```CamelContext``` by publishing ```RoutesBuilder``` in the ```ApplicationContext```.

# Configuration

* camel.delayer: Sets a delay value in millis that a message is delayed at every step it takes
in the route path, slowing the process down to better observe what is occurring Is disabled by
default

* camel.handleFault: Sets whether fault handling is enabled or not (default is disabled).

* camel.shutdownRoute: Sets the ShutdownRoute option for routes. Default is: ```ShutdownRoute#Default``` 

* camel.shutdownRunningTask: Sets the ShutdownRunningTask option to use when shutting down a
route. Default is: ```ShutdownRunningTask#CompleteCurrentTaskOnly```

* camel.streamCaching: Sets whether stream caching is enabled or not. Default is disabled.
* camel.tracing: Sets whether tracing is enabled or not (default is disabled).
