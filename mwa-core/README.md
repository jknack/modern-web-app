# MWA Core

## Introduction
MWA let you startup for application with minimal setup.

## Creates a Maven Project
* mkdir <your-project-name>
* cd <your-project-name>
* create a new file: pom.xml
* open the file: pom.xml and put this content:
```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
  <parent>
    <groupId>org.knowhow</groupId>
    <artifactId>modern-web-app</artifactId>
    <version>0.1.0-SNAPSHOT</version>
  </parent>

  <modelVersion>4.0.0</modelVersion>
  <groupId>your group id</groupId>
  <artifactId>your project name</artifactId>
  <version>1.0</version>
  <packaging>war</packaging>
</project>
```