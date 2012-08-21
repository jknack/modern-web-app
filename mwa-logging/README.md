# The logging module
The logging module is basically a maven pom project which configure SLF4J and Logback.

In addition, it removes all the legacy log libraries from your application (commons-logging, jul and log4j).

## logback.xml example

```xml
<configuration scanPeriod="1 seconds" scan="true">

  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%d{HH:mm:ss.SSS} [%thread] %-4level %logger - %msg%n
      </pattern>
    </encoder>
  </appender>

  <root level="info">
    <appender-ref ref="STDOUT" />
  </root>
</configuration>
```

You can check the [logback](http://logback.qos.ch/) site for more information.