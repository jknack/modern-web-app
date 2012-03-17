package ar.jug.view;

import org.knowhow.mwa.Application;
import org.knowhow.mwa.view.mustache.MustacheViewResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@EnableWebMvc
public class ViewModule extends WebMvcConfigurerAdapter {

  @Bean
  public ViewResolver viewResolver(final Application application) {
    MustacheViewResolver viewResolver = new MustacheViewResolver();
    viewResolver.setSuffix(".html");
    viewResolver.setCache(application.mode() != Application.DEV);
    return viewResolver;
  }

  @Override
  public void configureDefaultServletHandling(
      final DefaultServletHandlerConfigurer configurer) {
  }
}
