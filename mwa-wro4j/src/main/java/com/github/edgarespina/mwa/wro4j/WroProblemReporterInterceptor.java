package com.github.edgarespina.mwa.wro4j;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.group.processor.InjectorBuilder;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;

import com.github.edgarespina.mwa.Application;

/**
 * <p>
 * Intercept URI request and report any problems detected in JS or CSS
 * resources. If no problem is detected the interceptor does nothing.
 * </p>
 * NOTE: This component is enable in "dev" mode.
 *
 * @author edgar.espina
 * @since 0.1.2
 */
@Profile(Application.DEV_NAME)
@Component
public class WroProblemReporterInterceptor extends HandlerInterceptorAdapter {

  /**
   * The {@link WroModelFactory} service.
   */
  private BaseWroManagerFactory wroManagerFactory;

  /**
   * The logging system.
   */
  private Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * Creates a new {@link WroProblemReporter}.
   *
   * @param wroManagerFactory The {@link WroModelFactory} service. Required.
   */
  @Inject
  public WroProblemReporterInterceptor(
      final BaseWroManagerFactory wroManagerFactory) {
    this.wroManagerFactory =
        checkNotNull(wroManagerFactory, "The wroManagerFactory is required.");
  }

  /**
   * <p>
   * Intercept URI request and report any problems detected in JS or CSS
   * resources. If no problem is detected the interceptor does nothing.
   * </p>
   * {@inheritDoc}
   */
  @Override
  public void postHandle(final HttpServletRequest request,
      final HttpServletResponse response, final Object handler,
      final ModelAndView modelAndView)
      throws Exception {
    try {
      String viewName = modelAndView.getViewName();
      Context.set(Context.webContext(request, response, new WroFilterConfig(
          request.getServletContext())));

      Injector injector = InjectorBuilder.create(wroManagerFactory).build();
      WroManager wroManager = wroManagerFactory.create();
      ProcessorsFactory processorsFactory = wroManager.getProcessorsFactory();
      UriLocatorFactory uriLocatorFactory = wroManager.getUriLocatorFactory();

      WroModelFactory modelFactory = wroManagerFactory.getModelFactory();
      injector.inject(modelFactory);

      WroModel model = modelFactory.create();
      Group group = model.getGroupByName(viewName);
      if (group == null) {
        logger.debug("No group found for view: {}", viewName);
        return;
      }
      Collection<ResourcePreProcessor> preProcessors =
          processorsFactory.getPreProcessors();
      for (Resource resource : group.getResources()) {
        for (ResourcePreProcessor processor : preProcessors) {
          SupportedResourceType supportedResourceType =
              AnnotationUtils.findAnnotation(processor.getClass(),
                  SupportedResourceType.class);
          boolean apply =
              supportedResourceType == null
                  || supportedResourceType.value() == resource.getType();
          if (apply) {
            try {
              logger.debug("Verifying resource: {} with: {}",
                  resource.getUri(), processor.getClass().getSimpleName());
              StringReader reader =
                  new StringReader(WroHelper.safeRead(uriLocatorFactory,
                      resource));
              processor.process(resource, reader, new StringWriter());
            } catch (RuntimeException ex) {
              try {
                WroProblemReporter.bestFor(ex).report(ex, request, response);
              } catch (Exception inner) {
                logger.trace("Cannot display report", inner);
              }
            }
          }
        }
      }
    } finally {
      Context.unset();
    }
  }
}
