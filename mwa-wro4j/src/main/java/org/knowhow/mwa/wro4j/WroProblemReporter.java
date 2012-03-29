package org.knowhow.mwa.wro4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.UniqueTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.extensions.processor.support.linter.LinterError;
import ro.isdc.wro.extensions.processor.support.linter.LinterException;

/**
 * Wro's problem reporter.
 *
 * @author edgar.espina
 * @since 0.1
 */
public enum WroProblemReporter {

  /**
   * Send a 404 error.
   */
  DEFAULT() {
    @Override
    public void report(final WroRuntimeException ex,
        final HttpServletRequest request,
        final HttpServletResponse response) throws IOException {
      Throwable cause = ex.getCause();
      String requestURI =
          request.getRequestURI().replace(request.getContextPath(), "");
      if (cause instanceof JavaScriptException && requestURI.endsWith(".css")) {
        // Catch less, css errors.
        ServletContext context = Context.get().getServletContext();
        String content =
            IOUtils.toString(context.getResourceAsStream(requestURI));
        JavaScriptException jsEx = (JavaScriptException) cause;
        ScriptableObject error = (ScriptableObject) jsEx.getValue();
        Number line = (Number) ScriptableObject.getProperty(error, "line");
        Object reason = ScriptableObject.getProperty(error, "message");
        String evidence = content.split("\n")[line.intValue() - 1];
        PrintWriter writer = response.getWriter();
        writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>LessCss errors</title>");
        writer.println("</head>");
        writer.println("<body style=\"background-color: #FDDFDE\">");
        writer.println("<h4>Errors:</h4>");
        writer.println("<ul>");
        StringBuilder report = new StringBuilder();
        report.append("LessCss Errors:\n");
        report.append("  1 problem found in ").append(requestURI).append("\n");
        writer.println("<li style=\"color: #404040\">");
        writer.println("<p>");
        writer.println("<span style=\"color: #4183C4\">Line");
        writer.println(line.intValue());
        report.append("    Line ").append(line.intValue()).append(": ");
        writer.println(": </span>");
        writer.println("<code>");
        writer.println(evidence);
        report.append(evidence).append("\n");
        writer.println("</code>");
        writer.println("</p>");
        writer.println("<p>");
        writer.println(reason);
        report.append("    ").append(reason).append("\n\n");
        writer.println("</p>");
        writer.println("</li>");
        writer.println("</ul>");
        writer.println("</body>");
        writer.println("</html>");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        logger.error(report.toString());
      } else {
        response.sendError(HttpServletResponse.SC_NOT_FOUND,
            request.getRequestURI());
      }
    }
  },

  /**
   * Send a 404 error.
   */
  GROUP_NOT_FOUND() {
    @Override
    public void report(final WroRuntimeException ex,
        final HttpServletRequest request,
        final HttpServletResponse response) throws IOException {
      DEFAULT.report(ex, request, response);
    }
  },

