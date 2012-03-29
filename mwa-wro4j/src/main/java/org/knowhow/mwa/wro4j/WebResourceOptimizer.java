package org.knowhow.mwa.wro4j;

import static ro.isdc.wro.model.resource.processor.factory.ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS;
import static ro.isdc.wro.model.resource.processor.factory.ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Validate;
import org.knowhow.mwa.Application;
import org.springframework.core.env.Environment;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.ConfigConstants;
import ro.isdc.wro.extensions.manager.ExtensionsConfigurableWroManagerFactory;
import ro.isdc.wro.extensions.processor.css.LessCssProcessor;
import ro.isdc.wro.extensions.processor.js.JsHintProcessor;
import ro.isdc.wro.http.ConfigurableWroFilter;
import ro.isdc.wro.http.WroFilter;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.ModelTransformerFactory;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.factory.XmlModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;
import ro.isdc.wro.model.resource.locator.factory.SimpleUriLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.impl.PlaceholderProcessor;
import ro.isdc.wro.util.ObjectFactory;
import ro.isdc.wro.util.Transformer;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

/**
 * Build a {@link WroFilter} who takes sensible defaults for dev or production
 * like environments. It publish the 'contextPath' as a variable for JS/CSS
 * resources.
 *
 * @author edgar.espina
 * @since 0.1
 */
public class WebResourceOptimizer {

  /**
   * Create a group for each resource found.
   *
   * @author Edgar Espina&lt;edgar.espina@globant.com&gt;
   * @since 0.6
   */
  private static class DevModelTransformer implements Transformer<WroModel> {
    @Override
    public WroModel transform(final WroModel input) throws Exception {
      Collection<Group> groups = input.getGroups();
      Map<String, Set<Resource>> map = new HashMap<String, Set<Resource>>();
      for (Group group : groups) {
        List<Resource> resources = group.getResources();
        for (Resource resource : resources) {
          String groupName = FilenameUtils.getBaseName(resource.getUri());
          Set<Resource> newGroup = map.get(groupName);
          if (newGroup == null) {
            newGroup = new HashSet<Resource>();
            map.put(groupName, newGroup);
          }
          newGroup.add(resource);
        }
      }
      WroModel output = new WroModel();
      for (Entry<String, Set<Resource>> g : map.entrySet()) {
        Group newGroup = new Group(g.getKey());
        Set<Resource> resources = g.getValue();
        if (resources.size() > 2) {
          throw new IllegalStateException("Multiples resource " + resources
              + " for group: " + g.getKey());
        }
        for (Resource resource : resources) {
          newGroup.addResource(resource);
        }
        output.addGroup(newGroup);
      }
      return output;
    }
  }

  /**
   * The global Wro4j properties.
   */
  private final Properties configProperties = new Properties();

  /**
   * The Wro4j factory properties.
   */
  private final Properties factoryProperties = new Properties();

  /**
   * The model factory to use.
   */
  private WroModelFactory wroModelFactory;

  /**
   * Custom pre-processors.
   */
  private final Map<String, ResourcePreProcessor> preProcessors =
      new HashMap<String, ResourcePreProcessor>();

  /**
   * Custom post-processors.
   */
  private final Map<String, ResourcePostProcessor> postProcessors =
      new HashMap<String, ResourcePostProcessor>();

  /**
   * The placeholders.
   */
  private final Properties placeholders = new Properties();

  /**
   * Are we in dev?
   */
  private boolean dev;

  /**
   * This proccessors are off in dev.
   */
  private final Set<String> noDevProccessors = Sets.newHashSet("cssCompressor",
      "yuiCssMin", "yuiJsMin", "yuiJsMinAdvanced", "dojoShrinksafe",
      "uglifyJs", "googleClosureSimple", "googleClosureAdvanced");

