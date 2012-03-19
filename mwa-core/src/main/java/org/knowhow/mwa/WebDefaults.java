package org.knowhow.mwa;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.knowhow.mwa.handler.MessageConverterHandlerExceptionResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
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
class WebDefaults extends WebMvcConfigurerAdapter {

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
    Collection<HandlerExceptionResolver> customExceptionResolvers =
        applicationContext.getBeansOfType(HandlerExceptionResolver.class)
            .values();
    if (customExceptionResolvers == null) {
      customExceptionResolvers = Collections.emptySet();
    }
    return customExceptionResolvers;
  }

  /**
   * Enable the default servlet.
   * {@inheritDoc}
   */
  @Override
  public void configureDefaultServletHandling(
      final DefaultServletHandlerConfigurer configurer) {
    configurer.enable();
  }
}
