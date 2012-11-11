package com.github.jknack.examples.todomvc.view;

import static com.github.jknack.mwa.wro4j.Processors.excludes;
import static com.github.jknack.mwa.wro4j.Processors.googleClosureSimple;
import static com.github.jknack.mwa.wro4j.Processors.jsHint;
import static com.github.jknack.mwa.wro4j.Processors.propertyResolver;
import static com.github.jknack.mwa.wro4j.Processors.yuiCssCompressor;
import static org.apache.commons.lang3.Validate.notNull;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.SimpleProcessorsFactory;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Jackson2Helper;
import com.github.jknack.handlebars.springmvc.HandlebarsViewResolver;
import com.github.jknack.mwa.Mode;
import com.github.jknack.mwa.wro4j.LintOptions;

/**
 * The view module. It configures:
 *
 * <ul>
 * <li>A view resolver: {@link HandlebarsViewResolver}</li>
 * <li>Wro4j processors: linters and compressors</li>
 * </ul>
 *
 * @author edgar.espina
 *
 */
@Configuration
public class ViewModule {

  /**
   * Configure a {@link HandlebarsViewResolver} and add a JSON Helper.
   * The viewResolver cache is on for no-dev modes.
   *
   * @param mode The application's mode. Required.
   * @return A new {@link HandlebarsViewResolver}.
   */
  @Bean
  public HandlebarsViewResolver viewResolver(final Mode mode) {
    notNull(mode, "The mode is required.");
    final HandlebarsViewResolver viewResolver = new HandlebarsViewResolver() {
      @Override
      protected void configure(final Handlebars handlebars) {
        handlebars.registerHelper("@json", Jackson2Helper.INSTANCE);
        super.configure(handlebars);
      }
    };
    viewResolver.setCache(!mode.isDev());
    viewResolver.setSuffix(".html");

    return viewResolver;
  }

  /**
   * Creates a new {@link ProcessorsFactory}. It enables:
   * <ul>
   * <li>Access to environment properties using the ${expression} from CSS and JS files.</li>
   * <li>A js linter using jsHint</li>
   * <li>A google closure simple compiler and processor</li>
   * <li>A YUI css compressor</li>
   * </ul>
   *
   * @param env The application's environment.
   *
   * @return A new {@link ProcessorsFactory}.
   */
  @Bean
  public ProcessorsFactory processorsFactory(final Environment env) {
    LintOptions jsOptions = LintOptions.jsWhite()
        .option("nomen")
        .predefined("Backbone", "_", "$", "Handlebars", "ENTER_KEY");

    return new SimpleProcessorsFactory()
        .addPreProcessor(excludes(propertyResolver(env), "/**/lib/**/*.*"))
        .addPreProcessor(excludes(jsHint(jsOptions), "/**/lib/**/*.*"))
        .addPostProcessor(googleClosureSimple())
        .addPostProcessor(yuiCssCompressor());
  }
}
