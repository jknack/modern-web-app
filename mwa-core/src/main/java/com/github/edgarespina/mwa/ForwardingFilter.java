package com.github.edgarespina.mwa;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.enumeration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.GenericFilterBean;

/**
 * A forwarding filter that propagate to filter call to {@link FilterMapping
 * mappings} registered in the {@link WebApplicationContext}.
 *
 * @author edgar.espina
 * @since 0.1.3
 */
class ForwardingFilter extends GenericFilterBean {

  /**
   * The logging system.
   */
  private static final Logger logger = LoggerFactory
      .getLogger(ForwardingFilter.class);

  /**
   * The application context.
   */
  private WebApplicationContext webApplicationContext;

  /**
   * The list of {@link FilterMapping}.
   */
  private List<FilterMapping> mappings;

  /**
   * A forwarding filter.
   *
   * @param webApplicationContext The application context.
   */
  public ForwardingFilter(final WebApplicationContext webApplicationContext) {
    this.webApplicationContext =
        checkNotNull(webApplicationContext,
            "The web application context is required.");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doFilter(final ServletRequest request,
      final ServletResponse response, final FilterChain chain)
      throws IOException, ServletException {
    wrapChain(mappings, chain)
        .doFilter(request, response);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void initFilterBean() throws ServletException {
    this.mappings = lookFor(FilterMapping.class);
    Set<Filter> initialized = new HashSet<Filter>();
    for (FilterMapping mapping : mappings) {
      Filter filter = mapping.getFilter();
      if (initialized.add(filter)) {
        String filterName = filter.getClass().getSimpleName();
        logger.trace("Initializing filter: {}", filterName);
        filter.init(wrapFilterConfig(getServletContext(), filterName));
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void destroy() {
    if (mappings != null) {
      Set<Filter> destroyed = new HashSet<Filter>();
      for (FilterMapping mapping : mappings) {
        Filter filter = mapping.getFilter();
        if (destroyed.add(filter)) {
          String filterName = filter.getClass().getSimpleName();
          logger.trace("Destroying filter: {}", filterName);
          mapping.getFilter().destroy();
        }
      }
      mappings.clear();
      webApplicationContext = null;
    }
  }

  /**
   * Creates a {@link FilterChain} and call a filter if a path matches if found.
   *
   * @param mappings The list of filter mappings.
   * @param chain The filter chain.
   * @return A filter chaing to apply all the registered filters.
   */
  private static FilterChain wrapChain(final List<FilterMapping> mappings,
      final FilterChain chain) {
    return new FilterChain() {

      /**
       * The current filter mapping.
       */
      private final Iterator<FilterMapping> it = mappings.iterator();

      /**
       * Execute the filter.
       *
       * @param request The http servlet request.
       * @param response The http servlet response.
       * @throws IOException If something goes wrong.
       * @throws ServletException If something goes wrong.
       */
      @Override
      public void doFilter(final ServletRequest request,
          final ServletResponse response) throws IOException,
          ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response);
      }

      /**
       * Execute the filter.
       *
       * @param request The http servlet request.
       * @param response The http servlet response.
       * @throws IOException If something goes wrong.
       * @throws ServletException If something goes wrong.
       */
      private void doFilter(final HttpServletRequest request,
          final HttpServletResponse response) throws IOException,
          ServletException {
        if (it.hasNext()) {
          FilterMapping current = it.next();
          if (current.matches(request)) {
            logger.trace("Calling filter: {} for {}", current,
                request.getRequestURI());
            current.getFilter().doFilter(request, response, this);
          } else {
            logger.trace("Ignoring filter: {} for {}", current,
                request.getRequestURI());
            doFilter(request, response);
          }
        } else {
          chain.doFilter(request, response);
        }
      }
    };
  }

  /**
   * Creates a new filter config.
   *
   * @param servletContext The servlet context.
   * @param filterName The filter's name.
   * @return A new filter config.
   */
  private static FilterConfig wrapFilterConfig(
      final ServletContext servletContext, final String filterName) {
    return new FilterConfig() {
      @Override
      public ServletContext getServletContext() {
        return servletContext;
      }

      @Override
      public Enumeration<String> getInitParameterNames() {
        List<String> noParams = Collections.emptyList();
        return enumeration(noParams);
      }

      @Override
      public String getInitParameter(final String name) {
        return null;
      }

      @Override
      public String getFilterName() {
        return filterName;
      }
    };
  }

  /**
   * Look for bean of an specific type in the Application Context.
   *
   * @param beanType The bean type to look for.
   * @param <T> The bean generic type.
   * @return All the of the specific types found in the Application Context.
   */
  private <T> List<T> lookFor(final Class<T> beanType) {
    List<T> result = new ArrayList<T>();
    Collection<T> customExceptionResolvers =
        webApplicationContext.getBeansOfType(beanType).values();
    if (customExceptionResolvers != null) {
      result.addAll(customExceptionResolvers);
    }
    return result;
  }
}
