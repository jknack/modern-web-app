package com.github.jknack.mwa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

public class ApplicationContextConfigurerTest {

  public static class BeanTest {
    private String named;
    private String value;

    @Inject
    public BeanTest(@Named("prop") final String named, @Value("${prop}") final String value) {
      this.named = named;
      this.value = value;
    }
  }

  @Test
  public void contextConfigurer() {
    System.setProperty("prop", "it works");
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context.register(BeanTest.class);

    ApplicationContextConfigurer.configure(context, new MutablePropertySources());
    context.refresh();

    BeanTest bean = context.getBean(BeanTest.class);
    assertNotNull(bean);
    assertEquals("it works", bean.named);
    assertEquals("it works", bean.value);
  }

  @Test
  public void systemPropertiesPrecedence() {
    System.setProperty("prop", "system");
    MutablePropertySources propertySources = new MutablePropertySources();
    Map<String, Object> propertySource = new HashMap<String, Object>();
    propertySource.put("prop", "app");

    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context.register(BeanTest.class);

    propertySources.addFirst(new MapPropertySource("appProperties", propertySource ));

    ApplicationContextConfigurer.configure(context, propertySources);
    context.refresh();

    BeanTest bean = context.getBean(BeanTest.class);
    assertNotNull(bean);
    assertEquals("system", bean.named);
    assertEquals("system", bean.value);
  }
}
