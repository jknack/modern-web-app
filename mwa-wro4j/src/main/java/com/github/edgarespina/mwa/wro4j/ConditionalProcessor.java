package com.github.edgarespina.mwa.wro4j;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import ro.isdc.wro.model.resource.Resource;

import com.github.edgarespina.mwa.Mode;

/**
 * Run a wro processor if the result of {@link #process(Environment)} is true.
 *
 * @author edgar.espina
 * @since 0.2.3
 */
public class ConditionalProcessor extends ExtendedProcessorDecorator {

  /**
   * Allow to turn on/off a processor at runtime.
   *
   * @author edgar.espina
   * @since 0.2.3
   */
  public interface Condition {

    /**
     * True if the processor should runs.
     *
     * @param mode The application's mode.
     * @param environment The application's environment.
     * @return True if the processor should runs.
     */
    boolean process(Mode mode, Environment environment);
  }

  /**
   * The logging system.
   */
  private static final Logger logger = LoggerFactory
      .getLogger(ConditionalProcessor.class);

  /**
   * The wro configuration.
   */
  private Mode mode;

  /**
   * The application's environment.
   */
  private Environment environment;

  /**
   * Decide if a processor should or shouldn't run.
   */
  private Condition condition;

  /**
   * Creates a new {@link ConditionalProcessor}.
   *
   * @param processor A wro processor.
   * @param condition Decide if a processor should or shouldn't run.
   */
  public ConditionalProcessor(final Object processor,
      final Condition condition) {
    super(processor);
    this.condition = notNull(condition, "A condition is required.");
  }

  /**
   * A wro processor.
   *
   * @param <T> The processor type.
   * @return A wro processor.
   */
  @SuppressWarnings("unchecked")
  public <T> T getProcessor() {
    return (T) getOriginalDecoratedObject();
  }

  @Override
  public void process(final Resource resource, final Reader reader,
      final Writer writer)
      throws IOException {
    if (condition.process(mode, environment)) {
      super.process(resource, reader, writer);
    } else {
      logger.debug("Processor: {} is off.", getOriginalDecoratedObject());
      WroHelper.safeCopy(reader, writer);
    }
  }

  @Override
  public void setEnvironment(final Environment environment) {
    super.setEnvironment(environment);
    this.environment = environment;
  }

  @Override
  public void setMode(final Mode mode) {
    super.setMode(mode);
    this.mode = mode;
  }

  @Override
  public String toString() {
    return "?@" + getDecoratedObject().toString();
  }
}
