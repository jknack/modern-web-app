package org.knowhow.mwa;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.knowhow.mwa.Application.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

/**
 * <p>
 * A Servlet 3.0 Spring bootstrapper that offers the following functionality:
 * </p>
 * <ul>
 * <li>XML free configuration.
 * <li>Application context 'root' for Spring.
 * <li>Configure the {@link DispatcherServlet} with a MVC application context.
 * <li>Configure {@link Environment} using an application properties file.
 * <li>Configure the {@link PropertySourcesPlaceholderConfigurer} for
 * {@link Value @Value} usage.
 * <li>Organize your application in modules: normal-modules and web-modules.
 * <li>Module's package are scanned for detecting Spring beans (a.k.a component
 * scanning).
 * <li>Publish a 'debug' bean of type boolean. If the app is running in dev
 * mode: debug is true.
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
   * <li>Configure the {@link DispatcherServlet} with a MVC application context.
   * <li>Configure {@link Environment} using an application properties file.
   * <li>Configure the {@link PropertySourcesPlaceholderConfigurer} for
   * {@link Value @Value} usage.
   * <li>Organize your application in modules: normal-modules and web-modules.
   * <li>Module's package are scanned for detecting Spring beans.
   * <li>Publish a 'debug' bean of type boolean. If the app is running in dev
   * mode: debug is true.
   * </ul>
   */
  @Override
  public final void onStartup(final ServletContext servletContext)
      throws ServletException {
    SLF4JBridgeHandler.install();

    AnnotationConfigWebApplicationContext rootContext =
        new AnnotationConfigWebApplicationContext();
    servletContext.addListener(new ContextLoaderListener(rootContext));

    /**
     * Application's properties setup.
     * 1. It add the properties to the: environment.
     * 2. It enable @Value annotations.
     */
    ResourceLoader resourceLoader = new DefaultResourceLoader();
    Resource properties = resourceLoader.getResource(properties());
    // Add to the environment
    final ConfigurableEnvironment env = rootContext.getEnvironment();
    env.getPropertySources().addFirst(asPropertySource(properties));
    // Enable @Value
    PropertySourcesPlaceholderConfigurer propertyConfigurer =
        new PropertySourcesPlaceholderConfigurer();
    propertyConfigurer.setIgnoreResourceNotFound(true);
    propertyConfigurer.setLocation(properties);
    rootContext.addBeanFactoryPostProcessor(propertyConfigurer);

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
    /**
     * Scan beans under each module's package.
     */
    Set<Class<?>> modules = getModules();
    Set<Class<?>> webModules = Sets.newLinkedHashSet(
        Sets.filter(modules, new Predicate<Class<?>>() {
          @Override
          public boolean apply(final Class<?> module) {
            return module.getAnnotation(EnableWebMvc.class) != null
                || WebMvcConfigurationSupport.class.isAssignableFrom(module)
                || WebMvcConfigurer.class.isAssignableFrom(module);
          }
        }));
    modules.removeAll(webModules);
    registerModules(rootContext, "module", modules);

    /**
     * Register the application object.
     */
    rootContext.addBeanFactoryPostProcessor(new BeanFactoryPostProcessor() {
      @Override
      public void postProcessBeanFactory(
          final ConfigurableListableBeanFactory beanFactory)
          throws BeansException {
        logger.info("Starting application: {}", application);
        beanFactory.registerSingleton("__application_", application);
      }
    });

    /**
     * Creates the Spring MVC dispatcher.
     */
    AnnotationConfigWebApplicationContext mvcContext =
        new AnnotationConfigWebApplicationContext();
    ServletRegistration.Dynamic dispatcher = servletContext.addServlet(
        "spring-dispatcher", new DispatcherServlet(mvcContext));
    dispatcher.setLoadOnStartup(1);
    dispatcher.addMapping(dispatcherMapping());
    registerModules(mvcContext, "web-module", webModules);
    onStartup(env, servletContext, mvcContext);
  }

  private String defaultAppVersion(final String version) {
    String startupTime =
        new SimpleDateFormat(".yyyyMMdd.hhmmss").format(new Date());
    return Strings.isNullOrEmpty(version) ? startupTime : version + "."
        + startupTime;
  }

  /**
   * Add application's filters, listener and servlets.
   *
   * @param environment The application's environment.
   * @param servletContext The servlet's context.
   * @param rootContext The Spring MVC application context.
   */
  protected void onStartup(final Environment environment,
      final ServletContext servletContext,
      final AnnotationConfigWebApplicationContext rootContext) {
  }

  /**
   * The mapping for the Spring {@link DispatcherServlet dispatcher} servlet.
   * Default is: '/'.
   *
   * @return The mapping for the Spring {@link DispatcherServlet dispatcher}
   *         servlet. Default is: '/'.
   */
  protected String dispatcherMapping() {
    return "/";
  }

  /**
   * Add modules to the application context.
   */
  private void registerModules(
      final AnnotationConfigWebApplicationContext context,
      final String moduleType,
      final Set<Class<?>> modules) {
    List<String> packageToScan = new ArrayList<String>();
    for (Class<?> module : modules) {
      logger.debug("Registering {}: {}", moduleType, module.getSimpleName());
      packageToScan.add(module.getPackage().getName());
    }
    context.register(modules.toArray(new Class[modules.size()]));
    context.scan(packageToScan.toArray(new String[packageToScan.size()]));
  }

  /**
   * Check for valid module.
   *
   * @param module The module to check. Required.
   * @return The module.
   * @throw {@link IllegalArgumentException} If the module isn't valid.
   */
  private Class<?> checkModule(final Class<?> module) {
    if (isModule(module)) {
      return module;
    }
    throw new IllegalArgumentException("Module:" + module.getName()
        + " must have @" + Configuration.class.getName());
  }

  /**
   * Is a valid module? Reject null modules or not marked with
   * {@link Configuration}.
   */
  private boolean isModule(final Class<?> module) {
    checkNotNull(module, "The module's class is required.");
    return module.getAnnotation(Configuration.class) != null;
  }

  private ResourcePropertySource asPropertySource(final Resource resource)
      throws ServletException {
    try {
      return new ResourcePropertySource(resource);
    } catch(IOException ex) {
      throw new ServletException(ex);
    }
  }

  /**
   * Read application's modules.
   *
   * @return Read application's modules.
   */
  private Set<Class<?>> getModules() {
    Class<?>[] modules = modules();
    Set<Class<?>> result = new LinkedHashSet<Class<?>>();
    for (Class<?> module : modules) {
      result.add(checkModule(module));
    }
    return result;
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
