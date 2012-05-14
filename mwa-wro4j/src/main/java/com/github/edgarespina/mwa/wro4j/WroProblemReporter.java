package com.github.edgarespina.mwa.wro4j;

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
      write(response, html((RuntimeLinterException) ex));
    }

    /**
     * Transform a lint exception to HTML.
     *
     * @param ex The lint exception.
     * @return The HTMl content.
     */
    private String html(final RuntimeLinterException ex) {
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
        for (CssLintError error : ex.getCssErrors()) {
          if (error != null) {
            Map<String, Object> model = new HashMap<String, Object>();
            CssRule rule = error.getRule();
            model.put("line", error.getLine());
            model.put("column", error.getCol());
            model.put("reason", error.getMessage() + " Type: " + rule.getName()
                + ". Browsers: " + rule.getBrowsers());
            model.put("lang", "css");
            errors.add(model);
          }
        }
      }
      return html(tool, lang, ex.getFilename(), ex.getOptions(),
          ex.getContent(), errors);
    }
  };

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
  protected String html(final String tool, final String lang,
      final String filename, final String[] options, final String content,
      final List<Map<String, Object>> errors) {
    String errorTemplate = template("linter-error.html");
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
      errorList.append(merge(errorTemplate, error));
    }
    String htmlTemplate = template("linter.html");
    Map<String, Object> model = new HashMap<String, Object>();
    model.put("options", Joiner.on(", ").join(options));
    model.put("tool", tool);
    model.put("errors", errorList);
    return merge(htmlTemplate, model);
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
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    } catch (IOException ioex) {
      throw new IllegalStateException("Unable to write report", ioex);
    } finally {
      IOUtils.closeQuietly(writer);
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
