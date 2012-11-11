package com.github.jknack.mwa.camel;

import static org.junit.Assert.assertEquals;

import javax.inject.Named;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MutablePropertySources;

import com.github.jknack.mwa.ApplicationContextConfigurer;

public class CamelModuleTest {

  public static class CamelCallback implements CamelConfigurer {
    @Override
    public void configure(final CamelContext camelContext) throws Exception {
      camelContext.addRoutes(new RouteBuilder() {
        @Override
        public void configure() throws Exception {
          from("direct:configurer").to("mock:out");
        }
      });
    }
  }

  @Named("printer")
  public static class Printer implements Processor {

    @Override
    public void process(final Exchange exchange) throws Exception {
      String message = exchange.getIn().getBody(String.class);
      assertEquals("Hi camel!", message);
    }

  }

  public static class CamelRouteBuilder extends RouteBuilder {
    @Override
    public void configure() throws Exception {
      from("direct:router").to("mock:out");

      from("direct:props")
          .transform().simple("Hi ${myProp}!").processRef("printer");
    }
  }

  @Test
  public void camelContext() throws Exception {
    System.setProperty("myProp", "camel");

    AnnotationConfigApplicationContext context =
        new AnnotationConfigApplicationContext();
    context.register(CamelModule.class, CamelRouteBuilder.class,
        CamelCallback.class, Printer.class);
    ApplicationContextConfigurer.configure(context, new MutablePropertySources());
    context.refresh();

    ProducerTemplate template = context.getBean(ProducerTemplate.class);

    template.sendBody("direct:configurer", "Configurer works!");
    template.sendBody("direct:router", "Router works!");
    template.sendBody("direct:props", "...");

    context.destroy();
  }
}
