# TodoMVC JPA example
An adaptation of http://addyosmani.github.com/todomvc/architecture-examples/backbone/ created by Addy Osmani

## The demo includes:

* A modern web application architecture using Spring 3.x, Servlet 3.x and REST

* A startup or bootstrapper class: ```com.github.jknack.examples.todomvc.TodoMVC```

* The use of JPA 2.x

* The use of Wro4j (JavaScript/Css code quality, compression and optimization)

* The use of a Logic-less Template Engine: ```Handlebars```. Handlebars templates are compiled in the server and used in the frontend.

* Finally it enabled: Checkstyle and the Eclipse Code Formatter.

## Starting the demo app

* git clone ```git://github.com/jknack/modern-web-app.git```

* cd modern-web-app/examples/todomvc-jpa

* mvn jetty:run

* Open browser and got to: ```http://localhost:8080/todomvc-jpa/```
