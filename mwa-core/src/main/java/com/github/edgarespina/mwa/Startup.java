package com.github.edgarespina.mwa;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.LogManager;

import javax.inject.Named;
import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.util.StringValueResolver;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.google.common.collect.Sets;

/**
 * <p>
 * A Servlet 3.0 Spring bootstrapper that offers the following functionality:
 * </p>
 * <ul>
 * <li>XML free configuration.
 * <li>Application context 'root' for Spring.
 * <li>Configure the {@link DispatcherServlet} with the root application
 * context.
 * <li>Configure {@link Environment} using an application properties files.
 * <li>Configure the {@link PropertySourcesPlaceholderConfigurer} for
 * {@link Value} usage.
 * <li>Organize your application in modules: (a.k.a Spring Configuration).
 * <li>Module's package are scanned for detecting Spring beans (a.k.a component
 * scanning).
 * <li>Publish an {@link Application} object with: the application's name,
 * contextPath, version and mode.
 * </ul>
 *
 * @author edgar.espina
 * @since 0.1
 * @see WebApplicationInitializer
 */
public abstract class Startup implements WebApplicationInitializer {

  /**
   * Configure @Named for resolving properties from the environment.
   *
   * @author edgar.espina
   * @since 0.1
   */
  private static class ExtendedAutowireCandidateResolver extends
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
     * Creates a new {@link ExtendedAutowireCandidateResolver}.
     *
     * @param environment The application environment.
     * @param beanFactory The application bean factory.
     */
    @SuppressWarnings("unchecked")
    public ExtendedAutowireCandidateResolver(final Environment environment,
        final DefaultListableBeanFactory beanFactory) {
      this.valueAnnotationTypes = Sets.newHashSet(Value.class, Named.class);
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
          Object value = AnnotationUtils.getValue(annotation);
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
          String value = (String) AnnotationUtils.getValue(annotation);
          if (valueType == Named.class) {
            return environment.getProperty(value) != null;
          } else {
            // force to use ${} in @Value
            return value.startsWith("${") && value.endsWith("}");
          }
        }
      }
      return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolveStringValue(final String value) {
      return environment.getProperty(value, value);
    }
  }

  /**
   * Extend {@link AnnotationConfigWebApplicationContext} with more features.
   *
   * @author edgar.espina
   * @see ExtendedAutowireCandidateResolver
   */
  private static class ModernWebAppContext extends
      AnnotationConfigWebApplicationContext {
    /**
     * {@inheritDoc}
     */
    @Override
    protected void customizeBeanFactory(
        final DefaultListableBeanFactory beanFactory) {
      super.customizeBeanFactory(beanFactory);
      // Override the autowire candidate resolver
      new ExtendedAutowireCandidateResolver(getEnvironment(), beanFactory);
    }
  }

  /**
   * The logging system.
   */
  private final Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * <p>
   * A Servlet 3.0 Spring bootstrapper that offers the following functionality:
   * </p>
   * <ul>
   * <li>XML free configuration.
   * <li>Application context 'root' for Spring.
   * <li>Configure the {@link DispatcherServlet} with the root application
   * context.
   * <li>Configure {@link Environment} using an application properties files.
   * <li>Configure the {@link PropertySourcesPlaceholderConfigurer} for
   * {@link Value} usage.
   * <li>Organize your application in modules: (a.k.a Spring Configuration).
   * <li>Module's package are scanned for detecting Spring beans (a.k.a
   * component scanning).
   * <li>Publish an {@link Application} object with: the application's name,
   * contextPath, version and mode.
   * </ul>
   *
   * @param servletContext The servelt context.
   * @throws ServletException If something goes wrong.
   */
  @Override
  public final void onStartup(final ServletContext servletContext)
      throws ServletException {
    // redirect java util logging calls.
    configureJuli();

    final AnnotationConfigWebApplicationContext rootContext =
        new ModernWebAppContext();
    servletContext.addListener(new ContextLoaderListener(rootContext));

    // Configure the environment
    final ConfigurableEnvironment env =
        configureEnvironment(servletContext, rootContext);

    servletContext.getContextPath();
    Mode mode = Mode.valueOf(env.getRequiredProperty("application.mode"));
    // Activate the default profile
    env.setActiveProfiles(mode.name());

    /**
     * Scan beans under each module's package.
     */
    Class<?>[] modules = imports();
    registerModules(rootContext, modules);

    /**
     * Special beans.
     */
    rootContext.addBeanFactoryPostProcessor(registerMode(mode));

    /**
     * Creates the Spring MVC dispatcher servlet.
     */
    ServletRegistration.Dynamic dispatcher = servletContext.addServlet(
        "spring-dispatcher", new DispatcherServlet(rootContext));
    dispatcher.setLoadOnStartup(1);
    dispatcher.addMapping(dispatcherMapping());
    // Add the forwarding filter
    servletContext.addFilter("forwardingFilter", new ForwardingFilter(
        rootContext))
        .addMappingForUrlPatterns(
            EnumSet.of(DispatcherType.REQUEST), false, dispatcherMapping());
    onStartup(servletContext, rootContext);
  }

  /**
   * Turn off Juli and redirect Juli to SJF4J.
   */
  private void configureJuli() {
    java.util.logging.Logger rootLogger =
        LogManager.getLogManager().getLogger("");
    Handler[] handlers = rootLogger.getHandlers();
    for (Handler handler : handlers) {
      rootLogger.removeHandler(handler);
    }
    SLF4JBridgeHandler.install();
  }

  /**
   * Publish application properties files into the environment. Additionally, it
   * enabled the use of {@link Value} annotation.
   *
   * @param servletContext The servlet context.
   * @param rootContext The Spring application context.
   * @return The application environment.
   * @throws ServletException If the properties files failst to load.
   */
  private ConfigurableEnvironment configureEnvironment(
      final ServletContext servletContext,
      final ConfigurableWebApplicationContext rootContext)
      throws ServletException {
    try {
      ResourcePatternResolver resourceLoader =
          new PathMatchingResourcePatternResolver();
      Resource[] propertiesFiles = resourceLoader.getResources(properties());
      // Add to the environment
      final ConfigurableEnvironment env = rootContext.getEnvironment();
      Map<String, Object> webproperties = new HashMap<String, Object>();
      webproperties.put("contextPath", servletContext.getContextPath());
      webproperties.put("servletContextName",
          servletContext.getServletContextName());
      MutablePropertySources propertySources = env.getPropertySources();
      propertySources.addFirst(new MapPropertySource(servletContext
          .getContextPath(), webproperties));
      for (Resource propertyFile : propertiesFiles) {
        logger.debug("Adding property file: {}", propertyFile);
        propertySources.addFirst(asPropertySource(propertyFile));
      }
      // Enable @Value
      PropertySourcesPlaceholderConfigurer placeholderConfigurer =
          new PropertySourcesPlaceholderConfigurer();
      placeholderConfigurer.setEnvironment(env);
      rootContext.addBeanFactoryPostProcessor(placeholderConfigurer);
      return env;
    } catch (IOException ex) {
      throw new ServletException("The environment cannot be configured.", ex);
    }
  }

  /**
   * Add application's filters, listener and servlets.
   *
   * @param servletContext The servlet's context.
   * @param rootContext The Spring MVC application context.
   */
  protected void onStartup(final ServletContext servletContext,
      final ConfigurableWebApplicationContext rootContext) {
  }

  /**
   * The mapping for the Spring {@link DispatcherServlet dispatcher} servlet.
   * Default is: '/*'.
   *
   * @return The mapping for the Spring {@link DispatcherServlet dispatcher}
   *         servlet. Default is: '/*'.
   */
  protected String[] dispatcherMapping() {
    return new String[] {"/*" };
  }

  /**
   * Add modules to the application context.
   *
   * @param context The String application context.
   * @param modules The list of modules.
   * @throws ServletException If something goes wrong.
   */
  private void registerModules(
      final AnnotationConfigWebApplicationContext context,
      final Class<?>[] modules) throws ServletException {
    try {
      Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
      if (modules != null && modules.length > 0) {
        for (Class<?> module : modules) {
          classes.add(module);
        }
      }
      classes.add(WebDefaults.class);
      context.register(classes.toArray(new Class[classes.size()]));
      // Scan all the packages of the main class recursively.
      context.scan(getClass().getPackage().getName());
    } catch (Exception ex) {
      throw new ServletException("Cannot register modules.", ex);
    }
  }

  /**
   * Get a {@link PropertySource} from the resource.
   *
   * @param resource The resource.
   * @return A {@link PropertySource}.
   * @throws ServletException If the disk fails.
   */
  private ResourcePropertySource asPropertySource(final Resource resource)
      throws ServletException {
    try {
      return new ResourcePropertySource(resource);
    } catch (IOException ex) {
      throw new ServletException(ex);
    }
  }

  /**
   * Import external modules required by the application.
   *
   * @return All the imported modules.
   */
  protected Class<?>[] imports() {
    return new Class[] {};
  }

  /**
   * <p>
   * Provide the location of the application properties file, such as
   * {@code "classpath:/com/myco/foo.properties"} or
   * {@code "file:/path/to/file.properties"}.
   * </p>
   * <p>
   * Default is: application.properties.
   * </p>
   *
   * @return Provide the location of the application properties file, such as
   *         {@code "classpath:/com/myco/foo.properties"} or
   *         {@code "file:/path/to/file.properties"}.
   * @see ClassPathResource
   * @see FileSystemResource
   * @see UrlResource
   * @see Resource
   */
  protected String properties() {
    return "application.properties";
  }

  /**
   * Register the application mode in the spring context.
   *
   * @param mode The application's mode.
   * @return A new {@link BeanFactoryPostProcessor}.
   */
  private static BeanFactoryPostProcessor registerMode(final Mode mode) {
    return new BeanFactoryPostProcessor() {
      @Override
      public void postProcessBeanFactory(
          final ConfigurableListableBeanFactory beanFactory) {
        beanFactory.registerSingleton("#mode", mode);
      }
    };
  }
}
