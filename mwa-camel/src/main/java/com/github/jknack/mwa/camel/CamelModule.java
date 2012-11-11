package com.github.jknack.mwa.camel;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.NoSuchBeanException;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.ShutdownRoute;
import org.apache.camel.ShutdownRunningTask;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.component.properties.PropertiesResolver;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spi.Registry;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

/**
 * <h3>The Apache Camel Module</h3>
 * <p>
 * This module offers the following functionality:
 * <ul>
 * <li>A {@link CamelContext}.</li>
 * <li>A {@link ProducerTemplate}.</li>
 * <li>A {@link ConsumerTemplate}.</li>
 * <li>The 'properties://' endpoint is binding to the {@link Environment}.</li>
 * </ul>
 * </p>
 * <h3>The Camel Configurer</h3>
 * <p>
 * You can configure a {@link CamelContext} using the {@link CamelConfigurer} callback. Just add
 * them to the {@link ApplicationContext}. Optionally, a {@link CamelConfigurer} can implement the
 * {@link Ordered} interface.
 * </p>
 * <h3>Registering {@link RoutesBuilder}</h3>
 * <p>
 * You can add routes to the {@link CamelContext} by publishing {@link RoutesBuilder} in the
 * {@link ApplicationContext}.
 * </p>
 * <h3>Camel Configuration</h3>
 * <ul>
 * <li>camel.delayer: Sets a delay value in millis that a message is delayed at every step it takes
 * in the route path, slowing the process down to better observe what is occurring Is disabled by
 * default</li>
 * <li>camel.handleFault: Sets whether fault handling is enabled or not (default is disabled).</li>
 * <li>camel.shutdownRoute: Sets the ShutdownRoute option for routes. Default is:
 * {@link ShutdownRoute#Default}</li>
 * <li>camel.shutdownRunningTask: Sets the ShutdownRunningTask option to use when shutting down a
 * route. Default is: {@link ShutdownRunningTask#CompleteCurrentTaskOnly}</li>
 * <li>camel.streamCaching: Sets whether stream caching is enabled or not. Default is disabled.</li>
 * <li>camel.tracing: Sets whether tracing is enabled or not (default is disabled).</li>
 * </ul>
 *
 * @author edgar.espina
 */
@Configuration
public class CamelModule {

  /**
   * An {@link ApplicationContext} camel registry.
   *
   * @author edgar.espina
   *
   */
  private static final class ApplicationContextRegistry implements Registry {

    /**
     * The application's context.
     */
    private ApplicationContext applicationContext;

    /**
     * Creates a new {@link ApplicationContextRegistry}.
     *
     * @param applicationContext The {@link ApplicationContext}. Required.
     */
    public ApplicationContextRegistry(final ApplicationContext applicationContext) {
      this.applicationContext = notNull(applicationContext,
          "The application's context is required.");
    }

    @Override
    public <T> T lookup(final String name, final Class<T> type) {
      Object answer;
      try {
        answer = applicationContext.getBean(name, type);
      } catch (NoSuchBeanDefinitionException e) {
        return null;
      } catch (BeanNotOfRequiredTypeException e) {
        return null;
      }

      // just to be safe
      if (answer == null) {
        return null;
      }

      try {
        return type.cast(answer);
      } catch (Throwable e) {
        String msg = "Found bean: " + name + " in ApplicationContext: " + applicationContext
            + " of type: " + answer.getClass().getName() + " expected type was: " + type;
        throw new NoSuchBeanException(name, msg, e);
      }
    }

    @Override
    public Object lookup(final String name) {
      try {
        return applicationContext.getBean(name);
      } catch (NoSuchBeanDefinitionException e) {
        return null;
      }
    }

    @Override
    public <T> Map<String, T> lookupByType(final Class<T> type) {
      return applicationContext.getBeansOfType(type);
    }
  }

  /**
   * Startup a {@link CamelContext}, {@link ProducerTemplate} and a {@link ConsumerTemplate}.
   *
   * @author edgar.espina
   *
   */
  private static class StartCamelContext implements CamelConfigurer, Ordered {

    /**
     * The application's context.
     */

    private ApplicationContext context;

    /**
     * Creates a new {@link StartCamelContext}.
     *
     * @param context The application's context. Required.
     */
    public StartCamelContext(final ApplicationContext context) {
      this.context = notNull(context, "The application's context is required.");
    }

    @Override
    public void configure(final CamelContext camelContext) throws Exception {
      Iterable<RoutesBuilder> routes = context.getBeansOfType(RoutesBuilder.class).values();
      for (RoutesBuilder route : routes) {
        camelContext.addRoutes(route);
      }
      camelContext.start();
      context.getBean(ProducerTemplate.class).start();
      context.getBean(ConsumerTemplate.class).start();
    }

