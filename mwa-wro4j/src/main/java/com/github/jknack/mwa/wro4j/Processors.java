package com.github.jknack.mwa.wro4j;

import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;

import ro.isdc.wro.extensions.processor.css.SassCssProcessor;
import ro.isdc.wro.extensions.processor.css.YUICssCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.BeautifyJsProcessor;
import ro.isdc.wro.extensions.processor.js.CJsonProcessor;
import ro.isdc.wro.extensions.processor.js.CoffeeScriptProcessor;
import ro.isdc.wro.extensions.processor.js.DojoShrinksafeCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.DustJsProcessor;
import ro.isdc.wro.extensions.processor.js.GoogleClosureCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.HandlebarsJsProcessor;
import ro.isdc.wro.extensions.processor.js.JsonHPackProcessor;
import ro.isdc.wro.extensions.processor.js.PackerJsProcessor;
import ro.isdc.wro.extensions.processor.js.UglifyJsProcessor;
import ro.isdc.wro.extensions.processor.js.YUIJsCompressorProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.decorator.CopyrightKeeperProcessorDecorator;
import ro.isdc.wro.model.resource.processor.impl.css.ConformColorsCssProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssCompressorProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssDataUriPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.DuplicatesAwareCssDataUriPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.JawrCssMinifierProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.VariablizeColorsCssProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.SemicolonAppenderPreProcessor;
import ro.isdc.wro.model.resource.processor.support.CssCompressor;

import com.github.jknack.mwa.IApplicationMode;
import com.github.jknack.mwa.wro4j.ConditionalProcessor.Condition;
import com.github.jknack.mwa.wro4j.requirejs.RequireJsProcessor;
import com.google.javascript.jscomp.CompilationLevel;

/**
 * A factory class for {@link ResourcePreProcessor} and
 * {@link ResourcePostProcessor}.
 *
 * @author edgar.espina
 * @since 0.1.2
 */
public final class Processors {

  /**
   * Return true if the application is running in dev mode.
   */
  public static final Condition DEV = new Condition() {
    @Override
    public boolean process(final IApplicationMode mode, final Environment environment) {
      return mode.isDev();
    }
  };

  /**
   * Return true if the application is running in no-dev mode.
   */
  public static final Condition NO_DEV = new Condition() {
    @Override
    public boolean process(final IApplicationMode mode, final Environment environment) {
      return !mode.isDev();
    }
  };

  /**
   * Not allowed.
   */
  private Processors() {
  }

  /**
   * Rewrites background images url of the provided css content. Applied only on
   * css resources.
   *
   * @return A new {@link CssUrlRewritingProcessor}.
   * @see CssUrlRewritingProcessor.
   */
  public static CssUrlRewritingProcessor cssUrlRewriting() {
    return new CssUrlRewritingProcessor();
  }

  /**
   * Responsible for handling css @import statements. This should be used as a
   * pre processor and will be applied only on resources of type CSS. As a
   * result of processing, import statements will be removed and the processed
   * css file will contain a merged content of all imports.
   *
   * @return A new {@link CssImportPreProcessor}.
   * @see CssImportPreProcessor.
   */
  public static CssImportPreProcessor cssImport() {
    return new CssImportPreProcessor();
  }

  /**
   * <p>
   * The css variables support is achieved by the processor named
   * CssVariablesProcessor . As its name states, this processor works only with
   * CSS resources and it does the following:
   * </p>
   * <ul>
   * <li>parse the css and search for variables declaration (@variables token)
   * and variable usage.
   * <li>replace all declared variables with declared variable values.
   * <li>remove variable declaration, making the result a valid css.
   * </ul>
   * <p>
   * The processor can be used as both: pre processor & post processor.
   * </p>
   * <ul>
   * <li>When used as a pre processor, the scope of the declared variables is
   * reduced to a single css file.
   * <li>When used as a post processor, the scope of the declared variables is
   * the 'group' level (all css resources from the processed group).
   * </ul>
   *
   * @return A new {@link CssVariablesProcessor}.
   * @see CssVariablesProcessor.
   */
  public static CssVariablesProcessor cssVariables() {
    return new CssVariablesProcessor();
  }

