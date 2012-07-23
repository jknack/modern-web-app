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

import com.google.common.base.Joiner;

/**
 * A {@link ProcessorDecorator} with Ant path style support.
 *
 * @author edgar.espina
 * @since 0.1.2
 */
public class PathPatternDecorator extends ExtendedProcessorDecorator {

  /**
   * The logging system.
   */
  private static final Logger logger = LoggerFactory
      .getLogger(PathPatternDecorator.class);

  /**
   * The ant path patterns.
   */
  private final String[] patterns;

  /**
   * The path matcher.
   */
  private final AntPathMatcher matcher;

  /**
   * Creates a new {@link PathPatternDecorator}.
   *
   * @param processor The target processor. Required.
   * @param includes True if a possitive filter should be applied.
   * @param patterns The ant path patterns. Required.
   */
  public PathPatternDecorator(final Object processor, final boolean includes,
      final String... patterns) {
    super(processor);
    notEmpty(patterns, "A pattern set is required.");
    this.matcher = new AntPathMatcher() {
      @Override
      public boolean match(final String pattern, final String path) {
        boolean match = super.match(pattern, path);
        return includes ? match : !match;
      }

      @Override
      public String toString() {
        return (includes ? "" : "~") + "(" + Joiner.on(" OR ").join(patterns)
            + ")";
      }
    };
    this.patterns = patterns;
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
        if (matcher.match(pattern, resource.getUri())) {
          logger.debug("Processing resource: {} match found: {}",
              resource.getUri(), matcher);
          getDecoratedObject().process(resource, reader, writer);
          return;
        }
      }
      logger.debug("Skipping resource: {} no match found: {}",
          resource.getUri(), matcher);
      IOUtils.copy(reader, writer);
    } else {
      throw new WroRuntimeException("Wrong usage of "
          + toString() + ". Please use it as a pre-processor.");
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return getDecoratedObject().getClass().getName() + "@decorated";
  }
}