    @Override
    public int getOrder() {
      return LOWEST_PRECEDENCE;
    }

  }

  /**
   * Creates a new {@link CamelContext}.
   *
   * @param applicationContext The application context. Required.
   * @return A new {@link CamelContext}.
   */
  @Bean(destroyMethod = "stop")
  public DefaultCamelContext camelContext(final ApplicationContext applicationContext) {
    notNull(applicationContext, "The application's context is required.");
    Environment env = applicationContext.getEnvironment();

    final DefaultCamelContext camelContext = new DefaultCamelContext(
        new ApplicationContextRegistry(applicationContext));

    // Runtime configuration
    Long delayer = env.getProperty("camel.delayer", Long.class);
    camelContext.setDelayer(delayer);

    Boolean handleFault = env.getProperty("camel.handleFault", Boolean.class, Boolean.FALSE);
    camelContext.setHandleFault(handleFault);

    String shutdownRoute = env.getProperty("camel.shutdownRoute", ShutdownRoute.Default.name());
    camelContext.setShutdownRoute(ShutdownRoute.valueOf(shutdownRoute));

    String shutdownRunningTask = env.getProperty("camel.shutdownRunningTask",
        ShutdownRunningTask.CompleteCurrentTaskOnly.name());
    camelContext.setShutdownRunningTask(ShutdownRunningTask.valueOf(shutdownRunningTask));

    Boolean streamCaching = env.getProperty("camel.streamCaching", Boolean.class, Boolean.FALSE);
    camelContext.setStreamCaching(streamCaching);

    Boolean tracing = env.getProperty("camel.tracing", Boolean.class, Boolean.FALSE);
    camelContext.setTracing(tracing);

    camelContext.addComponent("properties", camelProperties(applicationContext));

    return camelContext;
  }

  /**
   * Start Camel once all the {@link RoutesBuilder} and {@link CamelConfigurer} has been published
   * into the {@link ApplicationContext}.
   *
   * @param context The application's context. Required.
   * @return The camel start configurer.
   */
  @Bean
  public CamelConfigurer camelStartConfigurer(final ApplicationContext context) {
    return new StartCamelContext(context);
  }

  /**
   * Creates and publish a {@link ProducerTemplate}.
   *
   * @param context The camel context. Required.
   * @return A new {@link ProducerTemplate}.
   * @throws Exception If something goes wrong.
   */
  @Bean(destroyMethod = "stop")
  public ProducerTemplate camelProducerTemplate(final CamelContext context) throws Exception {
    notNull(context, "The camel's context is required.");
    return context.createProducerTemplate();
  }

  /**
   * Creates and publish a {@link ConsumerTemplate}.
   *
   * @param context The camel context. Required.
   * @return A new {@link ConsumerTemplate}.
   * @throws Exception If something goes wrong.
   */
  @Bean(destroyMethod = "stop")
  public ConsumerTemplate camelConsumerTemplate(final CamelContext context) throws Exception {
    notNull(context, "The camel's context is required.");
    return context.createConsumerTemplate();
  }

  /**
   * Extract the environment properties as a {@link PropertiesComponent Camel Component}.
   *
   * @param applicationContext The application's context.
   * @return A {@link PropertiesComponent Camel Component}.
   */
  @SuppressWarnings("rawtypes")
  private static PropertiesComponent camelProperties(final ApplicationContext applicationContext) {
    ConfigurableEnvironment env = (ConfigurableEnvironment) applicationContext.getEnvironment();
    final MutablePropertySources propertySources = env.getPropertySources();
    List<String> names = new ArrayList<String>();
    for (PropertySource<?> propertySource : propertySources) {
      names.add(propertySource.getName());
    }

    PropertiesComponent properties = new PropertiesComponent(
        names.toArray(new String[names.size()]));
    properties.setPropertiesResolver(new PropertiesResolver() {
      @Override
      public Properties resolveProperties(final CamelContext context,
          final boolean ignoreMissingLocation,
          final String... names) throws Exception {
        Properties properties = new Properties();
        // Add in reverse order to preserve precedence.
        for (int i = names.length - 1; i >= 0; i--) {
          PropertySource<?> propertySource = propertySources.get(names[i]);
          if (propertySource instanceof EnumerablePropertySource) {
            String[] propertyNames = ((EnumerablePropertySource) propertySource).getPropertyNames();
            for (String propertyName : propertyNames) {
              properties.put(propertyName, propertySource.getProperty(propertyName));
            }
          }
        }
        return properties;
      }
    });
    properties.setPrefixToken("${");
    properties.setSuffixToken("}");
    return properties;
  }
}
