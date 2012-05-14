package com.github.edgarespina.mwa.wro4j;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.css.CssLintProcessor;
import ro.isdc.wro.extensions.processor.css.YUICssCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.DojoShrinksafeCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.GoogleClosureCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.JsHintProcessor;
import ro.isdc.wro.extensions.processor.js.JsLintProcessor;
import ro.isdc.wro.extensions.processor.js.UglifyJsProcessor;
import ro.isdc.wro.extensions.processor.js.YUIJsCompressorProcessor;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.impl.css.CssCompressorProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.JawrCssMinifierProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;

import com.github.edgarespina.mwa.Application;
import com.github.edgarespina.mwa.Application.Mode;

/**
 * Commons functions for Wro4j.
 *
 * @author edgar.espina
 * @since 0.1.2
 */
public final class WroHelper {

  /**
   * The list of excluded processors while the app is running in 'dev'.
   */
  private static final Class<?>[] NO_DEV_PROCESSORS = {
      CssCompressorProcessor.class,
      JawrCssMinifierProcessor.class,
      CssMinProcessor.class,
      JSMinProcessor.class,
      YUICssCompressorProcessor.class,
      YUIJsCompressorProcessor.class,
      YUIJsCompressorProcessor.class,
      DojoShrinksafeCompressorProcessor.class,
      UglifyJsProcessor.class,
      GoogleClosureCompressorProcessor.class
  };

  /**
   * The list of excluded processors while the app is running in 'NO-dev'.
   */
  private static final Class<?>[] DEV_PROCESSORS = {
      JsHintProcessor.class,
      JsLintProcessor.class,
      CssLintProcessor.class
  };

  /**
   * Not need it.
   */
  private WroHelper() {
  }

  /**
   * Read the files using the {@link UriLocatorFactory}.
   *
   * @param uriLocatorFactory The uri locator factory. Required.
   * @param resource The resource object. Required.
   * @return The file content.
   */
  public static String safeRead(final UriLocatorFactory uriLocatorFactory,
      final Resource resource) {
    InputStream in = null;
    try {
      in =
          checkNotNull(uriLocatorFactory,
              "The uri locator factory is required.").locate(
              checkNotNull(resource, "The resource is required.").getUri());
      return IOUtils.toString(in);
    } catch (IOException ex) {
      throw new WroRuntimeException("Unable to read: " + resource.getUri(),
          ex);
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  /**
   * True if the processor is enabled at the given mode.
   *
   * @param processor The candidate processor.
   * @param mode The application's mode.
   * @return True if the processor is enabled at the given mode.
   */
  static boolean enabled(final Object processor, final Mode mode) {
    Class<?>[] exclusions = mode == Application.DEV ? NO_DEV_PROCESSORS
        : DEV_PROCESSORS;
    for (Class<?> exclusion : exclusions) {
      if (exclusion.isInstance(processor)) {
        return false;
      }
    }
    return true;
  }

}
