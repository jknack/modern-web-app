package com.github.edgarespina.mwa;

import org.apache.commons.lang3.Validate;

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
