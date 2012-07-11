package com.github.edgarespina.mwa.wro4j;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.InvalidGroupNameException;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.group.processor.InjectorBuilder;

import com.github.edgarespina.mwa.mvc.AbstractModelContribution;
import com.github.edgarespina.mwa.mvc.ModelContribution;

/**
 * Base class for {@link ModelContribution} based on Wro4j.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
abstract class WroContribution extends AbstractModelContribution {

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
   * @param candidate The group candidate name.
   * @return The {@link WroModel} from wro.xml.
   */
  private Map<String, Group> lookupGroups(final HttpServletRequest request,
      final HttpServletResponse response, final String candidate) {
    Map<String, Group> groups = new HashMap<String, Group>();
    try {
      Context.set(Context.webContext(request, response, new WroFilterConfig(
          request.getServletContext())));
      Set<String> names = new LinkedHashSet<String>();
      names.add(candidate);
      names.add(candidate.replace("/", "_"));
      names.add(candidate.replace("-", "."));
      names.add(DEFAULT_GROUP);
      // TODO: Add a cache for no-dev.
      Injector injector = InjectorBuilder.create(wroManagerFactory).build();
      WroModelFactory modelFactory = wroManagerFactory.getModelFactory();
      injector.inject(modelFactory);
      WroModel model = modelFactory.create();
      for (String name : names) {
        try {
          Group group = model.getGroupByName(name);
          groups.put(name, group);
        } catch (InvalidGroupNameException ex) {
          // It's ok, just move on.
          logger.trace("Group not found: {}", name);
        }
      }
      if (groups.isEmpty()) {
        throw new InvalidGroupNameException("group(s) not found: "
            + names.toString());
      }
      for (String name : additionalGroups()) {
        groups.put(name, model.getGroupByName(name));
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
      String group = modelAndView.getViewName();
      Map<String, Group> groups = lookupGroups(request, response, group);
      doContribution(groups.remove(group), modelAndView, groups);
    } catch (InvalidGroupNameException ex) {
      Map<String, Object> model = modelAndView.getModel();
      model.put(varName(), "");
      model.put(resourcesVarName(), Collections.emptyList());
      logger.error("Groups not found: " + ex.getMessage(), ex);
    }
  }

  /**
   * Instruct which group should be loaded by this contribution.
   *
   * @return The group names.
   */
  protected Set<String> additionalGroups() {
    return Collections.emptySet();
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
}
