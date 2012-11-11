package com.github.jknack.mwa;

import org.springframework.core.Ordered;

/**
 * Configure an infrastructure components at the time the application is ready to go.
 * Additionally, a configurer might implement the Ordered contract.
 *
 * @author edgar.espina
 *
 * @param <T> The component type.
 * @see Ordered
 */
public interface ComponentConfigurer<T> {

  /**
   * Configure an infrastructure component.
   *
   * @param component The component to be configured. It is never null.
   * @throws Exception If somethings goes wrong.
   */
  void configure(T component) throws Exception;

}
