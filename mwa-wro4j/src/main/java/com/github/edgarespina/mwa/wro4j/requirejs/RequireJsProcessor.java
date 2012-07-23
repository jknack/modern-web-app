package com.github.edgarespina.mwa.wro4j.requirejs;

import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getPathNoEndSeparator;

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

import com.github.edgarespina.mwa.Mode;
import com.github.edgarespina.mwa.ModeAware;
import com.github.edgarespina.mwa.wro4j.RhinoExecutor;
import com.github.edgarespina.mwa.wro4j.RhinoExecutor.JsTask;
import com.github.edgarespina.mwa.wro4j.WroHelper;
import com.google.common.collect.Lists;

/**
 * A require.js processor. This processor is off in debug mode. It use a modify
 * version of r.js for rhino. The custom version doesn't write the output to a
 * file, instead it save the output to a global variable.
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
      this.source = IOUtils.toString(in);
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
        final String path = getPathNoEndSeparator(uri);
        final String baseName = getBaseName(uri);
        final String name = baseName + "-" + System.nanoTime();
        final String appDir = servletContext.getRealPath("/");
        final String baseUrl = appDir + path;
        final File tmpIn = new File(baseUrl, name + ".js");
        final File out = new File(tmpdir, name + "-bundle.js");

        // safe the current input to file and use it. doing this we make sure
        // that if any previous processor in the chain changed the input we use
        // that in r.js.
        WroHelper.safeCopy(reader, new FileWriter(tmpIn));

        // r.js arguments
        List<String> args = Lists.newArrayList(
            "-o",
            "name=" + name,
            "baseUrl=" + baseUrl,
            "out=" + out.getAbsolutePath(),
            "optimize=none"
            );

        // Find a specific build profile for the given file.
        File build =
            buildFile(baseUrl, baseName, profile, mode.name());
        if (build == null) {
          // no luck, find a global profile per environment
          build = buildFile(baseUrl, "build", profile, mode.name());
          if (build == null) {
            // no luck, defaults to build.js
            build = new File(baseUrl, "build.js");
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