  /**
   * A processor implementation using {@link CssCompressor} algorithm. This
   * processor can be used as both: PreProcessor & postProcessor.
   *
   * @return A new {@link CssCompressorProcessor}.
   */
  public static ResourcePostProcessor cssCompressor() {
    return new ConditionalProcessor(new CssCompressorProcessor(), NO_DEV);
  }

  /**
   * Adds a semicolon (';') character to the end of each js file if it is
   * missing, in order to avoid any problems after js resources are merged.
   *
   * @return A new {@link SemicolonAppenderPreProcessor}.
   */
  public static SemicolonAppenderPreProcessor semicolonAppender() {
    return new SemicolonAppenderPreProcessor();
  }

  /**
   * Rewrites background images by replacing the url with data uri of the image.
   * For more details see <a
   * href="http://code.google.com/p/wro4j/wiki/Base64DataUriSupport"
   * >Base64DataUriSupport</a>
   *
   * @return A new {@link CssDataUriPreProcessor}
   */
  public static CssDataUriPreProcessor cssDataUri() {
    return new CssDataUriPreProcessor();
  }

  /**
   * Similar to cssDataUri, but doesn't replace with dataUri when the same image
   * is used more than once.
   *
   * @return A new {@link DuplicatesAwareCssDataUriPreProcessor}.
   */
  public static DuplicatesAwareCssDataUriPreProcessor
      duplicatesAwareCssDataUri() {
    return new DuplicatesAwareCssDataUriPreProcessor();
  }

  /**
   * Css minimizer. The implementation is taken from jawr framework.
   *
   * @return A new {@link JawrCssMinifierProcessor}.
   */
  public static ResourcePostProcessor jawrCssMinifier() {
    return new ConditionalProcessor(new JawrCssMinifierProcessor(), NO_DEV);
  }

  /**
   * A simple css minimizer, which removes comments and unnecessary whitespaces.
   *
   * @return A new {@link CssMinProcessor}.
   */
  public static ResourcePostProcessor cssMinProcessor() {
    return new ConditionalProcessor(new CssMinProcessor(), NO_DEV);
  }

  /**
   * Use JSMin utility for js compression.
   *
   * @return A new {@link JSMinProcessor}.
   */
  public static ResourcePostProcessor jsMinProcessor() {
    return new ConditionalProcessor(new JSMinProcessor(), NO_DEV);
  }

  /**
   * Transforms named colors from css to #rgb format.
   *
   * @return A new {@link ConformColorsCssProcessor}.
   */
  public static ConformColorsCssProcessor conformColorsCss() {
    return new ConformColorsCssProcessor();
  }

  /**
   * Extracts all the colors used in css add details about how many times it was
   * used.
   *
   * @return A new {@link VariablizeColorsCssProcessor}.
   */
  public static VariablizeColorsCssProcessor variablizeColorsCss() {
    return new VariablizeColorsCssProcessor();
  }

  /**
   * A decorator for any processor. Inspects the resource for copyright
   * (license) header and inserts them back if the decorated processor removes
   * them.
   *
   * @param processor The processor to decorate. Required.
   * @return A new {@link CopyrightKeeperProcessorDecorator}.
   */
  public static CopyrightKeeperProcessorDecorator copyrightKeeper(
      final ResourcePreProcessor processor) {
    return CopyrightKeeperProcessorDecorator.decorate(processor);
  }

  /**
   * Parse a resource and search for placeholders of this type: ${} and replace
   * them with the values found in a map provided the by client.
   *
   * @param resolver The application environment. Required.
   * @return A {@link PropertyResolverProcessor}.
   */
  public static PropertyResolverProcessor propertyResolver(
      final PropertyResolver resolver) {
    return new PropertyResolverProcessor(resolver);
  }

