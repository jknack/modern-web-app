package com.github.jknack.mwa.wro4j.requirejs;

import static org.apache.commons.io.FilenameUtils.getBaseName;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.tools.shell.Global;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.util.StopWatch;

import com.github.jknack.mwa.Mode;
import com.github.jknack.mwa.ModeAware;
import com.github.jknack.mwa.wro4j.RhinoExecutor;
import com.github.jknack.mwa.wro4j.WroHelper;
import com.github.jknack.mwa.wro4j.RhinoExecutor.JsTask;
import com.google.common.collect.Lists;

/**
 * <p>
 * A require.js processor. Please note this processor should be off during
 * development. You must follow these steps for making it work:
 * </p>
 * <p>
 * 1. Configure a fallback group.
 * </p>
 *
 * <pre>
 * {@code
 *  wro.xml:
 *  <group name="fallback">
 *    <js>/js/*.js
 *  ...
 * }
 * </pre>
 *
 * With this, wro will see all your *.js files during development (mode=dev).
 * <p>
 * 2. Configure a require.js group
 *
 * <pre>
 * {@code
 *  wro.xml:
 *  <group name="home">
 *    <js>/js/home.js
 *  ...
 * }
 * </pre>
 *
 * </p>
 * Here is where you define a wro group that will be processed by require.js.
 * Remember you just need to add the 'AMD' module that will load all the
 * dependencies (e.g. 'home'). Require.js will trace all the dependencies of
 * 'home' and bundle them all together.
 * <p>
 * 3. Configure a build profile to instruct require.js how to build. The
 * processor can use custom build profiles if you store them in a "/build"
 * directory inside your web context.
 *
 * <pre>
 *  src/main/webapp/build/build.js:
 *  ({
 *    paths: {
 *      "underscore": "empty:",
 *      "backbone": "empty:",
 *      "mustache": "empty:",
 *      "jquery": "empty:",
 *      "jqueryUI": "js/libs/jquery-ui-1.8.18.custom"
 *    }
 *   })
 * </pre>
 *
 * Please note that path are resolved using the web context (e.g. "/").
 * <p>
 * Build profile precedence:
 * </p>
 * <ol>
 * <li>[groupName].[profile].js: where profile is a request parameter. Example:
 * /group.js?profile=myExternalApp
 * <li>[groupName].[mode].js: where module is resolved from the application.mode
 * environment property.
 * <li>build.[profile].js: same as 1) except that represents a global profile.
 * <li>build.[profile].js: same as 2) except that represents a global profile.
 * </ol>
 *
 * @author edgar.espina
 * @since 0.2.3
 */
@SupportedResourceType(ResourceType.JS)
public class RequireJsProcessor implements ResourcePostProcessor, ModeAware {

  /**
   * The r.js source.
   */
  private final String source;

  /**
   * The runs in mode.
   */
  private Mode mode;

  /**
   * The logging system.
   */
  private static final Logger logger = LoggerFactory
      .getLogger(RequireJsProcessor.class);

  /**
   * Creates a new {@link RequireJsProcessor}.
   */
  public RequireJsProcessor() {
    InputStream in = null;
    try {
      String rjs = "r-2.0.4.js";
      logger.debug("Loading: {}", rjs);
      in = script(rjs);
      source = IOUtils.toString(in);
    } catch (Exception ex) {
      throw new IllegalStateException("Cannot load r.js", ex);
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  @Override
  public void process(final Reader reader, final Writer writer)
      throws IOException {
    final StopWatch watch = new StopWatch();
    watch.start("rhino setup");
    RhinoExecutor.execute(new JsTask<String>() {
      @Override
      public String run(final Global global, final Context context,
          final Scriptable scope) throws IOException {
        watch.stop();
        watch.start("r.js");
        ServletContext servletContext = servletContext();
        final String tmpdir = System.getProperty("java.io.tmpdir");
        final HttpServletRequest currentRequest = currentRequest();
        final String profile = currentRequest.getParameter("profile");
        final String uri = currentRequest.getRequestURI().replace(
            servletContext.getContextPath(), "");
        final String baseName = getBaseName(uri);
        final String name = baseName + "-" + System.nanoTime();
        final String appDir = servletContext.getRealPath("/");
        final String base = appDir;
        // mkdirs
        new File(base).mkdirs();
        final String baseBuild = base + "/build";
        final File tmpIn = new File(base, name + ".js");
        final File out = new File(tmpdir, name + "-bundle.js");
        // safe the current input to file and use it. doing this we make sure
        // that if any previous processor in the chain changed the input we use
        // that in r.js.
        WroHelper.safeCopy(reader, new FileWriter(tmpIn));

        // r.js arguments
        List<String> args = Lists.newArrayList(
            "-o",
            "name=" + name,
            "baseUrl=" + base,
            "out=" + out.getAbsolutePath(),
            "optimize=none"
            );

        // Find a specific build profile for the given file.
        File build =
            buildFile(baseBuild, baseName, profile, mode.name());
        if (build == null) {
          // no luck, find a global profile per environment
          build = buildFile(baseBuild, "build", profile, mode.name());
          if (build == null) {
            // no luck, defaults to build.js
            build = new File(baseBuild, "build.js");
          }
        }
        if (build.exists()) {
          logger.debug("Build's profile found: {}", build.getName());
          args.add(1, build.getAbsolutePath());
        }

        global.defineProperty("arguments",
            context.newArray(global, args.toArray(new Object[args.size()])),
            ScriptableObject.DONTENUM);

        logger.debug("r.js {}", StringUtils.join(args, " "));

        // execute r.js
        Reader bundle = null;
        try {
          context.evaluateString(scope, source, "r.js", 1, null);
          // Read the r.js output.
          bundle = new FileReader(out);
          String content = IOUtils.toString(bundle).replace(name, baseName);
          WroHelper.safeCopy(new StringReader(content), writer);
          return null;
        } finally {
          logger.debug("Deleting: {}", tmpIn);
          FileUtils.deleteQuietly(tmpIn);

          logger.debug("Deleting: {}", out);
          FileUtils.deleteQuietly(out);
          watch.stop();
          logger.debug(watch.prettyPrint());
        }
      }

    });
  }

  /**
   * Look for a build file using the candidates suffix.
   *
   * @param baseUrl The root dir.
   * @param baseName The base file's name.
   * @param suffixes The file's suffixes.
   * @return A file found or null.
   */
  private File buildFile(final String baseUrl, final String baseName,
      final String... suffixes) {
    for (String suffix : suffixes) {
      if (suffix != null) {
        File build = new File(baseUrl, baseName + "." + suffix + ".js");
        logger.debug("Searching for build's profile: {}", build.getName());
        if (build.exists()) {
          return build;
        }
      }
    }
    return null;
  }

  /**
   * Load the r.js script.
   *
   * @param location The location of the r.js script.
   * @return An input stream.
   */
  protected InputStream script(final String location) {
    return getClass().getResourceAsStream(location);
  }

  /**
   * Return the servletContext.
   *
   * @return A servlet context.
   */
  protected ServletContext servletContext() {
    return ro.isdc.wro.config.Context.get().getServletContext();
  }

  /**
   * Return the current http request.
   *
   * @return The current http request.
   */
  protected HttpServletRequest currentRequest() {
    return ro.isdc.wro.config.Context.get().getRequest();
  }

  @Override
  public void setMode(final Mode mode) {
    this.mode = mode;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
