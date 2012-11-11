package com.github.jknack.mwa.mvc;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.PriorityOrdered;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * A {@link HandlerInterceptorAdapter} that enabled model contributions.
 *
 * @author edgar.espina
 * @since 0.1.2
 */
public class ModelContributionInterceptor extends HandlerInterceptorAdapter
    implements PriorityOrdered {

  /**
   * The logging system.
   */
  private final Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * The list of model contributions.
   */
  private List<ModelContribution> contributions;

  /**
   * Creates a new {@link ModelContributionInterceptor}.
   *
   * @param contributions The list of model contributions.
   */
  public ModelContributionInterceptor(
      final List<ModelContribution> contributions) {
    this.contributions =
        checkNotNull(contributions, "The contribution list is required.");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void postHandle(final HttpServletRequest request,
      final HttpServletResponse response, final Object handler,
      final ModelAndView modelAndView) throws Exception {
    if (modelAndView != null) {
      String viewName = modelAndView.getViewName();
      if (StringUtils.isEmpty(viewName)) {
        // No view name was returned find a default one.
        if (handler instanceof HandlerMethod) {
          HandlerMethod handlerMethod = (HandlerMethod) handler;
          viewName = handlerMethod.getMethod().getName();
          modelAndView.setViewName(viewName);
          logger.debug("View's not found for: {}, default is: {}", handler, viewName);
        }
      }
      if (viewName.startsWith("redirect:")) {
        // don't add contributions on redirect-call
        return;
      }
      Map<String, Object> model = modelAndView.getModel();
      model.put(ModelContribution.CONTEXT_PATH, request.getContextPath());
      model.put(ModelContribution.VIEW, viewName);
      for (ModelContribution contribution : contributions) {
        logger.debug(
            "Adding contribution: {} to: {}, view: {}",
            new Object[]{contribution.getClass().getSimpleName(),
                request.getRequestURI(), viewName });
        contribution.contribute(request, response, modelAndView);
      }
    }
  }

  @Override
  public int getOrder() {
    return HIGHEST_PRECEDENCE;
  }
}
