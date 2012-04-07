package org.knowhow.mwa;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.LogManager;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.knowhow.mwa.Application.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.google.common.base.Strings;

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
        new AnnotationConfigWebApplicationContext();
    servletContext.addListener(new ContextLoaderListener(rootContext));

    // Configure the environment
    final ConfigurableEnvironment env = configureEnvironment(rootContext);

    /**
     * Creates the application object.
     */
    String contextPath = servletContext.getContextPath();
    String name = env.getProperty("application.name");
    String mode = env.getProperty("application.mode");
    String version = env.getProperty("application.version");
    final Application application =
        new Application(contextPath,
            Strings.isNullOrEmpty(name) ? contextPath : name,
            defaultAppVersion(version),
            Strings.isNullOrEmpty(mode) ? Application.DEV : Mode.valueOf(mode));
    env.setActiveProfiles(application.mode().name());

    logger.debug("Starting application: {}", application);

    /**
     * Scan beans under each module's package.
     */
    Class<?>[] modules = modules();
    if (modules.length > 0) {
      registerModules(rootContext, modules);
    }

    /**
     * Register the application object.
     */
    rootContext.addBeanFactoryPostProcessor(new BeanFactoryPostProcessor() {
      @Override
      public void postProcessBeanFactory(
          final ConfigurableListableBeanFactory beanFactory) {
        beanFactory.registerSingleton("mwa.application", application);
      }
    });

    /**
     * Creates the Spring MVC dispatcher servlet.
     */
    ServletRegistration.Dynamic dispatcher = servletContext.addServlet(
        "spring-dispatcher", new DispatcherServlet(rootContext));
    dispatcher.setLoadOnStartup(1);
    dispatcher.addMapping(dispatcherMapping());
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
   * @param rootContext The Spring application context.
   * @return The application environment.
   * @throws ServletException If the properties files failst to load.
   */
  private ConfigurableEnvironment configureEnvironment(
      final ConfigurableWebApplicationContext rootContext)
      throws ServletException {
    try {
      ResourcePatternResolver resourceLoader =
          new PathMatchingResourcePatternResolver();
      Resource[] propertiesFiles = resourceLoader.getResources(properties());
      // Add to the environment
      final ConfigurableEnvironment env = rootContext.getEnvironment();
      for (Resource propertyFile : propertiesFiles) {
        logger.debug("Adding property file: {}", propertyFile);
        env.getPropertySources().addFirst(asPropertySource(propertyFile));
      }
      // Enable @Value
      PropertySourcesPlaceholderConfigurer propertyConfigurer =
          new PropertySourcesPlaceholderConfigurer();
      propertyConfigurer.setEnvironment(env);
      rootContext.addBeanFactoryPostProcessor(propertyConfigurer);
      return env;
    } catch (IOException ex) {
      throw new ServletException("The environment cannot be configured.", ex);
    }
  }

  /**
   * Build the application version number using the provided version and the
   * current date.
   *
   * @param version The version number. Optional.
   * @return A unique application version.
   */
  private String defaultAppVersion(final String version) {
    String startupTime =
        new SimpleDateFormat(".yyyyMMdd.hhmmss").format(new Date());
    return Strings.isNullOrEmpty(version) ? startupTime : version + "."
        + startupTime;
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
   * Default is: '/'.
   *
   * @return The mapping for the Spring {@link DispatcherServlet dispatcher}
   *         servlet. Default is: '/'.
   */
  protected String dispatcherMapping() {
    return "/*";
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
      Set<String> packageToScan = new LinkedHashSet<String>();
      Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
      classes.add(WebDefaults.class);
      for (Class<?> module : modules) {
        packageToScan.add(module.getPackage().getName());
        classes.add(module);
      }
      if (packageToScan.size() > 0) {
        /**
         * DO NOT scan sub-packages.
         */
        ClassPathScanner scanner =
            new ClassPathScanner(packageToScan.toArray(new String[packageToScan
                .size()])) {
              @Override
              protected TypeFilter[] typeFilters() {
                /**
                 * @Configuration must be manully added.
                 */
                TypeFilter filter = new TypeFilter() {
                  @Override
                  public boolean match(final MetadataReader metadataReader,
                      final MetadataReaderFactory metadataReaderFactory)
                      throws IOException {
                    AnnotationTypeFilter component =
                        new AnnotationTypeFilter(Component.class);
                    AnnotationTypeFilter configuration =
                        new AnnotationTypeFilter(Configuration.class);
                    return component.match(metadataReader,
                        metadataReaderFactory)
                        && !configuration.match(metadataReader,
                            metadataReaderFactory);
                  }
                };
                return new TypeFilter[] {filter};
              }
            };
        classes.addAll(scanner.getClasses());
      }
      context.register(classes.toArray(new Class[classes.size()]));
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
   * List the application's modules.
   *
   * @return All the application's modules.
   */
  protected abstract Class<?>[] modules();

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
}
