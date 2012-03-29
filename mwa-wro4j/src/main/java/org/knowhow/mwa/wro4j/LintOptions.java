package org.knowhow.mwa.wro4j;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * Configure JSHint/JSLint options.
 *
 * @author edgar.espina
 * @since 0.1
 */
public final class LintOptions {

  /**
   * Default number of errors.
   */
  private static final int MAX_ERRORS = 50;

  /**
   * The option list.
   */
  private final List<String> options = new ArrayList<String>();

  /**
   * Predefined/global variables.
   */
  private final List<String> predefined = new ArrayList<String>();

  /**
   * Use {@link #defaults()}.
   */
  private LintOptions() {
  }

  /**
   * Creates a defaults JS Hint/Lint options.
   *
   * @return A new options.
   */
  public static LintOptions defaults() {
    return new LintOptions()
        .option("forin").option("noarg").option("noempty").option("eqeqeq")
        .option("bitwise").option("strict", false).option("undef")
        .option("curly").option("browser").option("jquery").option("devel")
        .option("nomen", false).option("indent", 2).option("maxerr", MAX_ERRORS)
        .globalVar("define").globalVar("jquery").globalVar("$").globalVar("_")
        .globalVar("contextPath");
  }

  /**
   * Append a new option.
   *
   * @param name The option's name. Required.
   * @return This options.
   */
  public LintOptions option(final String name) {
    Validate.notEmpty(name, "The option's name is required.");
    options.add(name);
    return this;
  }

  /**
   * Append a new option.
   *
   * @param name The option's name. Required.
   * @param value The option's value. Required.
   * @return This options.
   */
  public LintOptions option(final String name, final boolean value) {
    Validate.notEmpty(name, "The option's name is required.");
    options.add(name + "=" + value);
    return this;
  }

  /**
   * Append a new option.
   *
   * @param name The option's name. Required.
   * @param value The option's value. Required.
   * @return This options.
   */
  public LintOptions option(final String name, final int value) {
    Validate.notEmpty(name, "The option's name is required.");
    options.add(name + "=" + value);
    return this;
  }

  /**
   * Append a new option.
   *
   * @param name The option's name. Required.
   * @param value The option's value. Required.
   * @return This options.
   */
  public LintOptions option(final String name, final String value) {
    Validate.notEmpty(name, "The option's name is required.");
    Validate.notEmpty(value, "The option's value is required.");
    options.add(name + "=" + value);
    return this;
  }

  /**
   * Append a new global variable.
   *
   * @param name The variable's name, like: jquery, $, etc.. Required.
   * @return This options.
   */
  public LintOptions globalVar(final String name) {
    Validate.notEmpty(name, "The global variable's name is required.");
    predefined.add("'" + name + "'");
    return this;
  }

  /**
   * Publish all the options as String array.
   *
   * @return The string array required by JS Lint/Hint.
   */
  /* package */String[] build() {
    String[] options =
        this.options.toArray(new String[this.options.size() + 1]);
    options[options.length - 1] =
        "predef=[" + StringUtils.join(predefined, ",") + "]";
    return options;
  }

  @Override
  public String toString() {
    return StringUtils.join(build(), ";");
  }
}
