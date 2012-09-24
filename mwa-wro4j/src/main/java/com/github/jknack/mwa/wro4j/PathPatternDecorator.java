package com.github.jknack.mwa.wro4j;

import static org.apache.commons.lang3.Validate.notEmpty;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.decorator.ProcessorDecorator;

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
   * Shall we include paths?
   */
  private boolean includes;

  /**
   * Creates a new {@link ExtendedProcessorDecorator}.
   *
   * @param processor The target processor. Required.
   * @param includes True if a possitive filter should be applied.
   * @param patterns The ant path patterns. Required.
   */
  public PathPatternDecorator(final Object processor, final boolean includes,
      final String... patterns) {
    super(processor);
    notEmpty(patterns, "A pattern set is required.");
    this.includes = includes;
    matcher = new AntPathMatcher();
    this.patterns = patterns;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Resource resource, final Reader reader,
      final Writer writer)
      throws IOException {
    final String uri;
    if (resource != null) {
      uri = resource.getUri();
    } else {
      uri = Context.get().getRequest().getRequestURI();
    }
    boolean process = false;
    if (includes) {
      // Match (p1 OR p2 OR .. pn)
      for (String pattern : patterns) {
        if (matcher.match(pattern, uri)) {
          logger.debug("Processing resource: {}. Match found: {}",
              uri, toString());
          process = true;
        }
      }
    } else {
      process = true;
      // Match !(p1 AND p2 AND .. pn)
      for (String pattern : patterns) {
        if (matcher.match(pattern, uri)) {
          process = false;
          break;
        }
      }
    }
    if (process) {
      logger.debug("Processing resource: {}. Match found: {}", uri,
          toString());
      getDecoratedObject().process(resource, reader, writer);
    } else {
      logger.debug("Skipping {} from {}. No match found: {}", new Object[] {
          uri, getDecoratedObject(), toString() });
      WroHelper.safeCopy(reader, writer);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder(includes ? "(" : "!(");
    String separator = includes ? " || " : " && ";
    for (String pattern : patterns) {
      buffer.append(pattern).append(separator);
    }
    buffer.setLength(buffer.length() - separator.length());
    buffer.append(")");
    return buffer.toString();
  }
}
