package com.globant.todomvc.view;

import static com.github.jknack.mwa.wro4j.Processors.excludes;
import static com.github.jknack.mwa.wro4j.Processors.jsHint;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.SimpleProcessorsFactory;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Jackson2Helper;
import com.github.jknack.handlebars.springmvc.HandlebarsViewResolver;
import com.github.jknack.mwa.Mode;
import com.github.jknack.mwa.wro4j.LintOptions;

@Configuration
public class ViewModule {

  @Bean
  public HandlebarsViewResolver viewResolver(final Mode mode) {
    HandlebarsViewResolver viewResolver = new HandlebarsViewResolver() {
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

  @Bean
  public ProcessorsFactory processorsFactory() {
    LintOptions options = LintOptions.jsWhite()
      .option("nomen")
      .predefined("Backbone", "_", "$", "Handlebars", "ENTER_KEY");
    return new SimpleProcessorsFactory()
      .addPreProcessor(excludes(jsHint(options), "/**/lib/**/*.*"));
  }
}
