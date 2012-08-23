package com.github.jknack.mwa.wro4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import ro.isdc.wro.extensions.processor.support.csslint.CssLintError;
import ro.isdc.wro.extensions.processor.support.csslint.CssRule;
import ro.isdc.wro.extensions.processor.support.linter.LinterError;
import ro.isdc.wro.model.group.InvalidGroupNameException;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

/**
 * Handle a {@link RuntimeException} during wro4j processing.
 *
 * @author edgar.espina
 * @since 0.1.2
 */
public enum WroProblemReporter {

  /**
   * The default handler. It just throw the exception.
   */
  DEFAULT {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean apply(final RuntimeException ex) {
      return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void report(final RuntimeException ex,
        final HttpServletRequest request,
        final HttpServletResponse response) {
      throw ex;
    }
  },

  /**
   * Handle resoureces not found errors.
   */
  GROUP_NOT_FOUND {
    @Override
    public boolean apply(final RuntimeException ex) {
      return ex instanceof InvalidGroupNameException;
    }

    @Override
    public void report(final RuntimeException ex,
        final HttpServletRequest request,
        final HttpServletResponse response) {
      try {
        response.sendError(HttpServletResponse.SC_NOT_FOUND,
            request.getRequestURI());
      } catch (IOException ioex) {
        throw new IllegalStateException(ex.getMessage(), ioex);
      }
    }
  },

  /**
   * The less css problem reporter.
   */
  LESS_CSS {
    /**
     * The lessCss template.
     */
    private String lessCssTemplate = template("lesscss.html");

    @Override
    public boolean apply(final RuntimeException ex) {
      return ex instanceof LessRuntimeException;
    }

    @Override
    public void report(final RuntimeException ex,
        final HttpServletRequest request,
        final HttpServletResponse response) {
      LessRuntimeException lessException = (LessRuntimeException) ex;
      LessCssError error = lessException.getLessCssError();
      Map<String, Object> model = new HashMap<String, Object>();
      model.put("lang", "css");
      model.put("firstLine", Math.max(1, error.getLine() - 1));
      model.put("line", error.getLine());
      model.put("column", error.getColumn());
      model.put("reason", error.getMessage());
      model.put("evidence", Joiner.on("\n").join(error.getExtract()));
      model.put("filename", error.getFilename());
      write(response, merge(lessCssTemplate, model));
    }
  },

  /**
   * Handle jsHint, jsLint and cssLint errors.
   */
  LINT {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean apply(final RuntimeException ex) {
      return ex instanceof RuntimeLinterException;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void report(final RuntimeException ex,
        final HttpServletRequest request,
        final HttpServletResponse response) {
      write(response, lintHtml((RuntimeLinterException) ex));
    }

    /**
     * Transform a lint exception to HTML.
     *
     * @param ex The lint exception.
     * @return The HTMl content.
     */
    private String lintHtml(final RuntimeLinterException ex) {
      List<Map<String, Object>> errors = new ArrayList<Map<String, Object>>();
      String lang;
      String tool;
      if (ex.getJsErrors() != null) {
        lang = "js";
        tool = "JSHint/JSLint";
        for (LinterError error : ex.getJsErrors()) {
          if (error != null) {
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("line", error.getLine());
            model.put("column", error.getCharacter());
            model.put("reason", error.getReason());
            model.put("lang", "js");
            errors.add(model);
          }
        }
      } else {
        lang = "css";
        tool = "CSSLint";
        String reason = "%s. %s Affected browsers: '%s'";
        for (CssLintError error : ex.getCssErrors()) {
          if (error != null) {
            Map<String, Object> model = new HashMap<String, Object>();
            CssRule rule = error.getRule();
            model.put("line", error.getLine());
            model.put("column", error.getCol());
            model.put("reason", String.format(reason, rule.getName(),
                error.getMessage(), rule.getBrowsers().toLowerCase()));
            model.put("lang", "css");
            errors.add(model);
          }
        }
      }
      return lintHtml(tool, lang, ex.getFilename(), ex.getOptions(),
          ex.getContent(), errors);
    }
  };

  /**
   * The lint error template.
   */
  private String lintErrorTemplate = template("lint-error.html");

  /**
   * The lint template.
   */
  private String lintTemplate = template("lint.html");

  /**
   * True if the exception can be handled.
   *
   * @param ex The candidate exception.
   * @return True if the exception can be handled.
   */
  public abstract boolean apply(final RuntimeException ex);

  /**
   * Produce a HTMl content.
   *
   * @param tool The tool: JSHint, JSLint or CSSLint.
   * @param lang The language: js or css.
   * @param filename The file's name.
   * @param options The lint's options.
   * @param content The file's content.
   * @param errors The lint's errors.
   * @return A HTML content.
   */
  protected String lintHtml(final String tool, final String lang,
      final String filename, final String[] options, final String content,
      final List<Map<String, Object>> errors) {
    List<String> evidence =
        Lists.newArrayList(Splitter.on("\n").split(content));
    StringBuilder errorList = new StringBuilder();
    for (Map<String, Object> error : errors) {
      int line = (Integer) error.get("line") - 1;
      int from = Math.max(0, line - 1);
      int to = Math.min(evidence.size(), line + 2);
      error.put("firstLine", from + 1);
      error.put("evidence", Joiner.on("\n").join(evidence.subList(from, to)));
      error.put("lang", lang);
      error.put("filename", filename);
      errorList.append(merge(lintErrorTemplate, error));
    }
    Map<String, Object> model = new HashMap<String, Object>();
    model.put("options", Joiner.on(", ").join(options));
    model.put("tool", tool);
    model.put("errors", errorList);
    return merge(lintTemplate, model);
  }

  /**
   * Write the given content into the HTTP response.
   *
   * @param response The HTTP response.
   * @param content The content.
   */
  protected void write(final HttpServletResponse response,
      final String content) {
    PrintWriter writer = null;
    try {
      writer = response.getWriter();
      writer.println(content);
    } catch (IOException ioex) {
      throw new IllegalStateException("Unable to write report", ioex);
    } finally {
      IOUtils.closeQuietly(writer);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Merge the model into the template.
   *
   * @param template The template.
   * @param model The model.
   * @return The merged template.
   */
  protected String merge(final String template,
      final Map<String, Object> model) {
    String result = template;
    for (Entry<String, Object> entry : model.entrySet()) {
      result =
          result.replace("${" + entry.getKey() + "}",
              entry.getValue().toString());
    }
    return result;
  }

  /**
   * Report the given exception.
   *
   * @param ex The exception.
   * @param request The HTTP request.
   * @param response The HTTP response.
   */
  public abstract void report(RuntimeException ex, HttpServletRequest request,
      HttpServletResponse response);

  /**
   * Load a template by it's name.
   *
   * @param name The template's name.
   * @return The template content.
   */
  protected String template(final String name) {
    InputStream in = null;
    try {
      in = getClass().getResourceAsStream(name);
      return IOUtils.toString(in);
    } catch (IOException ex) {
      throw new IllegalStateException("Unable to read: " + name, ex);
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  /**
   * Return the most appropiated problem reporter.
   *
   * @param ex The candidate exception.
   * @return The most appropiated problem reporter.
   */
  public static WroProblemReporter bestFor(final RuntimeException ex) {
    WroProblemReporter[] problemReporters = values();
    int best = -1;
    for (int i = 0; i < problemReporters.length; i++) {
      if (problemReporters[i].apply(ex)) {
        best = Math.max(i, best);
      }
    }
    return best >= 0 ? problemReporters[best] : DEFAULT;
  }
}
