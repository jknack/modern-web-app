package com.github.jknack.mwa.wro4j.requirejs;

import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.commons.io.FilenameUtils.getPath;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.web.servlet.ModelAndView;

import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.Resource;

import com.github.jknack.mwa.wro4j.JavaScriptExporter;
import com.github.jknack.mwa.wro4j.WroContribution;

/**
 * Publish a model attribute that define a require.js bootstrapping function.
 * It takes care of dev/no-dev environments.
 * This contribution read a wro.xml file and use wro groups to build
 * require.config instructions.
 * FIXME: make me better and use r.js
 *
 * @author edgar.espina
 * @since 0.2.2
 */
public class RequireJsConfig extends WroContribution {

  /**
   * Require.js default timeout.
   */
  private static final int DEFAULT_TIMEOUT = 7;

  /**
   * Specify if a require.js module should wait for a dom-ready event before
   * start loading.
   *
   * @author edgar.espina@globant.com
   * @since 0.6
   */
  public static enum Loader {
    /**
     * No wait, load inmediately.
     */
    NONE {
      @Override
      public String load(final String module) {
        return "require([\"" + module + "\"]);\n";
      }
    },

    /**
     * Wait for dom-ready and use jQuery.
     */
    JQUERY_DOM_READY {
      @Override
      public String load(final String module) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("require(['jquery'], function($) {\n");
        buffer.append("  $(function() {\n");
        buffer.append("    require(['").append(module).append("']);\n");
        buffer.append("  });\n");
        buffer.append("});\n");
        return buffer.toString();
      }
    },

    /**
     * Wait for dom-ready and use require.js dom ready module.
     */
    REQUIRE_DOM_READY {
      @Override
      public String load(final String module) {
        throw new UnsupportedOperationException();
      }
    };

    /**
     * Build the load call.
     *
     * @param module The module.
     * @return The load script call.
     */
    public abstract String load(String module);
  }

  /**
   * The default variable's name.
   */
  public static final String VARIABLE_NAME = "requireConfig";

  /**
   * The variable's name.
   */
  private String variableName;

  /**
   * The number of seconds to wait before giving up on loading a script. The
   * default is 7 seconds.
   */
  private int waitSeconds = DEFAULT_TIMEOUT;

  /**
   * Should the require.js call wait for dom ready or not?
   */
  private Loader loader = Loader.NONE;

  /**
   * Creates a new {@link RequireJSContributionFixed} and publish require
   * bootstrapping script as a model attribute using the {@link #VARIABLE_NAME}.
   *
   * @param wroManagerFactory The {@link BaseWroManagerFactory}. Required.
   */
  public RequireJsConfig(
      final BaseWroManagerFactory wroManagerFactory) {
    super(wroManagerFactory);
    variableName = VARIABLE_NAME;
  }

  /**
   * Publish a module attribute that define a require.js bootstrapping function.
   * It takes care of dev/no-dev environments.
   * This contribution read a wro.xml file and use wro groups to build
   * require.config instructions. {@inheritDoc}
   */
  @Override
  protected void doContribution(final Group group,
      final ModelAndView modelAndView, final Map<String, Group> groups)
      throws IOException {
    String bundle = group.getName();
    List<Resource> resources = group.getResources();
    StringBuilder buffer = new StringBuilder();
    // 1. Add the require.js script
    if (useCache()) {
      buffer.append(script("/bundle/require.js?v=" + version));
    } else {
      buffer.append(script(requireJs(groups.get("require"))));
    }
    // 2. require.config
    buffer.append("<script>\n");
    buffer.append("require.config({\n");
    final String pathSep = ",\n";
    if (useCache()) {
      buffer.append("paths: {");
      buffer.append("\"").append(bundle).append("\"").append(": \"")
          .append(contextPath()).append("/bundle/")
          .append(bundle).append("\"");
      buffer.append("},\n");
      buffer.append("waitSeconds: ").append(waitSeconds).append(",\n");
      buffer.append("urlArgs: \"v=" + version + "\"");
    } else {
      buffer.append("paths: {\n");
      for (Resource resource : resources) {
        String uri = resource.getUri();
        String name = getName(uri);
        if (name.endsWith(".js")) {
          // 3. configure module's paths.
          String location =
              contextPath() + "/" + getPath(uri) + getBaseName(name);
          buffer.append("\"").append(FilenameUtils.getBaseName(name))
              .append("\": ");
          buffer.append("\"").append(location).append("\"").append(pathSep);
        }
      }
      buffer.append("waitSeconds: ").append(waitSeconds).append("\n");
      buffer.append("}\n");
      modelAndView.addObject(resourcesVarName(), resources);
    }
    buffer.append("\n});\n");
    // 4. load the main module
    buffer.append(loader.load(bundle)).append("\n");
    buffer.append("</script>\n");
    // 5. Publish as a model attribute.
    modelAndView.getModel().put(variableName, buffer.toString());
    logger.debug("Publishing {}:\n{}", variableName, buffer);
  }

  /**
   * The number of seconds to wait before giving up on loading a script. The
   * default is 7 seconds.
   *
   * @param waitSeconds The number of seconds to wait before giving up on
   *        loading a script. The default is 7 seconds.
   * @return This {@link RequireJSContributionFixed}.
   */
  public RequireJsConfig waitSeconds(final int waitSeconds) {
    this.waitSeconds = waitSeconds;
    return this;
  }

  /**
   * Should the require.js call wait for dom ready or not?
   *
   * @param loader The load-on strategy. Required.
   * @return This require contribution.
   */
  public RequireJsConfig loader(final Loader loader) {
    Validate.notNull(loader, "The loader strategy is required.");
    this.loader = loader;
    return this;
  }

  /**
   * Read from wro.xml the 'require' group and return the location of it.
   *
   * @param requireJs The require.js group.
   * @return The location of 'require.js'.
   */
  private String requireJs(final Group requireJs) {
    String uri = requireJs.getResources().get(0).getUri();
    return uri;
  }

  @Override
  protected String varName() {
    return VARIABLE_NAME;
  }

  @Override
  protected String resourcesVarName() {
    return JavaScriptExporter.RESOURCES;
  }
}
