package com.github.edgarespina.mwa.view;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

/**
 * Provide additional models attributes to a view.
 *
 * @author edgar.espina
 * @since 0.1.0
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
   * Add new model attributes to the view.
   *
   * @param request The HTTP request.
   * @param response The HTTP response.
   * @param modelAndView The {@link ModelAndView}.
   * @throws IOException If something goes wrong.
   */
  void contribute(HttpServletRequest request, HttpServletResponse response,
      ModelAndView modelAndView) throws IOException;

  /**
   * Turn on/off the cache. In 'dev' the cache is off.
   *
   * @param cache True for turning on the cache.
   */
  void setUseCache(boolean cache);
}
