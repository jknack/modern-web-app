package com.github.edgarespina.mwa.wro4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import ro.isdc.wro.manager.factory.BaseWroManagerFactory;

import com.github.edgarespina.mwa.mvc.MvcModule;

/**
 * <p>
 * The {@link WroModule} configure all the necessary infrastructure required by
 * the <a href="http://code.google.com/p/wro4j/">WebResourceOptimizer</a>
 * </p>
 * <p>
 * Features:
 * </p>
 * <ul>
 * <li>Intercept all the *.js and *.css request and apply the all the registered
 * {@link Processors processors}.
 * <li>The wro file descriptor can be written in: 'xml', 'groovy' or 'json'.
 * <li>While running in 'dev', the resource is treated as a single file (no
 * group)
 * <li>While running in 'dev', a HTML is printed if a js or css file doesn't
 * follow the rules of jsHint, jsLint or cssLint.
 * <li>While running in 'NO-dev', a group of files can be merged, minified and
 * compressed as a single bundle.
 * </ul>
 * <p>
 * Please see the {@link Processors processors} for a full list of processors.
 * </p>
 *
 * @author edgar.espina
 * @since 0.1.2
 * @see Processors
 */
@Configuration
@Import(MvcModule.class)
public class WroModule extends WroBaseModule {

  /**
   * Publish a model variable with html scripts elements from a 'wro' file
   * descriptor.
   *
   * @param wroModelFactory The {@link BaseWroManagerFactory}. Required.
   * @return A new {@link CssExporter}.
   */
  @Bean
  public JavaScriptExporter wroJavaScriptExporter(
      final BaseWroManagerFactory wroModelFactory) {
    return new JavaScriptExporter(wroModelFactory);
  }

  /**
   * Publish a model variable with html links elements from a 'wro' file
   * descriptor.
   *
   * @param wroModelFactory The {@link BaseWroManagerFactory}. Required.
   * @return A new {@link CssExporter}.
   */
  @Bean
  public CssExporter wroCssExporter(
      final BaseWroManagerFactory wroModelFactory) {
    return new CssExporter(wroModelFactory);
  }

}
