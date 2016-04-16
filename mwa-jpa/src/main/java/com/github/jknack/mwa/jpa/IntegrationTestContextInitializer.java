package com.github.jknack.mwa.jpa;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import com.github.jknack.mwa.ApplicationContextConfigurer;

/**
 * Application context initializer that adds the properties
 * required by MWA's ApplicationContextConfigurer and related
 * classes. Useful mostly for integration tests and proofs of
 * concepts.
 * @author Paul Hicks
 */
public class IntegrationTestContextInitializer implements
    ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(final GenericApplicationContext applicationContext) {
    MutablePropertySources propertySources = new MutablePropertySources();
    // use mem db and publish a namespace
    Map<String, Object> testProperties = new HashMap<String, Object>();
    testProperties.put("db", "mem");
    testProperties.put("application.ns", getClass().getPackage().getName());

    applicationContext.registerBeanDefinition("conversionService",
        new RootBeanDefinition(DefaultConversionService.class));

    propertySources.addFirst(new MapPropertySource("integrationTest", testProperties));

    ApplicationContextConfigurer.configure(applicationContext, propertySources);
  }
}
