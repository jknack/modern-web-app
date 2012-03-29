package org.knowhow.mwa.wro4j;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.extensions.processor.js.JsHintProcessor;
import ro.isdc.wro.extensions.processor.support.linter.LinterException;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;

/**
 * Extends {@link JsHintProcessor} and report problem using HTML during dev.
 *
 * @author edgar.espina
 * @since 0.1
 */
@SupportedResourceType(ResourceType.JS)
class ExtendedJsHintProcessor extends JsHintProcessor {

  /**
   * These path won't be processed.
   */
  private Set<String> excludePaths = new HashSet<String>();

  /**
   * Creates a new {@link ExtendedJsHintProcessor}.
   *
   * @param options The lint options. Required.
   * @param excludePaths These path wont be processed.
   */
  public ExtendedJsHintProcessor(final LintOptions options,
      final String... excludePaths) {
    Validate.notNull(options, "The lint options is required.");
    this.excludePaths.addAll(Arrays.asList(excludePaths));
    setOptions(options.build());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Resource resource, final Reader reader,
      final Writer writer) throws IOException {
    try {
      String uri = extractURI(resource);
      if (exclude(uri)) {
        IOUtils.copy(reader, writer);
      } else {
        super.process(resource, reader, writer);
      }
    } finally {
      IOUtils.closeQuietly(reader);
      IOUtils.closeQuietly(writer);
    }
  }

  /**
   * True if the uri must be ignored.
   *
   * @param uri The uri.
   * @return True if the uri must be ignored.
   */
  private boolean exclude(final String uri) {
    for (String path : excludePaths) {
      if (uri.contains(path)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Extract the uri from the resource.
   *
   * @param resource The resource.
   * @return The resource's uri.
   */
  private String extractURI(final Resource resource) {
    if (resource == null) {
      HttpServletRequest request = Context.get().getRequest();
      return request.getRequestURI();
    }
    return resource.getUri();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onLinterException(final LinterException ex,
      final Resource resource) {
    throw new WroRuntimeException(ex.getMessage(), ex);
  }
}
