package ar.jug.view;

import org.knowhow.mwa.Application;
import org.knowhow.mwa.view.mustache.MustacheViewResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * The view module it configure the {@link ViewResolver} for the application.
 *
 * @author edgar.espina
 * @since 0.1
 */
@Configuration
@EnableWebMvc
public class ViewModule {

  /**
   * Publish a {@link MustacheViewResolver} into the application context.
   *
   * @param application The application object. Required.
   * @return A new {@link MustacheViewResolver}.
   * @see <a href="http://mustache.github.com/">Mustache</a>
   */
  @Bean
  public ViewResolver viewResolver(final Application application) {
    MustacheViewResolver viewResolver = new MustacheViewResolver();
    viewResolver.setCache(application.mode() != Application.DEV);
    return viewResolver;
  }

}
