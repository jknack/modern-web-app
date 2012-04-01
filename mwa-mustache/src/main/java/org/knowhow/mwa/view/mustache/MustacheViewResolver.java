package org.knowhow.mwa.view.mustache;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;

import javax.servlet.ServletContext;

import org.knowhow.mwa.view.ModelContribution;
import org.knowhow.mwa.view.ModernView;
import org.knowhow.mwa.view.ModernViewResolver;
import org.springframework.web.servlet.ViewResolver;

import com.sampullara.mustache.Mustache;
import com.sampullara.mustache.MustacheBuilder;
import com.sampullara.mustache.MustacheContext;
import com.sampullara.mustache.MustacheException;
import com.sampullara.util.FutureWriter;

/**
 * A Mustache's {@link ViewResolver view resolver}.
 *
 * @author edgar.espina
 * @since 0.1
 */
public class MustacheViewResolver extends ModernViewResolver {

  /**
   * The default content type.
   */
  private static final String DEFAULT_CONTENT_TYPE = "text/html;charset=UTF-8";

  /**
   * The default view prefix.
   */
  private static final String DEFAULT_PREFIX = "/";

  /**
   * The default view suffix.
   */
  private static final String DEFAULT_SUFFIX = ".html";

  /**
   * The mustache engine.
   */
  private MustacheBuilder engine;

  /**
   * True, for parallel template processing. Default is: false.
   */
  private boolean parallelProcessing = false;

  /**
   * The mustache superclass attribute.
   */
  private Class<? extends Mustache> superClass;

  /**
   * The template loader.
   */
  private MustacheContext mustacheContext;

  /**
   * The encoding charset.
   */
  private Charset encoding = Charset.forName("UTF-8");

  /**
   * Creates a new {@link MustacheViewResolver}.
   *
   * @param viewClass The mustache view class. Required.
   * @param contributions The model contributions. Cannot be null.
   */
  public MustacheViewResolver(final Class<? extends MustacheView> viewClass,
      final ModelContribution... contributions) {
    super(contributions);
    setViewClass(viewClass);
    setContentType(DEFAULT_CONTENT_TYPE);
    setPrefix(DEFAULT_PREFIX);
    setSuffix(DEFAULT_SUFFIX);
    setParallelProcessing(false);
  }

  /**
   * Creates a new {@link MustacheViewResolver}.
   *
   * @param contributions The model contributions. Cannot be null.
   */
  public MustacheViewResolver(final ModelContribution... contributions) {
    this(MustacheView.class, contributions);
  }

  /**
   * Configure a new {@link MustacheView}. {@inheritDoc}
   */
  @Override
  protected void buildView(final ModernView view) throws Exception {
    /** Build a mustache template. */
    ((MustacheView) view).setMustache(engine.parseFile(view.getUrl()));
  }

  /**
   * Configure Mustache. {@inheritDoc}
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();
    if (this.mustacheContext == null) {
      /** Bind the Servlet context. */
      final ServletContext servletContext = getServletContext();
      /** A mustache context for serving from web root. */
      this.mustacheContext = new MustacheContext() {
        @Override
        public BufferedReader getReader(final String path)
            throws MustacheException {
          // Fix for Jetty:
          final String absolutePath = path.startsWith("/") ? path : "/" + path;
          logger.trace("Loading mustache file: " + absolutePath);
          InputStream in = servletContext.getResourceAsStream(absolutePath);
          if (in == null) {
            throw new MustacheException("Page not found: " + absolutePath);
          }
          return new BufferedReader(new InputStreamReader(in, encoding));
        }
      };
    }
    engine = new MustacheBuilder(mustacheContext);
    if (superClass != null) {
      engine.setSuperclass(superClass.getName());
    }
    /** Use parallel processing */
    ExecutorService executorService = FutureWriter.getExecutorService();
    logger.debug("Use parallel processing: " + parallelProcessing);
    if (!parallelProcessing) {
      if (executorService != null) {
        FutureWriter.setExecutorService(null);
      }
    }
  }

  /**
   * Returns the mustache template engine.
   *
   * @return The mustache template engine.
   */
  public MustacheBuilder getTemplateEngine() {
    return engine;
  }

  /**
   * Set parallel template processing.
   *
   * @param parallelProcessing True, for parallel template processing. Default
   *        is: false.
   */
  public void setParallelProcessing(final boolean parallelProcessing) {
    this.parallelProcessing = parallelProcessing;
  }

  /**
   * Set the superclass mustache property.
   *
   * @param superClass The mustache superclass. Optional.
   */
  public void setSuperClass(final Class<? extends Mustache> superClass) {
    this.superClass = superClass;
  }

  /**
   * Set the mustache context.
   *
   * @param mustacheContext The mustache context. Optional.
   */
  public void setMustacheContext(final MustacheContext mustacheContext) {
    this.mustacheContext = mustacheContext;
  }

  /**
   * Set the charset encoding. Default is: UTF-8.
   *
   * @param encoding The charset encoding.
   */
  public void setEncoding(final String encoding) {
    this.encoding = Charset.forName(encoding);
  }

  /**
   * The required view class.
   *
   * @return The required view class.
   */
  @Override
  protected Class<?> requiredViewClass() {
    return MustacheView.class;
  }

}