  /**
   * PathMatcher implementation for Ant-style path patterns. Enforce decorated
   * processors to be applied only if the resource path matches one of the
   * patterns. Examples are provided below.
   * <p>
   * Part of this mapping code has been kindly borrowed from <a
   * href="http://ant.apache.org">Apache Ant</a>.
   * <p>
   * The mapping matches URLs using the following rules:<br>
   * <ul>
   * <li>? matches one character</li>
   * <li>* matches zero or more characters</li>
   * <li>** matches zero or more 'directories' in a path</li>
   * </ul>
   * <p>
   * Some examples:<br>
   * <ul>
   * <li><code>com/t?st.jsp</code> - matches <code>com/test.jsp</code> but also
   * <code>com/tast.jsp</code> or <code>com/txst.jsp</code></li>
   * <li><code>com/*.jsp</code> - matches all <code>.jsp</code> files in the
   * <code>com</code> directory</li>
   * <li><code>com/&#42;&#42;/test.jsp</code> - matches all
   * <code>test.jsp</code> files underneath the <code>com</code> path</li>
   * <li><code>org/springframework/&#42;&#42;/*.jsp</code> - matches all
   * <code>.jsp</code> files underneath the <code>org/springframework</code>
   * path</li>
   * <li><code>org/&#42;&#42;/servlet/bla.jsp</code> - matches
   * <code>org/springframework/servlet/bla.jsp</code> but also
   * <code>org/springframework/testing/servlet/bla.jsp</code> and
   * <code>org/servlet/bla.jsp</code></li>
   * </ul>
   *
   * @param processor The target processor. Required.
   * @param patterns A set of ant path patterns.
   * @return A {@link PropertyResolverProcessor}.
   */
  public static ResourcePreProcessor includes(
      final ResourcePreProcessor processor, final String... patterns) {
    return new PathPatternDecorator(processor, true, patterns);
  }

  /**
   * PathMatcher implementation for Ant-style path patterns. Enforce decorated
   * processors to be applied only if the resource path matches one of the
   * patterns. Examples are provided below.
   * <p>
   * Part of this mapping code has been kindly borrowed from <a
   * href="http://ant.apache.org">Apache Ant</a>.
   * <p>
   * The mapping matches URLs using the following rules:<br>
   * <ul>
   * <li>? matches one character</li>
   * <li>* matches zero or more characters</li>
   * <li>** matches zero or more 'directories' in a path</li>
   * </ul>
   * <p>
   * Some examples:<br>
   * <ul>
   * <li><code>com/t?st.jsp</code> - matches <code>com/test.jsp</code> but also
   * <code>com/tast.jsp</code> or <code>com/txst.jsp</code></li>
   * <li><code>com/*.jsp</code> - matches all <code>.jsp</code> files in the
   * <code>com</code> directory</li>
   * <li><code>com/&#42;&#42;/test.jsp</code> - matches all
   * <code>test.jsp</code> files underneath the <code>com</code> path</li>
   * <li><code>org/springframework/&#42;&#42;/*.jsp</code> - matches all
   * <code>.jsp</code> files underneath the <code>org/springframework</code>
   * path</li>
   * <li><code>org/&#42;&#42;/servlet/bla.jsp</code> - matches
   * <code>org/springframework/servlet/bla.jsp</code> but also
   * <code>org/springframework/testing/servlet/bla.jsp</code> and
   * <code>org/servlet/bla.jsp</code></li>
   * </ul>
   *
   * @param processor The target processor. Required.
   * @param patterns A set of ant path patterns.
   * @return A {@link PropertyResolverProcessor}.
   */
  public static ResourcePostProcessor includes(
      final ResourcePostProcessor processor, final String... patterns) {
    return new PathPatternDecorator(processor, true, patterns);
  }

