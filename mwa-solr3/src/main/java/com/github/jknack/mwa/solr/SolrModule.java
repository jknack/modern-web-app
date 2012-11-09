package com.github.jknack.mwa.solr;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.request.DirectXmlRequest;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.CoreContainer.Initializer;
import org.apache.solr.core.CoreDescriptor;
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
 * <h3>Configure the Solr home:</h3> The following property need to be present in the environment:
 *
 * <pre>
 * solr.home=
 * </pre>
 *
 * The 'solr.home' property can be a any valid Spring {@link Resource} expression.
 * Per each Solr 'core' a {@link SolrServer} is created and published into the
 * Spring Application Context.
 * The bean's name matches the name of the 'core', so at any time you can inject
 * a {@link SolrServer} by doing:
 *
 * <pre>
 * public MyService(@Named("core") SolrServer) {
 * ...
 * }
 * </pre>
 *
 * Of course if there is just one core you don't need to add the Named
 * annotation.
 *
 * <h3>Configure Solr Data Directory</h3> It is recommended to configure a Solr data directoy.
 *
 * <pre>
 * solr.dataDir=
 * </pre>
 *
 * If <code>solr.dataDir</code> isn't set a temporary directory will be selected.
 *
 * <h3>Configure Solr URI mapping:</h3> By default the Solr will be mounted at <code>/search</code>.
 * You can change that by:
 *
 * <pre>
 * solr.uri=/query
 * </pre>
 *
 * <h3>Fixtures</h3> You can add test or init data by creating a 'fixtures' directory under a Solr
 * core.
 * Test files are described using the Solr XML format for documents. <h4>Fixtures properties</h4>
 * <ul>
 * solr.fixtures: enabled or disabled the loading of test files. Default is: true.
 * solr.fixtures.async: if true, a new thread will be created for loading the fixtures. Default is:
 * true.
 * </ul>
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
  public static final String SOLR_HOME = "solr.home";

  /**
   * The solr.data property. Default is: ${java.io.tmpdir}/{application.name}
   */
  public static final String SOLR_DATA = "solr.dataDir";

  /**
   * The solr.uri property. Default is: /search.
   */
  public static final String SOLR_URI = "solr.uri";

  /**
   * Enabled or disabled solr fixtures. Default is: true.
   */
  public static final String SOLR_FIXTURES = "solr.fixtures";

  /**
   * Run fixtures in a new thread. Default: is true.
   */
  private static final String SOLR_FIXTURES_ASYNC = SOLR_FIXTURES + ".async";

  /**
   * The application context.
   */
  @Inject
  private ApplicationContext applicationContext;

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
  public static CoreContainer solrCores(final ApplicationContext context)
      throws IOException, ParserConfigurationException, SAXException {
    notNull(context, "The application's context is required.");
    Environment env = context.getEnvironment();

    final File solrHome = findSolrHome(context);
    File solrXml = new File(solrHome, "solr.xml");
    isTrue(solrXml.exists(), "File not found: {}", solrXml);

    final File dataDir = findSolrDataDir(env);

    final Map<String, String> coreDefs = new LinkedHashMap<String, String>();
    CoreContainer cores = new CoreContainer(solrHome.getAbsolutePath(), solrXml) {
      @Override
      public SolrCore create(final CoreDescriptor coreDescriptor)
          throws ParserConfigurationException, IOException, SAXException {
        coreDescriptor.getDataDir();
        String coreName = coreDescriptor.getName();
        if (coreName.length() == 0) {
          coreName = getDefaultCoreName();
        }
        // Set the core data directory.
        String coreDataDir = new File(dataDir, coreName).getAbsolutePath();
        coreDefs.put(coreName, coreDataDir);
        coreDescriptor.setDataDir(coreDataDir);
        return super.create(coreDescriptor);
      }
    };
    // Initialize cores
    cores.load(solrHome.getAbsolutePath(), solrXml);

    logger.info("Solr home directory: {}", solrHome);
    for (Entry<String, String> core : coreDefs.entrySet()) {
      logger.info("  core: {}, dataDir: {}", core.getKey(), core.getValue());
    }
    return cores;
  }

  /**
   * Add as many {@link SolrServer} as cores exists.
   *
   * @param cores The Solr cores.
   * @return A {@link BeanFactoryPostProcessor} that will creates as many {@link SolrServer} as
   *         cores exists.
   */
  @Bean
  public static BeanFactoryPostProcessor solrServerFactory(
      final CoreContainer cores) {
    return new BeanFactoryPostProcessor() {
      @Override
      public void postProcessBeanFactory(
          final ConfigurableListableBeanFactory beanFactory) {
        for (String coreName : coreNames(cores)) {
          coreName = isEmpty(coreName)
              ? cores.getDefaultCoreName()
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
  public static FilterMapping solrDispatcherFilter(final Environment env,
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
   * <h3>Fixtures</h3> You can add test or init data by creating a 'fixtures' directory under a Solr
   * core.
   * Test files are described using the Solr XML format for documents. <h4>Fixtures properties</h4>
   * <ul>
   * solr.fixtures: enabled or disabled the loading of test files. Default is: true.
   * solr.fixtures.async: if true, a new thread will be created for loading the fixtures. Default
   * is: true.
   * </ul>
   */
  @PostConstruct
  public void runFixtures() {
    Environment env = applicationContext.getEnvironment();
    boolean runFixtures = env.getProperty(SOLR_FIXTURES, boolean.class, true);
    if (runFixtures) {
      Map<String, SolrServer> servers = applicationContext.getBeansOfType(SolrServer.class);
      CoreContainer cores = applicationContext.getBean(CoreContainer.class);
      String solrHome = cores.getSolrHome();

      boolean async = env.getProperty(SOLR_FIXTURES_ASYNC, boolean.class, true);

      for (Entry<String, SolrServer> server : servers.entrySet()) {
        String coreName = server.getKey();
        File coreHome = new File(solrHome, coreName);
        File fixtures = new File(coreHome, "fixtures");
        if (fixtures.exists()) {
          populate(server.getValue(), coreName, fixtures, async);
        }
      }
    }
  }

  /**
   * Load fixtures and send it to the {@link SolrServer}.
   *
   * @param server The solr server.
   * @param core The core's name.
   * @param fixturesDir The fixtures directory.
   * @param async True for using a thread.
   */
  private static void populate(final SolrServer server, final String core, final File fixturesDir,
      final boolean async) {
    final Collection<File> xmlFiles = FileUtils.listFiles(fixturesDir, new String[]{"xml" }, true);
    if (xmlFiles.size() > 0) {
      Runnable post = new Runnable() {
        @Override
        public void run() {
          try {
            for (File xmlFile : xmlFiles) {
              logger.info("[{}]: sending: {}...", core, xmlFile);
              // Ensure it's in UTF-8 encoding
              Reader reader = new InputStreamReader(
                  new FileInputStream(xmlFile), "UTF-8");
              String body = IOUtils.toString(reader);
              SolrRequest request = new DirectXmlRequest("/update", body);
              /** Post the document to the Index */
              request.process(server);
              IOUtils.closeQuietly(reader);
            }
            // Commit the changes
            server.commit();
          } catch (Exception ex) {
            logger.error("Unable to initialize data", ex);
          }
        }
      };
      if (async) {
        Thread thread = new Thread(post, core + "-postData");
        thread.setDaemon(true);
        thread.start();
      } else {
        post.run();
      }
    }
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
      if (isEmpty(name)) {
        // This is the default core.
        name = cores.getDefaultCoreName();
      }
      names.add(name);
    }
    return names;
  }
}