  /**
   * Send a 500 error and print linter errors as HTML.
   */
  LESS_CSS() {
    @Override
    public void report(final WroRuntimeException ex,
        final HttpServletRequest request,
        final HttpServletResponse response) throws IOException {
      JavaScriptException jsEx = (JavaScriptException) ex.getCause();
      ScriptableObject error = (ScriptableObject) jsEx.getValue();
      LinterError linterError = new LinterError();
      Number line = readProperty(error, "line", -1);
      linterError.setLine(line.intValue());
      linterError.setReason((String) readProperty(error, "message", ""));
      String evidence = readProperty(error, "extract", "");
      if (line.intValue() >= 0) {
        String requestURI =
            request.getRequestURI().replace(request.getContextPath(), "");
        // Catch less, css errors.
        ServletContext context = Context.get().getServletContext();
        try {
          String content =
              IOUtils.toString(context.getResourceAsStream(requestURI));
          evidence = content.split("\n")[linterError.getLine() - 1];
        } catch (Exception ex2) {
          // don't override evince use the default one, which probably is not
          // good enough :S
          logger.trace("Unable to read: " + requestURI, ex2);
        }
      }
      linterError.setEvidence(evidence);
      html(Arrays.asList(linterError), "LessCSS Errors", request, response);
    }

    /**
     * Parse a Rhino JS property.
     *
     * @param error The error object.
     * @param property The property name.
     * @param defaultValue The default value.
     * @param <T> The value type.
     * @return A Rhino JS value.
     */
    @SuppressWarnings("unchecked")
    private <T> T readProperty(final ScriptableObject error,
        final String property, final Object defaultValue) {
      Object value = ScriptableObject.getProperty(error, property);
      if (value instanceof UniqueTag) {
        return (T) defaultValue;
      } else if (value instanceof NativeArray) {
        // fallback and return a String
        return (T) toString((NativeArray) value);
      }
      return (T) (value == null ? defaultValue : value);
    }

    /**
     * Transform the array to a string.
     *
     * @param array The array.
     * @return The string version.
     */
    private String toString(final NativeArray array) {
      StringBuilder buffer = new StringBuilder();
      String sep = "\n";
      for (int i = 0; i < array.getLength(); i++) {
        final Object value = ScriptableObject.getProperty(array, i);
        buffer.append(value).append(sep);
      }
      buffer.setLength(buffer.length() - sep.length());
      return buffer.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean match(final String uri, final WroRuntimeException ex) {
      return (uri.endsWith(".css") || uri.endsWith(".less"))
          && ex.getCause() instanceof JavaScriptException;
    }
  },
  /**
   * Send a 500 error and print linter errors as HTML.
   */
  LINTER() {

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean match(final String uri, final WroRuntimeException ex) {
      return ex.getCause() instanceof LinterException;
    }

    @Override
    public void report(final WroRuntimeException ex,
        final HttpServletRequest request,
        final HttpServletResponse response) throws IOException {
      LinterException linterEx = (LinterException) ex.getCause();
      html(linterEx.getErrors(), "JavaScript Errors", request, response);
    }
  };

  /**
   * The logging system.
   */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * Creates a {@link WroProblemReporter}.
   */
  private WroProblemReporter() {
  }

  /**
   * True if there is a problem reporter for the matching problem.
   *
   * @param uri The request URI.
   * @param ex The problem detected.
   * @return True if there is a problem reporter for the matching problem.
   */
  public final boolean match(final String uri, final Exception ex) {
    if (ex instanceof WroRuntimeException) {
      return match(uri, (WroRuntimeException) ex);
    }
    return false;
  }

  /**
   * True if there is a problem reporter for the matching problem.
   *
   * @param uri The request URI.
   * @param ex The problem detected.
   * @return True if there is a problem reporter for the matching problem.
   */
  protected boolean match(final String uri, final WroRuntimeException ex) {
    return true;
  }

  /**
   * Print a HTML report on the response.
   *
   * @param errors The collection of errors.
   * @param title The report's title.
   * @param request The servlet request.
   * @param response The servlet response.
   * @throws IOException If the response cannot be send.
   */
  protected void html(final Collection<LinterError> errors, final String title,
      final HttpServletRequest request, final HttpServletResponse response)
      throws IOException {
    PrintWriter writer = response.getWriter();
    writer.println("<html>");
    writer.println("<head>");
    writer.println("<title>");
    writer.println(title);
    writer.println("</title>");
    writer.println("</head>");
    writer.println("<body style=\"background-color: #FDDFDE\">");
    writer.println("<h4>");
    writer.println(title);
    writer.println(":</h4>");
    writer.println("<ul>");
    StringBuilder report = new StringBuilder();
    report.append(title).append(":\n");
    report.append("  ").append(errors.size()).append(" problem(s) found in ")
        .append("\"").append(request.getRequestURI()).append("\"\n");
    for (LinterError error : errors) {
      writer.println("<li style=\"color: #404040\">");
      writer.println("<p>");
      writer.println("<span style=\"color: #4183C4\">Line");
      writer.println(error.getLine());
      report.append("    Line ").append(error.getLine()).append(": ");
      writer.println(":</span>");
      writer.println("<code>");
      writer.println(error.getEvidence());
      report.append(error.getEvidence()).append("\n");
      writer.println("</code>");
      writer.println("</p>");
      writer.println("<p>");
      writer.println(error.getReason());
      report.append("    ").append(error.getReason()).append("\n\n");
      writer.println("</p>");
      writer.println("</li>");
    }
    writer.println("</ul>");
    writer.println("</body>");
    writer.println("</html>");
    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    logger.error(report.toString());
  }

  /**
   * Lookup for a problem reporter for the given error's type.
   *
   * @param uri The resource's uri type. Required.
   * @param ex The exception.
   * @return A problem's reporter or null if notFound.
   */
  public static WroProblemReporter bestFor(final String uri,
      final Exception ex) {
    Validate.notNull(uri, "The resource's uri is required.");
    WroProblemReporter[] problemReporters = values();
    int best = -1;
    for (int i = 0; i < problemReporters.length; i++) {
      if (problemReporters[i].match(uri, ex)) {
        best = Math.max(i, best);
      }
    }
    return best >= 0 ? problemReporters[best] : null;
  }

  /**
   * Report a {@link WroRuntimeException} error.
   *
   * @param ex The error.
   * @param request The request.
   * @param response The response.
   * @throws IOException If the response cannot be send.
   */
  public abstract void report(WroRuntimeException ex,
      HttpServletRequest request,
      HttpServletResponse response) throws IOException;
}