  /**
   * PathMatcher implementation for Ant-style path patterns. Enforce decorated
   * processors to be applied only if the resource path matches one of the
   * patterns. Examples are provided below.
   * <p>
   * Part of this mapping code has been kindly borrowed from <a
   * href="http://ant.apache.org">Apache Ant</a>.
   * <p>
   * The mapping matches URLs using the following rules:<br>
   * <ul>
   * <li>? matches one character</li>
   * <li>* matches zero or more characters</li>
   * <li>** matches zero or more 'directories' in a path</li>
   * </ul>
   * <p>
   * Some examples:<br>
   * <ul>
   * <li><code>com/t?st.jsp</code> - matches <code>com/test.jsp</code> but also
   * <code>com/tast.jsp</code> or <code>com/txst.jsp</code></li>
   * <li><code>com/*.jsp</code> - matches all <code>.jsp</code> files in the
   * <code>com</code> directory</li>
   * <li><code>com/&#42;&#42;/test.jsp</code> - matches all
   * <code>test.jsp</code> files underneath the <code>com</code> path</li>
   * <li><code>org/springframework/&#42;&#42;/*.jsp</code> - matches all
   * <code>.jsp</code> files underneath the <code>org/springframework</code>
   * path</li>
   * <li><code>org/&#42;&#42;/servlet/bla.jsp</code> - matches
   * <code>org/springframework/servlet/bla.jsp</code> but also
   * <code>org/springframework/testing/servlet/bla.jsp</code> and
   * <code>org/servlet/bla.jsp</code></li>
   * </ul>
   *
   * @param processor The target processor. Required.
   * @param patterns A set of ant path patterns.
   * @return A {@link PropertyResolverProcessor}.
   */
  public static ResourcePreProcessor excludes(
      final ResourcePreProcessor processor, final String... patterns) {
    return new PathPatternDecorator(processor, false, patterns);
  }

  /**
   * PathMatcher implementation for Ant-style path patterns. Enforce decorated
   * processors to be applied only if the resource path matches one of the
   * patterns. Examples are provided below.
   * <p>
   * Part of this mapping code has been kindly borrowed from <a
   * href="http://ant.apache.org">Apache Ant</a>.
   * <p>
   * The mapping matches URLs using the following rules:<br>
   * <ul>
   * <li>? matches one character</li>
   * <li>* matches zero or more characters</li>
   * <li>** matches zero or more 'directories' in a path</li>
   * </ul>
   * <p>
   * Some examples:<br>
   * <ul>
   * <li><code>com/t?st.jsp</code> - matches <code>com/test.jsp</code> but also
   * <code>com/tast.jsp</code> or <code>com/txst.jsp</code></li>
   * <li><code>com/*.jsp</code> - matches all <code>.jsp</code> files in the
   * <code>com</code> directory</li>
   * <li><code>com/&#42;&#42;/test.jsp</code> - matches all
   * <code>test.jsp</code> files underneath the <code>com</code> path</li>
   * <li><code>org/springframework/&#42;&#42;/*.jsp</code> - matches all
   * <code>.jsp</code> files underneath the <code>org/springframework</code>
   * path</li>
   * <li><code>org/&#42;&#42;/servlet/bla.jsp</code> - matches
   * <code>org/springframework/servlet/bla.jsp</code> but also
   * <code>org/springframework/testing/servlet/bla.jsp</code> and
   * <code>org/servlet/bla.jsp</code></li>
   * </ul>
   *
   * @param processor The target processor. Required.
   * @param patterns A set of ant path patterns.
   * @return A {@link PropertyResolverProcessor}.
   */
  public static ResourcePostProcessor excludes(
      final ResourcePostProcessor processor, final String... patterns) {
    return new PathPatternDecorator(processor, false, patterns);
  }

  /**
   * Use YUI css compression utility for processing a css resource.
   *
   * @return A new {@link YUICssCompressorProcessor}.
   */
  public static ResourcePostProcessor yuiCssCompressor() {
    return new ConditionalProcessor(new YUICssCompressorProcessor(), NO_DEV);
  }

  /**
   * Use YUI js compression utility for processing js resources.
   *
   * @return A new {@link YUIJsCompressorProcessor}.
   */
  public static ResourcePostProcessor yuJsMin() {
    return new ConditionalProcessor(
        YUIJsCompressorProcessor.noMungeCompressor(), NO_DEV);
  }

  /**
   * Same as {@link #yuJsMin()} but applies variable rename for better
   * compression.
   *
   * @return A new {@link YUIJsCompressorProcessor}.
   */
  public static ResourcePostProcessor yuiJsMinAdvanced() {
    return new ConditionalProcessor(
        YUIJsCompressorProcessor.doMungeCompressor(), NO_DEV);
  }

