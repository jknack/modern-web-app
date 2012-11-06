package com.github.jknack.mwa;

import static org.apache.commons.lang3.StringUtils.join;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;
import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
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
 * <li>Configure a {@link AnnotationConfigWebApplicationContext}.
 * <li>Configure the {@link DispatcherServlet} with the root application context.
 * <li>Configure {@link Environment} using property sources.
 * <li>Configure the {@link PropertySourcesPlaceholderConfigurer} for {@link Value} and
 * {@link Named} usage.
 * <li>Organize your application in modules: (a.k.a Spring Configuration).
 * <li>Application's namespace is scanned for detecting Spring beans (a.k.a component scanning).
 * <li>Add {@link Mode} and {@link ModeAware} support.
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
   * <li>Configure a {@link AnnotationConfigWebApplicationContext}.
   * <li>Configure the {@link DispatcherServlet} with the root application context.
   * <li>Configure {@link Environment} using property sources.
   * <li>Configure the {@link PropertySourcesPlaceholderConfigurer} for {@link Value} and
   * {@link Named} usage.
   * <li>Organize your application in modules: (a.k.a Spring Configuration).
   * <li>Application's namespace is scanned for detecting Spring beans (a.k.a component scanning).
   * <li>Add {@link Mode} and {@link ModeAware} support.
   * </ul>
   *
   * @param servletContext The servlet context.
   * @throws ServletException If something goes wrong.
   */
  @Override
  public final void onStartup(final ServletContext servletContext) throws ServletException {
    final AnnotationConfigWebApplicationContext context =
        new AnnotationConfigWebApplicationContext();

    servletContext.addListener(new ContextLoaderListener(context));

    MutablePropertySources propertySources =
        propertySources(servletContext.getContextPath(), context);

    configure(propertySources);

    ApplicationContextConfigurer.configure(context, propertySources);

    /**
     * Configure modules.
     */
    registerModules(context);

    /**
     * Creates the Spring MVC dispatcher servlet.
     */
    ServletRegistration.Dynamic dispatcher = servletContext.addServlet(
        "spring-dispatcher", new DispatcherServlet(context));
    dispatcher.setLoadOnStartup(1);
    dispatcher.addMapping(dispatcherMapping());

    // Add the forwarding filter
    servletContext.addFilter("forwardingFilter", new ForwardingFilter(context))
        .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, dispatcherMapping());

    onStartup(servletContext, context);
  }

  /**
   * Optionally, add one or more property sources to the application's environment.
   *
   * @param propertySources The property sources.
   */
  protected void configure(final MutablePropertySources propertySources) {
  }

  /**
   * Get property sources.
   *
   * @param contextPath The application's context path.
   * @param context The application's context.
   * @return A property sources array.
   * @throws ServletException If the environment cannot be configured.
   */
  private MutablePropertySources propertySources(final String contextPath,
      final ApplicationContext context) throws ServletException {
    try {
      List<Resource> properties = findResources(propertySources());
      if (properties.size() == 0) {
        logger.warn("No property files were found.");
      }
      final Environment env = context.getEnvironment();

      // Special properties
      Map<String, Object> specialProps = new HashMap<String, Object>();
      String appName = env.getProperty("application.name");
      if (appName == null) {
        // No set, defaults to contextPath
        appName = contextPath.replace("/", "");
        specialProps.put("application.name", appName);
      }
      // contextPath
      specialProps.put("application.contextPath", contextPath);
      // Same as application.contextPath but shorter
      specialProps.put("contextPath", contextPath);

      // All the namespace available
      specialProps.put("application.ns", join(rootPackageNames(), ","));

      // default name-space
      specialProps.put("application.default.ns", getClass().getPackage()
          .getName());

      MutablePropertySources propertySources = new MutablePropertySources();
      propertySources.addFirst(new MapPropertySource(appName, specialProps));

      for (int i = properties.size() - 1; i >= 0; i++) {
        propertySources.addFirst(asPropertySource(properties.get(i)));
      }

      return propertySources;
    } catch (IOException ex) {
      throw new ServletException("The environment cannot be configured.",
          ex);
    }
  }

  /**
   * Add application's filters, listener and servlets.
   *
   * @param servletContext The servlet's context.
   * @param applicationContext The Spring MVC application context.
   */
  protected void onStartup(final ServletContext servletContext,
      final ConfigurableWebApplicationContext applicationContext) {
  }

  /**
   * The mapping for the Spring {@link DispatcherServlet dispatcher} servlet.
   * Default is: '/*'.
   *
   * @return The mapping for the Spring {@link DispatcherServlet dispatcher} servlet. Default is:
   *         '/*'.
   */
  protected String[] dispatcherMapping() {
    return new String[]{"/*" };
  }

  /**
   * Add modules to the application context.
   *
   * @param context The String application context.
   * @throws ServletException If something goes wrong.
   */
  private void registerModules(final AnnotationConfigWebApplicationContext context)
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
   * A list with all the packages that will be added to the classpath
   * scanning. By default it scan all the package of the main or bootstrapper
   * class.
   *
   * @return A list with all the packages that will be added to the classpath
   *         scanning. By default it scan all the package of the main or
   *         bootstrapper class.
   */
  protected String[] namespace() {
    return new String[]{getClass().getPackage().getName() };
  }

  /**
   * A list with all the packages that will be added to the classpath
   * scanning. By default it scan all the package of the main or bootstrapper
   * class.
   *
   * @return A list with all the packages that will be added to the classpath
   *         scanning. By default it scan all the package of the main or
   *         bootstrapper class.
   */
  private String[] rootPackageNames() {
    String[] roots = namespace();
    Set<String> names = Sets.newHashSet();
    for (String ns : roots) {
      names.add(ns);
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
   * Import external modules required by the application. This is the same
   * mark the class with {@link Configuration} and add {@link Import} expressions.
   *
   * @return All the imported modules.
   */
  protected Class<?>[] imports() {
    return new Class[]{};
  }

  /**
   * <p>
   * Provide the location of the application properties file, such as
   * {@code "classpath:/com/myco/foo.properties"} or {@code "file:/path/to/file.properties"}.
   * </p>
   * <p>
   * Default is: application.properties.
   * </p>
   *
   * @return Provide the location of the application properties file, such as
   *         {@code "classpath:/com/myco/foo.properties"} or {@code "file:/path/to/file.properties"}
   *         .
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
   * {@code "classpath:/com/myco/foo.properties"} or {@code "file:/path/to/file.properties"}.
   * </p>
   * <p>
   * Default is: application.properties.
   * </p>
   *
   * @return Provide the location of the application properties file, such as
   *         {@code "classpath:/com/myco/foo.properties"} or {@code "file:/path/to/file.properties"}
   *         .
   * @see ClassPathResource
   * @see FileSystemResource
   * @see UrlResource
   * @see Resource
   */
  protected String[] propertySources() {
    return new String[]{propertySource() };
  }

  /**
   * Find all the resources for the given location.
   *
   * @param locations The locations.
   * @return A resource set.
   * @throws IOException If a resource file fail to be loaded.
   */
  private List<Resource> findResources(final String[] locations)
      throws IOException {
    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    Set<Resource> resources = Sets.newLinkedHashSet();
    for (String location : locations) {
      Resource[] candidates = resolver.getResources(location);
      for (Resource resource : candidates) {
        if (resource.exists()) {
          resources.add(resource);
        } else {
          logger.warn("Could not find any property file for: {}", location);
        }
      }
    }
    return new ArrayList<Resource>(resources);
  }

}
