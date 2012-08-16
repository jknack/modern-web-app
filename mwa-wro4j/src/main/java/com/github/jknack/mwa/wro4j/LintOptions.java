package com.github.jknack.mwa.wro4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * Configure JSHint/JSLint/CSSLint options.
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
   * Max number of chars per line.
   */
  private static final int MAX_LEN = 80;

  /**
   * The option list.
   */
  private final Map<String, Object> options = new HashMap<String, Object>();

  /**
   * Predefined/global variables.
   */
  private final List<String> predefined = new ArrayList<String>();

  /**
   * Use one of the static method.
   */
  private LintOptions() {
  }

  /**
   * Creates a defaults JS Hint/Lint options. After creation you can safely add
   * new options.
   *
   * @return A new options.
   */
  public static LintOptions jsDefaults() {
    return new LintOptions()
        .option("maxerr", MAX_ERRORS)
        .option("browser")
        .option("jquery")
        .option("devel")
        .option("globalstrict", false)
        .option("strict", false)
        .option("forin")
        .option("noarg")
        .option("noempty")
        .option("eqeqeq")
        .option("bitwise")
        .option("undef")
        .option("curly")
        .option("indent", 2)
        .option("maxlen", MAX_LEN);
  }

  /**
   * <p>
   * Available rules:
   * </p>
   * <ul>
   * <li>import: Don't use @import, use &lt;link&gt; instead.
   * <li>adjoining-classes: Don't use adjoining classes.
   * <li>important: Be careful when using !important declaration.
   * <li>box-sizing: The box-sizing properties isn't supported in IE6 and IE7.
   * <li>box-model: Don't use width or height when using padding or border.
   * <li>known-properties: Properties should be known (listed in CSS
   * specification) or be a vendor-prefixed property.
   * <li>duplicate-background-images: Every background-image should be unique.
   * Use a common class for e.g. sprites.
   * <li>compatible-vendor-prefixes: Include all compatible vendor prefixes to
   * reach a wider range of users.
   * <li>display-property-grouping: Certain properties shouldn't be used with
   * certain display property values.
   * <li>overqualified-elements: Don't use classes or IDs with elements (a.foo
   * or a#foo).
   * <li>fallback-colors: For older browsers that don't support RGBA, HSL, or
   * HSLA, provide a fallback color.
   * <li>duplicate-properties: Duplicate properties must appear one after the
   * other.
   * <li>empty-rules: Rules without any properties specified should be removed.
   * <li>errors: This rule looks for recoverable syntax errors.
   * <li>rules-count: Track how many rules there are.
   * <li>ids: Selectors should not contain IDs.
   * <li>font-sizes: Checks the number of font-size declarations.
   * <li>font-faces: Too many different web fonts in the same stylesheet.
   * <li>gradients: When using a vendor-prefixed gradient, make sure to use them
   * all.
   * <li>floats: This rule tests if the float property is used too many times
   * <li>outline-none: Use of outline: none or outline: 0 should be limited to
   * :focus rules.
   * <li>qualified-headings: Headings should not be qualified (namespaced).
   * <li>regex-selectors: Selectors that look like regular expressions are slow
   * and should be avoided.
   * <li>shorthand: Use shorthand properties where possible.
   * <li>text-indent: Checks for text indent less than -99px.
   * <li>unique-headings: Headings should be defined only once.
   * <li>universal-selector: The universal selector (*) is known to be slow.
   * <li>unqualified-attributes: Unqualified attribute selectors are known to be
   * slow.
   * <li>vendor-prefix: When using a vendor-prefixed property, make sure to
   * include the standard one.
   * <li>zero-units: You don't need to specify units when a value is 0.
   * </ul>
   *
   * @return A new options.
   */
  public static LintOptions cssDefaults() {
    return new LintOptions()
        .option("important")
        .option("box-sizing")
        .option("box-model")
        .option("known-properties")
        .option("duplicate-background-images")
        .option("compatible-vendor-prefixes")
        .option("display-property-grouping")
        .option("overqualified-elements")
        .option("fallback-colors")
        .option("duplicate-properties")
        .option("empty-rules")
        .option("errors")
        .option("ids")
        .option("font-sizes")
        .option("font-faces")
        .option("gradients")
        .option("floats")
        .option("outline-none")
        .option("qualified-headings")
        .option("regex-selectors")
        .option("shorthand")
        .option("text-indent")
        .option("unique-headings")
        .option("universal-selector")
        .option("unqualified-attributes")
        .option("vendor-prefix")
        .option("zero-units");
  }

  /**
   * Creates a new {@link LintOptions}. After creation you can safely add
   * new options.
   *
   * @param names A list of option's names.
   * @return A new {@link LintOptions}.
   */
  public static LintOptions options(final String... names) {
    return new LintOptions().option(names);
  }

  /**
   * Enable all the given options.
   *
   * @param names The option's name. Required.
   * @return This options.
   */
  public LintOptions option(final String... names) {
    Validate.notEmpty(names, "The option's name is required.");
    for (String name : names) {
      options.put(name, true);
    }
    return this;
  }

  /**
   * Enable or disable the given option.
   *
   * @param name The option's name. Required.
   * @param value The option's value. Required.
   * @return This options.
   */
  public LintOptions option(final String name, final boolean value) {
    Validate.notEmpty(name, "The option's name is required.");
    options.put(name, value);
    return this;
  }

  /**
   * Enable the given option.
   *
   * @param name The option's name. Required.
   * @param value The option's value. Required.
   * @return This options.
   */
  public LintOptions option(final String name, final int value) {
    Validate.notEmpty(name, "The option's name is required.");
    options.put(name, value);
    return this;
  }

  /**
   * Enable the given option.
   *
   * @param name The option's name. Required.
   * @param value The option's value. Required.
   * @return This options.
   */
  public LintOptions option(final String name, final String value) {
    Validate.notEmpty(name, "The option's name is required.");
    Validate.notEmpty(value, "The option's value is required.");
    options.put(name, value);
    return this;
  }

  /**
   * Append a new global/predefined variable.
   *
   * @param names The variable's name, like: jquery, $, etc.. Required.
   * @return This options.
   */
  public LintOptions predefined(final String... names) {
    Validate.notEmpty(names, "The global variable's name is required.");
    for (String name : names) {
      predefined.add("'" + name + "'");
    }
    return this;
  }

  /**
   * Publish all the options as String array.
   *
   * @return The string array required by JS Lint/Hint.
   */
  /* package */String[] build() {
    List<String> options = new ArrayList<String>(this.options.size());
    for (Entry<String, Object> option : this.options.entrySet()) {
      if (Boolean.valueOf(option.getValue().toString()) == Boolean.TRUE) {
        options.add(option.getKey());
      } else {
        options.add(option.getKey() + "=" + option.getValue());
      }
    }
    if (predefined.size() > 0) {
      options.add(
          "predef=[" + StringUtils.join(predefined, ",") + "]");
    }
    return options.toArray(new String[options.size()]);
  }

  @Override
  public String toString() {
    return StringUtils.join(build(), ";");
  }
}
