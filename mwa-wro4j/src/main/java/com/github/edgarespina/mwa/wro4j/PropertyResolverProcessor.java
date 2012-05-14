package com.github.edgarespina.mwa.wro4j;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.springframework.core.env.PropertyResolver;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

/**
 * A processor which parse a resource and search for placeholders of this type:
 * <code>${}</code> and replace them with the values found in a map provided the
 * by client.
 *
 * @author edgar.espina
 * @since 0.1.1
 */
class PropertyResolverProcessor implements ResourcePreProcessor {

  /**
   * The application's environment.
   */
  private PropertyResolver resolver;

  /**
   * Creates a new {@link PropertyResolverProcessor}.
   *
   * @param resolver The property resolver. Required.
   */
  public PropertyResolverProcessor(final PropertyResolver resolver) {
    this.resolver =
        checkNotNull(resolver, "The application's environment is required.");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Resource resource, final Reader reader,
      final Writer writer)
      throws IOException {
    final String content = IOUtils.toString(reader);
    boolean ignoreMissingResources =
        resolver.getProperty("wro.ignoreMissingResources", Boolean.class,
            true);
    String output =
        ignoreMissingResources ? resolver.resolvePlaceholders(content)
            : resolver.resolveRequiredPlaceholders(content);
    writer.write(output);
  }

}
