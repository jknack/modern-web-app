package com.github.edgarespina.mwa.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for {@link ModelContribution}.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public abstract class AbstractModelContribution implements ModelContribution {

  /**
   * True for enabled the cache. Default is: false.
   */
  private boolean useCache = false;

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
   * True if cache is enabled. Default is: false.
   *
   * @return True if cache is enabled. Default is: false.
   */
  public final boolean useCache() {
    return useCache;
  }
}
