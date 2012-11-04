package com.github.jknack.mwa;

import static org.apache.commons.lang3.Validate.notNull;

import java.beans.PropertyDescriptor;
import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;
import org.springframework.web.servlet.mvc.annotation.ResponseStatusExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.mwa.web.JacksonViewMethodProcessor;

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
class WebDefaults extends DelegatingWebMvcConfiguration implements
    InstantiationAwareBeanPostProcessor {

  /**
   * Log any uncaught exception.
   *
   * @author edgar.espina
   * @since 0.3.3
   */
  private static class LogUncaughtException extends DefaultHandlerExceptionResolver {

    /**
     * The logging system.
     */
    private Logger logger;

    /**
     * Creates a new exception handler.
     * @param context The application's context. Required.
     */
    public LogUncaughtException(final ApplicationContext context) {
      notNull(context, "The application's context is required.");
      String defaultNs = context.getEnvironment().getProperty("application.default.ns");
      logger = LoggerFactory.getLogger(defaultNs);
    }

    @Override
    protected void logException(final Exception ex, final HttpServletRequest request) {
      String message = "Handler execution resulted in exception\n"
          + "  uri: %s\n"
          + "  user: %s";
      String uri = request.getRequestURI();
      String queryString = request.getQueryString();
      if (queryString != null) {
        uri += "?" + queryString;
      }
      Principal principal = request.getUserPrincipal();
      String userName = principal == null ? "unknown" : principal.getName();
      logger.error(String.format(message, uri, userName), ex);
    }
  }

  /**
   * The jackson2 object mapper bean's name.
   */
  static final String OBJECT_MAPPER = "jackson2ObjectMapper";

  /**
   * The application's context.
   */
  private ApplicationContext applicationContext;

  @Override
  public Object postProcessBeforeInitialization(final Object bean,
      final String beanName) {
    return BeanPostProcessors.get(bean).processBeforeInitialization(
        applicationContext, bean);
  }

  @Override
  public Object postProcessAfterInitialization(final Object bean,
      final String beanName) {
    return BeanPostProcessors.get(bean).processAfterInitialization(
        applicationContext, bean);
  }

  @Override
  public void setApplicationContext(
      final ApplicationContext applicationContext) {
    super.setApplicationContext(applicationContext);
    this.applicationContext = applicationContext;
  }

  @Override
  public Object postProcessBeforeInstantiation(final Class<?> beanClass,
      final String beanName) {
    return null;
  }

  @Override
  public boolean postProcessAfterInstantiation(final Object bean,
      final String beanName) {
    BeanPostProcessors.get(bean).processAfterInstantiation(
        applicationContext, bean);
    return true;
  }

  @Override
  public PropertyValues postProcessPropertyValues(final PropertyValues pvs,
      final PropertyDescriptor[] pds, final Object bean,
      final String beanName) {
    return pvs;
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
   * Publish a new object mapper.
   *
   * @return A new object mapper.
   */
  @Bean(name = OBJECT_MAPPER)
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  public ObjectMapper jackson2ObjectMapper() {
    return new ObjectMapper();
  }

  /**
   * A return value processor for method marked with JsonView and ResponseBody.
   *
   * @return A return value processor for method marked with JsonView and
   *         ResponseBody.
   */
  @Bean
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  public JacksonViewMethodProcessor jacksonViewMethodProcessor() {
    return new JacksonViewMethodProcessor(jackson2ObjectMapper());
  }

  @Override
  public void configureHandlerExceptionResolvers(
      final List<HandlerExceptionResolver> exceptionResolvers) {
    ExceptionHandlerExceptionResolver exceptionHandlerExceptionResolver =
        new ExceptionHandlerExceptionResolver();
    exceptionHandlerExceptionResolver.setMessageConverters(getMessageConverters());
    exceptionHandlerExceptionResolver.afterPropertiesSet();

    exceptionResolvers.add(exceptionHandlerExceptionResolver);
    exceptionResolvers.add(new ResponseStatusExceptionResolver());
    exceptionResolvers.add(new LogUncaughtException(applicationContext));
  }
}
