package com.github.jknack.mwa.wro4j;

import ro.isdc.wro.WroRuntimeException;

/**
 * A lessCss errror.
 *
 * @author edgar.espina
 * @since 0.1.2
 */
public class LessRuntimeException extends WroRuntimeException {

  /**
   * Default serial uuid.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The lessCss error.
   */
  private LessCssError lessCssError;

  /**
   * Creates a new {@link LessRuntimeException}.
   *
   * @param lessError The lessCss error.
   * @param cause The cause of the error.
   */
  public LessRuntimeException(final LessCssError lessError,
      final Throwable cause) {
    super(lessError.toString(), cause);
    this.lessCssError = lessError;
  }

  /**
   * Get the lessCss error.
   *
   * @return The lessCss error.
   */
  public LessCssError getLessCssError() {
    return lessCssError;
  }
}
