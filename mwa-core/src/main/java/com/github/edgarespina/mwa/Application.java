package com.github.edgarespina.mwa;

import org.apache.commons.lang3.Validate;

/**
 * It represents commons application attributes, like: name, context path,
 * version and mode.
 *
 * @author edgar.espina
 * @since 0.1
 */
public class Application {

  /**
   * The application's mode: dev, stage, prod, etc. The 'dev' mode has special
   * meaning, you can built your application by trusting in dev or not dev mode.
   * For example, you can use an in-memory database if you're running in dev
   * mode.
   */
  public static final class Mode {
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

  /**
   * The default mode name.
   */
  private static final String DEV_NAME = "dev";

  /**
   * The default mode: dev.
   */
  public static final Mode DEV = new Mode("dev");

  /**
   * The application's name. Required.
   */
  private String name;

  /**
   * The application's context path. Required.
   */
  private String contextPath;

  /**
   * The application's version. Required.
   */
  private String version;

  /**
   * The application's mode. Required.
   */
  private Mode mode;

  /**
   * Creates a new {@link Application}.
   *
   * @param contextPath The application's context path. Required.
   * @param name The application's name. Required.
   * @param version The application's version. Required.
   * @param mode The application's mode. Required.
   */
  public Application(final String contextPath, final String name,
      final String version, final Mode mode) {
    Validate
        .notEmpty(contextPath, "The application's contextPath is required.");
    Validate.notEmpty(name, "The application's name is required.");
    Validate.notEmpty(version, "The application's version is required.");
    Validate.notNull(mode, "The application's mode is required.");
    this.contextPath = contextPath;
    this.name = name;
    this.version = version;
    this.mode = mode;
  }

  /**
   * The application's name. Required.
   *
   * @return The application's name. Required.
   */
  public String name() {
    return name;
  }

  /**
   * The application's version. Required.
   *
   * @return The application's version. Required.
   */
  public String version() {
    return version;
  }

  /**
   * The application's mode. Required.
   *
   * @return The application's mode. Required.
   */
  public Mode mode() {
    return mode;
  }

  /**
   * The application's context path. Required.
   *
   * @return The application's context path. Required.
   */
  public String contextPath() {
    return contextPath;
  }

  @Override
  public String toString() {
    return name + "; contextPath: " + contextPath + "; version: " + version
        + "; mode: " + mode;
  }
}
