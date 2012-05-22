package com.github.edgarespina.mwa.view.mustache;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.AbstractTemplateViewResolver;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

/**
 * A Mustache's {@link ViewResolver view resolver}.
 *
 * @author edgar.espina
 * @since 0.1
 */
public class MustacheViewResolver extends AbstractTemplateViewResolver
    implements InitializingBean {

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
   * The mustache factory.
   */
  private MustacheFactory mustacheFactory;

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
  public MustacheViewResolver(final Class<? extends MustacheView> viewClass) {
    setViewClass(viewClass);
    setContentType(DEFAULT_CONTENT_TYPE);
    setPrefix(DEFAULT_PREFIX);
    setSuffix(DEFAULT_SUFFIX);
  }

  /**
   * Creates a new {@link MustacheViewResolver}.
   *
   * @param contributions The model contributions. Cannot be null.
   */
  public MustacheViewResolver() {
    this(MustacheView.class);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected AbstractUrlBasedView buildView(final String viewName)
      throws Exception {
    Reader reader = null;
    try {
      MustacheView view = (MustacheView) super.buildView(viewName);
      String resourceName = view.getUrl();
      reader = read(resourceName);
      Mustache mustache = mustacheFactory.compile(reader, resourceName);
      view.setMustache(mustache);
      return view;
    } finally {
      if (reader != null) {
        reader.close();
      }
    }
  }

  /**
   * Configure Mustache. {@inheritDoc}
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    mustacheFactory = new DefaultMustacheFactory() {
      @Override
      public Reader getReader(final String resourceName) {
        return read(resourceName);
      }
    };
  }

  /**
   * Read the resource at the given path.
   *
   * @param path The resource's path.
   * @return The reader.
   */
  private Reader read(String path) {
    /** Build a mustache template. */
    final ServletContext servletContext = getServletContext();
    // Fix for Jetty:
    if (!path.startsWith("/")) {
      path = "/" + path;
    }
    InputStream input = servletContext.getResourceAsStream(path);
    notNull(input, "Resource not found: %s", path);
    return new BufferedReader(new InputStreamReader(input, encoding));
  }

  /**
   * Returns the mustache template engine.
   *
   * @return The mustache template engine.
   */
  public MustacheFactory getMustacheFactory() {
    return mustacheFactory;
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
