package com.github.jknack.mwa;

import java.beans.PropertyDescriptor;

import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

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
@EnableWebMvc
class WebDefaults extends WebMvcConfigurerAdapter implements
    InstantiationAwareBeanPostProcessor,
    ApplicationContextAware {

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
}
