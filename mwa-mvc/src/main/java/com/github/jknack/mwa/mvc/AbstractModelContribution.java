package com.github.jknack.mwa.mvc;

import java.io.File;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ServletContextAware;

import com.github.jknack.mwa.ApplicationModeAware;
import com.github.jknack.mwa.IApplicationMode;

/**
 * Base class for {@link ModelContribution}.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public abstract class AbstractModelContribution implements ModelContribution,
    ApplicationModeAware, ServletContextAware {

  /**
   * True for enabled the cache. Default is: false.
   */
  private Boolean useCache;

  /**
   * The logging system.
   */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * The context path.
   */
  private String contextPath;

  /**
   * The base web app directory.
   */
  private File baseDir;

  /**
   * The environment mode.
   */
  private IApplicationMode mode;

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setUseCache(final boolean cache) {
    useCache = cache;
  }

  @Override
  public void setMode(final IApplicationMode mode) {
    if (useCache == null) {
      useCache = !mode.isDev();
    }
    this.mode = mode;
  }

  /**
   * The environment mode.
   *
   * @return The environment mode.
   */
  public IApplicationMode mode() {
    return mode;
  }

  /**
   * True if cache is enabled. Default is: false.
   *
   * @return True if cache is enabled. Default is: false.
   */
  public final boolean useCache() {
    return useCache == null ? Boolean.FALSE : useCache;
  }

  @Override
  public void setServletContext(final ServletContext servletContext) {
    contextPath = servletContext.getContextPath();
    baseDir = new File(servletContext.getRealPath("/"));
  }

  /**
   * The base web app directory.
   *
   * @return The base web app directory.
   */
  public File baseDir() {
    return baseDir;
  }

  /**
   * The context path.
   *
   * @return The context path.
   */
  public String contextPath() {
    return contextPath;
  }
}
