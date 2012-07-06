package com.github.edgarespina.mwa;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.enumeration;
import static org.apache.commons.lang3.Validate.notEmpty;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.core.Ordered;
import org.springframework.util.AntPathMatcher;

import com.google.common.base.Joiner;

/**
 * {@link FilterMapping} are automatically registered as {@link Filter}. A
 * {@link FilterMapping} can be registered in the application context.
 *
 * @author edgar.espina
 * @since 0.1.3
 */
public final class FilterMapping implements Ordered {

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
   * Filter parameters.
   */
  private final Map<String, String> params =
      new LinkedHashMap<String, String>();

  /**
   * The filter precedence.
   */
  private int precedence = Ordered.LOWEST_PRECEDENCE;

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
   * A value between {@link Ordered#HIGHEST_PRECEDENCE} and
   * {@link Ordered#LOWEST_PRECEDENCE}.
   *
   * @param precedence The precedence order.
   * @return This filter mapping.
   */
  public FilterMapping order(final int precedence) {
    this.precedence = precedence;
    return this;
  }

  /**
   * Add a filter parameter.
   *
   * @param name The parameter's name. Required.
   * @param value The parameter's value.
   * @return This filter mapping.
   */
  public FilterMapping param(final String name, final String value) {
    params.put(notEmpty(name, "The param's name is required."), value);
    return this;
  }

  /**
   * Convert the mapping to a {@link FilterConfig}.
   *
   * @param servletContext The servlet context.
   * @return A new {@link FilterConfig}.
   */
  public FilterConfig asFilterConfig(final ServletContext servletContext) {
    return new FilterConfig() {

      @Override
      public ServletContext getServletContext() {
        return servletContext;
      }

      @Override
      public Enumeration<String> getInitParameterNames() {
        Enumeration<String> names = enumeration(params.keySet());
        return names;
      }

      @Override
      public String getInitParameter(final String name) {
        return params.get(name);
      }

      @Override
      public String getFilterName() {
        return filter.getClass().getSimpleName();
      }
    };
  }

  /**
   * Returns true if the request path matches one of the registered patterns.
   *
   * @param request The http servlet request. Required.
   * @return True if the request path matches one of the registered patterns.
   */
  public boolean matches(final HttpServletRequest request) {
    String requestURI = request.getRequestURI();
    return matches(requestURI.replace(request.getContextPath(), ""));
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
    return filter.getClass().getSimpleName() + ":("
        + Joiner.on(" OR").join(patterns) + ")";
  }

  @Override
  public int getOrder() {
    return precedence;
  }
}
