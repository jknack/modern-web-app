package com.github.edgarespina.mwa.wro4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.web.servlet.ModelAndView;

import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;

/**
 * Publish a module attribute that define a script elements.
 *
 * @author edgar.espina
 * @since 0.1
 */
public class JavaScriptExporter extends WroContribution {

  /**
   * The list of script resources.
   */
  public static final String RESOURCES = "javascript-resources";

  /**
   * The default variable's name.
   */
  public static final String VARIABLE_NAME = "scripts";

  /**
   * Creates a new {@link JavaScriptExporter} and js scripts under the
   * variable's name.
   *
   * @param wroManagerFactory The {@link BaseWroManagerFactory}. Required.
   */
  public JavaScriptExporter(final BaseWroManagerFactory wroManagerFactory) {
    super(wroManagerFactory);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doContribution(final Group group,
      final ModelAndView modelAndView,
      final Map<String, Group> additionalGroups) throws IOException {
    String bundle = group.getName();
    Map<String, Object> model = modelAndView.getModel();
    StringBuilder buffer = new StringBuilder();
    if (useCache()) {
      buffer.append(script("/bundle/" + bundle + ".js?v=" + version));
    } else {
      List<Resource> candidates = group.getResources();
      List<Resource> resources = new ArrayList<Resource>();
      for (Resource resource : candidates) {
        if (resource.getType() == ResourceType.JS) {
          buffer.append(script(resource.getUri()));
          resources.add(resource);
        }
      }
      model.put(resourcesVarName(), resources);
    }
    // 2. Publish as a model attribute.
    model.put(varName(), buffer.toString());
    logger.trace("Publishing {}:\n{}", varName(), buffer);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String varName() {
    return VARIABLE_NAME;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String resourcesVarName() {
    return RESOURCES;
  }
}