  /**
   * Creates a new {@link WebResourceOptimizer}.
   *
   * @param environment The environment object. Required.
   */
  public WebResourceOptimizer(final Environment environment) {
    Validate.notNull(environment, "The environmnt is required.");
    dev =
        Application.DEV.matches(environment.getProperty("application.mode"));
    configProperties.setProperty("debug", String.valueOf(dev).toString());
    boolean gzipEnabled;
    boolean disableCache;
    long cacheUpdatePeriod = 0;
    long modelUpdatePeriod;
    if (dev) {
      gzipEnabled = Boolean.FALSE;
      disableCache = Boolean.TRUE;
      // Update wro.xml every second
      modelUpdatePeriod = 1;
      ModelTransformerFactory modelTransformerFactory =
          new ModelTransformerFactory(new XmlModelFactory());
      modelTransformerFactory.setTransformers(Arrays
          .asList(new DevModelTransformer()));
      this.wroModelFactory = modelTransformerFactory;
    } else {
      gzipEnabled = Boolean.TRUE;
      disableCache = Boolean.FALSE;
      modelUpdatePeriod = 0;
      this.wroModelFactory = new XmlModelFactory();
    }
    // Publish context path
    withPreProcessor("commonVars", new PlaceholderProcessor()
        .setPropertiesFactory(new ObjectFactory<Properties>() {
          @Override
          public Properties create() {
            Context context = Context.get();
            placeholders
                .put("contextPath", context.getRequest().getContextPath());
            return placeholders;
          }
        })
        .setIgnoreMissingVariables(false));
    configProperties.setProperty(ConfigConstants.debug.name(),
        String.valueOf(dev));
    configProperties.setProperty(ConfigConstants.gzipResources.name(),
        String.valueOf(gzipEnabled));
    configProperties.setProperty(ConfigConstants.cacheUpdatePeriod.name(),
        String.valueOf(cacheUpdatePeriod));
    configProperties.setProperty(ConfigConstants.modelUpdatePeriod.name(),
        String.valueOf(modelUpdatePeriod));
    configProperties.setProperty(ConfigConstants.disableCache.name(),
        String.valueOf(disableCache));
    configProperties.setProperty(ConfigConstants.encoding.name(),
        "UTF-8");
    configProperties.setProperty(ConfigConstants.jmxEnabled.name(), "false");
    configProperties.setProperty("ignoreMissingResources", "false");
  }

  /**
   * Creates a new {@link JsHintProcessor} with HTML error reporting.
   *
   * @param options The lint options. Required.
   * @param excludePaths The paths to exclude. Optional.
   * @return A new {@link JsHintProcessor} with HTML error reporting.
   */
  public JsHintProcessor newJsHint(final LintOptions options,
      final String... excludePaths) {
    return new ExtendedJsHintProcessor(options, excludePaths);
  }

  /**
   * Creates a new {@link LessCssProcessor} with HTML error reporting.
   *
   * @return A new {@link LessCssProcessor} with HTML error reporting.
   */
  public LessCssProcessor newLessCss() {
    return new LessCssProcessor() {
      @Override
      protected void onException(final WroRuntimeException ex) {
        // Handle by problem reporter.
        throw ex;
      }
    };
  }

  /**
   * Publish a placeholder and later usage, usually from css or js files.
   *
   * @param name The placeholder's name. Required.
   * @param value The placeholder's value. Required.
   * @return This builder.
   * @see <a href="http://code.google.com/p/wro4j/wiki/PlaceholderProcessor">
   *      PlaceholderProcessor</a>
   */
  public WebResourceOptimizer withPlaceholder(final String name,
      final String value) {
    Validate.notEmpty(name, "The placeholder's name is required.");
    Validate.notEmpty(value, "The placeholder's value is required.");
    placeholders.setProperty(name, value);
    return this;
  }

