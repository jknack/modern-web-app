package org.knowhow.mwa.wro4j;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;

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
   * The default variable's name.
   */
  public static final String VARIABLE_NAME = "scripts";

  /**
   * The variable's name.
   */
  private String variableName;

  /**
   * Creates a new {@link JavaScriptExporter} and js scripts under the
   * variable's name.
   *
   * @param variableName The variable's name. Required.
   */
  public JavaScriptExporter(final String variableName) {
    Validate.notEmpty(variableName, "The variable's name is required.");
    this.variableName = variableName;
  }

  /**
   * Creates a new {@link JavaScriptExporter} and js scripts under the
   * {@link #VARIABLE_NAME}.
   */
  public JavaScriptExporter() {
    this(VARIABLE_NAME);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void contribute(final String view, final Map<String, Object> model)
      throws IOException {
    Group group = lookupGroup(view);
    StringBuilder buffer = new StringBuilder();
    String contextPath = (String) model.get(CONTEXT_PATH);
    if (useCache()) {
      buffer.append(script(contextPath, "bundle/" + view + ".js", version));
    } else {
      List<Resource> resources = group.getResources();
      for (Resource resource : resources) {
        if (resource.getType() == ResourceType.JS) {
          buffer.append(script(contextPath, resource.getUri(), null));
        }
      }
    }
    // 2. Publish as a model attribute.
    model.put(variableName, buffer.toString());
    logger.debug("Publishing {}:\n{}", variableName, buffer);
  }

  /**
   * Generate a script element.
   *
   * @param contextPath The app context path.
   * @param path The location of the file.
   * @param version The version. Optional.
   * @return A script element.
   */
  private String script(final String contextPath, final String path,
      final String version) {
    String script = "<script type='text/javascript' src='%s'></script>\n";
    StringBuilder file = new StringBuilder(contextPath)
        .append("/")
        .append(path);
    if (version != null) {
      file.append("?v=").append(version);
    }
    return String.format(script, file.toString());
  }
}
