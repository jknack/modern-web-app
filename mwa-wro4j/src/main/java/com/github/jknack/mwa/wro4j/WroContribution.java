package com.github.jknack.mwa.wro4j;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.ModelAndView;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.InvalidGroupNameException;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.group.processor.InjectorBuilder;

import com.github.jknack.mwa.mvc.AbstractModelContribution;
import com.github.jknack.mwa.mvc.ModelContribution;

/**
 * Base class for {@link ModelContribution} based on Wro4j.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public abstract class WroContribution extends AbstractModelContribution
    implements ServletContextAware {

  /**
   * A {@link FilterConfig} for wro filter.
   *
   * @author edgar.espina
   * @since 0.1.2
   */
  static final class WroFilterConfig implements FilterConfig {

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

  /**
   * The default group name, if the candidate group cannot be found.
   */
  public static final String DEFAULT_GROUP = "defaults";

  /**
   * The {@link WroModel} factory.
   */
  private BaseWroManagerFactory wroManagerFactory;

  /**
   * A clear cache flag.
   */
  protected final String version = new SimpleDateFormat("yyyyMMdd.hhmmss")
      .format(new Date());

  /**
   * The wro filter config.
   */
  private FilterConfig wroConfig;

  /**
   * Creates a new {@link WroContribution}.
   *
   * @param wroManagerFactory The {@link WroModelFactory}. Required.
   */
  public WroContribution(final BaseWroManagerFactory wroManagerFactory) {
    this.wroManagerFactory =
        checkNotNull(wroManagerFactory, "The wroManagerFactory is required.");
  }

  /**
   * The {@link WroModel} from wro.xml.
   *
   * @param request The HTTP request.
   * @param response The HTTP response.
   * @return The {@link WroModel} from wro.xml.
   */
  private Map<String, Group> lookupGroups(final HttpServletRequest request,
      final HttpServletResponse response) {
    try {
      Context.set(Context.webContext(request, response, wroConfig));
      // TODO: Add a cache for no-dev.
      Injector injector = InjectorBuilder.create(wroManagerFactory).build();
      WroModelFactory modelFactory = wroManagerFactory.getModelFactory();
      injector.inject(modelFactory);
      WroModel model = modelFactory.create();
      Map<String, Group> groups = new HashMap<String, Group>();
      for (Group group : model.getGroups()) {
        groups.put(group.getName(), group);
      }
      return groups;
    } finally {
      Context.unset();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void contribute(final HttpServletRequest request,
      final HttpServletResponse response, final ModelAndView modelAndView)
      throws IOException {
    try {
      Map<String, Group> groups = lookupGroups(request, response);
      String candidate = defaultGroup(modelAndView);
      Group group = groups.remove(candidate);
      if (group == null) {
        group = groups.remove("defaults");
      }
      if (group == null) {
        throw new InvalidGroupNameException("'" + candidate
            + "'. Available groups are: " + groups.keySet());
      }
      doContribution(group, modelAndView, groups);
    } catch (InvalidGroupNameException ex) {
      modelAndView.addObject(varName(), "");
      modelAndView.addObject(resourcesVarName(), Collections.emptyList());
      logger.error("Groups not found: " + ex.getMessage(), ex);
    }
  }

  /**
   * Extract the default group name from the model.
   *
   * @param modelAndView The model and view.
   * @return The group's name.
   */
  protected String defaultGroup(final ModelAndView modelAndView) {
    return modelAndView.getViewName();
  }

  /**
   * Add wro contribution to the model.
   *
   * @param group The group behind the view.
   * @param modelAndView The model and view.
   * @param additionalGroups Additional groups, defined by
   *        {@link #additionalGroups()}.
   * @throws IOException If something goes wrong.
   */
  protected abstract void doContribution(final Group group,
      final ModelAndView modelAndView, Map<String, Group> additionalGroups)
      throws IOException;

  /**
   * The name of the model attribute.
   *
   * @return The name of the model attribute.
   */
  protected abstract String varName();

  /**
   * The name of the model attribute.
   *
   * @return The name of the model attribute.
   */
  protected abstract String resourcesVarName();

  @Override
  public void setServletContext(final ServletContext servletContext) {
    super.setServletContext(servletContext);
    wroConfig = new WroFilterConfig(servletContext);
  }

  /**
   * Generate a script element for the javascript URI.
   *
   * @param uri The javascript URI.
   * @return A script HTML element.
   */
  protected String script(final String uri) {
    String script = "<script type=\"text/javascript\" src=\"%s\"></script>\n";
    return String.format(script, normalizePath(uri));
  }

  /**
   * Check if the path start with the servlet context. If not the servlet
   * context is prepend to the URI.
   *
   * @param uri A URI.
   * @return A URI with the servlet context.
   */
  protected String normalizePath(final String uri) {
    String contextPath = contextPath();
    if (uri.startsWith(contextPath)) {
      return uri;
    }
    return contextPath + (uri.startsWith("/") ? uri : "/" + uri);
  }

  /**
   * Generate a link element for the css URI.
   *
   * @param uri The css URI.
   * @return A link HTML element.
   */
  protected String link(final String uri) {
    String link = "<link rel=\"stylesheet\" text=\"text/css\" href=\"%s\">\n";
    return String.format(link, normalizePath(uri));
  }
}
