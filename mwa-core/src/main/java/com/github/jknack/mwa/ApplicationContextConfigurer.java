package com.github.jknack.mwa;

import static com.github.jknack.mwa.ApplicationConstants.APP_MODE;
import static org.apache.commons.lang3.Validate.notNull;
import static org.springframework.core.GenericTypeResolver.resolveTypeArgument;
import static org.springframework.core.annotation.AnnotationUtils.getValue;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.OrderComparator;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringValueResolver;

/**
 * Configure a Spring Application Context with:
 * <ul>
 * <li>Extra property sources</li>
 * <li>Add a {@link Mode} bean and {@link ModeAware} support. Default mode is: 'dev'.</li>
 * <li>Enable a Spring profile that matches the configured mode.</li>
 * <li>Enable Named/Value annotation for injection environment properties</li>
 * </ul>
 *
 * @author edgar.espina
 * @since 0.3.3
 */
public final class ApplicationContextConfigurer {

  /**
   * Configure @Named for resolving properties from the environment.
   *
   * @author edgar.espina
   * @since 0.1
   */
  private static class EnvironmentPropertyResolver extends
      QualifierAnnotationAutowireCandidateResolver implements
      StringValueResolver {

    /**
     * The list of annotation type to resolve.
     */
    private final Set<Class<? extends Annotation>> valueAnnotationTypes;

    /**
     * The application environment.
     */
    private Environment environment;

    /**
     * Creates a new {@link EnvironmentPropertyResolver}.
     *
     * @param environment The application environment.
     * @param beanFactory The application bean factory.
     */
    public EnvironmentPropertyResolver(final Environment environment,
        final DefaultListableBeanFactory beanFactory) {
      valueAnnotationTypes = new HashSet<Class<? extends Annotation>>();
      valueAnnotationTypes.add(Value.class);
      valueAnnotationTypes.add(Named.class);
      this.environment = environment;
      beanFactory.setAutowireCandidateResolver(this);
      beanFactory.addEmbeddedValueResolver(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object findValue(final Annotation[] annotationsToSearch) {
      for (Annotation annotation : annotationsToSearch) {
        if (isInstance(annotation)) {
          Object value = getValue(annotation);
          if (value == null) {
            throw new IllegalStateException(
                "Value/Named annotation must have a value attribute");
          }
          return value;
        }
      }
      return null;
    }

    /**
     * Returns true if the given annotation is one of Value or Named.
     *
     * @param annotation The annotation instance.
     * @return True if the given annotation is one of Value or Named.
     */
    private boolean isInstance(final Annotation annotation) {
      for (Class<? extends Annotation> valueType : valueAnnotationTypes) {
        if (valueType.isInstance(annotation)) {
          String value = (String) getValue(annotation);
          if (valueType == Named.class) {
            return environment.getProperty(value) != null;
          }
          // force to use ${} in @Value
          return value.startsWith("${") && value.endsWith("}");
        }
      }
      return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolveStringValue(final String value) {
      try {
        return environment.getProperty(value, value);
      } catch (IllegalArgumentException ex) {
        // required Spring 3.2.x
        return value;
      }
    }
  }

  /**
   * Looks for all the {@link ComponentConfigurer} and call them all.
   *
   * @author edgar.espina
   *
   */
  private static class ConfigureComponents implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    @SuppressWarnings({"rawtypes", "unchecked" })
    public void onApplicationEvent(final ContextRefreshedEvent event) {
      ApplicationContext context = event.getApplicationContext();
      Collection<ComponentConfigurer> configurers = context.getBeansOfType(
          ComponentConfigurer.class).values();
      Map<Class, List<ComponentConfigurer>> orderedMap =
          new HashMap<Class, List<ComponentConfigurer>>();
      // Dump all the configurer in the orderedMap
      for (ComponentConfigurer configurer : configurers) {
        Class componentType = resolveTypeArgument(configurer.getClass(), ComponentConfigurer.class);
        notNull(componentType, "Missing component's type for: %s", configurer);
        List<ComponentConfigurer> configurerList = orderedMap.get(componentType);
        if (configurerList == null) {
          configurerList = new ArrayList<ComponentConfigurer>();
          orderedMap.put(componentType, configurerList);
        }
        configurerList.add(configurer);
      }
      // Call each configurer by precedence
      for (Entry<Class, List<ComponentConfigurer>> entry : orderedMap.entrySet()) {
        Class componentType = entry.getKey();
        List<ComponentConfigurer> configurerList = entry.getValue();
        OrderComparator.sort(configurerList);
        for (ComponentConfigurer configurer : configurerList) {
          notNull(componentType, "Missing component type for: " + configurer);
          Iterable beans = context.getBeansOfType(componentType).values();
          for (Object bean : beans) {
            try {
              configurer.configure(bean);
            } catch (Exception ex) {
              throw new BeanInitializationException("Cannot configurer bean: " + componentType, ex);
            }
          }
        }
      }
    }
  }

  /**
   * The logging system.
   */
  private static final Logger logger = LoggerFactory.getLogger(ApplicationContextConfigurer.class);

  /**
   * Not allowed.
   */
  private ApplicationContextConfigurer() {
  }

  /**
   * Configure an application's context with:
   * <ul>
   * <li>Extra property sources</li>
   * <li>Add a {@link Mode} bean and {@link ModeAware} support. Default mode is: 'dev'.</li>
   * <li>Enable a Spring profile that matches the configured mode.</li>
   * <li>Enable Named/Value annotation for injection environment properties</li>
   * </ul>
   *
   * @param context The application's context. Required.
   * @param propertySources The property sources. Required.
   * @return The given application's context.
   */
  public static ConfigurableApplicationContext configure(
      final ConfigurableApplicationContext context, final MutablePropertySources propertySources) {
    ConfigurableEnvironment env = configureEnvironment(context, propertySources);

    String modeProperty = env.getProperty(APP_MODE);
    if (StringUtils.isBlank(modeProperty)) {
      modeProperty = Mode.DEV.name();
      logger.warn("{} isn't set, using: {}", APP_MODE, modeProperty);
    } else {
      logger.info("{} is: {}", APP_MODE, modeProperty);
    }
    Mode mode = Mode.valueOf(modeProperty);

    // Activate the default profile
    env.setActiveProfiles(mode.name());

    complement(context, mode);

    return context;
  }

  /**
   * <ul>
   * <li>Add {@link ModeAware} support.</li>
   * <li>Publish 'mode' in the given environment.</li>
   * <li>Configure Named annotation for injecting environment's properties.
   * </ul>
   *
   * @param context The application's context.
   * @param mode The application's mode.
   */
  private static void complement(final ConfigurableApplicationContext context, final Mode mode) {
    context.addApplicationListener(new ConfigureComponents());
    context.addBeanFactoryPostProcessor(new BeanFactoryPostProcessor() {
      @Override
      public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) {
        beanFactory.addBeanPostProcessor(modeAwareBeanPostProcessor(mode));
        beanFactory.registerSingleton(APP_MODE, mode);
        // Enable @Named and @Value
        new EnvironmentPropertyResolver(context.getEnvironment(),
            (DefaultListableBeanFactory) beanFactory);
      }
    });
  }

  /**
   * Configure {@link ModeAware} beans.
   *
   * @param mode The application's mode.
   * @return A bean mode aware processor.
   */
  private static BeanPostProcessor modeAwareBeanPostProcessor(final Mode mode) {
    return new BeanPostProcessor() {
      @Override
      public Object postProcessBeforeInitialization(final Object bean,
          final String beanName) {
        if (bean instanceof ModeAware) {
          ((ModeAware) bean).setMode(mode);
        }
        return bean;
      }

      @Override
      public Object postProcessAfterInitialization(final Object bean,
          final String beanName) {
        return bean;
      }
    };
  }

  /**
   * Publish application properties files into the environment. Property sources will be added by
   * precedence. For example: the element at '0' will have the highest precedence.
   *
   * @param context The Spring application context. Required.
   * @param propertySources The property's source. Required.
   * @return The application environment.
   */
  public static ConfigurableEnvironment configureEnvironment(
      final ConfigurableApplicationContext context, final MutablePropertySources propertySources) {
    notNull(context, "The context is required.");
    notNull(propertySources, "The propertySources are required.");

    final ConfigurableEnvironment env = context.getEnvironment();
    if (propertySources.size() == 0) {
      logger.warn("No property files were found.");
    }
    // Add property's by precedence.
    MutablePropertySources mutablePropertySources = env.getPropertySources();
    for (PropertySource<?> propertySource : propertySources) {
      logger.debug("Adding property file: {}", propertySource);
      mutablePropertySources.addLast(propertySource);
    }
    // Move some less-used property source to the end of the chain.
    String[] moveToEnd = {"servletConfigInitParams", "servletContextInitParams", "jndiProperties" };
    for (String propertySourceName : moveToEnd) {
      PropertySource<?> propertySource = mutablePropertySources.remove(propertySourceName);
      if (propertySource != null) {
        mutablePropertySources.addLast(propertySource);
      }
    }
    // Enable @Value
    PropertySourcesPlaceholderConfigurer placeholderConfigurer =
        new PropertySourcesPlaceholderConfigurer();
    placeholderConfigurer.setEnvironment(env);
    context.addBeanFactoryPostProcessor(placeholderConfigurer);
    return env;
  }

}
