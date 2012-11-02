package com.github.jknack.mwa;

import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.core.annotation.AnnotationUtils.getValue;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;
import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
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
      valueAnnotationTypes = Sets.newHashSet(Value.class, Named.class);
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
   * @param servletContext The servlet context.
   * @throws ServletException If something goes wrong.
   */
  @Override
  public final void onStartup(final ServletContext servletContext)
      throws ServletException {

    final AnnotationConfigWebApplicationContext rootContext =
        new ModernWebAppContext();
    servletContext.addListener(new ContextLoaderListener(rootContext));

    // Configure the environment
    final ConfigurableEnvironment env =
        configureEnvironment(servletContext, rootContext);

    ApplicationModeFactory<? extends ApplicationMode> applicationModeFactory =
    		createApplicationModeFactory();
    
    String modeProperty = env.getProperty("application.mode");
    if (StringUtils.isBlank(modeProperty)) {
      modeProperty = applicationModeFactory.createForDev().getName();
      logger.warn("application.mode isn't set, using: {}", modeProperty);
    }
    ApplicationMode mode = applicationModeFactory.createFor(modeProperty);

    // Activate the default profile
    env.setActiveProfiles(mode.getName());

    /**
     * Configure modules.
     */
    registerModules(rootContext);

    /**
     * Special beans.
     */
    rootContext.addBeanFactoryPostProcessor(registerSingletons(mode,
        namespace()));

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
   * Publish application properties files into the environment. Additionally, it
   * enabled the use of {@link Value} annotation.
   *
   * @param servletContext The servlet context.
   * @param rootContext The Spring application context.
   * @return The application environment.
   * @throws ServletException If the properties files fail to load.
   */
  private ConfigurableEnvironment configureEnvironment(
      final ServletContext servletContext,
      final ConfigurableWebApplicationContext rootContext)
      throws ServletException {
    try {
      Set<Resource> properties = findResources(propertySources());
      if (properties.size() == 0) {
        logger.warn("No property files were found.");
      }
      // Add to the environment
      final ConfigurableEnvironment env = rootContext.getEnvironment();
      Map<String, Object> webproperties = new HashMap<String, Object>();
      webproperties.put("contextPath", servletContext.getContextPath());
      webproperties.put("servletContextName",
          servletContext.getServletContextName());
      MutablePropertySources propertySources = env.getPropertySources();
      propertySources.addFirst(new MapPropertySource(servletContext
          .getContextPath(), webproperties));
      for (Resource propertyFile : properties) {
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
   * The {@link ApplicationModeFactory} that is responsible for converting the
   * application mode from a property to an {@link ApplicationMode}
   * 
   * @return a fully initialized {@link ApplicationModeFactory}.
   *         Default is a {@link DefaultApplicationModeFactory} using {@link DefaultApplicationMode}
   *         as its mode. 
   */
  protected ApplicationModeFactory<? extends ApplicationMode> createApplicationModeFactory() {
	  return new EnumApplicationModeFactory<DefaultApplicationMode>(DefaultApplicationMode.class);
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
    return new String[] {"/*"};
  }

  /**
   * Add modules to the application context.
   *
   * @param context The String application context.
   * @throws ServletException If something goes wrong.
   */
  private void registerModules(
      final AnnotationConfigWebApplicationContext context)
      throws ServletException {
    try {
      Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
      Class<?>[] modules = imports();
      if (modules != null && modules.length > 0) {
        for (Class<?> module : modules) {
          if (isConfiguration(module)) {
            classes.add(module);
          } else {
            throw new ServletException(
                "Class must be marked with @Configuration: "
                    + module.getName());
          }
        }
      }
      classes.add(WebDefaults.class);
      context.register(classes.toArray(new Class[classes.size()]));
      // Scan all the packages of the main class recursively.
      context.scan(rootPackageNames());
    } catch (Exception ex) {
      throw new ServletException("Cannot register modules.", ex);
    }
  }

  /**
   * Return true if the candidate class is marked as {@link Configuration}.
   *
   * @param candidate The candidate class.
   * @return True if the candidate class is marked as {@link Configuration}.
   */
  private boolean isConfiguration(final Class<?> candidate) {
    return findAnnotation(candidate, Configuration.class) != null;
  }

  /**
   * A list with all the packages that will be added to the classpath scanning.
   * By default it scan all the package of the main or bootstrapper class.
   *
   * @return A list with all the packages that will be added to the classpath
   *         scanning. By default it scan all the package of the main or
   *         bootstrapper class.
   */
  protected Package[] namespace() {
    return new Package[] {getClass().getPackage()};
  }

  /**
   * A list with all the packages that will be added to the classpath scanning.
   * By default it scan all the package of the main or bootstrapper class.
   *
   * @return A list with all the packages that will be added to the classpath
   *         scanning. By default it scan all the package of the main or
   *         bootstrapper class.
   */
  private String[] rootPackageNames() {
    Package[] roots = namespace();
    Set<String> names = Sets.newHashSet();
    for (Package root : roots) {
      names.add(root.getName());
    }
    return names.toArray(new String[names.size()]);
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
   * Import external modules required by the application. This is the same mark
   * the class with {@link Configuration} and add {@link Import} expressions.
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
  protected String propertySource() {
    return "application.properties";
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
  protected String[] propertySources() {
    return new String[] {propertySource()};
  }

  /**
   * Find all the resources for the given location.
   *
   * @param locations The locations.
   * @return A resource set.
   * @throws IOException If a resource file fail to be loaded.
   */
  private Set<Resource> findResources(final String[] locations)
      throws IOException {
    ResourcePatternResolver resolver =
        new PathMatchingResourcePatternResolver();
    Set<Resource> resources = Sets.newLinkedHashSet();
    for (String location : locations) {
      Resource[] candidates = resolver.getResources(location);
      for (Resource resource : candidates) {
        if (resource.exists()) {
          resources.add(resource);
        }
      }
    }
    return resources;
  }

  /**
   * Register the application mode in the spring context.
   *
   * @param mode The application's mode.
   * @param roots The roots packages.
   * @return A new {@link BeanFactoryPostProcessor}.
   */
  private static BeanFactoryPostProcessor registerSingletons(
      final ApplicationMode mode, final Package[] roots) {
    return new BeanFactoryPostProcessor() {
      @Override
      public void postProcessBeanFactory(
          final ConfigurableListableBeanFactory beanFactory) {
        beanFactory.addBeanPostProcessor(modeAwareBeanPostProcessor(mode));
        beanFactory.registerSingleton("#mode", mode);
        // register roots
        for (int i = 0; i < roots.length; i++) {
          beanFactory.registerSingleton("#root$" + i, roots[i]);
        }
      }
    };
  }

  /**
   * Configure {@link ApplicationModeAware} beans.
   *
   * @param mode The application's mode.
   * @return A bean mode aware processor.
   */
  private static BeanPostProcessor modeAwareBeanPostProcessor(final ApplicationMode mode) {
    return new BeanPostProcessor() {
      @Override
      public Object postProcessBeforeInitialization(final Object bean,
          final String beanName) {
        if (bean instanceof ApplicationModeAware) {
          ((ApplicationModeAware) bean).setMode(mode);
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
}
