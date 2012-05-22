package com.github.edgarespina.mwa;

import java.beans.PropertyDescriptor;

import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Extend the Mvc Support with common features.
 *
 * @author edgar.espina
 * @since 0.1.3
 */
class ExtendedMvcSupport implements InstantiationAwareBeanPostProcessor,
    ApplicationContextAware {

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

}
