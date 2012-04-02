package org.knowhow.mwa.validation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * A JSR-303 Bean Validation Module for Spring.
 *
 * @author edgar.espina
 * @since 0.1
 */
@Configuration
public class ValidationModule {

  /**
   * Publish a JSR-303 validator factory.
   *
   * @return A new JSR-303 validator factory.
   */
  @Bean
  public LocalValidatorFactoryBean localValidatorFactoryBean() {
    return new LocalValidatorFactoryBean();
  }

  /**
   * Publish a {@link JSR303HandlerExceptionResolver JSR-303} message resolver.
   *
   * @return A new {@link JSR303HandlerExceptionResolver} message resolver.
   */
  @Bean
  public Jsr303HandlerExceptionResolver jsr303ExceptionResolver() {
    return new Jsr303HandlerExceptionResolver();
  }
}
