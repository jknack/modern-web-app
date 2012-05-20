package com.github.edgarespina.mwa;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;

import org.springframework.util.AntPathMatcher;

import com.google.common.base.Joiner;

/**
 * {@link FilterMapping} are automatically registered as {@link Filter}. A
 * {@link FilterMapping} can be registered in the application context.
 *
 * @author edgar.espina
 * @since 0.1.3
 */
public final class FilterMapping {

  /**
   * The path matcher.
   */
  private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

  /**
   * The patterns to be matched.
   */
  private final String[] patterns;

  /**
   * The associated filter.
   */
  private Filter filter;

  /**
   * Creates a new {@link FilterMapping}.
   *
   * @param patterns The path patterns.
   */
  private FilterMapping(final String[] patterns) {
    this.patterns = patterns;
  }

  /**
   * Bind the {@link Filter} to the this {@link FilterMapping}.
   *
   * @param filter The filter. Required.
   * @return This filter mapping.
   */
  public FilterMapping through(final Filter filter) {
    if (this.filter != null) {
      throw new IllegalStateException("Filter was set already!");
    }
    this.filter = checkNotNull(filter, "The filter is required.");
    return this;
  }

  /**
   * Returns true if the request path matches one of the registered patterns.
   *
   * @param request The http servlet request. Required.
   * @return True if the request path matches one of the registered patterns.
   */
  public boolean matches(final HttpServletRequest request) {
    return matches(request.getRequestURI());
  }

  /**
   * Returns true if the request path matches one of the registered patterns.
   *
   * @param path The request uri path. Required.
   * @return True if the request path matches one of the registered patterns.
   */
  boolean matches(final String path) {
    for (String pattern : patterns) {
      if (PATH_MATCHER.match(pattern, path)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the {@link Filter}.
   *
   * @return The target filter.
   */
  public Filter getFilter() {
    return filter;
  }

  /**
   * Creates a new {@link FilterMapping}.
   *
   * @param patterns The list of patterns.
   * @return A new {@link FilterMapping}.
   */
  public static FilterMapping filter(final String... patterns) {
    return new FilterMapping(patterns);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return filter.getClass().getSimpleName() + ":"
        + Joiner.on(", ").join(patterns);
  }
}
