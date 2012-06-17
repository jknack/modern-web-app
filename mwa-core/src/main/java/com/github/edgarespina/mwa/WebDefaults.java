package com.github.edgarespina.mwa;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

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
   * The default object mapper name.
   */
  public static final String OBJECT_MAPPER = "globalObjectMapper";

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
  public ObjectMapper globalObjectMapper() {
    return new ObjectMapper();
  }
}
