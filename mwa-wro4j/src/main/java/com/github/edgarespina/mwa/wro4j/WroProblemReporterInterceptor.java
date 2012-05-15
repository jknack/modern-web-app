package com.github.edgarespina.mwa.wro4j;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.model.factory.WroModelFactory;
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
   * Track changes between resources.
   */
  private ConcurrentMap<String, String> changeSet =
      new ConcurrentHashMap<String, String>();

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
  @SuppressWarnings("unchecked")
  public void postHandle(final HttpServletRequest request,
      final HttpServletResponse response, final Object handler,
      final ModelAndView modelAndView) throws Exception {
    if (modelAndView == null) {
      return;
    }
    Map<String, Object> model = modelAndView.getModel();
    /**
     * Prepare js and css resources.
     */
    List<Resource> resources = new ArrayList<Resource>();
    List<Resource> scripts =
        (List<Resource>) model.get(JavaScriptExporter.RESOURCES);
    List<Resource> css = (List<Resource>) model.get(CssExporter.RESOURCES);
    resources.addAll(scripts);
    resources.addAll(css);
    if (resources.isEmpty()) {
      return;
    }
    try {
      Context.set(Context.webContext(request, response, new WroFilterConfig(
          request.getServletContext())));

      WroManager wroManager = wroManagerFactory.create();
      ProcessorsFactory processorsFactory = wroManager.getProcessorsFactory();
      UriLocatorFactory uriLocatorFactory = wroManager.getUriLocatorFactory();

      Collection<ResourcePreProcessor> preProcessors =
          processorsFactory.getPreProcessors();

      for (Resource resource : resources) {
        String uri = resource.getUri();
        String input = WroHelper.safeRead(uriLocatorFactory, resource);
        String hash = DigestUtils.md5DigestAsHex(input.getBytes());
        String prevHash = changeSet.get(uri);
        if (!hash.equals(prevHash)) {
          changeSet.putIfAbsent(uri, hash);
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
                StringWriter writer = new StringWriter();
                processor.process(resource, new StringReader(input), writer);
                // Override input for the next processor.
                input = writer.toString();
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
      }
    } finally {
      Context.unset();
    }
  }
}