  /**
   * Compresses javascript code using compressor implemented by Dojo Shrinksafe
   * utility.
   *
   * @return A new {@link DojoShrinksafeCompressorProcessor}.
   */
  public static ResourcePostProcessor dojoShrinksafeCompressor() {
    return new ConditionalProcessor(new DojoShrinksafeCompressorProcessor(),
        NO_DEV);
  }

  /**
   * Compress js using <a href="https://github.com/mishoo/UglifyJS">uglifyJs</a>
   * utility.
   *
   * @return A new {@link UglifyJsProcessor}.
   */
  public static UglifyJsProcessor uglifyJs() {
    return new UglifyJsProcessor();
  }

  /**
   * Perform a beautify operation on javascript by nicely formatting it.
   *
   * @return A new {@link BeautifyJsProcessor}.
   */
  public static BeautifyJsProcessor beautifyJs() {
    return new BeautifyJsProcessor();
  }

  /**
   * Uses <a href="http://dean.edwards.name/packer/">Dean Edwards packer
   * utility</a> to pack js resources.
   *
   * @return A new {@link PackerJsProcessor}.
   */
  public static PackerJsProcessor packerJs() {
    return new PackerJsProcessor();
  }

  /**
   * A processor using lessCss engine: <a
   * href="http://www.asual.com/lesscss/">lessCss</a>
   * <p>
   * The main css goodies are:
   * </p>
   * <ul>
   * <li>Variables - Variables allow you to specify widely used values in a
   * single place, and then re-use them throughout the style sheet, making
   * global changes as easy as changing one line of code.
   *
   * <pre>
   * brand_color: #4D926F;
   * #header { color: @brand_color; }
   * h2 { color: @brand_color; }
   * </pre>
   *
   * </li>
   * <li>Mixins - Mixins allow you to embed all the properties of a class into
   * another class by simply including the class name as one of its properties.
   * It's just like variables, but for whole classes. Mixins can also behave
   * like functions, and take arguments, as seen in the example bellow.
   *
   * <pre>
   *  .rounded_corners (@radius: 5px) {
   *   -moz-border-radius: @radius;
   *   -webkit-border-radius: @radius;
   *   border-radius: @radius;
   * }
   *
   * #header {
   *   .rounded_corners;
   * }
   *
   * #footer {
   *   .rounded_corners(10px);
   * }
   * </pre>
   *
   * </li>
   * <li>Nested Rules - Rather than constructing long selector names to specify
   * inheritance, in Less you can simply nest selectors inside other selectors.
   * This makes inheritance clear and style sheets shorter
   *
   * <pre>
   * #header {
   *   color: red;
   *   a {
   *     font-weight: bold;
   *     text-decoration: none;
   *   }
   * }
   * </pre>
   *
   * </li>
   * <li>Operations - Are some elements in your style sheet proportional to
   * other elements? Operations let you add, subtract, divide and multiply
   * property values and colors, giving you the power to do create complex
   * relationships between properties.
   *
   * <pre>
   * @the-border: 1px;
   * @base-color: #111;
   *
   * #header {
   *   color: @base-color * 3;
   *   border-left: @the-border;
   *   border-right: @the-border * 2;
   * }
   *
   * #footer {
   *   color: (@base-color + #111) * 1.5;
   * }
   * </pre>
   *
   * </li>
   * </ul>
   * <p>
   * If processing encounter any issues during processing, no change will be
   * applied to the resource.
   * <p/>
   *
   * @return A new {@link LessCssProcessor}.
   */
  public static ResourcePostProcessor lessCss() {
    return new LessCssProcessor();
  }

  /**
   * Transforms sass css code into vanilla css.
   *
   * @return A new {@link SassCssProcessor}.
   */
  public static SassCssProcessor sassCss() {
    return new SassCssProcessor();
  }

  /**
   * Compress javascript code with google closure compiler using
   * {@link CompilationLevel#SIMPLE_OPTIMIZATIONS}.
   *
   * @return A new {@link GoogleClosureCompressorProcessor}.
   */
  public static ResourcePostProcessor googleClosureSimple() {
    return new ConditionalProcessor(new GoogleClosureCompressorProcessor(
        CompilationLevel.SIMPLE_OPTIMIZATIONS), NO_DEV);
  }

