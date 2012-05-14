package com.github.edgarespina.mwa.wro4j;

import static org.apache.commons.lang3.Validate.notEmpty;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.support.ProcessorDecorator;

/**
 * A {@link ProcessorDecorator} with Ant path style support.
 *
 * @author edgar.espina
 * @since 0.1.2
 */
final class PathPatternDecorator extends ProcessorDecorator {

  /**
   * The logging system.
   */
  private static final Logger logger = LoggerFactory
      .getLogger(PathPatternDecorator.class);

  /**
   * The ant path patterns.
   */
  private String[] patterns;

  /**
   * The path matcher.
   */
  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  /**
   * Creates a new {@link PathPatternDecorator}.
   *
   * @param processor The target processor. Required.
   * @param patterns The ant path patterns. Required.
   */
  public PathPatternDecorator(final Object processor,
      final String... patterns) {
    super(processor);
    notEmpty(patterns, "A pattern set is required.");
    this.patterns = new String[patterns.length];
    for (int i = 0; i < patterns.length; i++) {
      if (patterns[i].startsWith("/")) {
        this.patterns[i] = patterns[i];
      } else {
        this.patterns[i] = "/" + patterns[i];
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Resource resource, final Reader reader,
      final Writer writer)
      throws IOException {
    if (resource != null) {
      for (String pattern : patterns) {
        if (pathMatcher.match(pattern, resource.getUri())) {
          logger.debug("Processing resource: {}. Match found: {}",
              resource.getUri(), pattern);
          getDecoratedObject().process(resource, reader, writer);
          return;
        }
      }
      logger.debug("Skipping resource: {}. No match found: {}",
          resource.getUri(), patterns);
      IOUtils.copy(reader, writer);
    } else {
      throw new WroRuntimeException("Wrong usage of "
          + getClass().getSimpleName() + ". Please use it as a pre-processor.");
    }
  }
}
