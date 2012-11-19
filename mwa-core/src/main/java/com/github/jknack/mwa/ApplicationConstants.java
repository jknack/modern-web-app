package com.github.jknack.mwa;

/**
 * Constants used in the platform.
 *
 * @author edgar.espina
 *
 */
public final class ApplicationConstants {

  /**
   * The application's running mode. Default is: <code>dev</code>.
   */
  public static final String APP_MODE = "application.mode";

  /**
   * The application's name. By default is the simple class name of {@link #APP_STARTUP_CLASS}.
   */
  public static final String APP_NAME = "application.name";

  /**
   * The application's context path.
   */
  public static final String CONTEXT_PATH = "contextPath";

  /**
   * The application's namespace. Usually, one or more Java packages.
   */
  public static final String APP_NAMESPACE = "application.ns";

  /**
   * The application's default namespace. By default is the package's name of the
   * {@link #APP_STARTUP_CLASS}.
   */
  public static final String APP_DEFAULT_NAMESPACE = "application.default.ns";

  /**
   * The startup's class name.
   */
  public static final String APP_STARTUP_CLASS = "application.startup.class";

  /**
   * Not allowed.
   */
  private ApplicationConstants() {
  }
}
