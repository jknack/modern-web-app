package com.github.jknack.examples.todomvc.domain;

/**
 * Raised when a todo item wasn't found.
 *
 * @author edgar.espina
 */
public class TodoNotFoundException extends RuntimeException {

  /**
   * Default serial.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Creates a new {@link TodoNotFoundException}.
   *
   * @param id The id that wasn't found.
   */
  public TodoNotFoundException(final int id) {
    super("" + id);
  }
}
