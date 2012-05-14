package com.github.edgarespina.mwa.wro4j;

import java.util.Enumeration;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

/**
 * A {@link FilterConfig} for wro filter.
 *
 * @author edgar.espina
 * @since 0.1.2
 */
final class WroFilterConfig implements FilterConfig {

  /**
   * The servlet's context.
   */
  private ServletContext servletContext;

  /**
   * Creates a new {@link WroFilterConfig}.
   *
   * @param servletContext The servlet's context.
   */
  public WroFilterConfig(final ServletContext servletContext) {
    this.servletContext = servletContext;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getFilterName() {
    return "wroFilter";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ServletContext getServletContext() {
    return servletContext;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getInitParameter(final String name) {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Enumeration<String> getInitParameterNames() {
    return null;
  }

}
