package com.github.edgarespina.mwa.wro4j.requirejs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import ro.isdc.wro.manager.factory.BaseWroManagerFactory;

import com.github.edgarespina.mwa.mvc.MvcModule;
import com.github.edgarespina.mwa.wro4j.WroBaseModule;

/**
 * Add some model contributions to the wro module.
 *
 * @author edgar.espina
 * @since 0.2.3
 */
@Configuration
@Import(MvcModule.class)
public class WroRequireJsModule extends WroBaseModule {

  /**
   * You can access to this contribution using: <code>${requireConfig}</code>.
   *
   * @param wroManagerFactory The wro manager factory.
   * @return A new require configuration.
   */
  @Bean
  public RequireJsConfig wroRequireConfigContrib(
      final BaseWroManagerFactory wroManagerFactory) {
    return new RequireJsConfig(wroManagerFactory);
  }
}
