package com.github.jknack.mwa.wro4j;

import java.io.IOException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.tools.ToolErrorReporter;
import org.mozilla.javascript.tools.shell.Global;

/**
 * Utility class for running javascript code in rhino.
 *
 * @author edgar.espina
 * @since 0.2.3
 */
public final class RhinoExecutor {

  /**
   * The JavaScript task.
   *
   * @author edgar.espina
   * @since 0.2.3
   * @param <V> The resulting value.
   */
  public interface JsTask<V> {

    /**
     * Execute a JavaScript task.
     *
     * @param global Define some global functions particular to the shell. Note
     *        that these functions are not part of ECMA.
     * @param context The excecution context.
     * @param scope The script scope.
     * @return A resulting value.
     * @throws IOException If something goes wrong.
     */
    V run(Global global, Context context, Scriptable scope) throws IOException;
  }

  /**
   * Not allowed.
   */
  private RhinoExecutor() {
  }

  /**
   * Execute a JavaScript task using Rhino.
   *
   * @param task The JavaScript task.
   * @return The resulting value.
   * @param <V> The resulting value.
   * @throws IOException If something goes wrong.
   */
  public static <V> V execute(final JsTask<V> task) throws IOException {
    Context context = null;
    try {
      context = Context.enter();
      context.setOptimizationLevel(-1);
      context.setErrorReporter(new ToolErrorReporter(false));
      context.setLanguageVersion(Context.VERSION_1_8);

      Global global = new Global();
      global.init(context);

      Scriptable scope = context.initStandardObjects(global);

      return task.run(global, context, scope);
    } finally {
      if (context != null) {
        Context.exit();
      }
    }
  }

}
