package com.github.edgarespina.mwa.wro4j;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.support.csslint.CssLintError;
import ro.isdc.wro.extensions.processor.support.linter.LinterError;

/**
 * Convert linter exception as {@link WroRuntimeException}.
 *
 * @author edgar.espina
 * @since 0.1.2
 */
final class RuntimeLinterException extends WroRuntimeException {

  /**
   * The default serial UID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The list of JS errors.
   */
  private LinterError[] jsErrors;

  /**
   * The list of CSS errors.
   */
  private CssLintError[] cssErrors;

  /**
   * The file's content.
   */
  private String content;

  /**
   * The file's name.
   */
  private String filename;

  /**
   * The lint's options.
   */
  private String[] options;

  /**
   * Creates a new {@link RuntimeLinterException}.
   *
   * @param filename The file's name.
   * @param content The file's content.
   * @param options The lint's options.
   * @param errors The lint's errors.
   */
  public RuntimeLinterException(final String filename, final String content,
      final String[] options, final LinterError[] errors) {
    super(errors.toString());
    this.filename = filename;
    this.content = content;
    this.jsErrors = errors;
    this.options = options;
  }

  /**
   * Creates a new {@link RuntimeLinterException}.
   *
   * @param filename The file's name.
   * @param content The file's content.
   * @param options The lint's options.
   * @param errors The lint's errors.
   */
  public RuntimeLinterException(final String filename, final String content,
      final String[] options, final CssLintError[] errors) {
    super(errors.toString());
    this.filename = filename;
    this.content = content;
    this.cssErrors = errors;
    this.options = options;
  }

  /**
   * The lint's options.
   *
   * @return The lint's options.
   */
  public String[] getOptions() {
    return options;
  }

  /**
   * The js errors.
   *
   * @return The js errors.
   */
  public LinterError[] getJsErrors() {
    return jsErrors;
  }

  /**
   * The css errors.
   *
   * @return The css errors.
   */
  public CssLintError[] getCssErrors() {
    return cssErrors;
  }

  /**
   * The file's name.
   *
   * @return The file's name.
   */
  public String getFilename() {
    return filename;
  }

  /**
   * The file's content.
   *
   * @return The file's content.
   */
  public String getContent() {
    return content;
  }
}
