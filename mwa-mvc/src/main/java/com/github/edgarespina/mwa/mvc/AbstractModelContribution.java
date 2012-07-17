package com.github.edgarespina.mwa.mvc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.edgarespina.mwa.Mode;
import com.github.edgarespina.mwa.ModeAware;

/**
 * Base class for {@link ModelContribution}.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public abstract class AbstractModelContribution implements ModelContribution,
    ModeAware {

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

  public void setMode(final Mode mode) {
    if (useCache == null) {
      useCache = !mode.isDev();
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
