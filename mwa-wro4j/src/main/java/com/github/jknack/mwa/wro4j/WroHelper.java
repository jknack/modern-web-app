package com.github.jknack.mwa.wro4j;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;

/**
 * Commons functions for Wro4j.
 *
 * @author edgar.espina
 * @since 0.1.2
 */
public final class WroHelper {

  /**
   * No need it.
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
   * Copy and close the reader and writer streams.
   *
   * @param reader The source stream.
   * @param writer The destintation stream.
   * @throws IOException If content cannot be copy.
   */
  public static void safeCopy(final Reader reader, final Writer writer)
      throws IOException {
    try {
      IOUtils.copy(reader, writer);
    } finally {
      IOUtils.closeQuietly(reader);
      IOUtils.closeQuietly(writer);
    }
  }
}
