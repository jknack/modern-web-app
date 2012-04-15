package org.knowhow.mwa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.knowhow.mwa.handler.BindHandlerExceptionResolver;
import org.knowhow.mwa.handler.MessageConverterHandlerExceptionResolver;
import org.resthub.web.springmvc.router.RouterHandlerAdapter;
import org.resthub.web.springmvc.router.RouterHandlerMapping;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ViewNameMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

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
   * Look for {@link HandlerExceptionResolver} in the Application Context.
   *
   * @return All the {@link HandlerExceptionResolver} found in the Application
   *         Context.
   */
  private Iterable<HandlerExceptionResolver> customExceptionResolvers() {
    Collection<HandlerExceptionResolver> result =
        new ArrayList<HandlerExceptionResolver>();
    result.add(new BindHandlerExceptionResolver());
    Collection<HandlerExceptionResolver> customExceptionResolvers =
        applicationContext.getBeansOfType(HandlerExceptionResolver.class)
            .values();
    if (customExceptionResolvers != null) {
      result.addAll(customExceptionResolvers);
    }
    return result;
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
   * Configure web methods from 'routes' file.
   *
   * @param contextPath The servlet context path.
   * @return A {@link RouterHandlerMapping}.
   */
  @Bean
  public RouterHandlerMapping routerHandlerMapping(
      @Named("contextPath") final String contextPath) {
    RouterHandlerMapping routerMapping = new RouterHandlerMapping();
    routerMapping.setRouteFile("classpath:/routes");
    routerMapping.setInterceptors(getInterceptors());
    routerMapping.setOrder(0);
    routerMapping.setServletPrefix(contextPath);
    return routerMapping;
  }

  /**
   * Configure web methods from routes.conf.
   *
   * @return A {@link RouterHandlerMapping}.
   */
  @Bean
  public RouterHandlerAdapter routerHandlerAdater() {
    ConfigurableWebBindingInitializer webBindingInitializer =
        new ConfigurableWebBindingInitializer();
    webBindingInitializer.setConversionService(mvcConversionService());
    webBindingInitializer.setValidator(mvcValidator());

    List<HandlerMethodArgumentResolver> argumentResolvers =
        new ArrayList<HandlerMethodArgumentResolver>();
    addArgumentResolvers(argumentResolvers);

    List<HandlerMethodReturnValueHandler> returnValueHandlers =
        new ArrayList<HandlerMethodReturnValueHandler>();
    addReturnValueHandlers(returnValueHandlers);

    RouterHandlerAdapter adapter = new RouterHandlerAdapter();
    List<HttpMessageConverter<?>> converters = getMessageConverters();
    adapter.setMessageConverters(converters
        .toArray(new HttpMessageConverter[converters.size()]));
    adapter.setWebBindingInitializer(webBindingInitializer);
    return adapter;
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
      returnValueHandlers.set(index,
          new MethodNameToViewNameMethodReturnHandler());
      // Override
      requestMappingHandler.setReturnValueHandlers(returnValueHandlers);
    }
  }
}
