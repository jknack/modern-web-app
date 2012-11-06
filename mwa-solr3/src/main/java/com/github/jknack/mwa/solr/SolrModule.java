package com.github.jknack.mwa.solr;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.CoreContainer.Initializer;
import org.apache.solr.core.SolrCore;
import org.apache.solr.servlet.SolrDispatchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;
import org.xml.sax.SAXException;

import com.github.jknack.mwa.FilterMapping;

/**
 * Configure one or more {@link SolrServer} base on the property:
 * <pre>
 * solr.home=
 * </pre>
 * Per each Solr 'core' a {@link SolrServer} is created and shared in the
 * Spring Application Context.
 * The bean's name matches the name of the 'core', so at any time you can inject
 * a {@link SolrServer} by doing:
 * <pre>
 * public MyService(@Named("core") SolrServer) {
 * ...
 * }
 * </pre>
 * Of course if there is just one core you don't need to add the Named
 * annotation.
 *
 * @author edgar.espina
 */
@Configuration
public class SolrModule {

  /**
   * The logging system.
   */
  private static final Logger logger =
      LoggerFactory.getLogger(SolrModule.class);

  /**
   * The solr.home property.
   */
  private static final String SOLR_HOME = "solr.home";

  /**
   * The solr.data property. Default is: ${java.io.tmpdir}/{application.name}
   */
  private static final String SOLR_DATA = "solr.data";

  /**
   * The solr.uri property. Default is: /search.
   */
  private static final String SOLR_URI = "solr.uri";

  /**
   * Creates a {@link CoreContainer} object which has all the Solr cores.
   *
   * @param context The application's context. Required.
   * @return A {@link CoreContainer} object which has all the Solr cores.
   * @throws IOException If the Solr home cannot be created.
   * @throws ParserConfigurationException If the any of the Solr XML files are
   *         corrupted.
   * @throws SAXException If the any of the Solr XML files are corrupted.
   */
  @Bean
  public CoreContainer solrCores(final ApplicationContext context)
      throws IOException, ParserConfigurationException, SAXException {
    notNull(context, "The application's context is required.");
    Environment env = context.getEnvironment();

    final File solrHome = findSolrHome(context);
    logger.debug("Setting {}: {}", SOLR_HOME, solrHome);
    System.setProperty("solr." + SOLR_HOME, solrHome.getAbsolutePath());

    File dataDir = findSolrDataDir(env);
    logger.debug("Setting {}: {}", SOLR_DATA, dataDir);
    System.setProperty(SOLR_DATA, dataDir.getAbsolutePath() + File.separator);

    CoreContainer.Initializer initializer =
        new CoreContainer.Initializer();
    CoreContainer cores = initializer.initialize();
    logger.info("Solr configuration");
    logger.info("  home dir: {}", solrHome);
    logger.info("  data dir: {}", dataDir);
    logger.info("  cores: {}", coreNames(cores));
    return cores;
  }

  /**
   * Add as many {@link SolrServer} as cores exists.
   *
   * @param cores The Solr cores.
   * @return A {@link BeanFactoryPostProcessor} that will creates as many
   *         {@link SolrServer} as cores exists.
   */
  @Bean
  public static BeanFactoryPostProcessor solrServerFactory(
      final CoreContainer cores) {
    return new BeanFactoryPostProcessor() {
      @Override
      public void postProcessBeanFactory(
          final ConfigurableListableBeanFactory beanFactory) {
        for (String coreName : coreNames(cores)) {
          coreName =
              StringUtils.isEmpty(coreName) ? cores.getDefaultCoreName()
                  : coreName;
          logger.debug("Creating Solr server for: {}", coreName);
          SolrServer server = new EmbeddedSolrServer(cores, coreName);
          beanFactory.registerSingleton(coreName, server);
        }
      }
    };
  }

  /**
   * Add a Solr dispatcher filter under {@link #SOLR_URI} uri mapping. Default
   * is: '/search'.
   *
   * @param env The application environment. Required.
   * @param solrCores The Solr cores. Required.
   * @return A Solr dispatcher filter.
   */
  @Bean
  public FilterMapping solrDispatcherFilter(final Environment env,
      final CoreContainer solrCores) {
    notNull(env, "The environment is required.");
    notNull(solrCores, "The Solr's cores are required.");

    String uri = env.getProperty(SOLR_URI, "/search");
    if (!uri.startsWith("/")) {
      uri = "/" + uri;
    }
    if (uri.endsWith("/")) {
      uri = uri.substring(0, uri.length() - 1);
    }
    final String pattern = uri + "/**/*";
    return FilterMapping.filter(pattern)
        .param("path-prefix", uri)
        .through(new SolrDispatchFilter() {
          @Override
          protected Initializer createInitializer() {
            logger.info("Solr is listening at: {}", pattern);
            return new Initializer() {
              @Override
              public CoreContainer initialize() throws IOException,
                  ParserConfigurationException, SAXException {
                // Don't initialize cores twice just use the Spring one.
                return solrCores;
              }
            };
          }
        });
  }

  /**
   * Find the solr's data from the given environment. Default is:
   * ${java.io.tmpdir}/{application.name}.
   *
   * @param env The application's environment.
   * @return The solr's data directory.
   */
  private static File findSolrDataDir(final Environment env) {
    String app = env.getProperty("application.name", "solr-data");
    File defaultDataDir = new File(env.getProperty("java.io.tmpdir"), app);

    File dataDir = new File(env.getProperty(SOLR_DATA,
        defaultDataDir.getAbsolutePath()));
    if (!dataDir.exists()) {
      dataDir.mkdirs();
    }
    return dataDir;
  }

  /**
   * Find the solr home from the given environment.
   *
   * @param context The application's environment.
   * @return The solr's home directory.
   * @throws IOException If the solr.home cannot be resolve.
   */
  private static File findSolrHome(final ApplicationContext context)
      throws IOException {
    Environment env = context.getEnvironment();
    String solrHome = env.getRequiredProperty(SOLR_HOME);
    File solrHomeDir = new File(solrHome);
    if (!solrHomeDir.exists()) {
      // Ask Spring for it
      Resource resource = context.getResource(solrHome);
      if (!resource.exists()) {
        resource =
            context.getResource(ResourceUtils.CLASSPATH_URL_PREFIX + solrHome);
      }
      if (resource.exists()) {
        solrHomeDir = resource.getFile();
      }
    }

    isTrue(solrHomeDir.exists(), "Cannot to set: '%s', '%s' doesn't exist",
        SOLR_HOME, solrHomeDir);
    isTrue(solrHomeDir.isDirectory(),
        "Cannot to set: '%s', '%s' must be a directory", SOLR_HOME,
        solrHomeDir);

    return solrHomeDir;
  }

  /**
   * Extract core names.
   *
   * @param cores All the available cores.
   * @return The core's name.
   */
  private static Iterable<String> coreNames(final CoreContainer cores) {
    List<String> names = new ArrayList<String>();
    for (SolrCore core : cores.getCores()) {
      String name = core.getName();
      if (StringUtils.isEmpty(name)) {
        // This is the default core.
        name = cores.getDefaultCoreName();
      }
      names.add(name);
    }
    return names;
  }
}