  /**
   * Compress javascript code with google closure compiler using
   * {@link CompilationLevel#ADVANCED_OPTIMIZATIONS}.
   *
   * @return A new {@link GoogleClosureCompressorProcessor}.
   */
  public static ResourcePostProcessor googleClosureAdvanced() {
    return new ConditionalProcessor(new GoogleClosureCompressorProcessor(
        CompilationLevel.ADVANCED_OPTIMIZATIONS), NO_DEV);
  }

  /**
   * Uses coffee script to compile to javascript code.
   *
   * @return A new {@link CoffeeScriptProcessor}.
   */
  public static CoffeeScriptProcessor coffeeScript() {
    return new CoffeeScriptProcessor();
  }

  /**
   * Compress JSON objects using CJson algorithm.
   *
   * @return A new {@link CJsonProcessor}.
   */
  public static CJsonProcessor cJsonPack() {
    return new CJsonProcessor(true);
  }

  /**
   * Uncompress JSON objects previously compressed with CJson algorithm.
   *
   * @return A new {@link CJsonProcessor}.
   */
  public static CJsonProcessor cJsonUnpack() {
    return new CJsonProcessor(false);
  }

  /**
   * Compress JSON objects using HPack algorithm.
   *
   * @return A new {@link CJsonProcessor}.
   */
  public static JsonHPackProcessor jsonhPack() {
    return new JsonHPackProcessor(true);
  }

  /**
   * Uncompress JSON objects previously compressed with HPack algorithm.
   *
   * @return A new {@link CJsonProcessor}.
   */
  public static JsonHPackProcessor jsonhUnpack() {
    return new JsonHPackProcessor(false);
  }

  /**
   * Does the static analysis of the javascript code using jsHint code quality
   * tool.
   *
   * @param options The lint options. Required.
   * @return A new jshint processor.
   */
  public static ResourcePreProcessor jsHint(final LintOptions options) {
    return new ConditionalProcessor(LinterProcessor.jsHint(options), DEV);
  }

  /**
   * Does the static analysis of the javascript code using jsHint code quality
   * tool.
   *
   * @return A new jshint processor.
   */
  public static ResourcePreProcessor jsHint() {
    return jsHint(LintOptions.jsDefaults());
  }

  /**
   * Does the static analysis of the javascript code using jsLint code quality
   * tool.
   *
   * @param options The lint options. Required.
   * @return A new jslint processor.
   */
  public static ResourcePreProcessor jsLint(final LintOptions options) {
    return new ConditionalProcessor(LinterProcessor.jsLint(options), DEV);
  }

  /**
   * Does the static analysis of the javascript code using jsLint code quality
   * tool.
   *
   * @return A new jslint processor.
   */
  public static ResourcePreProcessor jsLint() {
    return jsLint(LintOptions.jsDefaults());
  }

  /**
   * Does the static analysis of the css code using cssLint code quality tool.
   *
   * @param options The lint options. Required.
   * @return A new csslint processor.
   */
  public static ResourcePreProcessor cssLint(final LintOptions options) {
    return new ConditionalProcessor(new CssLinterProcessor(options), DEV);
  }

  /**
   * Does the static analysis of the css code using cssLint code quality tool.
   *
   * @return A new csslint processor.
   */
  public static ResourcePreProcessor cssLint() {
    return cssLint(LintOptions.cssDefaults());
  }

  /**
   * Uses DustJs engine to compile a template into javascript.
   *
   * @return A new {@link DustJsProcessor}.
   */
  public static DustJsProcessor dustJs() {
    return new DustJsProcessor();
  }

  /**
   * A require.js pre-processor.
   *
   * @return A require.js pre-processor.
   */
  public static ResourcePreProcessor requireJs() {
    return new ConditionalProcessor(new RequireJsProcessor(), NO_DEV);
  }

  /**
   * Compiles Handlebars templates to JavaScript code.
   *
   * @return A handlebars processor (only for no-dev environment).
   */
  public static ResourcePreProcessor handlebarsJs() {
    return new ConditionalProcessor(new HandlebarsJsProcessor(), NO_DEV);
  }
}
