package org.knowhow.mwa.wro4j;

import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getPath;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;

/**
 * Publish a module attribute that define a css links elements.
 *
 * @author edgar.espina
 * @since 0.1
 */
public class CssExporter extends WroContribution {

  /**
   * The default variable's name.
   */
  public static final String VARIABLE_NAME = "cssLinks";

  /**
   * The variable's name.
   */
  private String variableName;

  /**
   * Creates a new {@link CssExporter} and css links under the
   * variable's name.
   *
   * @param variableName The variable's name. Required.
   */
  public CssExporter(final String variableName) {
    Validate.notEmpty(variableName, "The variable's name is required.");
    this.variableName = variableName;
  }

  /**
   * Creates a new {@link CssExporter} and css links under the
   * {@link #VARIABLE_NAME}.
   */
  public CssExporter() {
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
      buffer.append("<link rel=\"stylesheet\" text=\"text/css\" href=\"")
          .append(contextPath).append("/bundle/").append(view)
          .append(".css").append("?v=").append(version).append("\">");
    } else {
      List<Resource> resources = group.getResources();
      for (Resource resource : resources) {
        if (resource.getType() == ResourceType.CSS) {
          // If less or sass is used strip the extension of it and set it to
          // css.
          String uri = "/" + getPath(resource.getUri())
              + getBaseName(resource.getUri()) + ".css";
          // 1. Collect css.
          buffer.append("<link rel=\"stylesheet\" text=\"text/css\" href=\"")
              .append(contextPath).append(uri).append("\">");
        }
      }
    }
    // 2. Publish as a model attribute.
    model.put(variableName, buffer.toString());
    logger.debug("Publishing {}:\n{}", variableName, buffer);
  }

}
