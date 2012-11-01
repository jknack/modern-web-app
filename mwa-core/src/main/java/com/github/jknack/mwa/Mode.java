package com.github.jknack.mwa;

import static org.apache.commons.lang3.Validate.notNull;

import java.lang.reflect.Method;

import org.apache.commons.lang3.Validate;
import org.springframework.util.ReflectionUtils;

/**
 * The application's mode: dev, stage, prod, etc. The 'dev' mode has special
 * meaning, you can built your application by trusting in dev or not dev mode.
 * For example, you can use an in-memory database if you're running in dev
 * mode.
 */
public final class Mode {
  /**
   * The "dev" mode.
   */
  private static final String DEV_NAME = "dev";

  /**
   * The default mode: dev.
   */
  public static final Mode DEV = new Mode(DEV_NAME);

  /**
   * The mode's name. Required.
   */
  private final String name;

  /**
   * Creates a new {@link Mode}.
   *
   * @param name The mode's name. Required.
   */
  private Mode(final String name) {
    this.name = name.toLowerCase();
  }

  /**
   * The mode's name.
   *
   * @return The mode's name.
   */
  public String name() {
    return name;
  }

  /**
   * True if the applications runs in dev mode.
   *
   * @return True if the applications runs in dev mode.
   */
  public boolean isDev() {
    return matches(DEV_NAME);
  }

  /**
   * True, if the mode matches this mode's name.
   *
   * @param mode The mode's name.
   * @return True, if the mode matches this mode's name.
   */
  public boolean matches(final String mode) {
    return name.equalsIgnoreCase(mode);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof Mode) {
      Mode that = (Mode) obj;
      return this.matches(that.name);
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return name.hashCode();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return name;
  }

  /**
   * Execute a callback method matching the given mode.
   * <p>
   * Example if <code>application.mode=prod</code> the <code>onProd</code>
   * method will be executed. Or if the <code>onProd</code> is not present, the
   * {@link ModeCallback#on(Mode)} method will be executed.
   * </p>
   *
   * @param callback The callback. Required.
   * @param <T> The callback result.
   * @return The callback result.
   */
  @SuppressWarnings("unchecked")
  public <T> T execute(final ModeCallback<T> callback) {
    notNull(callback, "A mode's callback is required.");
    if (isDev()) {
      return callback.onDev();
    }
    String methodName =
        "on" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    Method method = ReflectionUtils.findMethod(callback.getClass(), methodName);
    if (method == null) {
      return callback.on(this);
    }
    ReflectionUtils.makeAccessible(method);
    return (T) ReflectionUtils.invokeMethod(method, callback);
  }

  /**
   * Factory method for {@link Mode}.
   *
   * @param mode The mode's name. Required.
   * @return An application's mode.
   */
  public static Mode valueOf(final String mode) {
    Validate.notEmpty(mode, "The application's mode is required.");
    if (DEV_NAME.equalsIgnoreCase(mode)) {
      return DEV;
    }
    return new Mode(mode);
  }
}