  /**
   * Publish a placeholder for later usage, usually from css or js files.
   *
   * @param name The placeholder's name. Required.
   * @param value The placeholder's value. Required.
   * @return This builder.
   * @see <a href="http://code.google.com/p/wro4j/wiki/PlaceholderProcessor">
   *      PlaceholderProcessor</a>
   */
  public WebResourceOptimizer withPlaceholder(final String name,
      final Boolean value) {
    Validate.notEmpty(name, "The placeholder's name is required.");
    Validate.notNull(value, "The placeholder's value is required.");
    placeholders.setProperty(name, value.toString());
    return this;
  }

  /**
   * Publish a placeholder for later usage, usually from css or js files.
   *
   * @param name The placeholder's name. Required.
   * @param value The placeholder's value. Required.
   * @return This builder.
   * @see <a href="http://code.google.com/p/wro4j/wiki/PlaceholderProcessor">
   *      PlaceholderProcessor</a>
   */
  public WebResourceOptimizer withPlaceholder(final String name,
      final Integer value) {
    Validate.notEmpty(name, "The placeholder's name is required.");
    Validate.notNull(value, "The placeholder's value is required.");
    placeholders.setProperty(name, value.toString());
    return this;
  }

  /**
   * Publish a placeholder for later usage, usually from css or js files.
   *
   * @param name The placeholder's name. Required.
   * @param value The placeholder's value. Required.
   * @return This builder.
   * @see <a href="http://code.google.com/p/wro4j/wiki/PlaceholderProcessor">
   *      PlaceholderProcessor</a>
   */
  public WebResourceOptimizer withPlaceholder(final String name,
      final Long value) {
    Validate.notEmpty(name, "The placeholder's name is required.");
    Validate.notNull(value, "The placeholder's value is required.");
    placeholders.setProperty(name, value.toString());
    return this;
  }

  /**
   * Publish a placeholder for later usage, usually from css or js files.
   *
   * @param name The placeholder's name. Required.
   * @param value The placeholder's value. Required.
   * @return This builder.
   * @see <a href="http://code.google.com/p/wro4j/wiki/PlaceholderProcessor">
   *      PlaceholderProcessor</a>
   */
  public WebResourceOptimizer withPlaceholder(final String name,
      final Float value) {
    Validate.notEmpty(name, "The placeholder's name is required.");
    Validate.notNull(value, "The placeholder's value is required.");
    placeholders.setProperty(name, value.toString());
    return this;
  }

  /**
   * Publish a placeholder for later usage, usually from css or js files.
   *
   * @param name The placeholder's name. Required.
   * @param value The placeholder's value. Required.
   * @return This builder.
   * @see <a href="http://code.google.com/p/wro4j/wiki/PlaceholderProcessor">
   *      PlaceholderProcessor</a>
   */
  public WebResourceOptimizer withPlaceholder(final String name,
      final Double value) {
    Validate.notEmpty(name, "The placeholder's name is required.");
    Validate.notNull(value, "The placeholder's value is required.");
    placeholders.setProperty(name, value.toString());
    return this;
  }

  /**
   * Append a built-in pre-processor to the list.
   *
   * @param name The pre-processor's name. Required.
   * @return This builder.
   * @see <a href="http://code.google.com/p/wro4j/wiki/AvailableProcessors">
   *      AvailableProcessors</a>
   */
  public WebResourceOptimizer withPreProcessor(final String name) {
    Validate.notEmpty(name, "The preprocessor's name is required.");
    append(PARAM_PRE_PROCESSORS, name);
    return this;
  }

  /**
   * Append a custom pre-processor to the list.
   *
   * @param name The pre-processor's name. Required.
   * @param processor The custom pre-processor. Required.
   * @return This builder.
   */
  public WebResourceOptimizer withPreProcessor(final String name,
      final ResourcePreProcessor processor) {
    Validate.notEmpty(name, "The preprocessor's name is required.");
    Validate.notNull(processor, "The preprocessor's is required.");
    preProcessors.put(name, processor);
    withPreProcessor(name);
    return this;
  }

