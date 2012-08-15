package com.github.jknack.mwa.wro4j;

import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

/**
 * Callback interface for {@link ResourcePreProcessor} and
 * {@link ResourcePostProcessor}.
 *
 * @author edgar.espina
 * @internal
 * @since 0.1.2
 */
interface UriLocatorFactoryAware {
  /**
   * Set the {@link UriLocatorFactory}.
   *
   * @param uriLocatorFactory The {@link UriLocatorFactory}. Never null.
   */
  void setUriLocatorFactory(UriLocatorFactory uriLocatorFactory);
}
