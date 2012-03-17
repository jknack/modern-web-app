package org.knowhow.mwa.view;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletContext;

/**
 * Provide additional models attributes to view.
 *
 * @author edgar.espina
 * @since 0.1
 * @see WebView
 */
public interface ModelContribution {

  /**
   * Store the view's name in the model.
   */
  String VIEW = "viewName";

  /**
   * Store the context path variable in the model.
   */
  String CONTEXT_PATH = "contextPath";

  /**
   * Called once during initialization.
   *
   * @param context The servlet context. It's never null.
   * @throws IOException
   */
  void init(ServletContext context) throws IOException;

  /**
   * Add new model attributes to the view.
   *
   * @param view The view's name. It never nulls.
   * @param model The model. It never nulls.
   */
  void contribute(String view, Map<String, Object> model) throws IOException;

  /**
   * Enabled or disable the cache.
   *
   * @param cache True for enable the cache.
   */
  void setUseCache(boolean cache);
}
