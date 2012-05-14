package com.github.edgarespina.mwa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ViewNameMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import com.github.edgarespina.mwa.handler.BindHandlerExceptionResolver;
import com.github.edgarespina.mwa.handler.MessageConverterHandlerExceptionResolver;

/**
 * Apply sensible defaults Spring MVC options, like:
 * <ul>
 * <li>Enable default servlet.
 * <li>Get HandlerExceptionResolver from the Spring Application Context.
 * </ul>
 *
 * @author edgar.espina
 * @since 0.1
 */
@Configuration
class WebDefaults extends WebMvcConfigurationSupport {

  /**
   * Bind a method name to a view name.
   */
  private static class MethodNameToViewNameMethodReturnHandler extends
      ViewNameMethodReturnValueHandler {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsReturnType(final MethodParameter returnType) {
      return void.class.equals(returnType.getParameterType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleReturnValue(final Object returnValue,
        final MethodParameter returnType,
        final ModelAndViewContainer mavContainer,
        final NativeWebRequest webRequest)
        throws Exception {
      if (returnValue == null) {
        String viewName = returnType.getMethod().getName();
        mavContainer.setViewName(viewName);
      }
      super
          .handleReturnValue(returnValue, returnType, mavContainer, webRequest);
    }

  }

  /**
   * Get message converters.
   */
  @Inject
  private RequestMappingHandlerAdapter requestMappingHandler;

  /**
   * Save the application context.
   */
  @Inject
  private ApplicationContext applicationContext;

  /**
   * {@inheritDoc}
   */
  @Override
  public void configureHandlerExceptionResolvers(
      final List<HandlerExceptionResolver> exceptionResolvers) {
    for (HandlerExceptionResolver cer : customExceptionResolvers()) {
      if (cer instanceof MessageConverterHandlerExceptionResolver) {
        ((MessageConverterHandlerExceptionResolver) cer)
            .setMessageConverters(requestMappingHandler.getMessageConverters());
      }
      exceptionResolvers.add(cer);
    }
    final DefaultHandlerExceptionResolver exceptionResolver =
        new DefaultHandlerExceptionResolver();
    exceptionResolver.setWarnLogCategory(getClass().getPackage().getName());
    exceptionResolvers.add(exceptionResolver);
  }

  /**
   * Add {@link HandlerInterceptor} and {@link WebRequestInterceptor}.
   *
   * @param registry The interceptor registry.
   */
  @Override
  protected void addInterceptors(final InterceptorRegistry registry) {
    for (HandlerInterceptor interceptor : lookFor(HandlerInterceptor.class)) {
      InterceptorRegistration registration =
          registry.addInterceptor(interceptor);
      RequestMapping mapping =
          AnnotationUtils.findAnnotation(interceptor.getClass(),
              RequestMapping.class);
      if (mapping != null && mapping.value().length > 0) {
        registration.addPathPatterns(mapping.value());
      }
    }
  }

  /**
   * Look for {@link HandlerExceptionResolver} in the Application Context.
   *
   * @return All the {@link HandlerExceptionResolver} found in the Application
   *         Context.
   */
  private Iterable<HandlerExceptionResolver> customExceptionResolvers() {
    List<HandlerExceptionResolver> result =
        lookFor(HandlerExceptionResolver.class);
    result.add(0, new BindHandlerExceptionResolver());
    return result;
  }

  /**
   * Look for bean of an specific type in the Application Context.
   *
   * @return All the of the specific types found in the Application Context.
   */
  private <T> List<T> lookFor(final Class<T> beanType) {
    List<T> result = new ArrayList<T>();
    Collection<T> customExceptionResolvers =
        applicationContext.getBeansOfType(beanType).values();
    if (customExceptionResolvers != null) {
      result.addAll(customExceptionResolvers);
    }
    return result;
  }

  @Override
  @Bean
  public HandlerMapping defaultServletHandlerMapping() {
    AbstractHandlerMapping handlerMapping =
        (AbstractHandlerMapping) super.defaultServletHandlerMapping();
    handlerMapping.setInterceptors(getInterceptors());
    return handlerMapping;
  }

  /**
   * Enable the default servlet. {@inheritDoc}
   */
  @Override
  public void configureDefaultServletHandling(
      final DefaultServletHandlerConfigurer configurer) {
    configurer.enable();
  }

  /**
   * Complete configuration.
   */
  @PostConstruct
  public void onStartup() {
    List<HandlerMethodReturnValueHandler> returnValueHandlers =
        new ArrayList<HandlerMethodReturnValueHandler>(requestMappingHandler
            .getReturnValueHandlers().getHandlers());
    int index = -1;
    for (int i = 0; i < returnValueHandlers.size(); i++) {
      HandlerMethodReturnValueHandler current = returnValueHandlers.get(i);
      if (ViewNameMethodReturnValueHandler.class.isInstance(current)) {
        index = i;
        break;
      }
    }
    if (index >= 0) {
      returnValueHandlers.add(index,
          new MethodNameToViewNameMethodReturnHandler());
      // Override
      requestMappingHandler.setReturnValueHandlers(returnValueHandlers);
    }
  }
}
