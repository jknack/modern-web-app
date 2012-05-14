package com.github.edgarespina.mwa.wro4j;

import static com.google.common.base.Preconditions.checkNotNull;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.js.AbstractLinterProcessor;
import ro.isdc.wro.extensions.processor.js.JsHintProcessor;
import ro.isdc.wro.extensions.processor.js.JsLintProcessor;
import ro.isdc.wro.extensions.processor.support.linter.AbstractLinter;
import ro.isdc.wro.extensions.processor.support.linter.JsHint;
import ro.isdc.wro.extensions.processor.support.linter.JsLint;
import ro.isdc.wro.extensions.processor.support.linter.LinterError;
import ro.isdc.wro.extensions.processor.support.linter.LinterException;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

import com.github.edgarespina.mwa.Application;

/**
 * Similar to {@link JsLintProcessor} and/or {@link JsHintProcessor}. The main
 * difference is the HTML report generated at runtime if the application is
 * running in development mode.
 *
 * @author edgar.espina
 * @since 0.1.2
 */
@SupportedResourceType(ResourceType.JS)
final class LinterProcessor extends AbstractLinterProcessor implements
    UriLocatorFactoryAware, EnvironmentAware {

  /**
   * The underlying "lint" processor: jsHint or jsLint.
   */
  private AbstractLinter linter;

  /**
   * The uri locator factory. It resolve js/css resources location.
   */
  private UriLocatorFactory uriLocatorFactory;

  /**
   * True if application's mode is "dev".
   */
  private boolean debug;

  /**
   * The lint options.
   */
  private String[] options;

  /**
   * Creates a new {@link LinterProcessor}.
   *
   * @param linter The lint processor: jsHint or jsLint. Required.
   * @param options The lint options. Required.
   */
  private LinterProcessor(final AbstractLinter linter,
      final LintOptions options) {
    this(linter);
    checkNotNull(options, "The lint options are required.");
    setOptions(options.build());
    this.options = options.build();
  }

  /**
   * Creates a new {@link LinterProcessor}.
   *
   * @param linter The lint processor: jsHint or jsLint. Required.
   */
  private LinterProcessor(final AbstractLinter linter) {
    this.linter = checkNotNull(linter, "The linter is required.");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected AbstractLinter newLinter() {
    return linter;
  }

  /**
   * If debug is enable it wrap the {@link LinterException} as a
   * {@link RuntimeLinterException}. The exception is handled in other place.
   * {@inheritDoc}
   */
  @Override
  protected void onLinterException(final LinterException ex,
      final Resource resource) {
    if (debug) {
      throw new RuntimeLinterException(resource.getUri(), WroHelper.safeRead(
          uriLocatorFactory, resource),
          options, ex.getErrors().toArray(new LinterError[0]));
    } else {
      throw new WroRuntimeException("Linter error", ex);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setUriLocatorFactory(final UriLocatorFactory uriLocatorFactory) {
    this.uriLocatorFactory = uriLocatorFactory;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setEnvironment(final Environment environment) {
    debug = Application.DEV.matches(environment
        .getProperty(Application.APPLICATION_MODE));
  }

  /**
   * Creates a new JsHint processor.
   *
   * @param options The lint options. Required.
   * @return A new JsHint processor.
   */
  public static ResourcePreProcessor jsHint(final LintOptions options) {
    return new LinterProcessor(new JsHint(), options);
  }

  /**
   * Creates a new JsHint processor.
   *
   * @return A new JsHint processor.
   */
  public static ResourcePreProcessor jsHint() {
    return new LinterProcessor(new JsHint());
  }

  /**
   * Creates a new JsLint processor.
   *
   * @param options The lint options. Required.
   * @return A new JsLint processor.
   */
  public static ResourcePreProcessor jsLint(final LintOptions options) {
    return new LinterProcessor(new JsLint(), options);
  }

  /**
   * Creates a new JsLint processor.
   *
   * @return A new JsLint processor.
   */
  public static ResourcePreProcessor jsLint() {
    return new LinterProcessor(new JsLint());
  }
}
