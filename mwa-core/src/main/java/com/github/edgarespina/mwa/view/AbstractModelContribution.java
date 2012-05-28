package com.github.edgarespina.mwa.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import com.github.edgarespina.mwa.Application;

/**
 * Base class for {@link ModelContribution}.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public abstract class AbstractModelContribution implements ModelContribution,
    EnvironmentAware {

  /**
   * True for enabled the cache. Default is: false.
   */
  private Boolean useCache;

  /**
   * The logging system.
   */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setUseCache(final boolean cache) {
    this.useCache = cache;
  }

  /**
   * Set the use cache flag if it's not been set before.
   *
   * {@inheritDoc}
   */
  @Override
  public void setEnvironment(final Environment environment) {
    if (useCache == null) {
      useCache =
          !Application.DEV.matches(environment
              .getProperty(Application.APPLICATION_MODE));
    }
  }

  /**
   * True if cache is enabled. Default is: false.
   *
   * @return True if cache is enabled. Default is: false.
   */
  public final boolean useCache() {
    return useCache == null ? Boolean.FALSE : useCache;
  }
}