  /**
   * Append a post-processor to the list.
   *
   * @param name The preprocessor's name. Required.
   * @return This builder.
   * @see <a href="http://code.google.com/p/wro4j/wiki/AvailableProcessors">
   *      AvailableProcessors</a>
   */
  public WebResourceOptimizer withPostProcessor(final String name) {
    Validate.notEmpty(name, "The preprocessor's name is required.");
    append(PARAM_POST_PROCESSORS, name);
    return this;
  }

  /**
   * Append a custom post-processor to the list.
   *
   * @param name The post-processor's name. Required.
   * @param processor The custom post-processor. Required.
   * @return This builder.
   */
  public WebResourceOptimizer withPostProcessor(final String name,
      final ResourcePostProcessor processor) {
    Validate.notEmpty(name, "The preprocessor's name is required.");
    Validate.notNull(processor, "The preprocessor's is required.");
    postProcessors.put(name, processor);
    withPostProcessor(name);
    return this;
  }

  /**
   * Append a value and separate multiples values by comma.
   *
   * @param name The property's name.
   * @param value The property's value.
   */
  private void append(final String name, final String value) {
    if (dev && noDevProccessors.contains(value)) {
      return;
    }
    Iterable<String> existingValues = Splitter.on(",")
        .omitEmptyStrings()
        .trimResults()
        .split(factoryProperties.getProperty(name, ""));
    Collection<String> newValues = new LinkedHashSet<String>();
    for (String existingValue : existingValues) {
      newValues.add(existingValue);
    }
    newValues.add(value);
    factoryProperties.put(name, Joiner.on(",").join(newValues));
  }

  /**
   * Build a new {@link WroFilter} ready for development or production
   * environments.
   *
   * @return A new {@link WroFilter} ready for development or production
   *         environments.
   */
  public WroFilter build() {
    // Let's create a factory.
    ExtensionsConfigurableWroManagerFactory factory =
        new ExtensionsConfigurableWroManagerFactory() {
          @Override
          protected UriLocatorFactory newUriLocatorFactory() {
            final SimpleUriLocatorFactory factory =
                new SimpleUriLocatorFactory();
            factory.addUriLocator(new UriLocator() {
              @Override
              public boolean accept(final String uri) {
                return uri.trim().startsWith("/");
              }

              @Override
              public InputStream locate(final String uri) throws IOException {
                final ServletContext servletContext =
                    Context.get().getServletContext();
                InputStream stream = servletContext.getResourceAsStream(uri);
                if (stream == null) {
                  throw new IOException(
                      "Exception while reading resource from " + uri);
                }
                return stream;
              }
            });
            factory.addUriLocator(new ClasspathUriLocator());
            factory.addUriLocator(new UrlUriLocator());
            return factory;
          }

          @Override
          protected Properties newConfigProperties() {
            return factoryProperties;
          }

          @Override
          protected void contributePostProcessors(
              final Map<String, ResourcePostProcessor> map) {
            super.contributePostProcessors(map);
            map.putAll(postProcessors);
          }

          @Override
          protected void contributePreProcessors(
              final Map<String, ResourcePreProcessor> map) {
            super.contributePreProcessors(map);
            map.putAll(preProcessors);
          }
        };
    factory.setModelFactory(wroModelFactory);
    // Now the filter.
    ConfigurableWroFilter filter = new ConfigurableWroFilter() {
      @Override
      protected void onRuntimeException(final RuntimeException e,
          final HttpServletResponse response, final FilterChain chain) {
        try {
          HttpServletRequest request = Context.get().getRequest();
          WroProblemReporter reporter = WroProblemReporter
              .bestFor(request.getRequestURI(), e);
          if (reporter != null) {
            reporter.report((WroRuntimeException) e, request, response);
          } else {
            // go to the default behavior.
            super.onRuntimeException(e, response, chain);
          }
        } catch (IOException ex) {
          // go to the default behavior.
          super.onRuntimeException(e, response, chain);
        }
      }
    };
    filter.setProperties(configProperties);
    filter.setWroManagerFactory(factory);
    return filter;
  }
}
