package com.github.jknack.mwa;

/**
 * A callback for executing actions in one specific {@link Mode}. Due that
 * <code>dev</code> is special, this interface has a custom callback method for
 * running in dev.
 * <p>
 * Clients of this interface can provide methods: like
 * <code>on + modeName</code>
 * </p>
 * Example if <code>application.mode=prod</code> the <code>onProd</code> method
 * will be executed. Or if the <code>onProd</code> is not present, the
 * {@link #on(Mode)} method will be executed.
 *
 * @author edgar.espina
 * @param <T> The callback result.
 */
public abstract class ModeCallback<T> {

  /**
   * Call it if application's mode is set to <code>dev</code>.
   *
   * @return A callback result.
   */
  public abstract T onDev();

  /**
   * Call it if application's mode is NOT <code>dev</code>.
   *
   * @param mode The application's mode.
   * @return A callback result.
   */
  public abstract T on(Mode mode);
}
